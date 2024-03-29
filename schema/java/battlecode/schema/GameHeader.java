// automatically generated by the FlatBuffers compiler, do not modify

package battlecode.schema;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.BooleanVector;
import com.google.flatbuffers.ByteVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.DoubleVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.FloatVector;
import com.google.flatbuffers.IntVector;
import com.google.flatbuffers.LongVector;
import com.google.flatbuffers.ShortVector;
import com.google.flatbuffers.StringVector;
import com.google.flatbuffers.Struct;
import com.google.flatbuffers.Table;
import com.google.flatbuffers.UnionVector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The first event sent in the game. Contains all metadata about the game.
 */
@SuppressWarnings("unused")
public final class GameHeader extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
  public static GameHeader getRootAsGameHeader(ByteBuffer _bb) { return getRootAsGameHeader(_bb, new GameHeader()); }
  public static GameHeader getRootAsGameHeader(ByteBuffer _bb, GameHeader obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public GameHeader __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String specVersion() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer specVersionAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public ByteBuffer specVersionInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
  public battlecode.schema.TeamData teams(int j) { return teams(new battlecode.schema.TeamData(), j); }
  public battlecode.schema.TeamData teams(battlecode.schema.TeamData obj, int j) { int o = __offset(6); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int teamsLength() { int o = __offset(6); return o != 0 ? __vector_len(o) : 0; }
  public battlecode.schema.TeamData.Vector teamsVector() { return teamsVector(new battlecode.schema.TeamData.Vector()); }
  public battlecode.schema.TeamData.Vector teamsVector(battlecode.schema.TeamData.Vector obj) { int o = __offset(6); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public battlecode.schema.SpecializationMetadata specializationMetadata(int j) { return specializationMetadata(new battlecode.schema.SpecializationMetadata(), j); }
  public battlecode.schema.SpecializationMetadata specializationMetadata(battlecode.schema.SpecializationMetadata obj, int j) { int o = __offset(8); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int specializationMetadataLength() { int o = __offset(8); return o != 0 ? __vector_len(o) : 0; }
  public battlecode.schema.SpecializationMetadata.Vector specializationMetadataVector() { return specializationMetadataVector(new battlecode.schema.SpecializationMetadata.Vector()); }
  public battlecode.schema.SpecializationMetadata.Vector specializationMetadataVector(battlecode.schema.SpecializationMetadata.Vector obj) { int o = __offset(8); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public battlecode.schema.BuildActionMetadata buildActionMetadata(int j) { return buildActionMetadata(new battlecode.schema.BuildActionMetadata(), j); }
  public battlecode.schema.BuildActionMetadata buildActionMetadata(battlecode.schema.BuildActionMetadata obj, int j) { int o = __offset(10); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int buildActionMetadataLength() { int o = __offset(10); return o != 0 ? __vector_len(o) : 0; }
  public battlecode.schema.BuildActionMetadata.Vector buildActionMetadataVector() { return buildActionMetadataVector(new battlecode.schema.BuildActionMetadata.Vector()); }
  public battlecode.schema.BuildActionMetadata.Vector buildActionMetadataVector(battlecode.schema.BuildActionMetadata.Vector obj) { int o = __offset(10); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public battlecode.schema.GlobalUpgradeMetadata globalUpgradeMetadata(int j) { return globalUpgradeMetadata(new battlecode.schema.GlobalUpgradeMetadata(), j); }
  public battlecode.schema.GlobalUpgradeMetadata globalUpgradeMetadata(battlecode.schema.GlobalUpgradeMetadata obj, int j) { int o = __offset(12); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int globalUpgradeMetadataLength() { int o = __offset(12); return o != 0 ? __vector_len(o) : 0; }
  public battlecode.schema.GlobalUpgradeMetadata.Vector globalUpgradeMetadataVector() { return globalUpgradeMetadataVector(new battlecode.schema.GlobalUpgradeMetadata.Vector()); }
  public battlecode.schema.GlobalUpgradeMetadata.Vector globalUpgradeMetadataVector(battlecode.schema.GlobalUpgradeMetadata.Vector obj) { int o = __offset(12); return o != 0 ? obj.__assign(__vector(o), 4, bb) : null; }
  public battlecode.schema.GameplayConstants constants() { return constants(new battlecode.schema.GameplayConstants()); }
  public battlecode.schema.GameplayConstants constants(battlecode.schema.GameplayConstants obj) { int o = __offset(14); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }

  public static int createGameHeader(FlatBufferBuilder builder,
      int specVersionOffset,
      int teamsOffset,
      int specializationMetadataOffset,
      int buildActionMetadataOffset,
      int globalUpgradeMetadataOffset,
      int constantsOffset) {
    builder.startTable(6);
    GameHeader.addConstants(builder, constantsOffset);
    GameHeader.addGlobalUpgradeMetadata(builder, globalUpgradeMetadataOffset);
    GameHeader.addBuildActionMetadata(builder, buildActionMetadataOffset);
    GameHeader.addSpecializationMetadata(builder, specializationMetadataOffset);
    GameHeader.addTeams(builder, teamsOffset);
    GameHeader.addSpecVersion(builder, specVersionOffset);
    return GameHeader.endGameHeader(builder);
  }

  public static void startGameHeader(FlatBufferBuilder builder) { builder.startTable(6); }
  public static void addSpecVersion(FlatBufferBuilder builder, int specVersionOffset) { builder.addOffset(0, specVersionOffset, 0); }
  public static void addTeams(FlatBufferBuilder builder, int teamsOffset) { builder.addOffset(1, teamsOffset, 0); }
  public static int createTeamsVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startTeamsVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addSpecializationMetadata(FlatBufferBuilder builder, int specializationMetadataOffset) { builder.addOffset(2, specializationMetadataOffset, 0); }
  public static int createSpecializationMetadataVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startSpecializationMetadataVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addBuildActionMetadata(FlatBufferBuilder builder, int buildActionMetadataOffset) { builder.addOffset(3, buildActionMetadataOffset, 0); }
  public static int createBuildActionMetadataVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startBuildActionMetadataVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addGlobalUpgradeMetadata(FlatBufferBuilder builder, int globalUpgradeMetadataOffset) { builder.addOffset(4, globalUpgradeMetadataOffset, 0); }
  public static int createGlobalUpgradeMetadataVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startGlobalUpgradeMetadataVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static void addConstants(FlatBufferBuilder builder, int constantsOffset) { builder.addOffset(5, constantsOffset, 0); }
  public static int endGameHeader(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public GameHeader get(int j) { return get(new GameHeader(), j); }
    public GameHeader get(GameHeader obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

