package battlecode.util;

import battlecode.common.GlobalUpgrade;
import battlecode.common.TrapType;
import battlecode.schema.VecTable;
import battlecode.schema.Action;
import battlecode.schema.BuildActionType;
import battlecode.schema.GlobalUpgradeType;
import battlecode.schema.RGBTable;
import com.google.flatbuffers.FlatBufferBuilder;
import gnu.trove.TByteCollection;
import gnu.trove.list.TByteList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TCharList;
import gnu.trove.list.array.TByteArrayList;

import java.util.List;
import java.util.function.ObjIntConsumer;

/**
 * Misc. helper functions for working with flatbuffers.
 *
 * @author james
 */
public class FlatHelpers {
   
    public static byte getTrapActionFromTrapType(TrapType type) {
        switch(type) {
            case EXPLOSIVE:
                return Action.EXPLOSIVE_TRAP;
            case WATER:
                return Action.WATER_TRAP;
            case STUN:
                return Action.STUN_TRAP;
            default:
                throw new RuntimeException("No action type for " + type);
        }
    }

    public static byte getBuildActionFromTrapType(TrapType type) {
        switch(type) {
            case EXPLOSIVE:
                return BuildActionType.EXPLOSIVE_TRAP;
            case WATER:
                return BuildActionType.WATER_TRAP;
            case STUN:
                return BuildActionType.STUN_TRAP;
            default:
                throw new RuntimeException("No build action type for " + type);
        }
    }

    public static byte getGlobalUpgradeTypeFromGlobalUpgrade(GlobalUpgrade gu) {
        if(gu == GlobalUpgrade.ACTION) return GlobalUpgradeType.ACTION_UPGRADE;
        if(gu == GlobalUpgrade.HEALING) return GlobalUpgradeType.HEALING_UPGRADE;
        if(gu == GlobalUpgrade.CAPTURING) return GlobalUpgradeType.CAPTURING_UPGRADE;
        return Byte.MIN_VALUE;
    }


    /**
     * DO NOT CALL THIS WITH OFFSETS!
     * Only call it when you're adding an actual int[] to a buffer,
     * not a Table[].
     * For that, call offsetVector.
     *
     * Well that's a weird API.
     *
     * Call like so:
     * int xyzP = intVector(builder, xyz, BufferType::startXyzVector);
     */
    // public static int intVector(FlatBufferBuilder builder,
    //                             TIntList arr,
    //                             ObjIntConsumer<FlatBufferBuilder> start) {
    //     final int length = arr.size();
    //     start.accept(builder, length);

    //     // arrays go backwards in flatbuffers
    //     // for reasons
    //     for (int i = length - 1; i >= 0; i--) {
    //         builder.addInt(arr.get(i));
    //     }
    //     return builder.endVector();
    // }

    /**
     * This is DIFFERENT from intVector!
     *
     * Call this when you're adding a table of offsets, not flat ints.
     */
    // public static int offsetVector(FlatBufferBuilder builder,
    //                                TIntList arr,
    //                                ObjIntConsumer<FlatBufferBuilder> start) {
    //     final int length = arr.size();
    //     start.accept(builder, length);

    //     // arrays go backwards in flatbuffers
    //     // for reasons
    //     for (int i = length - 1; i >= 0; i--) {
    //         builder.addOffset(arr.get(i));
    //     }
    //     return builder.endVector();
    // }

    // public static int floatVector(FlatBufferBuilder builder,
    //                               TFloatList arr,
    //                               ObjIntConsumer<FlatBufferBuilder> start) {
    //     final int length = arr.size();
    //     start.accept(builder, length);

    //     for (int i = length - 1; i >= 0; i--) {
    //         builder.addFloat(arr.get(i));
    //     }
    //     return builder.endVector();
    // }

    // public static int byteVector(FlatBufferBuilder builder,
    //                              TByteList arr,
    //                              ObjIntConsumer<FlatBufferBuilder> start) {
    //     final int length = arr.size();
    //     start.accept(builder, length);

    //     for (int i = length - 1; i >= 0; i--) {
    //         builder.addByte(arr.get(i));
    //     }
    //     return builder.endVector();
    // }

    // public static int charVector(FlatBufferBuilder builder,
    //                               TCharList arr,
    //                               ObjIntConsumer<FlatBufferBuilder> start) {
    //     final int length = arr.size();
    //     start.accept(builder, length);

    //     for (int i = length - 1; i >= 0; i--) {
    //         builder.addInt(arr.get(i));
    //     }
    //     return builder.endVector();
    // }

    public static int createVecTable(FlatBufferBuilder builder, TIntList xs, TIntList ys) {
        if (xs.size() != ys.size()) {
            throw new RuntimeException("Mismatched x/y length: "+xs.size()+" != "+ys.size());
        }
        // int xsP = intVector(builder, xs, VecTable::startXsVector);
        // int ysP = intVector(builder, ys, VecTable::startYsVector);
        int xsP = VecTable.createXsVector(builder, xs.toArray());
        int ysP = VecTable.createYsVector(builder, ys.toArray());
        return VecTable.createVecTable(builder, xsP, ysP);
    }

    public static int createRGBTable(FlatBufferBuilder builder, TIntList red, TIntList green, TIntList blue) {
        if (red.size() != green.size() || green.size() != blue.size()) {
            throw new RuntimeException("Mismatched lengths: "+red.size()+", "+green.size()+", "+blue.size());
        }
        // int redP = intVector(builder, red, RGBTable::startRedVector);
        // int greenP = intVector(builder, green, RGBTable::startGreenVector);
        // int blueP = intVector(builder, blue, RGBTable::startBlueVector);
        int redP = RGBTable.createRedVector(builder, red.toArray());
        int greenP = RGBTable.createGreenVector(builder, green.toArray());
        int blueP = RGBTable.createGreenVector(builder, blue.toArray());
        return RGBTable.createRGBTable(builder, redP, greenP, blueP);
    }
}
