package fr.olympa.zta.weapons;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.gui.templates.PagedView;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;

public class WeaponsGiveView extends PagedView<ItemStackable> {
	
	private boolean give;
	
	public WeaponsGiveView(boolean give) {
		super(DyeColor.CYAN, ItemStackableManager.stackables);
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
	
	public OlympaGUI toGUI() {
		return super.toGUI("Don d'arme", 5);
	}

}
