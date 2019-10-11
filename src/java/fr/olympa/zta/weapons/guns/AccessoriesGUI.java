package fr.olympa.zta.weapons.guns;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.CustomInventory;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.weapons.guns.accessories.Accessory;
import fr.olympa.zta.weapons.guns.accessories.Accessory.AccessoryType;

public class AccessoriesGUI implements CustomInventory{
	
	private static final ItemStack separator = ItemUtils.item(Material.GRAY_STAINED_GLASS_PANE, "§7");
	
	private Gun gun;
	
	public AccessoriesGUI(Gun gun){
		this.gun = gun;
	}
	
	public Inventory open(Player p){
		Inventory inv = Bukkit.createInventory(null, 54, "Modifier son arme");
		
		for (int i = 0; i < 54; i++) inv.setItem(i, separator);
		inv.setItem(31, gun.createItemStack());
		for (AccessoryType type : AccessoryType.values()) {
			ItemStack item = null;
			if (type.isEnabled(gun)) {
				Accessory accessory = type.get(gun);
				if (accessory == null) {
					item = ItemUtils.item(Material.LIME_STAINED_GLASS_PANE, "§aEmplacement disponible : " + type.getName());
				}else item = accessory.createItemStack();
			}else item = ItemUtils.item(Material.RED_STAINED_GLASS_PANE, "§cEmplacement indisponible : " + type.getName());
			inv.setItem(type.getSlot(), item);
		}
		
		return p.openInventory(inv).getTopInventory();
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		return true;
	}
	
	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot){
		AccessoryType accessoryType = AccessoryType.getFromSlot(slot);
		if (accessoryType == null) return true;
		if (current.getType() == Material.RED_STAINED_GLASS_PANE) return true;
		
		return false;
	}
	
}
