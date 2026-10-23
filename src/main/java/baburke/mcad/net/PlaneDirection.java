package baburke.mcad.net;

public enum PlaneDirection {
    X("X"),
    Y("Y"),
    Z("Z");

    private final String displayName;

    PlaneDirection(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
