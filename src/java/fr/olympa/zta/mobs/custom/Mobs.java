package fr.olympa.zta.mobs.custom;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import fr.olympa.zta.OlympaZTA;
import net.citizensnpcs.nms.v1_16_R3.util.CustomEntityRegistry;
import net.minecraft.server.v1_16_R3.AttributeDefaults;
import net.minecraft.server.v1_16_R3.AttributeProvider;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.EntityZombie;
import net.minecraft.server.v1_16_R3.EnumMobSpawn;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import sun.misc.Unsafe;

public class Mobs {

	private static Random random = new Random();

	private static EntityTypes<CustomEntityZombie> customZombie;
	private static EntityTypes<CustomEntityMommy> customMommy;
	private static EntityTypes<CustomEntityDrowned> customDrowned;

	static {
		try {
			customZombie = replaceEntity(CustomEntityZombie::new, "zombie", EntityTypes.ZOMBIE, "ZOMBIE", CustomEntityZombie.getAttributeBuilder());
			customMommy = replaceEntity(CustomEntityMommy::new, "husk", EntityTypes.HUSK, "HUSK", CustomEntityMommy.getAttributeBuilder());
			customDrowned = replaceEntity(CustomEntityDrowned::new, "drowned", EntityTypes.DROWNED, "DROWNED", CustomEntityDrowned.getAttributeBuilder());
			OlympaZTA.getInstance().sendMessage("§63 §emobs custom ont été enregistrés.");
		}catch (Exception ex) {
			OlympaZTA.getInstance().sendMessage("§cUne erreur est survenue lors du chargement des mobs custom.");
			ex.printStackTrace();
		}
	}
	
	private Mobs() {}
	
	public static void spawnCommonZombie(Zombies zombieType, Location location) {
		location.setYaw(random.nextInt(360));
		location.add(0.5, 0, 0.5); // sinon entités sont spawnées dans les coins des blocs et risquent de s'étouffer
		EntityZombie zombie = spawnMob(zombieType.getType(), location, SpawnReason.NATURAL);
		if (zombie instanceof CustomEntityZombie) ((CustomEntityZombie) zombie).setZombieType(zombieType, true);
	}

	public static Zombie spawnMomifiedZombie(Location loc, ItemStack[] armor, ItemStack contents[], Player p) {
		CustomEntityMommy entityZombie = spawnMob(customMommy, loc, SpawnReason.CUSTOM);
		entityZombie.setContents(contents);
		entityZombie.setPlayer(p.getName());
		Zombie zombie = (Zombie) entityZombie.getBukkitEntity();
		zombie.getEquipment().setArmorContents(armor);
		zombie.setCustomName("§7§l" + p.getName() + "§7 momifié");
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

	private static <T extends EntityLiving> EntityTypes<T> replaceEntity(EntityTypes.b<T> function, String overrideName, EntityTypes<?> overrideType, String overrideTypeFieldName, AttributeProvider.Builder attributesBuilder) {
		try {
			EntityTypes<T> type = EntityTypes.Builder.<T>a(function, overrideType.e()).a(overrideName);

			CustomEntityRegistry registry = (CustomEntityRegistry) IRegistry.ENTITY_TYPE;
			registry.put(registry.a(overrideType), new MinecraftKey(overrideName), type);
			
			final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			final Unsafe unsafe = (Unsafe) unsafeField.get(null);

			Field entityTypesField = EntityTypes.class.getField(overrideTypeFieldName);
			entityTypesField.setAccessible(true);
			/*Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(entityTypesField, entityTypesField.getModifiers() & ~Modifier.FINAL);
			entityTypesField.set(null, type);*/
			Object staticFieldBase = unsafe.staticFieldBase(entityTypesField);
			long staticFieldOffset = unsafe.staticFieldOffset(entityTypesField);
			unsafe.putObject(staticFieldBase, staticFieldOffset, type);
			
			Field attributesMapField = AttributeDefaults.class.getDeclaredField("b");
			attributesMapField.setAccessible(true);
			//modifiersField.setInt(attributesMapField, attributesMapField.getModifiers() & ~Modifier.FINAL);
			Map<EntityTypes<? extends EntityLiving>, AttributeProvider> attributesMap = (Map<EntityTypes<? extends EntityLiving>, AttributeProvider>) attributesMapField.get(null);
			if (attributesMap instanceof ImmutableMap) {
				attributesMap = new HashMap<>(attributesMap);
				//attributesMapField.set(null, attributesMap);
				staticFieldBase = unsafe.staticFieldBase(attributesMapField);
				staticFieldOffset = unsafe.staticFieldOffset(attributesMapField);
				unsafe.putObject(staticFieldBase, staticFieldOffset, attributesMap);
			}
			attributesMap.put(type, attributesBuilder.a());
			
			return type;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public enum Zombies {
		COMMON(customZombie, "Infecté"),
		TNT(customZombie, "§cInfecté explosif"),
		SPEED(customZombie, "§bInfecté rapide"),
		TANK(customZombie, "§4Infecté tank"),
		TRAINING(customZombie, "Zombie d'entraînement", false),
		DROWNED(customDrowned, "Infecté noyé", false, EntityType.DROWNED);

		private final EntityTypes<? extends EntityZombie> type;
		private String name;
		private boolean loot;
		private EntityType bukkitType;

		private Zombies(EntityTypes<? extends EntityZombie> type, String name) {
			this(type, name, true, EntityType.ZOMBIE);
		}
		
		private Zombies(EntityTypes<? extends EntityZombie> type, String name, boolean loot) {
			this(type, name, loot, EntityType.ZOMBIE);
		}
		
		private Zombies(EntityTypes<? extends EntityZombie> type, String name, boolean loot, EntityType bukkitType) {
			this.type = type;
			this.name = name;
			this.loot = loot;
			this.bukkitType = bukkitType;
		}

		public EntityTypes<? extends EntityZombie> getType() {
			return type;
		}
		
		public String getName() {
			return name;
		}
		
		public boolean isLooting() {
			return loot;
		}
		
		public EntityType getBukkitType() {
			return bukkitType;
		}
	}

}
