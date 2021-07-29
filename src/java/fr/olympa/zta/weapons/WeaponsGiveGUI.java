package fr.olympa.zta.weapons;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.templates.PagedGUI;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;

public class WeaponsGiveGUI extends PagedGUI<ItemStackable> {
	
	public WeaponsGiveGUI() {
		super("Don d'arme", DyeColor.CYAN, ItemStackableManager.stackables, 5);
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
