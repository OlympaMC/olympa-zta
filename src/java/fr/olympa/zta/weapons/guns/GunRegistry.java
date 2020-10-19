package fr.olympa.zta.weapons.guns;

import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.ItemStackable;

public class GunRegistry {
	
	public static final NamespacedKey GUN_KEY = new NamespacedKey(OlympaZTA.getInstance(), "gunRegistry");
	
	public final String TABLE_NAME = "`zta_guns`";

	private final OlympaStatement createStatement = new OlympaStatement("INSERT INTO " + TABLE_NAME + " (`type`) VALUES (?)", true);
	private final OlympaStatement removeStatement = new OlympaStatement("DELETE FROM " + TABLE_NAME + " WHERE (`id` = ?)");
	private final OlympaStatement updateStatement = new OlympaStatement(""
			+ "UPDATE " + TABLE_NAME + " SET "
			+ "`ammos` = ?, "
			+ "`ready` = ?, "
			+ "`zoomed` = ?, "
			+ "`secondary_mode` = ?, "
			+ "`scope_id` = ?, "
			+ "`cannon_id` = ?, "
			+ "`stock_id` = ? "
			+ "WHERE (`id` = ?)");
	
	private final Map<Class<? extends Gun>, GunInstantiator<? extends Gun>> instantiators = new HashMap<>();
	
	public final Map<Integer, Gun> registry = new ConcurrentHashMap<>(200);
	public final List<Integer> toEvict = new ArrayList<>();
	
	private final BukkitTask evictingTask;
	public long nextEviction = 0;
	
	public GunRegistry(Class<? extends Gun>... classes) throws Exception {
		Statement statement = OlympaCore.getInstance().getDatabase().createStatement();
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
				"  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT," +
				"  `type` VARCHAR(30) NOT NULL," +
				"  `ammos` SMALLINT(2) UNSIGNED DEFAULT 0," +
				"  `ready` TINYINT(1) DEFAULT 1," +
				"  `zoomed` TINYINT(1) DEFAULT 0," +
				"  `secondary_mode` TINYINT(1) DEFAULT 0," +
				"  `scope_id` INT(10) DEFAULT -1," +
				"  `cannon_id` INT(10) DEFAULT -1," +
				"  `stock_id` INT(10) DEFAULT -1," +
				"  PRIMARY KEY (`id`))");
		statement.close();
		
