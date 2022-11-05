package battlecode.world;

import battlecode.common.*;

public class Inventory {

    /**
     * The maximum amount of resources that the inventory can hold. This will be set to -1 in cases where the inventory
     * has no maximum capacity.
     */
    private int maxCapacity;

    private int adamantium;

    private int mana;

    private int elixir;
    
    /**
     * Creates a new Inventory object with no maximum capacity
     */
    public Inventory() {
        maxCapacity = -1;
    }

    /**
     * Creates a new Inventory object with the given maxmimum capacity
     */
    public Inventory(int maxCapacity){
        this.maxCapacity = maxCapacity;
    }

    public void addAdamantium(int amount) {
        adamantium += amount;
    }

    public void addMana(int amount) {
        mana += amount;
    }

    public void addElixir(int amount) {
        elixir += amount;
    }

    public int getAdamantium() {
        return adamantium;
    }

    public int getMana() {
        return mana;
    }

    public int getElixir() {
        return elixir;
    }

    /*
     * Convenience method that adds a certain amount of the given resource
     */
    public void addResource(ResourceType type, int amount){
        if(type == ResourceType.ADAMANTIUM) adamantium += amount;
        else if(type == ResourceType.ELIXIR) elixir += amount;
        else mana += amount;
    }

    /**
     * Convenience method that returns the amount of the given resource type.
     */
    public int getResource(ResourceType type) {
        if(type == ResourceType.ADAMANTIUM) return adamantium;
        else if(type == ResourceType.ELIXIR) return elixir;
        return mana;
    }

    /**
     * Checks if the given weight of resources can be added to the inventory without going over the maximum capacity.
     */
    public boolean canAdd(int amount) {
        if(maxCapacity == -1) return true;
        int total = adamantium + mana + elixir;
        return total + amount <= maxCapacity;
    }
}
