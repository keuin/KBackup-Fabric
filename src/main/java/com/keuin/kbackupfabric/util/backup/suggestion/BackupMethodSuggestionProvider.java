package com.keuin.kbackupfabric.util.backup.suggestion;

import com.keuin.kbackupfabric.util.backup.BackupType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class BackupMethodSuggestionProvider {

    private static final List<String> suggestions = Arrays.asList(
            BackupType.OBJECT_TREE_BACKUP.getName(),
            BackupType.PRIMITIVE_ZIP_BACKUP.getName()
    ); // All backup methods

    public static SuggestionProvider<ServerCommandSource> getProvider() {
        return (context, builder) -> getCompletableFuture(builder);
    }

    private static CompletableFuture<Suggestions> getCompletableFuture(SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String string : suggestions) { // Iterate through the supplied list
            if (string.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(string); // Add every single entry to suggestions list.
            }
        }
        return builder.buildFuture(); // Create the CompletableFuture containing all the suggestions
    }

}
