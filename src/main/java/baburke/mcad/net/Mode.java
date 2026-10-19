package baburke.mcad.net;

public enum Mode {
    EXTRUDE("Extrude"),
    CUT("Cut");

    private final String displayName;

    Mode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}