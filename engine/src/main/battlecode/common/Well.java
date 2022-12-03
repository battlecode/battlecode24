package battlecode.common;

import battlecode.world.Inventory;

public class Well {

    private Inventory inv;

    private MapLocation loc;

    private ResourceType type;

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
        if (type == ResourceType.MANA && inv.getAdamantium() >= 15000) {
            type = ResourceType.ELIXIR;
            //TODO: should inventory be set to zero after an upgrade
            //TODO: Let's come back to this code
            inv.addAdamantium(-inv.getAdamantium());
            inv.addMana(-inv.getMana());
        }
    }

    public void addMana(int amount){
        inv.addMana(amount);
        if (type == ResourceType.ADAMANTIUM && inv.getMana() >= 15000) {
            type = ResourceType.ELIXIR;
            //TODO: should inventory be set to zero after an upgrade
            inv.addAdamantium(-inv.getAdamantium());
            inv.addMana(-inv.getMana());
        }

    }

    public void addElixir(int amount){
        inv.addElixir(amount);
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
        if (type == ResourceType.ADAMANTIUM && inv.getAdamantium() >= 10000)
            return true;
        else if (type == ResourceType.MANA && inv.getMana() >= 10000)
            return true;
        else if (type == ResourceType.ELIXIR && inv.getElixir() >= 10000)
            return true;
        else 
            return false;
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