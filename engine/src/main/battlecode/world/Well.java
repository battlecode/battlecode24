package battlecode.world;

import javax.lang.model.util.ElementScanner6;

import battlecode.common.MapLocation;
import battlecode.common.ResourceType;

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

    public Inventory getInventory(){
        return inv;
    }

    public MapLocation getMapLocation(){
        return loc;
    }

    public void addAdamantium(int amount){
        inv.addAdamantium(amount);
        if (type == ResourceType.MANA && inv.getAdamantium() >= 15000)
            type = ResourceType.ELIXIR;
            //TODO: should inventory be set to zero after an upgrade
            inv.addAdamantium(-inv.getAdamantium());
            inv.addMana(-inv.getMana());
    }

    public void addMana(int amount){
        inv.addMana(amount);
        if (type == ResourceType.ADAMANTIUM && inv.getMana() >= 15000)
            type = ResourceType.ELIXIR;
            //TODO: should inventory be set to zero after an upgrade
            inv.addAdamantium(-inv.getAdamantium());
            inv.addMana(-inv.getMana());

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

    public Well copy(){
        Inventory newInv = this.inv.copy();
        Well newWell = new Well(this.loc, this.type, newInv);
        return newWell;
    }

}