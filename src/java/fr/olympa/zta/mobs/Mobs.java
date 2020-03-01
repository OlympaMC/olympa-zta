package fr.olympa.zta.mobs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.citizensnpcs.nms.v1_15_R1.util.CustomEntityRegistry;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumMobSpawn;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.MinecraftKey;

public class Mobs {

	public static final int NO_DAMAGE_TICKS = 1;

	private static final List<PotionEffect> ZOMBIE_EFFECTS = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 0, false, false));
	private static final List<PotionEffect> MOMIFIED_ZOMBIE_EFFECTS = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 1, false, false), new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 0, false, false));
	private static Random random = new Random();

	private static EntityTypes<CustomEntityZombie> customZombie = replaceEntity(CustomEntityZombie::new, "zombie", EntityTypes.ZOMBIE, "ZOMBIE");

	public static void spawnCommonZombie(Location location) {
		location.setYaw(random.nextInt(360));
		location.add(0.5, 0, 0.5); // sinon entités sont spawnées dans les coins des blocs et risquent de s'étouffer
		Zombie zombie = spawnZombie(location, SpawnReason.NATURAL);
		zombie.addPotionEffects(ZOMBIE_EFFECTS);
		zombie.getEquipment().clear();
	}

	public static Zombie spawnMomifiedZombie(Player p) {
		Zombie zombie = spawnZombie(p.getLocation(), SpawnReason.CUSTOM);
		zombie.addPotionEffects(MOMIFIED_ZOMBIE_EFFECTS);
		zombie.getEquipment().setArmorContents(p.getInventory().getArmorContents());
		zombie.setCustomName(p.getName() + " momifié");
		return zombie;
	}

	private static Zombie spawnZombie(Location location, SpawnReason reason) {
		Zombie zombie = (Zombie) customZombie.spawnCreature(((CraftWorld) location.getWorld()).getHandle(), null, null, null, new BlockPosition(location.getX(), location.getY(), location.getZ()), EnumMobSpawn.TRIGGERED, false, false, reason).getBukkitEntity();
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
