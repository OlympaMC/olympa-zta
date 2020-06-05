package fr.olympa.zta.mobs.custom;

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

	private static Random random = new Random();

	private static EntityTypes<CustomEntityZombie> customZombie = replaceEntity(CustomEntityZombie::new, "zombie", EntityTypes.ZOMBIE, "ZOMBIE");
	private static EntityTypes<CustomEntityMommy> customMommy = replaceEntity(CustomEntityMommy::new, "husk", EntityTypes.HUSK, "HUSK");
	private static EntityTypes<CustomEntityDrowned> customDrowned = replaceEntity(CustomEntityDrowned::new, "drowned", EntityTypes.DROWNED, "DROWNED");

	public static void spawnCommonZombie(Zombies zombieType, Location location) {
		location.setYaw(random.nextInt(360));
		location.add(0.5, 0, 0.5); // sinon entités sont spawnées dans les coins des blocs et risquent de s'étouffer
		spawnMob(zombieType.getType(), location, SpawnReason.NATURAL);
	}

	public static Zombie spawnMomifiedZombie(Player p) {
		Zombie zombie = spawnMob(customMommy, p.getLocation(), SpawnReason.CUSTOM);
		zombie.getEquipment().setArmorContents(p.getInventory().getArmorContents());
		zombie.setCustomName(p.getName() + " momifié");
		zombie.setCustomNameVisible(true);
		if (p.getKiller() != null) zombie.setTarget(p.getKiller());
		return zombie;
	}

	private static <T extends EntityZombie> Zombie spawnMob(EntityTypes<T> entityType, Location location, SpawnReason reason) {
		Zombie zombie = (Zombie) entityType.spawnCreature(((CraftWorld) location.getWorld()).getHandle(), null, null, null, new BlockPosition(location.getX(), location.getY(), location.getZ()), EnumMobSpawn.TRIGGERED, false, false, reason).getBukkitEntity();
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

	public enum Zombies {
		COMMON(customZombie), DROWNED(customDrowned);

		private final EntityTypes<? extends EntityZombie> type;

		private Zombies(EntityTypes<? extends EntityZombie> type) {
			this.type = type;
		}

		public EntityTypes<? extends EntityZombie> getType() {
			return type;
		}
	}

}
