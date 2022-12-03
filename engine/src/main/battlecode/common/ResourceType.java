package battlecode.common;

public enum ResourceType {
    NO_RESOURCE(0),
    ADAMANTIUM(1),
    MANA(2),
    ELIXIR(3);


    public final int resourceID;

    private ResourceType(int id) {
        this.resourceID = id;
    }
}
