/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.arguments.EntityArgument
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.event.RegisterCommandsEvent
 */
package com.chromeware.humanity;

import com.chromeware.humanity.Humanity;
import com.chromeware.humanity.HumanityRules;
import com.chromeware.humanity.HumanityState;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class HumanityCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"humanity").requires(source -> source.hasPermission(2))).executes(ctx -> HumanityCommands.status((CommandSourceStack)ctx.getSource(), List.of(((CommandSourceStack)ctx.getSource()).getPlayerOrException())))).then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).executes(ctx -> HumanityCommands.status((CommandSourceStack)ctx.getSource(), EntityArgument.getPlayers((CommandContext)ctx, (String)"targets"))))).then(Commands.literal((String)"strain").then(((RequiredArgumentBuilder)Commands.argument((String)"percent", (ArgumentType)DoubleArgumentType.doubleArg((double)0.0, (double)100.0)).executes(ctx -> HumanityCommands.apply((CommandSourceStack)ctx.getSource(), List.of(((CommandSourceStack)ctx.getSource()).getPlayerOrException()), p -> Humanity.setStrain((Player)p, DoubleArgumentType.getDouble((CommandContext)ctx, (String)"percent"))))).then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).executes(ctx -> HumanityCommands.apply((CommandSourceStack)ctx.getSource(), EntityArgument.getPlayers((CommandContext)ctx, (String)"targets"), p -> Humanity.setStrain((Player)p, DoubleArgumentType.getDouble((CommandContext)ctx, (String)"percent")))))))).then(Commands.literal((String)"add").then(((RequiredArgumentBuilder)Commands.argument((String)"percent", (ArgumentType)DoubleArgumentType.doubleArg((double)0.0, (double)100.0)).executes(ctx -> HumanityCommands.apply((CommandSourceStack)ctx.getSource(), List.of(((CommandSourceStack)ctx.getSource()).getPlayerOrException()), p -> Humanity.strain((Player)p, DoubleArgumentType.getDouble((CommandContext)ctx, (String)"percent"))))).then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).executes(ctx -> HumanityCommands.apply((CommandSourceStack)ctx.getSource(), EntityArgument.getPlayers((CommandContext)ctx, (String)"targets"), p -> Humanity.strain((Player)p, DoubleArgumentType.getDouble((CommandContext)ctx, (String)"percent")))))))).then(((LiteralArgumentBuilder)Commands.literal((String)"clear").executes(ctx -> HumanityCommands.apply((CommandSourceStack)ctx.getSource(), List.of(((CommandSourceStack)ctx.getSource()).getPlayerOrException()), Humanity::clear))).then(Commands.argument((String)"targets", (ArgumentType)EntityArgument.players()).executes(ctx -> HumanityCommands.apply((CommandSourceStack)ctx.getSource(), EntityArgument.getPlayers((CommandContext)ctx, (String)"targets"), Humanity::clear)))));
    }

    private static int apply(CommandSourceStack source, Collection<ServerPlayer> players, Consumer<ServerPlayer> change) {
        players.forEach(change);
        return HumanityCommands.status(source, players);
    }

    private static int status(CommandSourceStack source, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            HumanityState s = Humanity.state((Player)player);
            source.sendSuccess(() -> Component.translatable((String)"commands.chromeware.humanity.status", (Object[])new Object[]{player.getDisplayName(), HumanityCommands.one(HumanityRules.humanity(s.loss())), HumanityCommands.one(s.permanent()), HumanityCommands.one(s.temporary()), HumanityCommands.one(s.suppression().stored())}), false);
        }
        return players.size();
    }

    private static String one(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}

