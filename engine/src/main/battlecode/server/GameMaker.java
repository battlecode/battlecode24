package battlecode.server;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.instrumenter.profiler.Profiler;
import battlecode.instrumenter.profiler.ProfilerCollection;
import battlecode.instrumenter.profiler.ProfilerEventType;
import battlecode.schema.*;
import battlecode.util.FlatHelpers;
import battlecode.util.TeamMapping;
import battlecode.world.*;
import com.google.flatbuffers.FlatBufferBuilder;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TCharArrayList;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.ToIntFunction;
import java.util.zip.GZIPOutputStream;

import static battlecode.util.FlatHelpers.*;

/**
 * Writes a game to a flatbuffer, hooray.
 */
public strictfp class GameMaker {

    /**
     * The protocol expects a series of valid state transitions;
     * we ensure that's true.
     */
    private enum State {
        /**
         * Waiting to write game header.
         */
        GAME_HEADER,
        /**
         * In a game, but not a match.
         */
        IN_GAME,
        /**
         * In a match.
         */
        IN_MATCH,
        /**
         * Complete.
         */
        DONE
    }
    private State state;

    // this un-separation-of-concerns makes me uncomfortable

    /**
     * We write the whole match to this builder, then write it to a file.
     */
    private final FlatBufferBuilder fileBuilder;

    /**
     * Null until the end of the match.
     */
    private byte[] finishedGame;

    /**
     * We have a separate byte[] for each packet sent to the client.
     * This is necessary because flatbuffers shares metadata between structures, so we
     * can't just cut out chunks of the larger buffer :/
     */
    private FlatBufferBuilder packetBuilder;

    /**
     * The server we're sending packets on.
     * May be null.
     */
    private final NetServer packetSink;

    /**
     * Information about the active game.
     */
    private final GameInfo gameInfo;

    /**
     * Only relevant to the file builder:
     * We add a table called a GameWrapper to the front of the saved files
     * that lets you quickly navigate to events by index, and tells you the
     * indices of headers and footers.
     */
    private TIntArrayList events;
    private TIntArrayList matchHeaders;
    private TIntArrayList matchFooters;

    /**
     * The MatchMaker associated with this GameMaker.
     */
    private final MatchMaker matchMaker;

    /**
     * Whether to serialize indicator dots and lines into the flatbuffer.
     */
    private final boolean showIndicators;

    /**
     * @param gameInfo the mapping of teams to bytes
     * @param packetSink the NetServer to send packets to
     * @param showIndicators whether to write indicator dots and lines to replay
     */
    public GameMaker(final GameInfo gameInfo, final NetServer packetSink, final boolean showIndicators) {
        this.state = State.GAME_HEADER;

        this.gameInfo = gameInfo;

        this.packetSink = packetSink;
        if (packetSink != null) {
            this.packetBuilder = new FlatBufferBuilder();
        }

        this.fileBuilder = new FlatBufferBuilder();

        this.events = new TIntArrayList();
        this.matchHeaders = new TIntArrayList();
        this.matchFooters = new TIntArrayList();

        this.matchMaker = new MatchMaker();

        this.showIndicators = showIndicators;
    }

    /**
     * Assert we're in a particular state.
     *
     * @param state
     */
    private void assertState(State state) {
        if (this.state != state) {
            throw new RuntimeException("Incorrect GameMaker state: should be "+
                    state+", but is: "+this.state);
        }
    }

    /**
     * Make a state transition.
     */
    private void changeState(State start, State end) {
        assertState(start);
        this.state = end;
    }


    /**
     * Convert entire game to a byte array.
     *
     * @return game as a packed flatbuffer byte array.
     */
    public byte[] toBytes() {
        if (finishedGame == null) {
            assertState(State.DONE);

            int events = GameWrapper.createEventsVector(fileBuilder, this.events.toArray());
            int matchHeaders = GameWrapper.createMatchHeadersVector(fileBuilder, this.matchHeaders.toArray());
            int matchFooters = GameWrapper.createMatchFootersVector(fileBuilder, this.matchFooters.toArray());

            GameWrapper.startGameWrapper(fileBuilder);
            GameWrapper.addEvents(fileBuilder, events);
            GameWrapper.addMatchHeaders(fileBuilder, matchHeaders);
            GameWrapper.addMatchFooters(fileBuilder, matchFooters);

            fileBuilder.finish(GameWrapper.endGameWrapper(fileBuilder));

            byte[] rawBytes = fileBuilder.sizedByteArray();

            try {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                GZIPOutputStream zipper = new GZIPOutputStream(result);
                IOUtils.copy(new ByteArrayInputStream(rawBytes), zipper);
                zipper.close();
                zipper.flush();
                result.flush();
                finishedGame = result.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Gzipping failed?", e);
            }
        }
        return finishedGame;
    }

    /**
     * Write a match out to a file.
     *
     * @param saveFile the file to save to
     */
    public void writeGame(File saveFile) {
        if (saveFile == null) {
            throw new RuntimeException("Null file provided to writeGame");
        }

        try {
            FileUtils.writeByteArrayToFile(saveFile, toBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run the same logic for both builders.
     *
     * @param perBuilder called with each builder; return event id. Should not mutate state.
     */
    private void createEvent(ToIntFunction<FlatBufferBuilder> perBuilder) {
        // make file event and add its offset to the list
        int eventAP = perBuilder.applyAsInt(fileBuilder);
        events.add(eventAP);

        if (packetSink != null) {
            // make packet event and package it up
            int eventBP = perBuilder.applyAsInt(packetBuilder);
            packetBuilder.finish(eventBP);
            packetSink.addEvent(packetBuilder.sizedByteArray());

            // reset packet builder
            packetBuilder = new FlatBufferBuilder(packetBuilder.dataBuffer());
        }
    }

    /**
     * Get the MatchMaker associated with this GameMaker.
     */
    public MatchMaker getMatchMaker() {
        return this.matchMaker;
    }

    public void makeGameHeader() {

        changeState(State.GAME_HEADER, State.IN_GAME);

        createEvent((builder) -> {
            int specVersionOffset = builder.createString(GameConstants.SPEC_VERSION);

            int name = builder.createString(gameInfo.getTeamAName());
            int packageName = builder.createString(gameInfo.getTeamAPackage());
            TeamData.startTeamData(builder);
            TeamData.addName(builder, name);
            TeamData.addPackageName(builder, packageName);
            TeamData.addTeamID(builder, TeamMapping.id(Team.A));
            int teamAOffset = TeamData.endTeamData(builder);

            name = builder.createString(gameInfo.getTeamBName());
            packageName = builder.createString(gameInfo.getTeamBPackage());
            TeamData.startTeamData(builder);
            TeamData.addName(builder, name);
            TeamData.addPackageName(builder, packageName);
            TeamData.addTeamID(builder, TeamMapping.id(Team.B));
            int teamBOffset = TeamData.endTeamData(builder);
            int[] teamsVec = {teamAOffset, teamBOffset};

            int teamsOffset = GameHeader.createTeamsVector(builder, teamsVec);
            int bodyTypeMetadataOffset = makeBodyTypeMetadata(builder);

            Constants.startConstants(builder);
            Constants.addIncreasePeriod(builder, GameConstants.ADD_LEAD_EVERY_ROUNDS);
            Constants.addLeadAdditiveIncease(builder, GameConstants.ADD_LEAD);
            int constantsOffset = Constants.endConstants(builder);

            GameHeader.startGameHeader(builder);
            GameHeader.addSpecVersion(builder, specVersionOffset);
            GameHeader.addTeams(builder, teamsOffset);
            GameHeader.addBodyTypeMetadata(builder, bodyTypeMetadataOffset);
            GameHeader.addConstants(builder, constantsOffset);
            int gameHeaderOffset = GameHeader.endGameHeader(builder);

            return EventWrapper.createEventWrapper(builder, Event.GameHeader, gameHeaderOffset);
        });
    }

    public int makeBodyTypeMetadata(FlatBufferBuilder builder) {
        TIntArrayList bodyTypeMetadataOffsets = new TIntArrayList();

        // Add robot metadata
        for (RobotType type : RobotType.values()) {
            BodyTypeMetadata.startBodyTypeMetadata(builder);
            BodyTypeMetadata.addType(builder, robotTypeToBodyType(type));
            int[] buildCostLeadArray = {type.buildCostLead, type.getLeadMutateCost(2), type.getLeadMutateCost(3)};
            int buildCostLeadOffset = BodyTypeMetadata.createBuildCostLeadVector(builder, buildCostLeadArray);
            int[] buildCostGoldArray = {type.buildCostGold, type.getGoldMutateCost(2), type.getGoldMutateCost(3)};
            int buildCostGoldOffset = BodyTypeMetadata.createBuildCostGoldVector(builder, buildCostGoldArray);
            BodyTypeMetadata.addBuildCostLead(builder, buildCostLeadOffset);
            BodyTypeMetadata.addBuildCostGold(builder, buildCostGoldOffset);
            BodyTypeMetadata.addActionCooldown(builder, type.actionCooldown);
            BodyTypeMetadata.addMovementCooldown(builder, type.movementCooldown);
            BodyTypeMetadata.addHealth(builder, type.health);
            BodyTypeMetadata.addDamage(builder, type.damage);
            BodyTypeMetadata.addActionRadiusSquared(builder, type.actionRadiusSquared);
            BodyTypeMetadata.addVisionRadiusSquared(builder, type.visionRadiusSquared);
            BodyTypeMetadata.addBytecodeLimit(builder, type.bytecodeLimit);
            bodyTypeMetadataOffsets.add(BodyTypeMetadata.endBodyTypeMetadata(builder));
        }

        // Make and return BodyTypeMetadata Vector offset
        return GameHeader.createBodyTypeMetadataVector(builder, bodyTypeMetadataOffsets.toArray());
    }

    private byte robotTypeToBodyType(RobotType type) {
        if (type == RobotType.ARCHON) return BodyType.ARCHON;
        if (type == RobotType.LABORATORY) return BodyType.LABORATORY;
        if (type == RobotType.WATCHTOWER) return BodyType.WATCHTOWER;
        if (type == RobotType.MINER) return BodyType.MINER;
        if (type == RobotType.BUILDER) return BodyType.BUILDER;
        if (type == RobotType.SOLDIER) return BodyType.SOLDIER;
        if (type == RobotType.SAGE) return BodyType.SAGE;
        return Byte.MIN_VALUE;
    }

    public void makeGameFooter(Team winner) {
        changeState(State.IN_GAME, State.DONE);

        createEvent((builder) -> EventWrapper.createEventWrapper(builder, Event.GameFooter,
                GameFooter.createGameFooter(builder, TeamMapping.id(winner))));
    }

    /**
     * Writes events from match to one or multiple flatbuffers.
     *
     * One of the rare cases where we want a non-static inner class in Java:
     * this basically just provides a restricted interface to GameMaker.
     *
     * There is only one of these per GameMaker.
     */
    public class MatchMaker {
        private TIntArrayList movedIDs; // ints
        // VecTable for movedLocs in Round
        private TIntArrayList movedLocsX;
        private TIntArrayList movedLocsY;

        // SpawnedBodyTable for spawnedBodies
        private TIntArrayList spawnedBodiesRobotIDs;
        private TByteArrayList spawnedBodiesTeamIDs;
        private TByteArrayList spawnedBodiesTypes;
        private TIntArrayList spawnedBodiesLocsXs; //For locs
        private TIntArrayList spawnedBodiesLocsYs; //For locs

        private TIntArrayList diedIDs; // ints

        private TIntArrayList actionIDs; // ints
        private TByteArrayList actions; // Actions
        private TIntArrayList actionTargets; // ints (IDs)

        private TIntArrayList leadDropLocsX;
        private TIntArrayList leadDropLocsY;
        private TIntArrayList leadDropValues;

        private TIntArrayList goldDropLocsX;
        private TIntArrayList goldDropLocsY;
        private TIntArrayList goldDropValues;

        // Round statistics
        private TIntArrayList teamIDs;
        private TIntArrayList teamLeadChanges;
        private TIntArrayList teamGoldChanges;

        private TIntArrayList indicatorStringIDs;
        private ArrayList<String> indicatorStrings;

        // Indicator dots with locations and RGB values
        private TIntArrayList indicatorDotIDs;
        private TIntArrayList indicatorDotLocsX;
        private TIntArrayList indicatorDotLocsY;
        private TIntArrayList indicatorDotRGBsRed;
        private TIntArrayList indicatorDotRGBsGreen;
        private TIntArrayList indicatorDotRGBsBlue;

        // Indicator lines with locations and RGB values
        private TIntArrayList indicatorLineIDs;
        private TIntArrayList indicatorLineStartLocsX;
        private TIntArrayList indicatorLineStartLocsY;
        private TIntArrayList indicatorLineEndLocsX;
        private TIntArrayList indicatorLineEndLocsY;
        private TIntArrayList indicatorLineRGBsRed;
        private TIntArrayList indicatorLineRGBsGreen;
        private TIntArrayList indicatorLineRGBsBlue;

        // Robot IDs and their bytecode usage
        private TIntArrayList bytecodeIDs;
        private TIntArrayList bytecodesUsed;

        // Used to write logs.
        private final ByteArrayOutputStream logger;

        public MatchMaker() {
            this.movedIDs = new TIntArrayList();
            this.movedLocsX = new TIntArrayList();
            this.movedLocsY = new TIntArrayList();
            this.spawnedBodiesRobotIDs = new TIntArrayList();
            this.spawnedBodiesTeamIDs = new TByteArrayList();
            this.spawnedBodiesTypes = new TByteArrayList();
            this.spawnedBodiesLocsXs = new TIntArrayList();
            this.spawnedBodiesLocsYs = new TIntArrayList();
            this.diedIDs = new TIntArrayList();
            this.actionIDs = new TIntArrayList();
            this.actions = new TByteArrayList();
            this.actionTargets = new TIntArrayList();
            this.leadDropLocsX = new TIntArrayList();
            this.leadDropLocsY = new TIntArrayList();
            this.leadDropValues = new TIntArrayList();
            this.goldDropLocsX = new TIntArrayList();
            this.goldDropLocsY = new TIntArrayList();
            this.goldDropValues = new TIntArrayList();
            this.teamIDs = new TIntArrayList();
            this.teamLeadChanges = new TIntArrayList();
            this.teamGoldChanges = new TIntArrayList();
            this.indicatorStringIDs = new TIntArrayList();
            this.indicatorStrings = new ArrayList<>();
            this.indicatorDotIDs = new TIntArrayList();
            this.indicatorDotLocsX = new TIntArrayList();
            this.indicatorDotLocsY = new TIntArrayList();
            this.indicatorDotRGBsRed = new TIntArrayList();
            this.indicatorDotRGBsBlue = new TIntArrayList();
            this.indicatorDotRGBsGreen = new TIntArrayList();
            this.indicatorLineIDs = new TIntArrayList();
            this.indicatorLineStartLocsX = new TIntArrayList();
            this.indicatorLineStartLocsY = new TIntArrayList();
            this.indicatorLineEndLocsX = new TIntArrayList();
            this.indicatorLineEndLocsY = new TIntArrayList();
            this.indicatorLineRGBsRed = new TIntArrayList();
            this.indicatorLineRGBsBlue = new TIntArrayList();
            this.indicatorLineRGBsGreen = new TIntArrayList();
            this.bytecodeIDs = new TIntArrayList();
            this.bytecodesUsed = new TIntArrayList();
            this.logger = new ByteArrayOutputStream();
        }

        public void makeMatchHeader(LiveMap gameMap) {
            changeState(State.IN_GAME, State.IN_MATCH);

            createEvent((builder) -> {
                int map = GameMapIO.Serial.serialize(builder, gameMap);

                return EventWrapper.createEventWrapper(builder, Event.MatchHeader,
                        MatchHeader.createMatchHeader(builder, map, gameMap.getRounds()));
            });

            matchHeaders.add(events.size() - 1);

            clearData();
        }

        public void makeMatchFooter(Team winTeam, int totalRounds, List<ProfilerCollection> profilerCollections) {
            changeState(State.IN_MATCH, State.IN_GAME);

            createEvent((builder) -> {
                TIntArrayList profilerFiles = new TIntArrayList();

                for (ProfilerCollection profilerCollection : profilerCollections) {
                    TIntArrayList frames = new TIntArrayList();
                    TIntArrayList profiles = new TIntArrayList();

                    for (String frame : profilerCollection.getFrames()) {
                        frames.add(builder.createString(frame));
                    }

                    for (Profiler profiler : profilerCollection.getProfilers()) {
                        TIntArrayList events = new TIntArrayList();

                        for (battlecode.instrumenter.profiler.ProfilerEvent event : profiler.getEvents()) {
                            ProfilerEvent.startProfilerEvent(builder);
                            ProfilerEvent.addIsOpen(builder, event.getType() == ProfilerEventType.OPEN);
                            ProfilerEvent.addAt(builder, event.getAt());
                            ProfilerEvent.addFrame(builder, event.getFrameId());
                            events.add(ProfilerEvent.endProfilerEvent(builder));
                        }

                        int nameOffset = builder.createString(profiler.getName());
                        int eventsOffset = ProfilerProfile.createEventsVector(builder, events.toArray());

                        ProfilerProfile.startProfilerProfile(builder);
                        ProfilerProfile.addName(builder, nameOffset);
                        ProfilerProfile.addEvents(builder, eventsOffset);
                        profiles.add(ProfilerProfile.endProfilerProfile(builder));
                    }

                    int framesOffset = ProfilerFile.createFramesVector(builder, frames.toArray());
                    int profilesOffset = ProfilerFile.createProfilesVector(builder, profiles.toArray());

                    profilerFiles.add(ProfilerFile.createProfilerFile(builder, framesOffset, profilesOffset));
                }

                int profilerFilesOffset = MatchFooter.createProfilerFilesVector(builder, profilerFiles.toArray());

                return EventWrapper.createEventWrapper(builder, Event.MatchFooter,
                    MatchFooter.createMatchFooter(builder, TeamMapping.id(winTeam), totalRounds, profilerFilesOffset));
            });

            matchFooters.add(events.size() - 1);
        }

        public void makeRound(int roundNum) {
            assertState(State.IN_MATCH);

            try {
                this.logger.flush();
            } catch (IOException e) {
                throw new RuntimeException("Can't flush byte[]outputstream?", e);
            }
            // byte[] logs = this.logger.toByteArray();
            this.logger.reset();

            createEvent((builder) -> {
                // The bodies that spawned
                int spawnedBodiesLocsP = createVecTable(builder, spawnedBodiesLocsXs, spawnedBodiesLocsYs);
                int spawnedBodiesRobotIDsP = SpawnedBodyTable.createRobotIDsVector(builder, spawnedBodiesRobotIDs.toArray());
                int spawnedBodiesTeamIDsP = SpawnedBodyTable.createTeamIDsVector(builder, spawnedBodiesTeamIDs.toArray());
                int spawnedBodiesTypesP = SpawnedBodyTable.createTypesVector(builder, spawnedBodiesTypes.toArray());
                SpawnedBodyTable.startSpawnedBodyTable(builder);
                SpawnedBodyTable.addLocs(builder, spawnedBodiesLocsP);
                SpawnedBodyTable.addRobotIDs(builder, spawnedBodiesRobotIDsP);
                SpawnedBodyTable.addTeamIDs(builder, spawnedBodiesTeamIDsP);
                SpawnedBodyTable.addTypes(builder, spawnedBodiesTypesP);
                int spawnedBodiesP = SpawnedBodyTable.endSpawnedBodyTable(builder);

                // Round statistics
                int teamIDsP = Round.createTeamIDsVector(builder, teamIDs.toArray());
                int teamLeadChangesP = Round.createTeamLeadChangesVector(builder, teamLeadChanges.toArray());
                int teamGoldChangesP = Round.createTeamGoldChangesVector(builder, teamGoldChanges.toArray());

                // The bodies that moved
                int movedIDsP = Round.createMovedIDsVector(builder, movedIDs.toArray());
                int movedLocsP = createVecTable(builder, movedLocsX, movedLocsY);

                // The bodies that died
                int diedIDsP = Round.createDiedIDsVector(builder, diedIDs.toArray());

                // The actions that happened
                int actionIDsP = Round.createActionIDsVector(builder, actionIDs.toArray());
                int actionsP = Round.createActionsVector(builder, actions.toArray());
                int actionTargetsP = Round.createActionTargetsVector(builder, actionTargets.toArray());

                // The lead and gold dropped
                int leadDropLocsP = createVecTable(builder, leadDropLocsX, leadDropLocsY);
                int leadDropValuesP = Round.createLeadDropValuesVector(builder, leadDropValues.toArray());
                int goldDropLocsP = createVecTable(builder, goldDropLocsX, goldDropLocsY);
                int goldDropValuesP = Round.createGoldDropValuesVector(builder, goldDropValues.toArray());

                // The indicator strings that were set
                int indicatorStringIDsP = Round.createIndicatorStringIDsVector(builder, indicatorStringIDs.toArray());
                TIntArrayList indicatorStringsIntList = new TIntArrayList();
                for (String s : indicatorStrings) {
                    indicatorStringsIntList.add(builder.createString(s));
                }
                int indicatorStringsP = Round.createIndicatorStringsVector(builder, indicatorStringsIntList.toArray());

                // The indicator dots that were set
                int indicatorDotIDsP = Round.createIndicatorDotIDsVector(builder, indicatorDotIDs.toArray());
                int indicatorDotLocsP = createVecTable(builder, indicatorDotLocsX, indicatorDotLocsY);
                int indicatorDotRGBsP = createRGBTable(builder, indicatorDotRGBsRed, indicatorDotRGBsGreen, indicatorDotRGBsBlue);

                // The indicator lines that were set
                int indicatorLineIDsP = Round.createIndicatorLineIDsVector(builder, indicatorLineIDs.toArray());
                int indicatorLineStartLocsP = createVecTable(builder, indicatorLineStartLocsX, indicatorLineStartLocsY);
                int indicatorLineEndLocsP = createVecTable(builder, indicatorLineEndLocsX, indicatorLineEndLocsY);
                int indicatorLineRGBsP = createRGBTable(builder, indicatorLineRGBsRed, indicatorLineRGBsGreen, indicatorLineRGBsBlue);

                // The bytecode usage
                int bytecodeIDsP = Round.createBytecodeIDsVector(builder, bytecodeIDs.toArray());
                int bytecodesUsedP = Round.createBytecodesUsedVector(builder, bytecodesUsed.toArray());

                Round.startRound(builder);
                Round.addTeamIDs(builder, teamIDsP);
                Round.addTeamLeadChanges(builder, teamLeadChangesP);
                Round.addTeamGoldChanges(builder, teamGoldChangesP);
                Round.addMovedIDs(builder, movedIDsP);
                Round.addMovedLocs(builder, movedLocsP);
                Round.addSpawnedBodies(builder, spawnedBodiesP);
                Round.addDiedIDs(builder, diedIDsP);
                Round.addActionIDs(builder, actionIDsP);
                Round.addActions(builder, actionsP);
                Round.addActionTargets(builder, actionTargetsP);
                Round.addLeadDropLocations(builder, leadDropLocsP);
                Round.addLeadDropValues(builder, leadDropValuesP);
                Round.addGoldDropLocations(builder, goldDropLocsP);
                Round.addGoldDropValues(builder, goldDropValuesP);
                Round.addIndicatorStringIDs(builder, indicatorStringIDsP);
                Round.addIndicatorStrings(builder, indicatorStringsP);
                Round.addIndicatorDotIDs(builder, indicatorDotIDsP);
                Round.addIndicatorDotLocs(builder, indicatorDotLocsP);
                Round.addIndicatorDotRGBs(builder, indicatorDotRGBsP);
                Round.addIndicatorLineIDs(builder, indicatorLineIDsP);
                Round.addIndicatorLineStartLocs(builder, indicatorLineStartLocsP);
                Round.addIndicatorLineEndLocs(builder, indicatorLineEndLocsP);
                Round.addIndicatorLineRGBs(builder, indicatorLineRGBsP);
                Round.addRoundID(builder, roundNum);
                Round.addBytecodeIDs(builder, bytecodeIDsP);
                Round.addBytecodesUsed(builder, bytecodesUsedP);
                int round = Round.endRound(builder);
                return EventWrapper.createEventWrapper(builder, Event.Round, round);
            });

            clearData();
        }

        /**
         * @return an outputstream that will be baked into the output file
         */
        public OutputStream getOut() {
            return logger;
        }

        public void addMoved(int id, MapLocation newLocation) {
            movedIDs.add(id);
            movedLocsX.add(newLocation.x);
            movedLocsY.add(newLocation.y);
        }

        public void addDied(int id) {
            diedIDs.add(id);
        }

        public void addAction(int userID, byte action, int targetID) {
            actionIDs.add(userID);
            actions.add(action);
            actionTargets.add(targetID);
        }

        public void addLeadDrop(MapLocation location, int value) {
            leadDropLocsX.add(location.x);
            leadDropLocsY.add(location.y);
            leadDropValues.add(value);
        }

        public void addGoldDrop(MapLocation location, int value) {
            goldDropLocsX.add(location.x);
            goldDropLocsY.add(location.y);
            goldDropValues.add(value);
        }

        public void addTeamInfo(Team team, int leadChange, int goldChange) {
            teamIDs.add(TeamMapping.id(team));
            teamLeadChanges.add(leadChange);
            teamGoldChanges.add(goldChange);
        }

        public void addIndicatorString(int id, String string) {
            if (!showIndicators) {
                return;
            }
            indicatorStringIDs.add(id);
            indicatorStrings.add(string);
        }

        public void addIndicatorDot(int id, MapLocation loc, int red, int green, int blue) {
            if (!showIndicators) {
                return;
            }
            indicatorDotIDs.add(id);
            indicatorDotLocsX.add(loc.x);
            indicatorDotLocsY.add(loc.y);
            indicatorDotRGBsRed.add(red);
            indicatorDotRGBsGreen.add(green);
            indicatorDotRGBsBlue.add(blue);
        }

        public void addIndicatorLine(int id, MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) {
            if (!showIndicators) {
                return;
            }
            indicatorLineIDs.add(id);
            indicatorLineStartLocsX.add(startLoc.x);
            indicatorLineStartLocsY.add(startLoc.y);
            indicatorLineEndLocsX.add(endLoc.x);
            indicatorLineEndLocsY.add(endLoc.y);
            indicatorLineRGBsRed.add(red);
            indicatorLineRGBsGreen.add(green);
            indicatorLineRGBsBlue.add(blue);
        }

        public void addBytecodes(int id, int bytecodes) {
            bytecodeIDs.add(id);
            bytecodesUsed.add(bytecodes);
        }

        public void addSpawnedRobot(InternalRobot robot) {
            spawnedBodiesRobotIDs.add(robot.getID());
            spawnedBodiesLocsXs.add(robot.getLocation().x);
            spawnedBodiesLocsYs.add(robot.getLocation().y);
            spawnedBodiesTeamIDs.add(TeamMapping.id(robot.getTeam()));
            spawnedBodiesTypes.add(FlatHelpers.getBodyTypeFromRobotType(robot.getType()));
        }

        private void clearData() {
            movedIDs.clear();
            movedLocsX.clear();
            movedLocsY.clear();
            spawnedBodiesRobotIDs.clear();
            spawnedBodiesTeamIDs.clear();
            spawnedBodiesTypes.clear();
            spawnedBodiesLocsXs.clear();
            spawnedBodiesLocsYs.clear();
            diedIDs.clear();
            actionIDs.clear();
            actions.clear();
            actionTargets.clear();
            leadDropLocsX.clear();
            leadDropLocsY.clear();
            leadDropValues.clear();
            goldDropLocsX.clear();
            goldDropLocsY.clear();
            goldDropValues.clear();
            teamIDs.clear();
            teamLeadChanges.clear();
            teamGoldChanges.clear();
            indicatorStringIDs.clear();
            indicatorStrings.clear();
            indicatorDotIDs.clear();
            indicatorDotLocsX.clear();
            indicatorDotLocsY.clear();
            indicatorDotRGBsRed.clear();
            indicatorDotRGBsBlue.clear();
            indicatorDotRGBsGreen.clear();
            indicatorLineIDs.clear();
            indicatorLineStartLocsX.clear();
            indicatorLineStartLocsY.clear();
            indicatorLineEndLocsX.clear();
            indicatorLineEndLocsY.clear();
            indicatorLineRGBsRed.clear();
            indicatorLineRGBsBlue.clear();
            indicatorLineRGBsGreen.clear();
            bytecodeIDs.clear();
            bytecodesUsed.clear();
        }
    }
}
