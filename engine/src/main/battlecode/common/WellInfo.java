package battlecode.common;

import battlecode.world.Inventory;

public class WellInfo {

    private Inventory inv;

    private MapLocation loc;

    private ResourceType type;

    private boolean isUpgraded;

    public WellInfo(MapLocation loc, ResourceType type, Inventory inv, boolean isUpgraded){
        this.inv = inv.copy();
        this.loc = loc;
        this.type = type;
        this.isUpgraded = isUpgraded;
    }

    /**
     * Returns the amount of resource in the well inventory.
     * 
     * @param rType the resource type of interest
     * @return the amount of resources of this type the inventory has
     * 
     * @battlecode.doc.costlymethod
     */
    public int getResource(ResourceType rType){
        return inv.getResource(rType);
    }

    /**
     * Returns the location of the well
     * 
     * @return the location of the well
     * 
     * @battlecode.doc.costlymethod
     */
    public MapLocation getMapLocation(){
        return loc;
    }

    /**
     * Returns the resource type this well holds
     * 
     * @return the resource this well holds
     * 
     * @battlecode.doc.costlymethod
     */
    public ResourceType getResourceType(){
        return type;
    }
  
    /**
     * Returns whether this well is upgraded
     * 
     * @return whether the well is upgraded
     * 
     * @battlecode.doc.costlymethod
     */
    public boolean isUpgraded(){
        return this.isUpgraded;
    }

    /**
     * Returns the maximum rate at which can be taken from this well
     * 
     * @return the maximum possible rate of extraction from this well
     * 
     * @battlecode.doc.costlymethod
     */
    public int getRate() {
        return isUpgraded() ? GameConstants.WELL_ACCELERATED_RATE : GameConstants.WELL_STANDARD_RATE;
    }

    public String toString(){
        return "Well{" +
                "loc=" + loc +
                ", type=" + type +
                ", inventory=" + inv +
                ", upgraded=" + isUpgraded +
                '}';

    }

}