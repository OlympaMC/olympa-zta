package fr.olympa.zta.mobs.custom;

import java.lang.reflect.Constructor;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.api.spigot.utils.Reflection;
import fr.olympa.api.spigot.utils.Reflection.ClassEnum;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.mobs.custom.CustomEntityZombie.PathfinderGoalCustomZombieAttack;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import net.minecraft.server.v1_16_R3.AttributeProvider;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.DifficultyDamageScaler;
import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.EntityDrowned;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.GroupDataEntity;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import net.minecraft.server.v1_16_R3.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_16_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldAccess;

public class CustomEntityDrowned extends EntityDrowned {

	private static Constructor<PathfinderGoal> drownedGoToWaterConstructor, drownedSwimUpConstructor;
	private static Function<EntityDrowned, PathfinderGoal> supplyDrownedGoToWater = (mob) -> {
		try {
			drownedGoToWaterConstructor.setAccessible(true);
			return drownedGoToWaterConstructor.newInstance(mob, 1.0);
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	};
	private static BiFunction<EntityDrowned, Integer, PathfinderGoal> supplyDrownedSwipUp = (mob, seaLevel) -> {
		try {
			drownedSwimUpConstructor.setAccessible(true);
			return drownedSwimUpConstructor.newInstance(mob, 1.0, seaLevel + 1);
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	};

	static {
		try {
			drownedGoToWaterConstructor = (Constructor<PathfinderGoal>) Reflection.getClass(ClassEnum.NMS, "EntityDrowned$c").getDeclaredConstructor(EntityCreature.class, double.class);
			drownedSwimUpConstructor = (Constructor<PathfinderGoal>) Reflection.getClass(ClassEnum.NMS, "EntityDrowned$e").getDeclaredConstructor(EntityDrowned.class, double.class, int.class);
		}catch (ReflectiveOperationException ex) {
			ex.printStackTrace();
		}
	}

	public CustomEntityDrowned(EntityTypes<? extends EntityDrowned> var0, World var1) {
		super(var0, var1);
		getBukkitEntity().setMetadata("ztaZombieType", new FixedMetadataValue(OlympaZTA.getInstance(), Zombies.DROWNED));
	}
	
	public static AttributeProvider.Builder getAttributeBuilder() {
		return EntityDrowned.eS().a(GenericAttributes.MOVEMENT_SPEED, 0.44).a(GenericAttributes.ATTACK_DAMAGE, 10.0);
	}

	@Override
	protected void initPathfinder() {
		this.goalSelector.a(1, supplyDrownedGoToWater.apply(this));
		this.goalSelector.a(2, new PathfinderGoalCustomDrownedAttack(this, 1.0, false, 20));
		this.goalSelector.a(3, supplyDrownedSwipUp.apply(this, OlympaZTA.getInstance().mobSpawning.seaLevel));
		this.goalSelector.a(4, new PathfinderGoalRandomStroll(this, 1.0));
		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this).a(CustomEntityZombie.class));
		this.targetSelector.a(2, new PathfinderGoalFixedDistanceTargetHuman(this, 2, 32, true, false));
	}

	@Override
	public boolean i(EntityLiving entity) {
		if (entity != null) {
			return entity.isInWater() || entity.h(this) < 1030;
		}
		return false;
	}
	
	@Override
	protected void dropDeathLoot(DamageSource damagesource, int i, boolean flag) {}
	
	@Override
	protected void a(DamageSource damagesource, boolean flag) {} // dropFromLootTable
	
	@Override
	public int getExpReward() {
		return 0;
	}

	@Override
	public GroupDataEntity prepare(WorldAccess var0, DifficultyDamageScaler var1, EnumMobSpawn var2, GroupDataEntity var3, NBTTagCompound var4) {
		return null;
	}

	@Override
	protected boolean T_() { // isSunSensitive
		return false;
	}

	@Override
	protected void a(DifficultyDamageScaler var0) {} // populateDefaultEquipmentSlots
	
	public static class PathfinderGoalCustomDrownedAttack extends PathfinderGoalCustomZombieAttack {
		
		private EntityDrowned drowned;
		
		public PathfinderGoalCustomDrownedAttack(EntityDrowned drowned, double speedModifier, boolean trackTarget, int attackInterval) {
			super(drowned, speedModifier, trackTarget, attackInterval);
			this.drowned = drowned;
		}
		
		@Override
		public boolean a() {
			return super.a() && drowned.i(drowned.getGoalTarget());
		}
		
		@Override
		public boolean b() {
			return super.b() && drowned.i(drowned.getGoalTarget());
		}
		
	}

}
