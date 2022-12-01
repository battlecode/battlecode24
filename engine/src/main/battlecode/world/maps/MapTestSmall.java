package battlecode.world.maps;

import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.world.GameMapIO;
import battlecode.world.LiveMap;
import battlecode.world.MapBuilder;
import battlecode.world.TestMapBuilder;

import battlecode.common.GameConstants;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Generate a map.
 */
public class MapTestSmall {

    // change this!!!
    public static final String mapName = "maptestsmall";

    // don't change this!!
    public static final String outputDirectory = "engine/src/main/battlecode/world/resources/";

    /**
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            makeSimple();
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("Generated a map!");
    }

    public static void makeSimple() throws IOException {
        MapBuilder mapBuilder = new MapBuilder(mapName, 32, 32, 0, 0, 30);
        mapBuilder.addSymmetricHeadquarter(5, 5);
        Random random = new Random(6147);

        for (int i = 0; i < mapBuilder.width / 2; i++) {
            for (int j = 0; j < mapBuilder.height; j++) {
                if (i == 5 && j == 5) 
                    continue;
                if (i == 6 && j == 6)
                    mapBuilder.setSymmetricResource(i, j, ResourceType.ADAMANTIUM.resourceID);
                else if (i == 4 && j == 4)
                    mapBuilder.setSymmetricResource(i, j, ResourceType.MANA.resourceID);
                else if (i*13 + j*11 % 10 != 0)
                    continue;
                else
                    mapBuilder.setSymmetricResource(i, j, random.nextInt(3) + 1);

            }
        }

        mapBuilder.saveMap(outputDirectory);
    }
}
