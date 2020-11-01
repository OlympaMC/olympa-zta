package fr.olympa.zta.mobs.custom;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

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
		EntityZombie zombie = spawnMob(zombieType.getType(), location, SpawnReason.NATURAL);
		if (zombie instanceof CustomEntityZombie) ((CustomEntityZombie) zombie).setZombieType(zombieType);
	}

	public static Zombie spawnMomifiedZombie(Location loc, ItemStack[] armor, ItemStack contents[], String name) {
		CustomEntityMommy entityZombie = spawnMob(customMommy, loc, SpawnReason.CUSTOM);
		entityZombie.setContents(contents);
		Zombie zombie = (Zombie) entityZombie.getBukkitEntity();
		zombie.getEquipment().setArmorContents(armor);
		zombie.setCustomName(name);
		zombie.setCustomNameVisible(true);
		zombie.getWorld().getEntitiesByClass(Player.class).stream().map(player -> new AbstractMap.SimpleEntry<>(player, loc.distanceSquared(player.getLocation()))).filter(entry -> entry.getValue() <= 900).sorted((o1, o2) -> Double.compare(o1.getValue(), o2.getValue())).findFirst().ifPresent(entry -> zombie.setTarget(entry.getKey()));
		return zombie;
	}

	private static <T extends EntityZombie> T spawnMob(EntityTypes<T> entityType, Location location, SpawnReason reason) {
		T zombie = entityType.spawnCreature(((CraftWorld) location.getWorld()).getHandle(), null, null, null, new BlockPosition(location.getX(), location.getY(), location.getZ()), EnumMobSpawn.TRIGGERED, false, false, reason);
		zombie.setBaby(false);
		if (zombie.isPassenger()) zombie.getVehicle().die();
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
		COMMON(customZombie), TNT(customZombie), TRAINING(customZombie), DROWNED(customDrowned);

		private final EntityTypes<? extends EntityZombie> type;

		private Zombies(EntityTypes<? extends EntityZombie> type) {
			this.type = type;
		}

		public EntityTypes<? extends EntityZombie> getType() {
			return type;
		}
	}

}
