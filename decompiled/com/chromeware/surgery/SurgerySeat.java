/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
 *  java.lang.MatchException
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.network.syncher.SynchedEntityData$Builder
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$MoveFunction
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BedPart
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.surgery;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.cyberware.Installing;
import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgeryChairBlock;
import com.chromeware.surgery.SurgeryStock;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class SurgerySeat
extends Entity {
    public static final int RECLINE_TICKS = 30;
    public static final int CLOSE_TICKS = 20;
    public static final int RAISE_TICKS = 30;
    public static final int PRESENT_TIMEOUT = 200;
    public static final int WORK_TIMEOUT = 400;
    public static final int HAND_BACK_TICKS = 400;
    public static final float SUPINE_DEGREES = 72.0f;
    public static final float PRONE_DEGREES = 90.0f;
    private static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(SurgerySeat.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(SurgerySeat.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> REMOVING = SynchedEntityData.defineId(SurgerySeat.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> IMPLANT = SynchedEntityData.defineId(SurgerySeat.class, (EntityDataSerializer)EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Optional<BlockPos>> ARM = SynchedEntityData.defineId(SurgerySeat.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Integer> PART = SynchedEntityData.defineId(SurgerySeat.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RIPPED = SynchedEntityData.defineId(SurgerySeat.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> ASSIST_IMPLANT = SynchedEntityData.defineId(SurgerySeat.class, (EntityDataSerializer)EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Optional<BlockPos>> ASSIST_ARM = SynchedEntityData.defineId(SurgerySeat.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private final List<Surgery.Request> queue = new ArrayList<Surgery.Request>();
    private Surgery.Request current;
    private Assist assist;
    private Surgery.Removal removal;
    private ItemStack extracted = ItemStack.EMPTY;
    private boolean opened;
    private boolean treated;
    private int extractedAt = -1000;
    private int stageStart;
    private int clientStageStart;
    private int openedAt;

    public SurgerySeat(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STAGE, (Object)0).define(KIND, (Object)0).define(REMOVING, (Object)false).define(IMPLANT, (Object)ItemStack.EMPTY).define(ARM, Optional.empty()).define(PART, (Object)0).define(RIPPED, (Object)false).define(ASSIST_IMPLANT, (Object)ItemStack.EMPTY).define(ASSIST_ARM, Optional.empty());
    }

    public Stage stage() {
        return Stage.values()[Mth.clamp((int)((Integer)this.entityData.get(STAGE)), (int)0, (int)(Stage.values().length - 1))];
    }

    public Kind kind() {
        return Kind.values()[Mth.clamp((int)((Integer)this.entityData.get(KIND)), (int)0, (int)(Kind.values().length - 1))];
    }

    public boolean removing() {
        return (Boolean)this.entityData.get(REMOVING);
    }

    public ItemStack implant() {
        return (ItemStack)this.entityData.get(IMPLANT);
    }

    public BlockPos arm() {
        return ((Optional)this.entityData.get(ARM)).orElse(null);
    }

    public BodyPart part() {
        int i = (Integer)this.entityData.get(PART) - 1;
        return i < 0 || i >= BodyPart.values().length ? null : BodyPart.values()[i];
    }

    public boolean ripped() {
        return (Boolean)this.entityData.get(RIPPED);
    }

    public boolean operating() {
        return this.stage() != Stage.IDLE;
    }

    public boolean busy() {
        return this.operating() || ((Optional)this.entityData.get(ASSIST_ARM)).isPresent();
    }

    public boolean assisting(BlockPos arm) {
        return arm != null && arm.equals(((Optional)this.entityData.get(ASSIST_ARM)).orElse(null));
    }

    public ItemStack assistImplant() {
        return (ItemStack)this.entityData.get(ASSIST_IMPLANT);
    }

    public BlockPos assistArm() {
        return ((Optional)this.entityData.get(ASSIST_ARM)).orElse(null);
    }

    public Player patient() {
        Player p;
        Entity entity = this.getFirstPassenger();
        return entity instanceof Player ? (p = (Player)entity) : null;
    }

    public boolean canLeave() {
        return (!this.operating() || this.stage() == Stage.WORK && !this.opened || this.stage() == Stage.PRESENT) && (this.assist == null || this.assist.stage == Stage.WORK && !this.assist.opened);
    }

    private void stage(Stage stage) {
        this.entityData.set(STAGE, (Object)stage.ordinal());
        this.stageStart = this.tickCount;
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (this.level().isClientSide() && key == STAGE) {
            this.clientStageStart = this.tickCount;
        }
    }

    public float stageTime(float partialTick) {
        return (float)(this.tickCount - (this.level().isClientSide() ? this.clientStageStart : this.stageStart)) + partialTick;
    }

    int stageTicks() {
        return this.tickCount - this.stageStart;
    }

    int openTicks() {
        return this.tickCount - this.openedAt;
    }

    public boolean opened() {
        return this.opened;
    }

    boolean treated() {
        return this.treated;
    }

    public BlockPos chair() {
        return this.blockPosition();
    }

    public Direction facing() {
        BlockState state = this.level().getBlockState(this.chair());
        return state.getBlock() instanceof SurgeryChairBlock ? (Direction)state.getValue((Property)SurgeryChairBlock.FACING) : Direction.NORTH;
    }

    public float patientYaw() {
        return this.facing().getOpposite().toYRot();
    }

    static Vec3 hip(BlockPos foot, Direction facing) {
        return Vec3.atBottomCenterOf((Vec3i)foot).add((double)facing.getStepX() * 0.3, 0.625, (double)facing.getStepZ() * 0.3);
    }

    static float ease(float x) {
        x = Mth.clamp((float)x, (float)0.0f, (float)1.0f);
        return x * x * (3.0f - 2.0f * x);
    }

    public float recline(float partialTick) {
        if (this.kind() == Kind.STANDARD) {
            return 0.0f;
        }
        float t = this.stageTime(partialTick);
        boolean prone = this.kind() == Kind.SPINE;
        float progress = switch (this.stage().ordinal()) {
            case 0 -> 0.0f;
            case 1 -> SurgerySeat.ease(t / (30.0f * (prone ? 0.6f : 1.0f)));
            case 5 -> {
                if (prone) {
                    yield 1.0f - SurgerySeat.ease((t - 12.0f) / 18.0f);
                }
                yield 1.0f - SurgerySeat.ease(t / 30.0f);
            }
            default -> 1.0f;
        };
        return progress * (prone ? 90.0f : 72.0f);
    }

    public static int closeTicks(Kind kind) {
        return kind == Kind.STANDARD ? 10 : 20;
    }

    public static int raiseTicks(Kind kind) {
        return kind == Kind.STANDARD ? 0 : 30;
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        BlockState state = this.level().getBlockState(this.chair());
        Player patient = this.patient();
        if (!(state.getBlock() instanceof SurgeryChairBlock) || state.getValue(SurgeryChairBlock.PART) != BedPart.FOOT) {
            this.abort(patient);
            this.ejectPassengers();
            this.discard();
            return;
        }
        if (patient != null && this.canLeave() && LimbAttachments.get(patient).isMissing(BodyPart.BODY)) {
            this.ejectPassengers();
            patient = null;
        }
        if (patient == null) {
            this.abort(null);
            if (!this.extracted.isEmpty() && this.tickCount - this.extractedAt < 400) {
                return;
            }
            this.discard();
            return;
        }
        patient.setYBodyRot(this.patientYaw());
        if (this.operating()) {
            Surgery.advance(this, patient);
        }
        if (this.assist != null) {
            this.assist.tick(patient);
        }
        this.startQueued(patient);
    }

    void schedule(Player patient, Surgery.Request request) {
        if (this.scheduled(request)) {
            patient.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_already_scheduled", (Object[])new Object[]{request.item().getHoverName()}), true);
            return;
        }
        Component refusal = Surgery.check(this, patient, request);
        if (refusal != null) {
            patient.displayClientMessage(refusal, true);
            return;
        }
        if (this.startable(request)) {
            Surgery.begin(this, patient, request, false);
            return;
        }
        if (this.canAssist(request)) {
            Surgery.begin(this, patient, request, true);
            return;
        }
        this.queue.add(request);
        patient.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_queued", (Object[])new Object[]{request.item().getHoverName(), this.queue.size() - 1}), true);
    }

    private boolean scheduled(Surgery.Request request) {
        if (this.current != null && this.current.same(request)) {
            return true;
        }
        if (this.removal != null && this.removal.equals(request.removal())) {
            return true;
        }
        if (this.assist != null && this.assist.request.same(request)) {
            return true;
        }
        return this.queue.stream().anyMatch(request::same);
    }

    private boolean startable(Surgery.Request request) {
        return !this.operating() && (this.assist == null || request.removal() == null && Surgery.kind(request.item()) == Kind.STANDARD);
    }

    private boolean canAssist(Surgery.Request request) {
        return this.assist == null && this.operating() && this.kind() == Kind.STANDARD && this.arm() != null && request.removal() == null && Surgery.kind(request.item()) == Kind.STANDARD && !Surgery.armHardware(request.item()) && SurgeryStock.supplier(this, request.item(), this.arm()) != null;
    }

    void reaim(Player patient) {
        if (this.current != null) {
            Surgery.aim(patient, this.current);
        }
    }

    private void startQueued(Player patient) {
        if (this.queue.isEmpty()) {
            return;
        }
        Surgery.Request next = (Surgery.Request)this.queue.getFirst();
        boolean primary = this.startable(next);
        if (!primary && !this.canAssist(next)) {
            return;
        }
        this.queue.removeFirst();
        Surgery.begin(this, patient, next, !primary);
    }

    void assist(Surgery.Request request, BlockPos arm) {
        this.assist = new Assist(request, arm);
        this.entityData.set(ASSIST_IMPLANT, (Object)request.item().copyWithCount(1));
        this.entityData.set(ASSIST_ARM, Optional.of(arm));
    }

    void plan(Surgery.Request request, ItemStack stack, BlockPos arm) {
        BodyPart part;
        Player patient = this.patient();
        if (patient == null || this.operating()) {
            return;
        }
        this.current = request;
        Kind kind = Surgery.kind(stack);
        BodyPart bodyPart = part = kind == Kind.LIMB ? CyberLimbs.target(patient, stack) : null;
        if (part != null) {
            CyberLimbs.request(patient, part);
        }
        this.entityData.set(IMPLANT, (Object)stack.copyWithCount(1));
        this.entityData.set(KIND, (Object)kind.ordinal());
        this.entityData.set(REMOVING, (Object)false);
        this.entityData.set(ARM, Optional.of(arm));
        this.entityData.set(PART, (Object)(part == null ? 0 : part.ordinal() + 1));
        this.entityData.set(RIPPED, (Object)false);
        this.removal = null;
        this.treated = false;
        this.opened = false;
        if (kind == Kind.STANDARD) {
            this.work();
        } else {
            this.stage(Stage.RECLINE);
        }
    }

    void work() {
        Player patient;
        this.stage(this.removing() ? Stage.PRESENT : Stage.WORK);
        if (!this.removing() && !this.needsVisit()) {
            this.open();
        }
        if (this.removing() && this.kind() != Kind.STANDARD && (patient = this.patient()) != null) {
            patient.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_exposed"), true);
        }
    }

    private void open() {
        this.opened = true;
        this.openedAt = this.tickCount;
    }

    private boolean needsVisit() {
        Player patient = this.patient();
        if (patient == null) {
            return false;
        }
        return switch (this.kind().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> true;
            case 1 -> {
                BodyPart part = this.part();
                if (!(part == null || part != BodyPart.BODY && LimbAttachments.get(patient).isMissing(part) || CyberLimbs.has(patient, part))) {
                    yield true;
                }
                yield false;
            }
            case 2 -> false;
        };
    }

    boolean beginRemoval(Surgery.Removal request, ItemStack expected) {
        if (this.operating()) {
            return false;
        }
        this.removal = request;
        Kind kind = request.category() == -3 ? Kind.LIMB : Surgery.kind(expected);
        this.entityData.set(IMPLANT, (Object)expected.copyWithCount(1));
        this.entityData.set(KIND, (Object)kind.ordinal());
        this.entityData.set(REMOVING, (Object)true);
        this.entityData.set(ARM, Optional.empty());
        this.entityData.set(PART, (Object)(request.category() == -3 ? Surgery.limb(request).ordinal() + 1 : 0));
        this.entityData.set(RIPPED, (Object)false);
        this.treated = false;
        this.opened = false;
        if (kind == Kind.STANDARD) {
            this.work();
        } else {
            this.stage(Stage.RECLINE);
        }
        return true;
    }

    boolean wantsVisit(BlockPos arm, boolean claim) {
        if (this.assisting(arm)) {
            return this.assist != null && this.assist.wantsVisit();
        }
        return this.wantsVisit() && this.assigned(arm, claim);
    }

    private boolean wantsVisit() {
        if (this.patient() == null) {
            return false;
        }
        if (this.stage() == Stage.PRESENT) {
            return !this.presented().isEmpty();
        }
        return this.stage() == Stage.WORK && !this.removing() && !this.opened && this.needsVisit();
    }

    boolean assigned(BlockPos arm, boolean claim) {
        if (this.assisting(arm)) {
            return false;
        }
        BlockPos current = this.arm();
        if (current != null && !(this.level().getBlockEntity(current) instanceof ArmBlockEntity)) {
            current = null;
        }
        if (current == null) {
            if (claim) {
                this.entityData.set(ARM, Optional.of(arm));
            }
            return true;
        }
        return current.equals((Object)arm);
    }

    ItemStack visit(BlockPos arm) {
        Player patient = this.patient();
        if (patient != null && this.assisting(arm)) {
            if (this.assist != null && this.assist.wantsVisit()) {
                this.assist.visit(patient);
            }
            return ItemStack.EMPTY;
        }
        if (patient == null || !this.assigned(arm, true)) {
            return ItemStack.EMPTY;
        }
        if (this.stage() == Stage.PRESENT) {
            return this.extract();
        }
        if (!this.wantsVisit()) {
            return ItemStack.EMPTY;
        }
        this.treated = true;
        if (this.kind() == Kind.STANDARD) {
            Surgery.punch(patient, Surgery.site(this, this.implant(), 0.0f));
            this.open();
            return ItemStack.EMPTY;
        }
        BodyPart part = this.part();
        Surgery.hurt(patient, Kind.LIMB);
        Surgery.rip(patient, part);
        this.entityData.set(RIPPED, (Object)true);
        this.open();
        return Surgery.severed(part);
    }

    ItemStack presented() {
        return this.stage() == Stage.PRESENT && this.removal != null ? this.implant() : ItemStack.EMPTY;
    }

    private ItemStack extract() {
        Player patient = this.patient();
        if (this.presented().isEmpty() || patient == null) {
            return ItemStack.EMPTY;
        }
        Vec3 at = Surgery.site(this, this.implant(), 0.0f);
        ItemStack removed = Surgery.remove(patient, this.removal);
        Kind kind = this.kind();
        this.removal = null;
        this.entityData.set(IMPLANT, (Object)ItemStack.EMPTY);
        if (!removed.isEmpty()) {
            this.extracted = removed.copy();
            this.extractedAt = this.tickCount;
            this.treated = true;
            if (kind == Kind.STANDARD) {
                Surgery.punch(patient, at);
            } else {
                Surgery.hurt(patient, kind);
            }
        }
        this.stage(Stage.CLOSE);
        return removed;
    }

    boolean accepts(ItemStack stack, BlockPos arm) {
        if (this.assisting(arm)) {
            return this.patient() != null && this.assist != null && this.assist.accepts(stack);
        }
        return this.accepts(stack) && this.assigned(arm, false);
    }

    private boolean accepts(ItemStack stack) {
        return this.patient() != null && this.stage() == Stage.WORK && !this.removing() && this.opened && ItemStack.isSameItemSameComponents((ItemStack)stack, (ItemStack)this.implant());
    }

    void deliver(ItemStack stack, BlockPos arm) {
        Player patient = this.patient();
        if (patient == null) {
            return;
        }
        if (this.assisting(arm)) {
            if (this.assist != null) {
                this.assist.deliver(patient, stack);
            }
            return;
        }
        if (!this.treated && this.kind() != Kind.STANDARD) {
            Surgery.hurt(patient, this.kind());
        }
        this.treated = true;
        Surgery.install(patient, this.current, stack);
        this.current = null;
        this.entityData.set(IMPLANT, (Object)ItemStack.EMPTY);
        this.entityData.set(RIPPED, (Object)false);
        this.stage(Stage.CLOSE);
    }

    boolean handBack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return !this.extracted.isEmpty() && this.tickCount - this.extractedAt < 400 && ItemStack.isSameItemSameComponents((ItemStack)stack, (ItemStack)this.extracted);
    }

    void receiveHandBack(ItemStack stack) {
        this.extracted = ItemStack.EMPTY;
        Player patient = this.patient();
        if (patient != null) {
            Installing.returnItem(patient, stack);
            patient.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_handed_back", (Object[])new Object[]{stack.getHoverName()}), true);
        } else {
            this.spawnAtLocation(stack);
        }
    }

    void giveUp() {
        Player patient = this.patient();
        if (patient != null) {
            patient.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_timeout"), true);
        }
        if (patient != null) {
            Surgery.clearRequests(patient);
        }
        this.removal = null;
        this.current = null;
        this.entityData.set(IMPLANT, (Object)ItemStack.EMPTY);
        this.entityData.set(RIPPED, (Object)false);
        if (this.treated || this.kind() != Kind.STANDARD) {
            this.stage(Stage.CLOSE);
        } else {
            this.stage(Stage.IDLE);
        }
    }

    void next(Stage stage) {
        this.stage(stage);
    }

    Surgery.Removal removal() {
        return this.removal;
    }

    private void abort(Player patient) {
        this.queue.clear();
        if (this.assist != null) {
            this.assist.end();
        }
        if (!this.operating()) {
            return;
        }
        if (patient != null) {
            Surgery.clearRequests(patient);
        }
        this.entityData.set(IMPLANT, (Object)ItemStack.EMPTY);
        this.entityData.set(RIPPED, (Object)false);
        this.removal = null;
        this.current = null;
        this.stage(Stage.IDLE);
    }

    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        if (!this.hasPassenger(passenger)) {
            return;
        }
        move.accept(passenger, this.getX(), this.getY() + 0.04, this.getZ());
    }

    public void onPassengerTurned(Entity passenger) {
        this.clamp(passenger);
    }

    private void clamp(Entity passenger) {
        passenger.setYBodyRot(this.patientYaw());
        float relative = Mth.wrapDegrees((float)(passenger.getYRot() - this.patientYaw()));
        float limited = Mth.clamp((float)relative, (float)-100.0f, (float)100.0f);
        passenger.yRotO += limited - relative;
        passenger.setYRot(passenger.getYRot() + limited - relative);
        passenger.setYHeadRot(passenger.getYRot());
    }

    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Direction side = this.facing().getClockWise();
        for (Direction d : new Direction[]{side, side.getOpposite(), this.facing().getOpposite()}) {
            BlockPos pos = this.chair().relative(d);
            if (!this.level().getBlockState(pos).getCollisionShape((BlockGetter)this.level(), pos).isEmpty() || !this.level().getBlockState(pos.above()).getCollisionShape((BlockGetter)this.level(), pos.above()).isEmpty()) continue;
            return Vec3.atBottomCenterOf((Vec3i)pos);
        }
        return Vec3.atBottomCenterOf((Vec3i)this.chair()).add(0.0, 1.0, 0.0);
    }

    public boolean shouldRiderSit() {
        return true;
    }

    public boolean isPickable() {
        return false;
    }

    public boolean shouldBeSaved() {
        return false;
    }

    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    public static enum Stage {
        IDLE,
        RECLINE,
        WORK,
        PRESENT,
        CLOSE,
        RAISE;

    }

    public static enum Kind {
        STANDARD,
        LIMB,
        SPINE;

    }

    private final class Assist {
        final Surgery.Request request;
        final BlockPos arm;
        Stage stage = Stage.WORK;
        int stageStart;
        int openedAt;
        boolean opened;
        boolean treated;

        Assist(Surgery.Request request, BlockPos arm) {
            this.stageStart = SurgerySeat.this.tickCount;
            this.request = request;
            this.arm = arm;
        }

        void tick(Player patient) {
            int t = SurgerySeat.this.tickCount - this.stageStart;
            if (this.stage == Stage.WORK && (this.opened ? SurgerySeat.this.tickCount - this.openedAt : t) >= 400) {
                patient.displayClientMessage((Component)Component.translatable((String)"message.chromeware.surgery_timeout"), true);
                if (this.treated) {
                    this.close();
                } else {
                    this.end();
                }
            } else if (this.stage == Stage.CLOSE && t >= SurgerySeat.closeTicks(Kind.STANDARD)) {
                Surgery.recover(patient, Kind.STANDARD);
                this.end();
            }
        }

        boolean wantsVisit() {
            return this.stage == Stage.WORK && !this.opened;
        }

        void visit(Player patient) {
            this.treated = true;
            Surgery.punch(patient, Surgery.site(SurgerySeat.this, this.request.item(), 0.0f));
            this.opened = true;
            this.openedAt = SurgerySeat.this.tickCount;
        }

        boolean accepts(ItemStack stack) {
            return this.stage == Stage.WORK && this.opened && ItemStack.isSameItemSameComponents((ItemStack)stack, (ItemStack)this.request.item());
        }

        void deliver(Player patient, ItemStack stack) {
            Surgery.install(patient, this.request, stack);
            SurgerySeat.this.reaim(patient);
            this.close();
        }

        void close() {
            this.stage = Stage.CLOSE;
            this.stageStart = SurgerySeat.this.tickCount;
            SurgerySeat.this.entityData.set(ASSIST_IMPLANT, (Object)ItemStack.EMPTY);
        }

        void end() {
            SurgerySeat.this.assist = null;
            SurgerySeat.this.entityData.set(ASSIST_IMPLANT, (Object)ItemStack.EMPTY);
            SurgerySeat.this.entityData.set(ASSIST_ARM, Optional.empty());
        }
    }
}

