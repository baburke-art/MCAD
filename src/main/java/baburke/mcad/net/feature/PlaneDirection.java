package baburke.mcad.net.feature;

public enum PlaneDirection {
    A("A", 1f, 0f, 0f, 0.04f),
    B("B", 0f, 0f, 1f, 0.04f),
    C("C", 0f, 1f, 0f, 0.04f);

    private final String displayName;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    PlaneDirection(String displayName, float red, float green, float blue, float alpha) {
        this.displayName = displayName;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public float red()   { return red; }
    public float green() { return green; }
    public float blue()  { return blue; }
    public float alpha() { return alpha; }

    @Override
    public String toString() {
        return displayName;
    }
}
