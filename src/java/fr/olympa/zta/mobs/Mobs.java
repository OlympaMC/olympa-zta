package fr.olympa.zta.mobs;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumCreatureType;
import net.minecraft.server.v1_15_R1.EnumMobSpawn;
import net.minecraft.server.v1_15_R1.IRegistry;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.World;

public class Mobs {

	public static final int NO_DAMAGE_TICKS = 1;

	private static final List<PotionEffect> ZOMBIE_EFFECTS = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 0, false, false));
	private static final List<PotionEffect> MOMIFIED_ZOMBIE_EFFECTS = Arrays.asList(new PotionEffect(PotionEffectType.SPEED, 999999, 0, false, false), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999, 1, false, false), new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, 0, false, false));
	private static Random random = new Random();

	private static EntityTypes<CustomEntityZombie> customZombie = injectNewEntity(EnumCreatureType.MONSTER, "customzombie", EntityTypes.ZOMBIE, "zombie", CustomEntityZombie::new);
	
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
		return zombie;
	}

	private static <T extends Entity> EntityTypes<T> injectNewEntity(EnumCreatureType type, String customName, EntityTypes<?> override, String overrideName, BiFunction<EntityTypes<T>, World, T> function) {
		customName = overrideName;
		EntityTypes<T> entityTypes = (EntityTypes<T>) EntityTypes.a.a((a, b) -> function.apply(a, b), type).a(customName);
		/*MinecraftKey key = new MinecraftKey(name);
		System.out.println("old " + IRegistry.ENTITY_TYPE.get(key).f());
		
		EntityTypes<?> oldEntityType = IRegistry.ENTITY_TYPE.get(key);
		int oldID = IRegistry.ENTITY_TYPE.a(oldEntityType);
		IRegistry.ENTITY_TYPE.a(oldID, key, entityTypes);
		System.out.println("old id " + oldID + " / new id " + IRegistry.ENTITY_TYPE.a(entityTypes));*/

		IRegistry.ENTITY_TYPE.a(IRegistry.ENTITY_TYPE.a(override), new MinecraftKey(/*"olympa", */customName), entityTypes);

		/*Map<Object, Type<?>> dataTypes = (Map<Object, Type<?>>) DataConverterRegistry.a()
				.getSchema(DataFixUtils.makeKey(SharedConstants.getGameVersion().getWorldVersion()))
				.findChoiceType(DataConverterTypes.ENTITY_TREE).types();
		dataTypes.put("minecraft:" + customName, dataTypes.get(entityTypes.f()));
		IRegistry.a(IRegistry.ENTITY_TYPE, customName, entityTypes);*/

		System.out.println("zombie " + IRegistry.ENTITY_TYPE.a(EntityTypes.ZOMBIE) + " | custom " + IRegistry.ENTITY_TYPE.a(entityTypes));

		return entityTypes;
	}

}
