package fr.olympa.zta.weapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WeaponsRegistry{
	
	private static final Map<Integer, Weapon> weapons = new HashMap<>(); // TODO: à sauvegarder dans une bdd ou un fichier YAML
	public static final List<Class<? extends Weapon>> weaponsTypes = new ArrayList<>();
	
	public static void registerWeapon(Weapon weapon){
		weapons.put(weapon.id, weapon);
	}
	
	public static Weapon getWeapon(ItemStack is){
		if (!is.hasItemMeta()) return null;
		ItemMeta im = is.getItemMeta();
		
		for (String s : im.getLore()) {
			if (s.contains("[")) {
				return weapons.get(Integer.parseInt(s.substring(s.indexOf("[") + 1, s.indexOf("]"))));
			}
		}
		
		return null;
	}
	
	public static void giveWeapon(Player p, Weapon weapon){
		ItemStack is = weapon.createItemStack();
		WeaponsRegistry.registerWeapon(weapon);
		p.getInventory().addItem(is);
	}
	
}
