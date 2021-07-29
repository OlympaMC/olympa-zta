package fr.olympa.zta.weapons;

import java.util.Arrays;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.templates.PagedGUI;
import fr.olympa.zta.weapons.guns.AmmoType;

public class WeaponsAmmosGUI extends PagedGUI<AmmoType> {
	
	public WeaponsAmmosGUI() {
		super("Give de munitions", DyeColor.LIGHT_BLUE, Arrays.asList(AmmoType.values()), 2);
	}
	
	@Override
	public ItemStack getItemStack(AmmoType object) {
		return object.getAmmo(10, true);
	}

	@Override
	public void click(AmmoType existing, Player p, ClickType click) {
		p.getInventory().addItem(existing.getAmmo(10, true));
	}
	
}
