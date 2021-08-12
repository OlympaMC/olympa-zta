package fr.olympa.zta.weapons;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.templates.PagedGUI;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;

public class WeaponsGiveGUI extends PagedGUI<ItemStackable> {
	
	private boolean give;
	
	public WeaponsGiveGUI(boolean give) {
		super("Don d'arme", DyeColor.CYAN, ItemStackableManager.stackables, 5);
		this.give = give;
	}

	@Override
	public ItemStack getItemStack(ItemStackable object) {
		return object.getDemoItem();
	}

	@Override
	public void click(ItemStackable existing, Player p, ClickType click) {
		if (give) {
			OlympaZTA.getInstance().sendMessage("Give de %s à %s.", existing.getId(), p.getName());
			p.getInventory().addItem(existing.createItem());
		}
	}

}
