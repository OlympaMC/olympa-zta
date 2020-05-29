package fr.olympa.zta.mobs.custom;

import java.lang.reflect.Constructor;
import java.util.function.BiFunction;
import java.util.function.Function;

import fr.olympa.api.utils.Reflection;
import fr.olympa.api.utils.Reflection.ClassEnum;
import fr.olympa.zta.OlympaZTA;
import net.minecraft.server.v1_15_R1.EntityCreature;
import net.minecraft.server.v1_15_R1.EntityDrowned;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.GenericAttributes;
import net.minecraft.server.v1_15_R1.PathfinderGoal;
import net.minecraft.server.v1_15_R1.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_15_R1.World;

public class CustomEntityDrowned extends EntityDrowned {

	private static Constructor<PathfinderGoal> drownedAttackConstructor, drownedSwimUpConstructor;
	private static Function<EntityDrowned, PathfinderGoal> supplyDrownedAttack = (mob) -> {
		try {
			drownedAttackConstructor.setAccessible(true);
			return drownedAttackConstructor.newInstance(mob, 1.0, false);
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	};
	private static BiFunction<EntityDrowned, Integer, PathfinderGoal> supplyDrownedSwipUp = (mob, seaLevel) -> {
		try {
			drownedSwimUpConstructor.setAccessible(true);
			return drownedSwimUpConstructor.newInstance(mob, 1.0, seaLevel);
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	};

	static {
		try {
			drownedAttackConstructor = (Constructor<PathfinderGoal>) Reflection.getClass(ClassEnum.NMS, "EntityDrowned$a").getDeclaredConstructor(EntityDrowned.class, double.class, boolean.class);
			drownedSwimUpConstructor = (Constructor<PathfinderGoal>) Reflection.getClass(ClassEnum.NMS, "EntityDrowned$e").getDeclaredConstructor(EntityDrowned.class, double.class, int.class);
		}catch (ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
	}

	public CustomEntityDrowned(EntityTypes<? extends EntityDrowned> var0, World var1) {
		super(var0, var1);
	}

	@Override
	protected void initPathfinder() {
		this.goalSelector.a(2, supplyDrownedAttack.apply(this));
		this.goalSelector.a(6, supplyDrownedSwipUp.apply(this, OlympaZTA.getInstance().mobSpawning.seaLevel));
		this.goalSelector.a(7, (PathfinderGoal) new PathfinderGoalRandomStroll((EntityCreature) this, 1.0));
		this.targetSelector.a(2, (PathfinderGoal) new PathfinderGoalFixedDistanceTargetHuman((EntityCreature) this, 5, 32, true, false));
	}

	@Override
	protected void initAttributes() {
		super.initAttributes();
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.4);
		this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(10.0);
	}

	@Override
	public boolean i(EntityLiving entity) {
		if (entity != null) return entity.isInWater();
		return false;
	}

}
