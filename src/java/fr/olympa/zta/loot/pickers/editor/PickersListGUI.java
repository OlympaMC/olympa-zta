package fr.olympa.zta.loot.pickers.editor;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.randomized.RandomizedPickerBase;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.gui.templates.HierarchyView;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.pickers.ZTAPicker;

public class PickersListGUI extends HierarchyView<RandomizedPickerBase<?>> {
	
	public PickersListGUI() {
		super(DyeColor.LIME, OlympaZTA.getInstance().pickers.getPickersHierarchy());
	}
	
	@Override
	public void click(RandomizedPickerBase<?> existing, Player p, ClickType click) {
		
	}
	
	@Override
	protected ItemStack getObjectItem(RandomizedPickerBase<?> object) {
		if (object instanceof ZTAPicker<?> picker) {
			return ItemUtils.item(Material.FILLED_MAP, "§a" + picker.getName() + " §f(" + picker.getClass().getSimpleName() + ")", picker.getDescription().stream().map(x -> "§7" + x).toArray(String[]::new));
		}
		return ItemUtils.item(Material.MAP, "§a" + object.getClass().getSimpleName());
	}
	
	public OlympaGUI toGUI() {
		return super.toGUI("Liste des pickers", 5);
	}
	
}
