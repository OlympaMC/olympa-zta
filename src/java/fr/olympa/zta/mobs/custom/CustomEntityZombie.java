package fr.olympa.zta.mobs.custom;

import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import net.minecraft.server.v1_16_R3.AttributeProvider;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.DifficultyDamageScaler;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EntityZombie;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.Explosion.Effect;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.GroupDataEntity;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.Items;
import net.minecraft.server.v1_16_R3.MobEffect;
import net.minecraft.server.v1_16_R3.MobEffects;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_16_R3.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_16_R3.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_16_R3.SoundCategory;
import net.minecraft.server.v1_16_R3.SoundEffects;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldAccess;

public class CustomEntityZombie extends EntityZombie {

	private static final Random random = new Random();
	
	private int primed = -1;

	private Zombies zombieType;
	
	private Entity primer;
	
	public CustomEntityZombie(EntityTypes<? extends CustomEntityZombie> type, World world) {
		super(type, world);
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.27 + random.nextDouble() * 0.02);
		this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.6 + random.nextDouble() * 1.5);
	}
	
	public static AttributeProvider.Builder getAttributeBuilder() {
		return EntityZombie.eS().a(GenericAttributes.FOLLOW_RANGE, 22.0);
	}
	
	public void setZombieType(Zombies zombieType) {
		this.zombieType = zombieType;
		getBukkitEntity().setMetadata("ztaZombieType", new FixedMetadataValue(OlympaZTA.getInstance(), zombieType));
		switch (zombieType) {
		case COMMON:
			initTargetGoals();
			initAttack();
			break;
		case TNT:
			setSlot(EnumItemSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
			initTargetGoals();
			break;
		case SPEED:
			getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.3);
			setSlot(EnumItemSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
			addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 9999999, 1, false, true), Cause.PLUGIN);
			initTargetGoals();
			initAttack();
			break;
		case TANK:
			getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.22);
			getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(7);
			getAttributeInstance(GenericAttributes.ARMOR).setValue(5);
			setSlot(EnumItemSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
			initTargetGoals();
			initAttack();
			break;
		case TRAINING:
			setSilent(true);
			break;
		}
	}

	@Override
	protected void initPathfinder() { // addBehaviourGoals
		//this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction((EntityCreature) this, 1.0));
		this.goalSelector.a(7, new PathfinderGoalRandomStrollLand((EntityCreature) this, 1.0));
	}
	
	protected void initAttack() {
		this.goalSelector.a(2, new PathfinderGoalCustomZombieAttack(this, 1.0, false));
	}

	protected void initTargetGoals() {
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
		this.targetSelector.a(2, new PathfinderGoalFixedDistanceTargetHuman((EntityCreature) this, 4, 8, true, false));
	}
	
	@Override
	protected boolean eN() { // convertsInWater
		return false;
	}

	@Override
	protected boolean T_() { // isSunSensitive
		return false;
	}
	
	@Override
	public boolean canPickupLoot() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (primed != -1 && --primed == 0) {
			killEntity();
			world.createExplosion(primer, super.locX(), super.locY(), super.locZ(), 3, false, Effect.NONE);
		}
	}
	
	@Override
	protected void dropDeathLoot(DamageSource damagesource, int i, boolean flag) {}
	
	@Override
	protected void a(DamageSource damagesource, boolean flag) {} // dropFromLootTable
	
	@Override
	public int getExpReward() {
		return 0;
	}
	
	@Override
	public boolean isPersistent() {
		return false;
	}
	
	@Override
	public boolean damageEntity(DamageSource damagesource, float f) {
		if (super.damageEntity(damagesource, f)) {
			if (zombieType == Zombies.TNT) {
				if (primed == -1) {
					primed = 11;
					primer = damagesource.getEntity();
					world.playSound(null, locX(), locY(), locZ(), SoundEffects.ENTITY_CREEPER_PRIMED, SoundCategory.HOSTILE, 1, 1);
				}
			}else if (this.getGoalTarget() == null && damagesource.getEntity() instanceof EntityLiving) {
				setGoalTarget((EntityLiving) damagesource.getEntity(), TargetReason.TARGET_ATTACKED_ENTITY, true);
			}
			return true;
		}
		return false;
	}

	@Override
	public @Nullable GroupDataEntity prepare(WorldAccess worldaccess, DifficultyDamageScaler difficultydamagescaler, EnumMobSpawn enummobspawn, @Nullable GroupDataEntity groupdataentity, @Nullable NBTTagCompound nbttagcompound) {
		//if (zombieType == Zombies.TNT) setSlot(EnumItemSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
		return null;
	}
	
	@Override
	public void saveData(NBTTagCompound nbttagcompound) {
		super.saveData(nbttagcompound);
		if (zombieType == null) {
			OlympaCore.getInstance().sendMessage("§cZombie sans type.");
		}else nbttagcompound.setString("ZTAType", zombieType.name());
	}
	
	@Override
	public void loadData(NBTTagCompound nbttagcompound) {
		super.loadData(nbttagcompound);
		if (nbttagcompound.hasKey("ZTAType")) setZombieType(Zombies.valueOf(nbttagcompound.getString("ZTAType")));
	}
	
	public static class PathfinderGoalCustomZombieAttack extends PathfinderGoalMeleeAttack {

		private EntityZombie zombie;

		public PathfinderGoalCustomZombieAttack(EntityZombie zombie, double speedModifier, boolean trackTarget) {
			super(zombie, speedModifier, trackTarget);
			this.zombie = zombie;
		}

		@Override
		public void c() {
			super.c();
			zombie.setAggressive(true);
		}

		@Override
		public void d() {
			super.d();
			zombie.setAggressive(false);
		}

	}

}
