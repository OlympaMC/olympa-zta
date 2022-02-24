package fr.olympa.zta.weapons;

import java.util.Arrays;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.gui.templates.PagedView;
import fr.olympa.zta.weapons.guns.AmmoType;

public class WeaponsAmmosView extends PagedView<AmmoType> {
	
	public WeaponsAmmosView() {
		super(DyeColor.LIGHT_BLUE, Arrays.asList(AmmoType.values()));
	}
	
	@Override
	public ItemStack getItemStack(AmmoType object) {
		return object.getAmmo(10, true);
	}

	@Override
	public void click(AmmoType existing, Player p, ClickType click) {
		p.getInventory().addItem(existing.getAmmo(10, true));
	}
	
	public OlympaGUI toGUI() {
		return super.toGUI("Give de munitions", 2);
	}
	
}
