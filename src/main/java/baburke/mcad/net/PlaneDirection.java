package baburke.mcad.net;

public enum PlaneDirection {
    A("A"),
    B("B"),
    C("C");

    private final String displayName;

    PlaneDirection(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
