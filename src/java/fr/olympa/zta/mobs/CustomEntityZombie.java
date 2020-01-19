package fr.olympa.zta.mobs;

import org.bukkit.event.entity.EntityTargetEvent;

import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EntityZombie;
import net.minecraft.server.v1_15_R1.PathfinderGoal;
import net.minecraft.server.v1_15_R1.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_15_R1.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_15_R1.PathfinderGoalTarget;
import net.minecraft.server.v1_15_R1.PathfinderGoalZombieAttack;
import net.minecraft.server.v1_15_R1.PathfinderTargetCondition;
import net.minecraft.server.v1_15_R1.World;

public class CustomEntityZombie extends EntityZombie {

	public CustomEntityZombie(EntityTypes<CustomEntityZombie> type, World world) {
		super(type, world);
	}

	protected void l() {
		this.goalSelector.a(5, (PathfinderGoal) new PathfinderGoalMoveTowardsRestriction((EntityCreature) this, 1.0));
		this.goalSelector.a(2, (PathfinderGoal) new PathfinderGoalZombieAttack(this, 1.0, false));
		this.goalSelector.a(7, (PathfinderGoal) new PathfinderGoalRandomStrollLand((EntityCreature) this, 1.0));
		this.targetSelector.a(2, (PathfinderGoal) new PathfinderGoalFixedDistanceTargetHuman((EntityCreature) this, 8, true));
	}

	public void tick() {
		super.tick();
	}

	protected boolean et() {
		return false;
	}

	class PathfinderGoalFixedDistanceTargetHuman extends PathfinderGoalTarget {

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

}
