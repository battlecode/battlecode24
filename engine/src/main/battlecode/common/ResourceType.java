package battlecode.common;

public enum ResourceType {
    NO_RESOURCE(0),
    ADAMANTIUM(1),
    MANA(3),
    ELIXIR(2);


    public final int resourceID;

    private ResourceType(int id) {
        this.resourceID = id;
    }
}
