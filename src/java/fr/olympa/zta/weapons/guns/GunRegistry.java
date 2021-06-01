package fr.olympa.zta.weapons.guns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;

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
						try (PreparedStatement pstatement = updateStatement.createStatement()) {
							evicted.updateDatas(pstatement);
							updateStatement.executeUpdate(pstatement);
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
	}
	
	public boolean removeObject(Gun object) {
		if (!registry.containsKey(object.getID())) return false;
		try (PreparedStatement statement = removeStatement.createStatement()) {
			statement.setInt(1, object.getID());
			removeStatement.executeUpdate(statement);
			
			registry.remove(object.getID());
			
			OlympaZTA.getInstance().sendMessage("Objet §6%s (%d) §esupprimé du registre.", object.getType().getName(), object.getID());
			return true;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean removeObject(int id) {
		Gun gun = registry.get(id);
		if (gun != null) {
			removeObject(gun);
			return true;
		}
		try (PreparedStatement statement = removeStatement.createStatement()) {
			statement.setInt(1, id);
			removeStatement.executeUpdate(statement);
			OlympaZTA.getInstance().sendMessage("Objet déchargé §6%d §esupprimé du registre.", id);
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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
	
	public void itemRemove(ItemStack is) {
		if (is == null) return;
		if (!is.hasItemMeta()) return;
		ItemMeta im = is.getItemMeta();
		if (!im.hasLore()) return;
		
		int id = im.getPersistentDataContainer().getOrDefault(GUN_KEY, PersistentDataType.INTEGER, -1);
		if (id != -1) removeObject(id);
	}
	
	public Gun createGun(GunType type) throws SQLException {
		try (PreparedStatement statement = createStatement.createStatement()) {
			statement.setString(1, type.name());
			createStatement.executeUpdate(statement);
			ResultSet generatedKeys = statement.getGeneratedKeys();
			generatedKeys.next();
			int id = generatedKeys.getInt("id");
			generatedKeys.close();
			Gun gun = new Gun(id, type);
			registry.put(id, gun);
			return gun;
		}
	}
	
	public int loadFromItems(ItemStack[] items) throws SQLException {
		if (items == null || items.length == 0) return 0;
		synchronized (toEvict) {
			Set<Integer> ids = new HashSet<>();
			for (ItemStack item : items) {
				if (item == null) continue;
				if (!item.hasItemMeta()) continue;
				ItemMeta im = item.getItemMeta();
				int id = im.getPersistentDataContainer().getOrDefault(GUN_KEY, PersistentDataType.INTEGER, 0);
				/*if (id == 0 && im.hasLore()) {
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
				}*/
				if (id != 0) {
					if (registry.containsKey(id)) {
						toEvict.remove((Object) id);
					}else {
						if (!ids.add(id)) OlympaZTA.getInstance().sendMessage("§cL'objet du registre %d était contenu en double dans un inventaire.", id);
					}
				}
			}
			if (ids.isEmpty()) return 0;
			Statement statement = OlympaCore.getInstance().getDatabase().createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + ids.stream().map(x -> "id = '" + x.intValue() + "'").collect(Collectors.joining(" OR ")));
			int i = 0;
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				ids.remove(id);
				try {
					GunType type = GunType.valueOf(resultSet.getString("type"));
					Gun gun = new Gun(id, type);
					gun.loadDatas(resultSet);
					registry.put(id, gun);
					i++;
				}catch (Exception e) {
					OlympaZTA.getInstance().sendMessage("§cUne erreur est survenue lors du chargement de l'objet %d du registre.", id);
					e.printStackTrace();
					continue;
				}
			}
			ids.forEach(id -> OlympaZTA.getInstance().sendMessage("§cAucun objet trouvé dans le registre pour l'item avec ID %d.", id));
			statement.close();
			resultSet.close();
			return i;
		}
	}
	
	public void launchEvictItems(ItemStack[] items) {
		if (items == null) return;
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
			try (PreparedStatement statement = updateStatement.createStatement()) {
				object.updateDatas(statement);
				updateStatement.executeUpdate(statement);
			}catch (SQLException e) {
				e.printStackTrace();
			}
			iterator.remove();
		}
	}
	
}
