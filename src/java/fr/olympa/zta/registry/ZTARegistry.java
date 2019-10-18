package fr.olympa.zta.registry;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ZTARegistry{
	
	private static final Map<Integer, Registrable> registry = new HashMap<>(); // TODO: à sauvegarder dans une bdd ou un fichier YAML
	public static final Map<String, Class<? extends Registrable>> registrable = new HashMap<>();
	
	/**
	 * Enregistrer un type d'objet pouvant être enregistré dans le registre
	 * @param objectClass Classe de l'objet à enregistrer
	 */
	public static void registerObjectType(Class<? extends Registrable> objectClass){
		registrable.put(objectClass.getName(), objectClass);
	}
	
	/**
	 * Enregistrer dans le registre l'objet spécifié, dont les caractéristiques seront sauvegardées dans la BDD
	 * @param object Objet à enregistrer
	 * @return ID de l'objet enregistré (renvoie {@link Registrable#getID()})
	 */
	public static int registerObject(Registrable object) {
		if (!registrable.containsValue(object.getClass())) throw new IllegalArgumentException("Registrable object \"" + object.getClass().getName() + "\" has not been registered!");
		registry.put(object.getID(), object);
		return object.getID();
	}
	
	/**
	 * Chercher dans le registre l'objet correspondant à l'ID
	 * @param id ID de l'objet
	 * @return objet correspondant à l'ID spécifié
	 */
	public static Registrable getObject(int id) {
		return registry.get(id);
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
			if (s.contains("[I")) {
				return (ItemStackable) registry.get(Integer.parseInt(s.substring(s.indexOf("[I") + 2, s.indexOf("]"))));
			}
		}
		
		return null;
	}
	
	/**
	 * Donner l'item découlant d'un objet ItemStackable à un joueur et enregistrer cet objet dans le registre
	 * @param <T> Objet de type {@link ItemStackable}
	 * @param p Joueur à qui donner l'item
	 * @param object Objet pour lequel sera créé l'item et qui sera enregistré dans le registre.
	 */
	public static <T extends ItemStackable> void giveItem(Player p, T object){
		ItemStack is = object.createItemStack();
		ZTARegistry.registerObject(object);
		p.getInventory().addItem(is);
	}
	
}
