package fr.olympa.zta.mobs;

import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.GenericAttributes;
import net.minecraft.server.v1_15_R1.PathfinderGoal;
import net.minecraft.server.v1_15_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_15_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_15_R1.PathfinderGoalZombieAttack;
import net.minecraft.server.v1_15_R1.World;

public class CustomEntityMommy extends CustomEntityZombie { // ! it's a husk !

	public CustomEntityMommy(EntityTypes<CustomEntityMommy> type, World world) {
		super(type, world);
	}

	@Override
	protected void l() { // addBehaviourGoals
		this.goalSelector.a(5, (PathfinderGoal) new PathfinderGoalMoveTowardsRestriction((EntityCreature) this, 1.0));
		this.goalSelector.a(2, (PathfinderGoal) new PathfinderGoalZombieAttack(this, 1.0, false));
		this.goalSelector.a(7, (PathfinderGoal) new PathfinderGoalRandomStrollLand((EntityCreature) this, 1.0));
		this.targetSelector.a(2, (PathfinderGoal) new PathfinderGoalFixedDistanceTargetHuman((EntityCreature) this, 8, true));
	}
	
	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.32);
		this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(8);
		this.getAttributeInstance(GenericAttributes.ARMOR).setValue(4);
	}
	
	@Override
	protected boolean et() { // convertsInWater
		return false;
	}

	@Override
	protected boolean K_() { // isSunSensitive
		return false;
	}

}
