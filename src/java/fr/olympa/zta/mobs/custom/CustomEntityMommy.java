package fr.olympa.zta.mobs.custom;

import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.GenericAttributes;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.PathfinderGoal;
import net.minecraft.server.v1_15_R1.World;

public class CustomEntityMommy extends CustomEntityZombie { // ! it's a husk !

	public final int MOMMY_DIE_TICKS = 6000;

	private int dieTime = MOMMY_DIE_TICKS;
	private int lastTick = MinecraftServer.currentTick;

	public CustomEntityMommy(EntityTypes<CustomEntityMommy> type, World world) {
		super(type, world);
	}
	
	@Override
	protected void initTargetGoals() {
		this.targetSelector.a(2, (PathfinderGoal) new PathfinderGoalFixedDistanceTargetHuman((EntityCreature) this, 1, 20, true, false));
	}

	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.32);
		this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(9);
		this.getAttributeInstance(GenericAttributes.ARMOR).setValue(4);
	}
	
	@Override
	public void tick() {
		super.tick();
		if (!killed) {
			int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
			this.lastTick = MinecraftServer.currentTick;
			dieTime -= elapsedTicks;
			if (dieTime < 0) this.killEntity();
		}
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
