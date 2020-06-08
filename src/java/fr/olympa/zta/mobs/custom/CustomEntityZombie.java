package fr.olympa.zta.mobs.custom;

import java.util.Random;

import net.minecraft.server.v1_15_R1.DifficultyDamageScaler;
import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EntityZombie;
import net.minecraft.server.v1_15_R1.EnumMobSpawn;
import net.minecraft.server.v1_15_R1.GeneratorAccess;
import net.minecraft.server.v1_15_R1.GenericAttributes;
import net.minecraft.server.v1_15_R1.GroupDataEntity;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.PathfinderGoal;
import net.minecraft.server.v1_15_R1.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_15_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_15_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_15_R1.World;

public class CustomEntityZombie extends EntityZombie {

	private static final Random random = new Random();

	public CustomEntityZombie(EntityTypes<? extends CustomEntityZombie> type, World world) {
		super(type, world);
	}

	@Override
	protected void l() { // addBehaviourGoals
		this.goalSelector.a(2, (PathfinderGoal) new PathfinderGoalCustomZombieAttack(this, 1.0, false));
		this.goalSelector.a(5, (PathfinderGoal) new PathfinderGoalMoveTowardsRestriction((EntityCreature) this, 1.0));
		this.goalSelector.a(7, (PathfinderGoal) new PathfinderGoalRandomStrollLand((EntityCreature) this, 1.0));
		initTargetGoals();
	}

	protected void initTargetGoals() {
		this.targetSelector.a(2, (PathfinderGoal) new PathfinderGoalFixedDistanceTargetHuman((EntityCreature) this, 5, 8, true, false));
	}
	
	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.27 + random.nextDouble() * 0.02);
		this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(3.5 + random.nextDouble());
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
	public GroupDataEntity prepare(GeneratorAccess var0, DifficultyDamageScaler var1, EnumMobSpawn var2, GroupDataEntity var3, NBTTagCompound var4) {
		return null;
	}

	static class PathfinderGoalCustomZombieAttack extends PathfinderGoalMeleeAttack {

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