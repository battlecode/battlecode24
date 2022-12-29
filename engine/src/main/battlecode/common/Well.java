package battlecode.common;

import battlecode.world.Inventory;

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

    public void addAdamantium(int amount){
        inv.addAdamantium(amount);
        if (type == ResourceType.MANA && inv.getAdamantium() >= GameConstants.UPGRADE_TO_ELIXIR) {
            type = ResourceType.ELIXIR;
            //TODO: should inventory be set to zero after an upgrade
            inv.addAdamantium(-inv.getAdamantium());
            inv.addMana(-inv.getMana());
        }
        if (inv.getAdamantium() >= GameConstants.UPGRADE_WELL_AMOUNT && !this.isUpgraded) {
            this.isUpgraded = true;
        }
    }

    public void addMana(int amount){
        inv.addMana(amount);
        if (type == ResourceType.ADAMANTIUM && inv.getMana() >= GameConstants.UPGRADE_TO_ELIXIR) {
            type = ResourceType.ELIXIR;
            //TODO: should inventory be set to zero after an upgrade
            inv.addAdamantium(-inv.getAdamantium());
            inv.addMana(-inv.getMana());
        }
        if (inv.getMana() >= GameConstants.UPGRADE_WELL_AMOUNT && !this.isUpgraded) {
            this.isUpgraded = true;
        }

    }

    public void addElixir(int amount){
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
  
    // TODO: Rather than doing this we should have an upgrade part of the state, this can become weird as we empty it
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

    public String toString(){
        return "Well{" +
                "loc=" + loc +
                ", type=" + type +
                ", inventory=" + inv +
                '}';

    }

}