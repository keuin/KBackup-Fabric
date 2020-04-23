package com.keuin.kbackupfabric;

import com.keuin.kbackupfabric.util.PermissionValidator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class KBCommandRegister {

    // First make method to register
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // register /kb and /kb help for help menu
        dispatcher.register(CommandManager.literal("kb").executes(KBCommandHandler::help));
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("help").executes(KBCommandHandler::help)));

        // register /kb list for showing the backup list. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("list").requires(PermissionValidator::op).executes(KBCommandHandler::list)));

        // register /kb backup [name] for performing backup. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("backup").then(CommandManager.argument("backupName", StringArgumentType.string()).requires(PermissionValidator::op).executes(KBCommandHandler::backup)).requires(PermissionValidator::op).executes(KBCommandHandler::backupWithDefaultName)));

        // register /kb restore <name> for performing restore. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("restore").then(CommandManager.argument("backupName", StringArgumentType.string()).requires(PermissionValidator::op).executes(KBCommandHandler::restore)).executes(KBCommandHandler::list)));

        // register /kb confirm for confirming the execution. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("confirm").requires(PermissionValidator::op).executes(KBCommandHandler::confirm)));

        // register /kb cancel for cancelling the execution to be confirmed. OP is required.
        dispatcher.register(CommandManager.literal("kb").then(CommandManager.literal("cancel").requires(PermissionValidator::op).executes(KBCommandHandler::cancel)));

//        LiteralCommandNode<ServerCommandSource> basenode = dispatcher.register(literal("findBiome")
//                .then(argument("biome_identifier", identifier()).suggests(BiomeCompletionProvider.BIOMES) // We use Biome suggestions for identifier argument
//                        .then(argument("distance", integer(0, 20000))
//                                .executes(ctx -> execute(ctx.getSource(), getIdentifier(ctx, "biome_identifier"), getInteger(ctx, "distance"))))
//                        .executes(ctx -> execute(ctx.getSource(), getIdentifier(ctx, "biome_identifier"), 1000))));
//        // Register redirect
//        dispatcher.register(literal("biome")
//                .redirect(basenode));
    }

//    // Beginning of the method
//    private static int execute(ServerCommandSource source, Identifier biomeId, int range) throws CommandSyntaxException {
//        Biome biome = Registry.BIOME.get(biomeId);
//
//        if(biome == null) { // Since the argument is an Identifier we need to check if the identifier actually exists in the registry
//            throw new SimpleCommandExceptionType(new TranslatableText("biome.not.exist", biomeId)).create();
//        }
//
//        List<Biome> bio = new ArrayList<Biome>();
//        bio.add(biome);
//
//        ServerWorld world = source.getWorld();
//
//        BiomeSource bsource = world.getChunkManager().getChunkGenerator().getBiomeSource();
//
//        BlockPos loc = new BlockPos(source.getPosition());
//        // Now here is the heaviest part of the method.
//        BlockPos pos = bsource.locateBiome(loc.getX(), loc.getZ(), range, bio, new Random(world.getSeed()));
//
//        // Since this method can return null if it failed to find a biome
//        if(pos == null) {
//            throw new SimpleCommandExceptionType(new TranslatableText("biome.notfound", biome.getTranslationKey())).create();
//        }
//
//        int distance = MathHelper.floor(getDistance(loc.getX(), loc.getZ(), pos.getX(), pos.getZ()));
//        // Popup text that can suggest commands. This is the exact same system that /locate uses.
//        Text teleportButtonPopup = Texts.bracketed(new TranslatableText("chat.coordinates", new Object[] { pos.getX(), "~", pos.getZ()})).styled((style_1x) -> {
//            style_1x.setColor(Formatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " ~ " + pos.getZ())).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip", new Object[0])));
//        });
//
//        source.sendFeedback(new TranslatableText("commands.locate.success", new Object[] { new TranslatableText(Registry.BIOME.get(biomeId).getTranslationKey()), teleportButtonPopup, distance}), false);
//
//        return 1;
//    }
//    // Just a normal old 2d distance method.
//    private static float getDistance(int int_1, int int_2, int int_3, int int_4) {
//        int int_5 = int_3 - int_1;
//        int int_6 = int_4 - int_2;
//
//        return MathHelper.sqrt((float) (int_5 * int_5 + int_6 * int_6));
//    }
//
//    public static class BiomeCompletionProvider {
//        // This provides suggestions of what biomes can be selected. Since this uses the registry, mods that add new biomes will work without modification.
//        public static final SuggestionProvider<ServerCommandSource> BIOMES = SuggestionProviders.register(new Identifier("biomes"), (ctx, builder) -> {
//            Registry.BIOME.getIds().stream().forEach(identifier -> builder.suggest(identifier.toString(), new TranslatableText(Registry.BIOME.get(identifier).getTranslationKey())));
//            return builder.buildFuture();
//        });
//
//    }
}
