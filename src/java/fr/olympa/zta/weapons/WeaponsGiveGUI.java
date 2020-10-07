package fr.olympa.zta.weapons;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.zta.registry.ItemStackableInstantiator;
import fr.olympa.zta.registry.ZTARegistry;

public class WeaponsGiveGUI extends PagedGUI<ItemStackableInstantiator<?>> {

	public WeaponsGiveGUI() {
		super("Don d'arme", DyeColor.CYAN, ZTARegistry.get().itemStackables, 6);
	}

	@Override
	public ItemStack getItemStack(ItemStackableInstantiator<?> object) {
		return object.getDemoItem();
	}

	@Override
	public void click(ItemStackableInstantiator<?> existing, Player p) {
		try {
			p.getInventory().addItem(ZTARegistry.get().createItem(existing.create()));
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

}
