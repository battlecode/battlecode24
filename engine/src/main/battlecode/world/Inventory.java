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

    private int numStandardAnchors = 0;

    private int numAcceleratingAnchors = 0;

    
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

    public Inventory(int maxCapacity, int adamantium, int mana, int elixir, int numStandardAnchors, int numAcceleratingAnchors) {
        this.maxCapacity = maxCapacity;
        this.adamantium = adamantium;
        this.mana = mana;
        this.elixir = elixir;
        this.numStandardAnchors = numStandardAnchors;
        this.numAcceleratingAnchors = numAcceleratingAnchors;
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

    public void addAnchor(Anchor anchor) {
        switch (anchor) {
            case STANDARD:
                numStandardAnchors ++;
                break;
            case ACCELERATING:
                numAcceleratingAnchors ++;
                break;
        }
    }

    public void releaseAnchor(Anchor anchor) {
        switch (anchor) {
            case STANDARD:
                numStandardAnchors --;
                break;
            case ACCELERATING:
                numAcceleratingAnchors --;
                break;
        }
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

    public int getNumAnchors(Anchor anchor) {
        switch (anchor) {
            case STANDARD:
                return numStandardAnchors;
            case ACCELERATING:
                return numAcceleratingAnchors;
            default:
                throw new IllegalArgumentException("No other types of anchors");
        }
    }

    public int getTotalAnchors() {
        return getNumAnchors(Anchor.STANDARD) + getNumAnchors(Anchor.ACCELERATING);
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
        switch (type) {
            case ADAMANTIUM: 
                return adamantium;
            case MANA:
                return mana;
            case ELIXIR:
                return elixir;
            default:
                return 0;
        }
    }

    public int getWeight() {
        int total = (getTotalAnchors() * GameConstants.ANCHOR_WEIGHT) + adamantium + mana + elixir;
        return total;
    }

    /**
     * Checks if the given weight of resources can be added to the inventory without going over the maximum capacity.
     */
    public boolean canAdd(int amount) {
        if(maxCapacity == -1) return true;
        int total = getWeight();
        return total + amount <= maxCapacity;
    }

    public boolean equals(Object o) {
        if (o instanceof Inventory) {
            Inventory other = (Inventory) o;
            return other.maxCapacity == this.maxCapacity 
                && other.adamantium == this.adamantium 
                && other.mana == this.mana 
                && other.elixir == this.elixir 
                && other.numStandardAnchors == this.numStandardAnchors
                && other.numAcceleratingAnchors == this.numAcceleratingAnchors;
        }
        return false;
    }

    public int hashCode() {
        return this.maxCapacity*47 + this.adamantium*37 + this.mana*41 + this.elixir*43 + this.numStandardAnchors*47 + this.numAcceleratingAnchors*51;
    }

    public String toString() {
        return "Inventory{" +
                "maxCapacity=" + maxCapacity +
                ", adamantium=" + adamantium +
                ", mana=" + mana +
                ", elixir=" + elixir + 
                ", standard anchors=" + numStandardAnchors +
                ", accelerating anchors=" + numAcceleratingAnchors +
                '}';
    }

    public Inventory copy() {
        Inventory newInventory = new Inventory(this.maxCapacity, this.adamantium, this.mana, this.elixir, this.numStandardAnchors, this.numAcceleratingAnchors);
        return newInventory;
    }
}
