package baburke.mcad.net;

import net.minecraft.core.BlockPos;

public enum Direction {
    X("X"),
    Y("Y"),
    Z("Z");

    private final String displayName;

    Direction(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
