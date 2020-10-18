package fr.olympa.zta.weapons.guns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.Registrable;

public class GunRegistry {
	
	public static final NamespacedKey PERISTENT_DATA_KEY = new NamespacedKey(OlympaZTA.getInstance(), "gunRegistry");
	
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
	
	public GunRegistry() throws SQLException {
		Statement statement = OlympaCore.getInstance().getDatabase().createStatement();
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
				"  `id` UNSIGNED INT NOT NULL AUTO_INCREMENT," +
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
	}
	
	/**
	 * Enregistrer dans le registre l'objet spécifié, dont les caractéristiques seront sauvegardées dans la BDD
	 * @param clazz Objet à enregistrer
	 * @return ID de l'objet enregistré (renvoie {@link Registrable#getID()})
	 */
	public synchronized Gun registerGun(Class<? extends Gun> clazz) throws SQLException, ReflectiveOperationException {
		PreparedStatement statement = createStatement.getStatement();
		statement.setString(1, clazz.getName());
		statement.executeUpdate();
		ResultSet generatedKeys = statement.getGeneratedKeys();
		generatedKeys.next();
		int id = generatedKeys.getInt("id");
		generatedKeys.close();
		Gun gun = clazz.getConstructor(int.class).newInstance(id);
		registry.put(id, gun);
		return gun;
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
	
	/**
	 * Chercher dans le registre l'objet correspondant à l'ID
	 * @param id ID de l'objet
	 * @return objet correspondant à l'ID spécifié
	 */
	public <T extends Gun> T getObject(int id) {
		return (T) registry.get(id);
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
		
		int id = im.getPersistentDataContainer().getOrDefault(PERISTENT_DATA_KEY, PersistentDataType.INTEGER, 0);
		if (id != 0) return registry.get(id);
		
		return null;
	}
	
	public void ifGun(ItemStack item, Consumer<Gun> consumer) {
		Gun gun = getGun(item);
		if (gun != null) consumer.accept(gun);
	}
	
	/**
	 * Créer l'item découlant d'un objet ItemStackable et enregistrer cet objet dans le registre
	 * @param <T> Objet de type {@link ItemStackable}
	 * @param object Objet pour lequel sera créé l'item et qui sera enregistré dans le registre.
	 * @return Item créé
	 */
	public <T extends Gun> ItemStack createItem(Class<? extends Gun> clazz) throws SQLException, ReflectiveOperationException {
		Gun gun = registerGun(clazz);
		return gun.createItemStack();
	}
	
	public int loadFromItems(ItemStack[] items) throws SQLException {
		synchronized (toEvict) {
			List<Integer> ids = new ArrayList<>();
			for (ItemStack item : items) {
				if (item == null) continue;
				if (!item.hasItemMeta()) continue;
				ItemMeta im = item.getItemMeta();
				int id = im.getPersistentDataContainer().getOrDefault(PERISTENT_DATA_KEY, PersistentDataType.INTEGER, 0);
				if (id == 0 && im.hasLore()) {
					for (String s : im.getLore()) {
						int index = s.indexOf("[I");
						if (index != -1) {
							OlympaZTA.getInstance().getLogger().warning("Found registry object with lore: " + s);
							id = Integer.parseInt(s.substring(index + 2, s.indexOf("]")));
							im.getPersistentDataContainer().set(PERISTENT_DATA_KEY, PersistentDataType.INTEGER, id);
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
					Class<? extends Gun> clazz = (Class<? extends Gun>) Class.forName(type);
					Gun gun = clazz.getConstructor(int.class).newInstance(id);
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
	
}
