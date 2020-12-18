package fr.olympa.zta.mobs.custom;

import java.util.Random;

import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.DifficultyDamageScaler;
import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EntityZombie;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.EnumMobSpawn;
import net.minecraft.server.v1_15_R1.Explosion.Effect;
import net.minecraft.server.v1_15_R1.GeneratorAccess;
import net.minecraft.server.v1_15_R1.GenericAttributes;
import net.minecraft.server.v1_15_R1.GroupDataEntity;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.Items;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_15_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_15_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_15_R1.SoundCategory;
import net.minecraft.server.v1_15_R1.SoundEffects;
import net.minecraft.server.v1_15_R1.World;

public class CustomEntityZombie extends EntityZombie {

	private static final Random random = new Random();
	
	private int primed = -1;

	private Zombies zombieType;
	
	public CustomEntityZombie(EntityTypes<? extends CustomEntityZombie> type, World world) {
		super(type, world);
	}
	
	public void setZombieType(Zombies zombieType) {
		this.zombieType = zombieType;
		getBukkitEntity().setMetadata("ztaZombieType", new FixedMetadataValue(OlympaZTA.getInstance(), zombieType));
		switch (zombieType) {
		case TRAINING:
			setSilent(true);
			break;
		case TNT:
			setSlot(EnumItemSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
		case COMMON:
			this.goalSelector.a(2, new PathfinderGoalCustomZombieAttack(this, 1.0, false));
			initTargetGoals();
			break;
		}
	}

	@Override
	protected void l() { // addBehaviourGoals
		//this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction((EntityCreature) this, 1.0));
		this.goalSelector.a(7, new PathfinderGoalRandomStrollLand((EntityCreature) this, 1.0));
	}

	protected void initTargetGoals() {
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this));
		this.targetSelector.a(2, new PathfinderGoalFixedDistanceTargetHuman((EntityCreature) this, 4, 8, true, false));
	}
	
	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.27 + random.nextDouble() * 0.02);
		this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.5 + random.nextDouble());
		this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(22.0);
	}
	
	@Override
	protected boolean et() { // convertsInWater
		return false;
	}

	@Override
	protected boolean K_() { // isSunSensitive
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (primed != -1 && --primed == 0) {
			killEntity();
			world.createExplosion(null, super.locX(), super.locY(), super.locZ(), 3, false, Effect.NONE);
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
	public GroupDataEntity prepare(GeneratorAccess var0, DifficultyDamageScaler var1, EnumMobSpawn var2, GroupDataEntity var3, NBTTagCompound var4) {
		//if (zombieType == Zombies.TNT) setSlot(EnumItemSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
		return null;
	}
	
	@Override
	public void b(NBTTagCompound nbttagcompound) {
		super.b(nbttagcompound);
		nbttagcompound.setString("ZTAType", zombieType.name());
	}
	
	@Override
	public void a(NBTTagCompound nbttagcompound) {
		super.a(nbttagcompound);
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
			zombie.q(true);
		}

		@Override
		public void d() {
			super.d();
			zombie.q(false);
		}

	}

}
