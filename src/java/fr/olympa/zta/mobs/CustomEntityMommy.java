package fr.olympa.zta.mobs;

import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.GenericAttributes;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.PathfinderGoal;
import net.minecraft.server.v1_15_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_15_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_15_R1.PathfinderGoalZombieAttack;
import net.minecraft.server.v1_15_R1.World;

public class CustomEntityMommy extends CustomEntityZombie { // ! it's a husk !

	public final int MOMMY_DIE_TICKS = 6000;

	private int dieTime = MOMMY_DIE_TICKS;
	private int lastTick = MinecraftServer.currentTick;

	public CustomEntityMommy(EntityTypes<CustomEntityMommy> type, World world) {
		super(type, world);
	}

	@Override
	protected void l() { // addBehaviourGoals
		this.goalSelector.a(5, (PathfinderGoal) new PathfinderGoalMoveTowardsRestriction((EntityCreature) this, 1.0));
		this.goalSelector.a(2, (PathfinderGoal) new PathfinderGoalZombieAttack(this, 1.0, false));
		this.goalSelector.a(7, (PathfinderGoal) new PathfinderGoalRandomStrollLand((EntityCreature) this, 1.0));
		this.targetSelector.a(2, (PathfinderGoal) new PathfinderGoalFixedDistanceTargetHuman((EntityCreature) this, 1, 8, true, false));
	}
	
	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.32);
		this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(8);
		this.getAttributeInstance(GenericAttributes.ARMOR).setValue(4);
	}
	
	@Override
	public void tick() {
		super.tick();
		int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
		this.lastTick = MinecraftServer.currentTick;
		dieTime -= elapsedTicks;
		if (dieTime < 0) this.die();
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
	public boolean I() { // requiresCustomPersistence
		return true;
	}

	@Override
	public void b(NBTTagCompound nbttagcompound) {
		super.b(nbttagcompound);
		nbttagcompound.setInt("MommyDieTime", this.dieTime);
	}

	@Override
	public void a(NBTTagCompound nbttagcompound) {
		super.a(nbttagcompound);
		if (nbttagcompound.hasKey("MommyDieTime")) this.dieTime = nbttagcompound.getInt("MommyDieTime");
	}

}
