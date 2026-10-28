package baburke.mcad.net.feature;

public enum Mode {
    EXTRUDE("Extrude"),
    JOIN("Join"),
    CUT("Cut"),
    FASTCUT("Fast-Cut"),
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