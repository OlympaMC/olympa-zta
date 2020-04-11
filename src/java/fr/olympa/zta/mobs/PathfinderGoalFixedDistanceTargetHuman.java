package fr.olympa.zta.mobs;

import org.bukkit.event.entity.EntityTargetEvent;

import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_15_R1.PathfinderTargetCondition;

public class PathfinderGoalFixedDistanceTargetHuman extends PathfinderGoalTarget {

	private double targetDistance = 8.0;
	private final int chance;
	protected EntityHuman target;

	public PathfinderGoalFixedDistanceTargetHuman(EntityCreature entitycreature, double distance, boolean sight) {
		this(entitycreature, 10, distance, sight, false);
	}

	public PathfinderGoalFixedDistanceTargetHuman(EntityCreature entitycreature, int chance, double distance, boolean sight, boolean nearby) {
		super(entitycreature, sight, nearby);
		this.chance = chance;
		this.targetDistance = distance;
		this.a(1);
	}

	public boolean a() { // shouldExecute
		if (this.chance > 0 && this.e.getRandom().nextInt(this.chance) != 0) {
			return false;
		}
		this.target = this.e.world.a(new PathfinderTargetCondition().a(targetDistance), this.e.locX(), this.e.locY() + this.e.getHeadHeight(), this.e.locZ());
		/*new Function<EntityHuman, Double>() {
			@Override
			public Double apply(final EntityHuman entityhuman) {
				final ItemStack itemstack = entityhuman.getEquipment(EnumItemSlot.HEAD);
				return ((!(e instanceof EntitySkeleton) || itemstack.getItem() != Items.SKELETON_SKULL) && (!(e instanceof EntityZombie) || itemstack.getItem() != Items.ZOMBIE_HEAD) && (!(e instanceof EntityCreeper) || itemstack.getItem() != Items.CREEPER_HEAD))
						? 1.0
						: 0.5;
			}
		}*/
		return this.target != null;
	}

	public void c() { // startExecuting
		e.setGoalTarget(this.target, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
		super.c();
	}
}