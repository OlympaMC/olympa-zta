package fr.olympa.zta.weapons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.templates.PagedGUI;

public class WeaponsGiveGUI extends PagedGUI<ItemStackable> {
	
	public static List<ItemStackable> stackables = new ArrayList<>();
	
	public WeaponsGiveGUI() {
		super("Don d'arme", DyeColor.CYAN, stackables, 6);
	}

	@Override
	public ItemStack getItemStack(ItemStackable object) {
		return object.getDemoItem();
	}

	@Override
	public void click(ItemStackable existing, Player p, ClickType click) {
		p.getInventory().addItem(existing.createItem());
	}

}
