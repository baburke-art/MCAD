package baburke.mcad.net;

public enum Mode {
    EXTRUDE("Extrude"),
    JOIN("Join"),
    CUT("Cut"),
    MATCHCUT("Match-Cut");

    private final String displayName;

    Mode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}