package fr.olympa.zta.mobs.custom;

import org.bukkit.event.entity.EntityTargetEvent;

import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.PathfinderGoalTarget;

public class PathfinderGoalFixedDistanceTargetHuman extends PathfinderGoalTarget {

	private double targetDistanceSquared;
	private final int chance;
	protected EntityHuman target;

	public PathfinderGoalFixedDistanceTargetHuman(EntityCreature entitycreature, int chance, double distance, boolean sight, boolean nearby) {
		super(entitycreature, sight, nearby);
		this.chance = chance;
		this.targetDistanceSquared = distance * distance;
		this.a(1);
	}

	public boolean a() { // shouldExecute
		if (this.chance > 1 && this.e.getRandom().nextInt(this.chance) != 0) {
			return false;
		}
		target = null;
		double bestDistance = Integer.MAX_VALUE;
		for (EntityHuman potential : this.e.world.getPlayers()) {
			double distance = potential.h(this.e);
			if (distance < targetDistanceSquared && distance < bestDistance) {
				target = potential;
				bestDistance = distance;
			}
		}
		//this.target = this.e.world.a(new PathfinderTargetCondition().a(targetDistanceSquared).e(), this.e.locX(), this.e.locY() + this.e.getHeadHeight(), this.e.locZ());
		return this.target != null;
	}

	public void c() { // startExecuting
		super.c();
		e.setGoalTarget(this.target, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
	}
}