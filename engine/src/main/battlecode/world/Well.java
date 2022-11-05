package battlecode.world;

import javax.lang.model.util.ElementScanner6;

import battlecode.common.ResourceType;

public class Well {
    private Inventory inv;

    private MapLocation loc;

    private ResourceType type;

    public Well(MapLocation loc){
        inv = new Inventory();
        this.loc = loc;
    }

    public Inventory getInventory(){
        return inv;
    }

    public MapLocation getMapLocation(){
        return loc;
    }

    public void addAdamantium(int amount){
        inv.addAdamantium(amount);
        if (type == ResourceType.MANA && inv.getAdAmount() >= 15000)
            type = ResourceType.ELIXIR;
    }

    public void addMana(int amount){
        inv.addMana(amount);
        if (type == ResourceType.ADAMANTIUM && inv.getMnAmount() >= 15000)
            type = ResourceType.ELIXIR;
    }

    public void addElixir(int amount){
        inv.addElixir(amount);
    }
        
    public ResourceType getResourceType(){
        return type;
    }
  
    public boolean isUpgraded(){
        if (type == ResourceType.ADAMANTIUM && inv.getAdAmount() >= 10000)
            return true;
        else if (type == ResourceType.MANA && inv.getMnAmount() >= 10000)
            return true;
        else if (type == ResourceType.ELIXIR && inv.getExAmount() >= 10000)
            return true;
        else 
            return false;
    }

}
    
