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

    /**
     * Checks if the given weight of resources can be added to the inventory without going over the maximum capacity.
     */
    public boolean canAdd(int amount) {
        if(capacity == -1) return true;
        int total = adamantium + mana + elixir;
        return total + amount <= capacity;
    }
}
