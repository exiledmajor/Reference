/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.simibubi.create.AllBlocks
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity$Phase
 *  com.simibubi.create.foundation.ponder.CreateSceneBuilder
 *  net.createmod.catnip.levelWrappers.WrappedClientLevel
 *  net.createmod.catnip.math.Pointing
 *  net.createmod.ponder.api.PonderPalette
 *  net.createmod.ponder.api.level.PonderLevel
 *  net.createmod.ponder.api.scene.SceneBuilder
 *  net.createmod.ponder.api.scene.SceneBuildingUtil
 *  net.createmod.ponder.api.scene.Selection
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.client.resources.DefaultPlayerSkin
 *  net.minecraft.client.resources.PlayerSkin
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.client.ponder;

import com.chromeware.client.ponder.SurgeryPonderSeat;
import com.chromeware.client.surgery.SurgeryAnimation;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.surgery.SurgeryBlocks;
import com.chromeware.surgery.SurgerySeat;
import com.chromeware.surgery.SurgerySeats;
import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import java.util.UUID;
import net.createmod.catnip.levelWrappers.WrappedClientLevel;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SurgeryScenes {
    public static final Direction CHAIR_FACING = Direction.EAST;
    public static final BlockPos CHAIR = new BlockPos(2, 1, 2);
    public static final BlockPos ARM = new BlockPos(2, 1, 0);
    public static final BlockPos COG = ARM.east();
    public static final BlockPos MOTOR = COG.above();
    public static final BlockPos STOCK = new BlockPos(0, 1, 1);

    public static void installation(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("surgery_installation", "Installing cyberware with a Mechanical Arm");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        Selection chair = util.select().fromTo(CHAIR, CHAIR.relative(CHAIR_FACING));
        Selection power = util.select().fromTo(COG, MOTOR);
        Selection arm = util.select().position(ARM);
        Vec3 stockTop = util.vector().topOf(STOCK);
        Vec3 chairTop = util.vector().topOf(CHAIR);
        ItemStack hardware = CyberwareItems.CYBER_ARM.toStack();
        ItemStack armItem = AllBlocks.MECHANICAL_ARM.asStack();
        scene.idle(15);
        scene.world().showSection(chair.add(util.select().position(STOCK)), Direction.DOWN);
        scene.overlay().showControls(stockTop, Pointing.DOWN, 40).rightClick().withItem(armItem);
        scene.overlay().showOutlineWithText(util.select().position(STOCK), 80).attachKeyFrame().colored(PonderPalette.INPUT).text("Before placing the Mechanical Arm, select the stock Depot.").pointAt(stockTop).placeNearTarget();
        scene.idle(90);
        scene.overlay().showControls(chairTop, Pointing.RIGHT, 45).rightClick().withItem(armItem);
        scene.overlay().showOutlineWithText(chair, 100).colored(PonderPalette.OUTPUT).text("Select the chair as a yellow input.").pointAt(chairTop).placeNearTarget();
        scene.idle(110);
        scene.world().showSection(power, Direction.UP);
        scene.world().showSection(arm, Direction.DOWN);
        scene.world().setKineticSpeed(power, -64.0f);
        scene.world().setKineticSpeed(arm, 64.0f);
        scene.effects().rotationDirectionIndicator(ARM);
        scene.overlay().showText(65).text("Place the arm within reach and power it").pointAt(util.vector().centerOf(ARM)).placeNearTarget();
        scene.idle(75);
        scene.overlay().showControls(stockTop, Pointing.DOWN, 45).rightClick().withItem(hardware);
        scene.world().createItemOnBeltLike(STOCK, Direction.DOWN, hardware);
        scene.overlay().showText(85).attachKeyFrame().colored(PonderPalette.INPUT).text("Put the implant on the Depot.").pointAt(stockTop).placeNearTarget();
        scene.idle(95);
        scene.overlay().showControls(chairTop, Pointing.RIGHT, 40).rightClick();
        scene.world().createEntity(SurgeryPonderSeat::new);
        scene.world().createEntity(world -> {
            SurgeryPonderSeat seat = (SurgeryPonderSeat)((Object)((Object)SurgerySeats.in(world, new AABB(CHAIR).inflate(1.0)).getFirst()));
            return new SurgeryPonderPatient((PonderLevel)world, seat);
        });
        scene.overlay().showText(100).text("Sit down and open Cyberware (B by default). Go to the section implant is for and install it.").pointAt(chairTop.add(0.0, 0.8, 0.0)).placeNearTarget();
        scene.idle(110);
        scene.world().modifyEntities(SurgeryPonderSeat.class, s -> s.showStage(SurgerySeat.Stage.RECLINE));
        scene.overlay().showText(100).attachKeyFrame().text("The Arm will put the implant for you and remove the flesh limb if necessary.").pointAt(chairTop).placeNearTarget();
        scene.idle(30);
        scene.world().modifyEntities(SurgeryPonderSeat.class, s -> s.showStage(SurgerySeat.Stage.WORK));
        scene.world().instructArm(ARM, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, ItemStack.EMPTY, 0);
        scene.idle(24);
        scene.world().modifyEntities(SurgeryPonderSeat.class, SurgeryPonderSeat::removeFlesh);
        scene.world().instructArm(ARM, ArmBlockEntity.Phase.SEARCH_OUTPUTS, SurgeryBlocks.SEVERED_LIMB.toStack(), -1);
        scene.idle(24);
        scene.effects().emitParticles(util.vector().centerOf(ARM).add(0.0, 1.1, 0.0), scene.effects().simpleParticleEmitter((ParticleOptions)ParticleTypes.POOF, Vec3.ZERO), 1.0f, 8);
        scene.world().instructArm(ARM, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.idle(18);
        scene.world().instructArm(ARM, ArmBlockEntity.Phase.MOVE_TO_INPUT, ItemStack.EMPTY, 0);
        scene.idle(24);
        scene.world().removeItemsFromBelt(STOCK);
        scene.world().instructArm(ARM, ArmBlockEntity.Phase.SEARCH_OUTPUTS, hardware, -1);
        scene.idle(24);
        scene.world().instructArm(ARM, ArmBlockEntity.Phase.MOVE_TO_OUTPUT, hardware, 0);
        scene.idle(24);
        scene.world().modifyEntities(SurgeryPonderSeat.class, SurgeryPonderSeat::fitFrame);
        scene.world().instructArm(ARM, ArmBlockEntity.Phase.SEARCH_INPUTS, ItemStack.EMPTY, -1);
        scene.world().modifyEntities(SurgeryPonderSeat.class, s -> s.showStage(SurgerySeat.Stage.CLOSE));
        scene.effects().indicateSuccess(CHAIR);
        scene.idle(20);
        scene.world().modifyEntities(SurgeryPonderSeat.class, s -> s.showStage(SurgerySeat.Stage.RAISE));
        scene.idle(30);
        scene.world().modifyEntities(SurgeryPonderSeat.class, s -> s.showStage(SurgerySeat.Stage.IDLE));
        scene.overlay().showText(100).colored(PonderPalette.GREEN).text("The implant is fitted. If you want your biological limb back, drink regeneration potions.").pointAt(chairTop.add(0.0, 0.75, -0.38)).placeNearTarget();
        scene.idle(110);
        scene.markAsFinished();
    }

    static final class SurgeryPonderPatient
    extends RemotePlayer {
        private static final UUID ID = new UUID(0L, 15L);

        SurgeryPonderPatient(PonderLevel world, SurgeryPonderSeat seat) {
            super((ClientLevel)WrappedClientLevel.of((Level)world), new GameProfile(ID, "Ponder Patient"));
            this.setLevel((Level)world);
            this.setNoGravity(true);
            this.setPos(seat.getX(), seat.getY() + 0.04, seat.getZ());
            this.setYRot(seat.patientYaw());
            this.yHeadRot = this.yHeadRotO = seat.patientYaw();
            this.yBodyRotO = this.yHeadRotO;
            this.yBodyRot = this.yHeadRotO;
            this.yRotO = this.yHeadRotO;
            this.startRiding(seat, true);
            this.setOldPosAndRot();
        }

        public PlayerSkin getSkin() {
            return DefaultPlayerSkin.get((UUID)ID);
        }

        protected PlayerInfo getPlayerInfo() {
            return null;
        }

        public void sendSystemMessage(Component message) {
        }

        public void tick() {
            SurgeryAnimation.update((AbstractClientPlayer)this);
            super.tick();
            Entity entity = this.getVehicle();
            if (entity instanceof SurgeryPonderSeat) {
                SurgeryPonderSeat seat = (SurgeryPonderSeat)entity;
                this.setPos(seat.getX(), seat.getY() + 0.04, seat.getZ());
                this.yHeadRot = this.yHeadRotO = seat.patientYaw();
                this.yBodyRotO = this.yHeadRotO;
                this.yBodyRot = this.yHeadRotO;
                this.yRotO = this.yHeadRotO;
                this.setYRot(seat.patientYaw());
            }
        }
    }
}

