package fr.olympa.zta.mobs.custom;

import org.bukkit.event.entity.EntityTargetEvent;

import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.PathfinderGoalTarget;
import net.minecraft.server.v1_16_R3.PathfinderTargetCondition;

public class PathfinderGoalFixedDistanceTargetHuman extends PathfinderGoalTarget {

	private double targetDistanceSquared;
	private final int chance;
	protected EntityHuman target;
	private PathfinderTargetCondition targetCondition;

	public PathfinderGoalFixedDistanceTargetHuman(EntityCreature entitycreature, int chance, double distance, boolean sight, boolean nearby) {
		super(entitycreature, sight, nearby);
		this.chance = chance;
		this.targetDistanceSquared = distance * distance;
		this.targetCondition = new PathfinderTargetCondition();
		this.a(1);
	}

	@Override
	public boolean a() { // shouldExecute
		if (this.chance > 1 && this.e.getRandom().nextInt(this.chance) != 0) {
			return false;
		}
		target = null;
		double bestDistance = Integer.MAX_VALUE;
		for (EntityHuman potential : this.e.world.getPlayers()) {
			if (!targetCondition.a(e, potential)) continue;
			double distance = potential.h(this.e);
			if (distance < targetDistanceSquared && distance < bestDistance) {
				target = potential;
				bestDistance = distance;
			}
		}
		//this.target = this.e.world.a(new PathfinderTargetCondition().a(targetDistanceSquared).e(), this.e.locX(), this.e.locY() + this.e.getHeadHeight(), this.e.locZ());
		return this.target != null;
	}

	@Override
	public void c() { // startExecuting
		super.c();
		e.setGoalTarget(this.target, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
	}
}