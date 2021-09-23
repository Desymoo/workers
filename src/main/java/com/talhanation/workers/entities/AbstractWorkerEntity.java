package com.talhanation.workers.entities;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class AbstractWorkerEntity extends TameableEntity {
    private static final DataParameter<Optional<BlockPos>> START_POS = EntityDataManager.defineId(AbstractWorkerEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Optional<BlockPos>> DEST_POS = EntityDataManager.defineId(AbstractWorkerEntity.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Boolean> FOLLOW = EntityDataManager.defineId(AbstractWorkerEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> IS_WORKING = EntityDataManager.defineId(AbstractWorkerEntity.class, DataSerializers.BOOLEAN);

    public AbstractWorkerEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
        this.setOwned(false);
        this.xpReward = 2;
    }

    ///////////////////////////////////TICK/////////////////////////////////////////

    public double getMyRidingOffset() {
        return -0.35D;
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    public void tick() {
        super.tick();
        updateSwingTime();
        updateSwimming();
    }

    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof CreatureEntity) {
            CreatureEntity creatureentity = (CreatureEntity)this.getVehicle();
            this.yBodyRot = creatureentity.yBodyRot;
        }

    }

    @Nullable
    public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance diff, SpawnReason reason, @Nullable ILivingEntityData spawnData, @Nullable CompoundNBT nbt) {
        setRandomSpawnBonus();
        canPickUpLoot();
        return spawnData;
    }
    public void setRandomSpawnBonus(){
        getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("heath_bonus", this.random.nextGaussian() * 0.10D, AttributeModifier.Operation.MULTIPLY_BASE));
        getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("speed_bonus", this.random.nextGaussian() * 0.10D, AttributeModifier.Operation.MULTIPLY_BASE));

    }

    public void setDropEquipment(){
        this.dropEquipment();
    }

    ////////////////////////////////////REGISTER////////////////////////////////////


    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_WORKING, false);
        this.entityData.define(FOLLOW, false);
        this.entityData.define(START_POS, Optional.empty());
        this.entityData.define(DEST_POS, Optional.empty());
    }

    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("Follow", this.getFollow());
        nbt.putBoolean("isWorking", this.getIsWorking());

        this.getStartPos().ifPresent((pos) -> {
            nbt.putInt("StartPosX", pos.getX());
            nbt.putInt("StartPosY", pos.getY());
            nbt.putInt("StartPosZ", pos.getZ());
        });

        this.getDestPos().ifPresent((pos) -> {
            nbt.putInt("DestPosX", pos.getX());
            nbt.putInt("DestPosY", pos.getY());
            nbt.putInt("DestPosZ", pos.getZ());
        });
    }

    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("Follow", 1)) {
            this.setFollow(nbt.getBoolean("Follow"));
        }

        if (nbt.contains("isWorking", 1)) {
            this.setIsWorking(nbt.getBoolean("isWorking"));
        }

        if (nbt.contains("StartPosX", 99) &&
                nbt.contains("StartPosY", 99) &&
                nbt.contains("StartPosZ", 99)) {
            BlockPos blockpos = new BlockPos(
                    nbt.getInt("StartPosX"),
                    nbt.getInt("StartPosY"),
                    nbt.getInt("StartPosZ"));
            this.setStartPos(Optional.of(blockpos));
        }

        if (nbt.contains("DestPosX", 99) &&
                nbt.contains("DestPosY", 99) &&
                nbt.contains("DestPosZ", 99)) {
            BlockPos blockpos = new BlockPos(
                    nbt.getInt("DestPosX"),
                    nbt.getInt("DestPosY"),
                    nbt.getInt("DestPosZ"));
            this.setDestPos(blockpos);
        }

    }


    ////////////////////////////////////GET////////////////////////////////////

    public Optional<BlockPos> getDestPos(){
        return this.entityData.get(DEST_POS);
    }


    public Optional<BlockPos> getStartPos(){
        return this.entityData.get(START_POS);
    }

    public boolean getFollow(){
        return this.entityData.get(FOLLOW);
    }

    public boolean getIsWorking(){
        return this.entityData.get(IS_WORKING);
    }

    public SoundEvent getHurtSound(DamageSource ds) {
        if (this.isBlocking())
            return SoundEvents.SHIELD_BLOCK;
        return SoundEvents.VILLAGER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    protected float getSoundVolume() {
        return 0.4F;
    }

    protected float getStandingEyeHeight(Pose pos, EntitySize size) {
        return size.height * 0.9F;
    }

    public int getMaxHeadXRot() {
        return this.isInSittingPose() ? 20 : super.getMaxHeadXRot();
    }


    ////////////////////////////////////SET////////////////////////////////////


    public void setDestPos(BlockPos pos){
        this.entityData.set(DEST_POS, Optional.of(pos));
    }

    public void setStartPos(Optional<BlockPos> pos){
        this.entityData.set(START_POS, pos);
    }

    public void setFollow(boolean bool){
        this.entityData.set(FOLLOW, bool);

        LivingEntity owner = this.getOwner();

        if (bool) {
            owner.sendMessage(new StringTextComponent("I will follow you!"), owner.getUUID());
        }
        else
            owner.sendMessage(new StringTextComponent("I will not follow you!"), owner.getUUID());
    }

    public void setIsWorking(boolean bool) {
        entityData.set(IS_WORKING, bool);

        LivingEntity owner = this.getOwner();

        if (bool) {
            owner.sendMessage(new StringTextComponent("Im working now!"), owner.getUUID());
        }
        else
            owner.sendMessage(new StringTextComponent("I stopped working now!"), owner.getUUID());
    }

    public void setOwned(boolean owned) {
        super.setTame(owned);
    }


    public void setEquipment(){}


    ////////////////////////////////////ON FUNCTIONS////////////////////////////////////

    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();
        if (this.level.isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || isInSittingPose() || item == Items.BONE && !this.isTame();
            return flag ? ActionResultType.CONSUME : ActionResultType.PASS;
        } else {
            if (this.isTame() && player.getUUID().equals(this.getOwnerUUID())) {

                if (player.isCrouching()) {
                    //openInventory();
                }
                if(!player.isCrouching()) {
                    setFollow(!getFollow());
                    return ActionResultType.SUCCESS;
                }

            } else if (item == Items.EMERALD && !this.isTame() && playerHasEnoughEmeralds(player)) {
                if (!player.abilities.instabuild) {
                    if (!player.isCreative()) {
                        itemstack.shrink(workerCosts());
                    }
                    return ActionResultType.SUCCESS;
                }

                if (!net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(false);
                    this.setIsWorking(false);
                    this.level.broadcastEntityEvent(this, (byte)7);
                    return ActionResultType.SUCCESS;
                } else {
                    this.level.broadcastEntityEvent(this, (byte)6);
                }

                return ActionResultType.SUCCESS;
            }
            else if (item == Items.EMERALD  && !this.isTame() && !playerHasEnoughEmeralds(player)) {
                    player.sendMessage(new StringTextComponent("You need " + workerCosts() + " Emeralds to hire me!"), player.getUUID());
            }
            else if (!this.isTame() && item != Items.EMERALD ) {
                        player.sendMessage(new StringTextComponent("I am a " + workerName()), player.getUUID());

            }
            return super.mobInteract(player, hand);
        }
    }

    private boolean playerHasEnoughEmeralds(PlayerEntity player) {
        int recruitCosts = this.workerCosts();
        int emeraldCount = player.getItemInHand(Hand.MAIN_HAND).getCount();
        if (emeraldCount >= recruitCosts){
            return true;
        }
        if (player.isCreative()){
            return true;
        }
        else return false;
    }

    ////////////////////////////////////ATTACK FUNCTIONS////////////////////////////////////

    public boolean hurt(DamageSource dmg, float amt) {
        if (this.isInvulnerableTo(dmg)) {
            return false;
        } else {
            Entity entity = dmg.getEntity();
            this.setOrderedToSit(false);
            if (entity != null && !(entity instanceof PlayerEntity) && !(entity instanceof AbstractArrowEntity)) {
                amt = (amt + 1.0F) / 2.0F;
            }

            return super.hurt(dmg, amt);
        }
    }

    public boolean doHurtTarget(Entity entity) {
        boolean flag = entity.hurt(DamageSource.mobAttack(this), (float)((int)this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if (flag) {
            this.doEnchantDamageEffects(this, entity);

        }

        return flag;
    }

    public void die(DamageSource dmg) {
        super.die(dmg);
    }


    ////////////////////////////////////OTHER FUNCTIONS////////////////////////////////////

    public boolean isOwnedByThisPlayer(AbstractWorkerEntity recruit, PlayerEntity player){
        return  (recruit.getOwnerUUID() == player.getUUID());
    }



    @Override
    public boolean canBeLeashed(PlayerEntity player) {
        return false;
    }
    public abstract int workerCosts() ;

    public abstract String workerName();

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void spawnTamingParticles(boolean p_70908_1_) {
        IParticleData iparticledata = ParticleTypes.HAPPY_VILLAGER;
        if (!p_70908_1_) {
            iparticledata = ParticleTypes.SMOKE;
        }

        for(int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level.addParticle(iparticledata, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
        }

    }

}
