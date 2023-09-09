package battlecode.world;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.WellInfo;

public class Well {

    private Inventory inv;

    private MapLocation loc;

    private ResourceType type;

    private boolean isUpgraded;

    public Well(MapLocation loc, ResourceType type){
        inv = new Inventory();
        this.loc = loc;
        this.type = type;
    }

    public Well(MapLocation loc, ResourceType type, Inventory inv){
        this.inv = inv;
        this.loc = loc;
        this.type = type;
    }

    public int getResource(ResourceType rType){
        return inv.getResource(rType);
    }

    public MapLocation getMapLocation(){
        return loc;
    }

    private void addAdamantium(int amount){
        inv.addAdamantium(amount);
        if (type == ResourceType.MANA && inv.getAdamantium() >= GameConstants.UPGRADE_TO_ELIXIR) {
            type = ResourceType.ELIXIR;
        }
        if (inv.getAdamantium() >= GameConstants.UPGRADE_WELL_AMOUNT && !this.isUpgraded) {
            this.isUpgraded = true;
        }
    }

    private void addMana(int amount){
        inv.addMana(amount);
        if (type == ResourceType.ADAMANTIUM && inv.getMana() >= GameConstants.UPGRADE_TO_ELIXIR) {
            type = ResourceType.ELIXIR;
        }
        if (inv.getMana() >= GameConstants.UPGRADE_WELL_AMOUNT && !this.isUpgraded) {
            this.isUpgraded = true;
        }

    }

    private void addElixir(int amount){
        inv.addElixir(amount);
        if (inv.getElixir() >= GameConstants.UPGRADE_WELL_AMOUNT && !this.isUpgraded) {
            this.isUpgraded = true;
        }
    }

    public void addResourceAmount(ResourceType rType, int amount) {
        switch (rType) {
            case ADAMANTIUM:
                addAdamantium(amount);
                break;
            case MANA:
                addMana(amount);
                break;
            case ELIXIR:
                addElixir(amount);
                break;
            default:
                break;
        }
    }
        
    public ResourceType getResourceType(){
        return type;
    }
  
    public boolean isUpgraded(){
        return this.isUpgraded;
    }

    public int getRate() {
        return isUpgraded() ? GameConstants.WELL_ACCELERATED_RATE : GameConstants.WELL_STANDARD_RATE;
    }

    public int accelerationId() {
        return this.isUpgraded() ? 1 : 0;
    }

    public Well copy(){
        Inventory newInv = this.inv.copy();
        Well newWell = new Well(this.loc, this.type, newInv);
        return newWell;
    }

    public WellInfo getWellInfo(){
        return new WellInfo(loc, type, inv.copy(), isUpgraded);
    }

    public String toString(){
        return "Well{" +
                "loc=" + loc +
                ", type=" + type +
                ", inventory=" + inv +
                '}';

    }

}