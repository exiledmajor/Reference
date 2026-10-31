/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 */
package com.chromeware.humanity;

import com.chromeware.config.ChromewareConfig;
import com.chromeware.humanity.Humanity;
import com.chromeware.humanity.HumanityPackets;
import com.chromeware.humanity.Suppression;
import com.chromeware.humanity.SyringeInjection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class ImmunosuppressantItem
extends Item {
    public static final ResourceKey<DamageType> REACTION = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"immunosuppressant"));
    public static final double REACTION_PER_LOSS = 0.05;
    private static final int MAX_LINE_LENGTH = 42;
    private static final Style DESCRIPTION_STYLE = Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(ChatFormatting.GRAY);
    public final Suppression.Grade grade;

    public ImmunosuppressantItem(Suppression.Grade grade, Item.Properties properties) {
        super(properties);
        this.grade = grade;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        ServerPlayer serverPlayer;
        ItemStack stack = user.getItemInHand(hand);
        if (!level.isClientSide() && user instanceof ServerPlayer && !SyringeInjection.beginSelf(serverPlayer = (ServerPlayer)user, hand)) {
            return InteractionResultHolder.fail((Object)stack);
        }
        return InteractionResultHolder.sidedSuccess((Object)stack, (boolean)level.isClientSide());
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand hand) {
        ServerPlayer serverPlayer;
        if (!user.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(target instanceof Player)) {
            return InteractionResult.FAIL;
        }
        if (user.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return user instanceof ServerPlayer && SyringeInjection.beginOther(serverPlayer = (ServerPlayer)user, target, hand) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    public boolean inject(ServerPlayer user, LivingEntity target, ItemStack stack) {
        ServerPlayer targetPlayer;
        if (!ChromewareConfig.humanityEnabled()) {
            return false;
        }
        if (!(target instanceof Player)) {
            return false;
        }
        Player patient = (Player)target;
        Humanity.Application application = Humanity.suppress(patient, this.grade);
        if (target instanceof ServerPlayer) {
            ServerPlayer injected = (ServerPlayer)target;
            injected.serverLevel().playSound(null, injected.getX(), injected.getEyeY(), injected.getZ(), SoundEvents.EVOKER_HURT, SoundSource.PLAYERS, 0.9f, 0.85f);
            float reactionDamage = (float)(application.removed() * 0.05);
            if (reactionDamage > 0.0f && injected.isAlive()) {
                injected.invulnerableTime = 0;
                injected.hurt(new DamageSource((Holder)injected.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(REACTION)), reactionDamage);
            }
        }
        HumanityPackets.send(user, new HumanityPackets.Notice(0, application.removed()));
        if (target instanceof ServerPlayer && (targetPlayer = (ServerPlayer)target) != user) {
            HumanityPackets.send(targetPlayer, new HumanityPackets.Notice(0, application.removed()));
        }
        return true;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        for (String line : ImmunosuppressantItem.wrap(Component.translatable((String)(this.getDescriptionId() + ".description")).getString())) {
            tooltip.add((Component)Component.literal((String)line).setStyle(DESCRIPTION_STYLE));
        }
        tooltip.add((Component)Component.empty());
        tooltip.add((Component)ImmunosuppressantItem.label("tooltip.chromeware.immunosuppressant.suppressed", ChatFormatting.LIGHT_PURPLE).append((Component)ImmunosuppressantItem.value(this.grade.removal + "%")));
        tooltip.add((Component)ImmunosuppressantItem.label("tooltip.chromeware.immunosuppressant.suppression_time", ChatFormatting.GREEN).append((Component)ImmunosuppressantItem.value(ImmunosuppressantItem.formatTime(this.grade.protection / 20))));
        tooltip.add((Component)ImmunosuppressantItem.label("tooltip.chromeware.immunosuppressant.rebound_rate", ChatFormatting.RED).append((Component)ImmunosuppressantItem.value(ImmunosuppressantItem.format(this.grade.returnPerSecond()) + "% ")).append((Component)Component.translatable((String)"tooltip.chromeware.immunosuppressant.per_second").withStyle(ChatFormatting.GRAY)));
        tooltip.add((Component)ImmunosuppressantItem.label("tooltip.chromeware.immunosuppressant.injection_damage", ChatFormatting.DARK_RED).append((Component)ImmunosuppressantItem.value("0.25 ")).append((Component)Component.translatable((String)"tooltip.chromeware.immunosuppressant.hearts_per_suppressed").withStyle(ChatFormatting.GRAY)));
        tooltip.add((Component)Component.empty());
        tooltip.add((Component)Component.translatable((String)"tooltip.chromeware.immunosuppressant.redose_note").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add((Component)Component.translatable((String)"tooltip.chromeware.immunosuppressant.temporary_note").withStyle(ChatFormatting.DARK_GRAY));
    }

    static List<String> wrap(String text) {
        ArrayList<String> lines = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            if (current.isEmpty()) {
                current.append(word);
                continue;
            }
            if (current.length() + 1 + word.length() <= 42) {
                current.append(' ').append(word);
                continue;
            }
            lines.add(current.toString());
            current = new StringBuilder(word);
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private static MutableComponent label(String key, ChatFormatting color) {
        return Component.translatable((String)key).withStyle(color).append((Component)Component.literal((String)": ").withStyle(ChatFormatting.GRAY));
    }

    private static MutableComponent value(String value) {
        return Component.literal((String)value).withStyle(ChatFormatting.WHITE);
    }

    static String format(double value) {
        return Math.abs(value - Math.rint(value)) < 1.0E-6 ? String.format(Locale.ROOT, "%.0f", value) : String.format(Locale.ROOT, "%.1f", value);
    }

    static String formatTime(int seconds) {
        return seconds % 60 == 0 ? seconds / 60 + "m" : seconds + "s";
    }

    public static String clock(int ticks) {
        int seconds = (Math.max(0, ticks) + 19) / 20;
        return seconds / 60 + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
    }
}

