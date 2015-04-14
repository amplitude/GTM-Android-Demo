package com.lecz.android.tiltmazes;

public final class Wall {
    public static final int TOP = 1 << 0;
    public static final int RIGHT = 1 << 1;
    public static final int BOTTOM = 1 << 2;
    public static final int LEFT = 1 << 3;

    private Wall() {
        throw new AssertionError();
    }
}
