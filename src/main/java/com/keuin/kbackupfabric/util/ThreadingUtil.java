package com.keuin.kbackupfabric.util;

public class ThreadingUtil {
    public static int getRecommendedThreadCount() {
        int coreCount = Runtime.getRuntime().availableProcessors();

        // if the cores are too few, we regress to single thread
        if (coreCount <= 2)
            return 1;

        // we have multiple cores, but not too many
        if (coreCount == 3)
            return 2;

        // if we have multiple core, but not too many, we use a half
        if (coreCount <= 6) // 4, 5, 6 -> 3, 3, 4
            return (coreCount + 2) / 2;

        // cores are sufficient, we use almost all of them, except a fixed count remained for the OS
        return coreCount - 2;
    }
}
