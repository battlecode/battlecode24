public class Inventory {

    private int maxCapacity;

    private int adamantium;

    private int mana;

    private int elixir;
    
    public Inventory() {
        maxCapacity = -1;
    }

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

    public boolean canAdd(int amount) {
        if(capacity == -1) return true;
        int total = adamantium + mana + elixir;
        return total + amount <= capacity;
    }
}
