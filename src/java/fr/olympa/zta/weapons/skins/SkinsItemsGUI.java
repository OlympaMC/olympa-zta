package fr.olympa.zta.weapons.skins;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.templates.ItemsDropGUI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.weapons.WeaponsListener;

public class SkinsItemsGUI extends ItemsDropGUI {
	
	public SkinsItemsGUI() {
		super("Pose les armes auxquelles mettre un skin", 1);
	}
	
	private List<Skinable> skinables = new ArrayList<>(6);
	
	@Override
	protected boolean done(Player p, ClickType click, Spliterator<ItemStack> items) {
		items.forEachRemaining(item -> {
			if (WeaponsListener.getWeapon(item)instanceof Skinable skinable) skinables.add(skinable);
		});
		if (skinables.isEmpty()) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as mis aucun objet qui peut recevoir un skin ici...");
		}else {
			
		}
		return true;
	}
	
}
