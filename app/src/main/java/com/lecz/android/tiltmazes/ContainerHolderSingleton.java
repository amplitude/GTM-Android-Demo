package com.lecz.android.tiltmazes;

import com.google.android.gms.tagmanager.ContainerHolder;

/**
 * Created by danieljih on 5/6/16.
 * Singleton to hold the GTM Container (since it should only be created once
 * per run of the app).
 */
public class ContainerHolderSingleton {
    private static ContainerHolder containerHolder;

    /**
     * Utility class; don't instantiate.
     */
    private ContainerHolderSingleton() {}

    public static ContainerHolder getContainerHolder() {
        return containerHolder;
    }

    public static void setContainerHolder(ContainerHolder c) {
        containerHolder = c;
    }
}
