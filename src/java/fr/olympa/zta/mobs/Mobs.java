package fr.olympa.zta.mobs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.citizensnpcs.nms.v1_15_R1.util.CustomEntityRegistry;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EntityZombie;
import net.minecraft.server.v1_15_R1.EnumMobSpawn;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.MinecraftKey;

public class Mobs {

	public static final int NO_DAMAGE_TICKS = 1;

	private static Random random = new Random();

	private static EntityTypes<CustomEntityZombie> customZombie = replaceEntity(CustomEntityZombie::new, "zombie", EntityTypes.ZOMBIE, "ZOMBIE");
	private static EntityTypes<CustomEntityMommy> customMommy = replaceEntity(CustomEntityMommy::new, "husk", EntityTypes.HUSK, "HUSK");

	public static void spawnCommonZombie(Location location) {
		location.setYaw(random.nextInt(360));
		location.add(0.5, 0, 0.5); // sinon entités sont spawnées dans les coins des blocs et risquent de s'étouffer
		Zombie zombie = spawnMob(customZombie, location, SpawnReason.NATURAL);
		zombie.getEquipment().clear();
	}

	public static Zombie spawnMomifiedZombie(Player p) {
		Zombie zombie = spawnMob(customMommy, p.getLocation(), SpawnReason.CUSTOM);
		zombie.getEquipment().setArmorContents(p.getInventory().getArmorContents());
		zombie.setCustomName(p.getName() + " momifié");
		return zombie;
	}

	private static <T extends EntityZombie> Zombie spawnMob(EntityTypes<T> entityType, Location location, SpawnReason reason) {
		Zombie zombie = (Zombie) entityType.spawnCreature(((CraftWorld) location.getWorld()).getHandle(), null, null, null, new BlockPosition(location.getX(), location.getY(), location.getZ()), EnumMobSpawn.TRIGGERED, false, false, reason).getBukkitEntity();
		zombie.setMaximumNoDamageTicks(NO_DAMAGE_TICKS);
		if (zombie.isBaby()) zombie.setBaby(false);
		if (zombie.isInsideVehicle()) zombie.getVehicle().remove();
		return zombie;
	}

	private static <T extends Entity> EntityTypes<T> replaceEntity(EntityTypes.b<T> function, String overrideName, EntityTypes<?> overrideType, String overrideTypeFieldName) {
		try {
			EntityTypes<T> type = EntityTypes.a.<T>a(function, overrideType.e()).a(overrideName);

			CustomEntityRegistry registry = (CustomEntityRegistry) IRegistry.ENTITY_TYPE;
			registry.put(registry.a(overrideType), new MinecraftKey(overrideName), type);

			Field entityTypesField = EntityTypes.class.getField(overrideTypeFieldName);
			entityTypesField.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(entityTypesField, entityTypesField.getModifiers() & ~Modifier.FINAL);
			entityTypesField.set(null, type);

			return type;
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	}

}
