package com.panicbar.client;
public class ClientPanicState {

    private static volatile float panicPercent = 0f;
    private static volatile boolean lockedOut = false;

    public static void update(float percent, boolean locked) {
        panicPercent = percent;
        lockedOut = locked;
    }

    public static float getPanicPercent() {
        return panicPercent;
    }

    public static boolean isLockedOut() {
        return lockedOut;
    }
}
