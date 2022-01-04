package com.keuin.kbackupfabric.backup.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class BackupNameSuggestionProvider {

    private static final List<String> candidateCacheList = new ArrayList<>();
    private static final Object syncSetDirectory = new Object();
    private static final Object syncCache = new Object();
    private static final long CACHE_TTL = 8000;
    private static String backupSaveDirectory;
    private static int cacheUpdateTime = 0;

    public static void setBackupSaveDirectory(String backupSaveDirectory) {
        synchronized (syncSetDirectory) {
            BackupNameSuggestionProvider.backupSaveDirectory = backupSaveDirectory;
        }
        // update immediately
        updateCandidateList();
    }

    public static void updateCandidateList() {
        synchronized (syncCache) {
            synchronized (syncSetDirectory) {
                try {
                    File file = new File(backupSaveDirectory);
                    candidateCacheList.clear();
                    File[] files = file.listFiles();
                    if (files == null)
                        return;
                    Arrays.stream(files).map(File::getName).filter(
                            ((Predicate<String>) s -> s.toLowerCase().endsWith(".zip"))
                                    .or(s -> s.toLowerCase().endsWith(".kbi"))
                    ).forEach(candidateCacheList::add);
                    cacheUpdateTime = (int) System.currentTimeMillis();
                } catch (NullPointerException ignored) {
                }
            }
        }
    }

//    private static void updateCandidateList(Collection<String> stringCollection) {
//        candidateList.clear();
//        candidateList.addAll(stringCollection);
//    }

    public static SuggestionProvider<ServerCommandSource> getProvider() {
//        return (context, builder) -> getCompletableFuture(builder);
        return (context, builder) -> {
            if (isCacheExpired())
                updateCandidateList();
            return CommandSource.suggestMatching(candidateCacheList, builder);
        };
    }

//    private static CompletableFuture<Suggestions> getCompletableFuture(SuggestionsBuilder builder) {
//        if (isCacheExpired())
//            updateCandidateList();
//        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
//        synchronized (syncCache) {
//            if (candidateCacheList.isEmpty()) { // If the list is empty then return no suggestions
//                return Suggestions.empty(); // No suggestions
//            }
//
//            for (String string : candidateCacheList) { // Iterate through the supplied list
//                if (string.toLowerCase(Locale.ROOT).startsWith(remaining)) {
//                    builder.suggest(string); // Add every single entry to suggestions list.
//                }
//            }
//        }
//        return builder.buildFuture(); // Create the CompletableFuture containing all the suggestions
//    }

    private static boolean isCacheExpired() {
        return ((int) System.currentTimeMillis()) - cacheUpdateTime > CACHE_TTL || cacheUpdateTime == 0;
    }
}
