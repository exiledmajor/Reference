/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.SharedSuggestionProvider
 *  net.minecraft.commands.arguments.EntityArgument
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbIntegrity;
import com.chromeware.body.LimbState;
import com.chromeware.cyberware.Installing;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class LimbCommands {
    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder limbs = (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"limbs").then(LimbCommands.changeCommand("remove", false))).then(LimbCommands.changeCommand("restore", true));
        LiteralArgumentBuilder install = (LiteralArgumentBuilder)Commands.literal((String)"install").executes(ctx -> {
            Installing.installHeld((Player)((CommandSourceStack)ctx.getSource()).getPlayerOrException());
            return 1;
        });
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"chrome").requires(source -> source.hasPermission(2))).then((ArgumentBuilder)limbs)).then((ArgumentBuilder)install));
    }

    private static boolean selectable(BodyPart part) {
        return part == BodyPart.HEAD || part == BodyPart.BODY || part.parent() == BodyPart.BODY;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> changeCommand(String name, boolean restore) {
        List keys = Arrays.stream(BodyPart.values()).filter(LimbCommands::selectable).map(BodyPart::key).collect(Collectors.toList());
        keys.add("all");
        return (LiteralArgumentBuilder)Commands.literal((String)name).then(((RequiredArgumentBuilder)Commands.argument((String)"part", (ArgumentType)StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest((Iterable)keys, (SuggestionsBuilder)builder)).executes(ctx -> LimbCommands.change((CommandSourceStack)ctx.getSource(), List.of(((CommandSourceStack)ctx.getSource()).getPlayerOrException()), StringArgumentType.getString((CommandContext)ctx, (String)"part"), restore))).then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).executes(ctx -> LimbCommands.change((CommandSourceStack)ctx.getSource(), EntityArgument.getPlayers((CommandContext)ctx, (String)"targets"), StringArgumentType.getString((CommandContext)ctx, (String)"part"), restore))));
    }

    private static int change(CommandSourceStack source, Collection<ServerPlayer> players, String key, boolean restore) throws CommandSyntaxException {
        BodyPart part = null;
        if (!key.equals("all")) {
            try {
                part = BodyPart.fromKey(key);
                if (!LimbCommands.selectable(part)) {
                    throw new IllegalArgumentException("Select its whole arm or leg");
                }
            }
            catch (IllegalArgumentException ex) {
                source.sendFailure((Component)Component.literal((String)("Unknown limb: " + key + ". Use command tab completion.")));
                return 0;
            }
        }
        int changed = 0;
        for (ServerPlayer player : players) {
            LimbState next;
            LimbState previous = LimbAttachments.get((Player)player);
            try {
                next = part == null ? (restore ? LimbState.INTACT : LimbState.CORE) : (restore ? previous.restore(part) : previous.remove(part));
            }
            catch (IllegalStateException ex) {
                source.sendFailure((Component)Component.literal((String)(player.getScoreboardName() + ": " + ex.getMessage())));
                continue;
            }
            LimbAttachments.set((Player)player, next);
            if (!restore) {
                LimbCommands.removeHardware((Player)player, part);
            }
            if (restore) {
                LimbIntegrity integrity = (LimbIntegrity)player.getData(LimbAttachments.INTEGRITY);
                for (BodyPart restored : BodyPart.values()) {
                    if (part != null && (part.subtreeMask() & restored.bit()) == 0) continue;
                    integrity = integrity.withRatio(restored, 1.0f);
                }
                player.setData(LimbAttachments.INTEGRITY, (Object)integrity);
            }
            String verb = restore ? "Restored " : "Removed ";
            source.sendSuccess(() -> Component.literal((String)(verb + key + " for " + player.getScoreboardName())), true);
            ++changed;
        }
        return changed;
    }

    public static void removeHardware(Player player, BodyPart part) {
        for (BodyPart limb : new BodyPart[]{BodyPart.LEFT_ARM, BodyPart.RIGHT_ARM, BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG}) {
            if (part != null && (part.subtreeMask() & limb.bit()) == 0 || !CyberLimbs.has(player, limb)) continue;
            CyberLimbs.remove(player, limb);
        }
    }
}

