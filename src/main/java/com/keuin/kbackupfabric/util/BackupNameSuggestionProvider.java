package com.keuin.kbackupfabric.util;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class BackupNameSuggestionProvider {

    private static final List<String> candidateCacheList = new ArrayList<>();
    private static final Object syncSetDirectory = new Object();
    private static final Object syncUpdate = new Object();
    private static final long CACHE_TTL = 10000;
    private static String backupSaveDirectory;
    private static long cacheUpdateTime = 0;

    public static void setBackupSaveDirectory(String backupSaveDirectory) {
        synchronized (syncSetDirectory) {
            BackupNameSuggestionProvider.backupSaveDirectory = backupSaveDirectory;
        }
        // Immediately perform a update
        updateCandidateList();
    }

    public static void updateCandidateList() {
        synchronized (syncUpdate) {
            try {
                File file = new File(backupSaveDirectory);
                candidateCacheList.clear();
                File[] files = file.listFiles();
                if (files == null)
                    return;
                for (File f : files)
                    candidateCacheList.add(BackupFilesystemUtil.getBackupName(f.getName()));
                cacheUpdateTime = System.currentTimeMillis();
            } catch (NullPointerException ignored) {
            }
        }
    }

//    private static void updateCandidateList(Collection<String> stringCollection) {
//        candidateList.clear();
//        candidateList.addAll(stringCollection);
//    }

    public static SuggestionProvider<ServerCommandSource> getProvider() {
        return (context, builder) -> getCompletableFuture(builder);
    }

    private static CompletableFuture<Suggestions> getCompletableFuture(SuggestionsBuilder builder) {
        if (isCacheExpired())
            updateCandidateList();
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        if (candidateCacheList.isEmpty()) { // If the list is empty then return no suggestions
            return Suggestions.empty(); // No suggestions
        }

        for (String string : candidateCacheList) { // Iterate through the supplied list
            if (string.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(string); // Add every single entry to suggestions list.
            }
        }
        return builder.buildFuture(); // Create the CompletableFuture containing all the suggestions
    }

    private static boolean isCacheExpired() {
        return System.currentTimeMillis() - cacheUpdateTime > CACHE_TTL;
    }
}