		int period = 20 * 60 * 5;
		evictingTask = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), () -> {
			nextEviction = System.currentTimeMillis() + period * 50;
			synchronized (toEvict) {
				int evictedAmount = 0;
				for (Iterator<Integer> iterator = toEvict.iterator(); iterator.hasNext();) {
					Integer idToEvict = iterator.next();
					Gun evicted = registry.remove(idToEvict);
					if (evicted != null) {
						try {
							evicted.updateDatas(updateStatement.getStatement());
						}catch (SQLException e) {
							e.printStackTrace();
						}
					}
					iterator.remove();
					evictedAmount++;
				}
				if (evictedAmount != 0) OlympaZTA.getInstance().sendMessage("§6%d §eobjets déchargés.", evictedAmount);
			}
		}, 0, period);
		
		for (Class<? extends Gun> clazz : classes) {
			instantiators.put(clazz, new GunInstantiator<>(clazz));
		}
	}
	
	public boolean removeObject(Gun object) {
		if (!registry.containsKey(object.getID())) return false;
		try {
			System.out.println("GunRegistry.removeObject()");
			
			PreparedStatement statement = removeStatement.getStatement();
			statement.setInt(1, object.getID());
			statement.executeUpdate();
			
			registry.remove(object.getID());
			return true;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public <T extends Gun> GunInstantiator<T> addInstantiator(Class<T> clazz) {
		try {
			GunInstantiator<T> instantiator = new GunInstantiator<>(clazz);
			instantiators.put(clazz, instantiator);
			return instantiator;
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public <T extends Gun> GunInstantiator<T> getInstantiator(Class<T> clazz) {
		return (GunInstantiator<T>) instantiators.get(clazz);
	}
	
	public GunInstantiator<?> getInstantiator(String simpleName) throws ClassNotFoundException {
		return instantiators.get(Class.forName(getClass().getPackageName() + ".created." + simpleName));
	}
	
	public Collection<GunInstantiator<? extends Gun>> getInstantiators() {
		return instantiators.values();
	}
	
	/**
	 * Chercher dans le registre l'objet correspondant à l'ID
	 * @param id ID de l'objet
	 * @return objet correspondant à l'ID spécifié
	 */
	public Gun getGun(int id) {
		return registry.get(id);
	}
	
	public int getRegistrySize() {
		return registry.size();
	}
	
	/**
	 * Chercher dans le registre l'objet correspondant à l'item
	 * @param is Item pour lequel récupérer l'objet. Doit contenir une ligne de lore <code>[Ixxxxxx]</code>
	 * @return Objet correspondant à l'immatriculation de l'item. Peut renvoyer <i>null</i>.
	 */
	public Gun getGun(ItemStack is) {
		if (is == null) return null;
		if (!is.hasItemMeta()) return null;
		ItemMeta im = is.getItemMeta();
		if (!im.hasLore()) return null;
		
		int id = im.getPersistentDataContainer().getOrDefault(GUN_KEY, PersistentDataType.INTEGER, -1);
		if (id != -1) return registry.get(id);
		
		return null;
	}
	
	public void ifGun(ItemStack item, Consumer<Gun> consumer) {
		Gun gun = getGun(item);
		if (gun != null) consumer.accept(gun);
	}
	
	public int loadFromItems(ItemStack[] items) throws SQLException {
		synchronized (toEvict) {
			List<Integer> ids = new ArrayList<>();
			for (ItemStack item : items) {
				if (item == null) continue;
				if (!item.hasItemMeta()) continue;
				ItemMeta im = item.getItemMeta();
				int id = im.getPersistentDataContainer().getOrDefault(GUN_KEY, PersistentDataType.INTEGER, 0);
				if (id == 0 && im.hasLore()) {
					for (String s : im.getLore()) {
						int index = s.indexOf("[I");
						if (index != -1) {
							OlympaZTA.getInstance().getLogger().warning("Found registry object with lore: " + s);
							id = Integer.parseInt(s.substring(index + 2, s.indexOf("]")));
							im.getPersistentDataContainer().set(GUN_KEY, PersistentDataType.INTEGER, id);
							item.setItemMeta(im);
							break;
						}
					}
				}
				if (id != 0) {
					if (registry.containsKey(id)) {
						toEvict.remove((Object) id);
					}else {
						ids.add(id);
					}
				}
			}
			if (ids.isEmpty()) return 0;
			Statement statement = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ids.stream().map(x -> "id = '" + x.intValue() + "'").collect(Collectors.joining(" OR ")));
			int i = 0;
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				try {
					String type = resultSet.getString("type");
					GunInstantiator<?> instantiator = getInstantiator(type);
					Gun gun = instantiator.generate(id);
					gun.loadDatas(resultSet);
					registry.put(id, gun);
					i++;
				}catch (Exception e) {
					OlympaZTA.getInstance().sendMessage("Une erreur est survenue lors du chargement de l'objet " + id + " du registre.");
					e.printStackTrace();
					continue;
				}
			}
			statement.close();
			resultSet.close();
			return i;
		}
	}
	
	public void launchEvictItems(ItemStack[] items) {
		synchronized (toEvict) {
			for (ItemStack item : items) {
				ifGun(item, gun -> toEvict.add(gun.getID()));
			}
		}
	}
	
	public void unload() {
		evictingTask.cancel();
		toEvict.clear();
		for (Iterator<Gun> iterator = registry.values().iterator(); iterator.hasNext();) {
			Gun object = iterator.next();
			try {
				object.updateDatas(updateStatement.getStatement());
			}catch (SQLException e) {
				e.printStackTrace();
			}
			iterator.remove();
		}
	}
	
	public class GunInstantiator<T extends Gun> implements ItemStackable {
		private Class<T> clazz;
		private Constructor<T> constructor;
		private ItemStack demoItem;
		
		private GunInstantiator(Class<T> clazz) throws ReflectiveOperationException {
			this.clazz = clazz;
			constructor = clazz.getDeclaredConstructor(int.class);
			demoItem = new ItemStack((Material) clazz.getDeclaredField("TYPE").get(null));
			ItemMeta meta = demoItem.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			meta.setDisplayName("§e" + clazz.getDeclaredField("NAME").get(null));
			meta.setCustomModelData(1);
			demoItem.setItemMeta(meta);
		}
		
		public Class<T> getClazz() {
			return clazz;
		}
		
		@Override
		public ItemStack getDemoItem() {
			return demoItem;
		}
		
		@Override
		public ItemStack createItem() {
			try {
				T gun = create();
				return gun.createItemStack();
			}catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		
		private T generate(int id) throws ReflectiveOperationException {
			return constructor.newInstance(id);
		}
		
		private T create() throws SQLException, ReflectiveOperationException {
			PreparedStatement statement = createStatement.getStatement();
			statement.setString(1, clazz.getSimpleName());
			statement.executeUpdate();
			ResultSet generatedKeys = statement.getGeneratedKeys();
			generatedKeys.next();
			int id = generatedKeys.getInt("id");
			generatedKeys.close();
			T gun = generate(id);
			registry.put(id, gun);
			return gun;
		}
	}
	
}
