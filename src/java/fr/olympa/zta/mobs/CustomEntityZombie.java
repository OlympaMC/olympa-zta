package fr.olympa.zta.mobs;

import java.util.function.Function;

import org.bukkit.event.entity.EntityTargetEvent;

import net.minecraft.server.v1_13_R2.EntityCreature;
import net.minecraft.server.v1_13_R2.EntityCreeper;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.EntitySkeleton;
import net.minecraft.server.v1_13_R2.EntityZombie;
import net.minecraft.server.v1_13_R2.EnumItemSlot;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.Items;
import net.minecraft.server.v1_13_R2.PathfinderGoal;
import net.minecraft.server.v1_13_R2.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_13_R2.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_13_R2.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_13_R2.PathfinderGoalRandomStrollLand;
import net.minecraft.server.v1_13_R2.PathfinderGoalTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalZombieAttack;
import net.minecraft.server.v1_13_R2.World;

public class CustomEntityZombie extends EntityZombie {

	public CustomEntityZombie(World world) {
		super(world);
	}

	protected void n() {
		this.goalSelector.a(5, (PathfinderGoal) new PathfinderGoalMoveTowardsRestriction((EntityCreature) this, 1.0));
		this.goalSelector.a(8, (PathfinderGoal) new PathfinderGoalLookAtPlayer((EntityInsentient) this, EntityHuman.class, 8.0f));
		this.goalSelector.a(8, (PathfinderGoal) new PathfinderGoalRandomLookaround((EntityInsentient) this));
		this.goalSelector.a(2, (PathfinderGoal) new PathfinderGoalZombieAttack(this, 1.0, false));
		this.goalSelector.a(7, (PathfinderGoal) new PathfinderGoalRandomStrollLand((EntityCreature) this, 1.0));
		this.targetSelector.a(2, (PathfinderGoal) new PathfinderGoalFixedDistanceTargetHuman((EntityCreature) this, 8, true));
	}

	public void tick() {
		super.tick();
	}

	protected boolean dC() {
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
			this.target = this.e.world.a(this.e.locX, this.e.locY + this.e.getHeadHeight(), this.e.locZ, targetDistance, targetDistance / 2, new Function<EntityHuman, Double>() {
				@Override
				public Double apply(final EntityHuman entityhuman) {
					final ItemStack itemstack = entityhuman.getEquipment(EnumItemSlot.HEAD);
					return ((!(e instanceof EntitySkeleton) || itemstack.getItem() != Items.SKELETON_SKULL) && (!(e instanceof EntityZombie) || itemstack.getItem() != Items.ZOMBIE_HEAD) && (!(e instanceof EntityCreeper) || itemstack.getItem() != Items.CREEPER_HEAD))
							? 1.0
							: 0.5;
				}
			}, null);
			return this.target != null;
		}

		public void c() { // startExecuting
			e.setGoalTarget(this.target, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
			super.c();
		}
	}

}
