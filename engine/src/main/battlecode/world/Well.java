package battlecode.world;

import javax.lang.model.util.ElementScanner6;

import battlecode.common.ResourceType;

public class Well {
    private Inventory Inv;

    private MapLocation loc;

    private ResourceType type;

    public Well(MapLocation loc){
        inv = new Inventory();
        this.loc = loc;
    }

    public Inventory getInventory(){
        return Inv;
    }

    public MapLocation getMapLocation(){
        return loc;
    }

    public ResourceType getResourceType(){
        if (type == ResourceType.ADAMANTIUM && Inv.getMnAmount >= 15000)
            type = ResourceType.ELIXIR;
        else if (type == ResourceType.MANA && Inv.getAdAmount >= 15000)
            type = ResourceType.ELIXIR;
        return type;
    }
  
    public boolean isUpgraded(){
        if (type == ResourceType.ADAMANTIUM && Inv.getAdAmount >= 10000)
            return true;
        else if (type == ResourceType.MANA && Inv.getMnAmount >= 10000)
            return true;
        else if (type == ResourceType.ELIXIR && Inv.getExAmount >= 10000)
            return true;
        else 
            return false;
    }

}
    
