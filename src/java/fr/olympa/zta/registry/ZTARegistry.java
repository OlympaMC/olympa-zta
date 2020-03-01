package fr.olympa.zta.registry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;

public class ZTARegistry{

	public static final String TABLE_NAME = "`zta_registry`";

	public static final Map<String, RegistryType<?>> registrable = new HashMap<>();
	private static final Map<Integer, Registrable> registry = new HashMap<>(200);
	private static final Random idGen = new Random();
	
	private static PreparedStatement insertRegistrable;
	private static PreparedStatement removeRegistrable;

	/**
	 * Enregistrer un type d'objet pouvant être enregistré dans le registre
	 * @param objectClass Classe de l'objet à enregistrer
	 * @throws SQLException 
	 */
	public static <T extends Registrable> void registerObjectType(Class<T> objectClass, String tableName, String tableCreatorStatement, DeserializeDatas<T> deserialize) {
		if (tableName != null) {
			try {
				OlympaCore.getInstance().getDatabase().createStatement().executeUpdate(tableCreatorStatement);
			}catch (SQLException e) {
				e.printStackTrace();
				return;
			}
		}
		registrable.put(objectClass.getSimpleName(), new RegistryType<>(objectClass, tableName, deserialize));
	}
	
	/**
	 * Enregistrer dans le registre l'objet spécifié, dont les caractéristiques seront sauvegardées dans la BDD
	 * @param object Objet à enregistrer
	 * @return ID de l'objet enregistré (renvoie {@link Registrable#getID()})
	 */
	public static synchronized int registerObject(Registrable object) {
		if (!registrable.containsKey(object.getClass().getSimpleName())) throw new IllegalArgumentException("Registrable object \"" + object.getClass().getName() + "\" has not been registered!");
		registry.put(object.getID(), object);
		try {
			if (insertRegistrable == null || insertRegistrable.isClosed()) insertRegistrable = OlympaCore.getInstance().getDatabase().prepareStatement("INSERT INTO " + TABLE_NAME + " (`id`, `type`) VALUES (?, ?)");
			insertRegistrable.setInt(1, object.getID());
			insertRegistrable.setString(2, object.getClass().getSimpleName());
			insertRegistrable.executeUpdate();

			object.createDatas();
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return object.getID();
	}

	public static boolean removeObject(Registrable object) {
		if (!registry.containsKey(object.getID())) return false;
		try {
			if (removeRegistrable == null || removeRegistrable.isClosed()) removeRegistrable = OlympaCore.getInstance().getDatabase().prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE (`id` = ?)");
			removeRegistrable.setInt(1, object.getID());
			removeRegistrable.executeUpdate();

			registrable.get(object.getClass().getSimpleName()).removeDatas(object.getID());

			registry.remove(object.getID());
			return true;
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static int generateID() {
		int id = idGen.nextInt();
		if (registry.containsKey(id)) id = generateID();
		return id;
	}

	/**
	 * Chercher dans le registre l'objet correspondant à l'ID
	 * @param id ID de l'objet
	 * @return objet correspondant à l'ID spécifié
	 */
	public static <T extends Registrable> T getObject(int id) {
		return (T) registry.get(id);
	}

	public static int getRegistrySize() {
		return registry.size();
	}

	/**
	 * Chercher dans le registre l'objet correspondant à l'item
	 * @param is Item pour lequel récupérer l'objet. Doit contenir une ligne de lore <code>[Ixxxxxx]</code>
	 * @return Objet correspondant à l'immatriculation de l'item. Peut renvoyer <i>null</i>.
	 */
	public static ItemStackable getItemStackable(ItemStack is){
		if (!is.hasItemMeta()) return null;
		ItemMeta im = is.getItemMeta();
		if (!im.hasLore()) return null;
		
		for (String s : im.getLore()) {
			int index = s.indexOf("[I");
			if (index != -1) {
				return (ItemStackable) registry.get(Integer.parseInt(s.substring(index + 2, s.indexOf("]"))));
			}
		}
		
		return null;
	}
	
	/**
	 * Créer l'item découlant d'un objet ItemStackable et enregistrer cet objet dans le registre
	 * @param <T> Objet de type {@link ItemStackable}
	 * @param object Objet pour lequel sera créé l'item et qui sera enregistré dans le registre.
	 * @return Item créé
	 */
	public static <T extends ItemStackable> ItemStack createItem(T object) {
		ItemStack is = object.createItemStack();
		ZTARegistry.registerObject(object);
		return is;
	}

	public static int loadFromDatabase() throws SQLException {
		Statement statement = OlympaCore.getInstance().getDatabase().createStatement();
		statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (\r\n" +
				"  `id` INT NOT NULL,\r\n" + 
				"  `type` VARCHAR(45) NOT NULL,\r\n" + 
				"  PRIMARY KEY (`id`))");
		ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TABLE_NAME);
		while (resultSet.next()){
			int id = resultSet.getInt("id");
			try {
				RegistryType<?> type = registrable.get(resultSet.getString("type"));
				ResultSet objectSet = null;
				if (type.tableName != null) {
					objectSet = statement.executeQuery("SELECT * FROM " + type.tableName + " WHERE (`id` = '" + id + "')");
					objectSet.next();
				}
				registry.put(id, type.deserialize.deserialize(objectSet, id, type.clazz));
			}catch (Exception e) {
				OlympaZTA.getInstance().sendMessage("Une erreur est survenue lors du chargement de l'objet " + id + " du registre.");
				e.printStackTrace();
				continue;
			}
		}
		return registry.size();
	}

	public static void saveDatabase() {
		for (Registrable object : registry.values()) {
			try {
				object.updateDatas();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class RegistryType<T extends Registrable> {
		public Class<T> clazz;
		public String tableName;
		public DeserializeDatas<T> deserialize;

		private PreparedStatement removeDatas;

		public RegistryType(Class<T> clazz, String tableName, DeserializeDatas<T> deserialize) {
			this.clazz = clazz;
			this.tableName = tableName;
			this.deserialize = deserialize;
		}

		synchronized void removeDatas(int id) throws SQLException {
			if (removeDatas == null || removeDatas.isClosed()) removeDatas = OlympaCore.getInstance().getDatabase().prepareStatement("DELETE FROM " + tableName + " WHERE (`id` = ?)");
			removeDatas.setInt(1, id);
			removeDatas.executeUpdate();
		}
	}

	public static interface DeserializeDatas<T extends Registrable> {
		public abstract T deserialize(ResultSet set, int id, Class<?> clazz) throws Exception;

		static final DeserializeDatas<?> EASY_CLASS = (set, id, clazz) -> {
			return (Registrable) clazz.getDeclaredConstructor(int.class).newInstance(id);
		};

		public static <Z extends Registrable> DeserializeDatas<Z> easyClass() {
			return (DeserializeDatas<Z>) EASY_CLASS;
		}
	}

}
