package fr.olympa.zta.weapons.guns;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.gui.CustomInventory;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.weapons.guns.accessories.Accessory;
import fr.olympa.zta.weapons.guns.accessories.Accessory.AccessoryType;

public class AccessoriesGUI implements CustomInventory{
	
	private static final ItemStack separator = ItemUtils.item(Material.GRAY_STAINED_GLASS_PANE, "§7");
	
	private Gun gun;
	private ItemStack editedItem;
	
	public AccessoriesGUI(Gun gun, ItemStack item){
		this.gun = gun;
		this.editedItem = item;
	}
	
	public Inventory open(Player p){
		Inventory inv = Bukkit.createInventory(null, 45, "Modifier son arme");
		
		for (int i = 0; i < 45; i++) inv.setItem(i, separator);
		inv.setItem(22, gun.createItemStack(false));
		for (AccessoryType type : AccessoryType.values()) {
			ItemStack item = null;
			if (type.isEnabled(gun)) {
				Accessory accessory = gun.accessories[type.ordinal()];
				if (accessory == null) {
					item = type.getAvailableItemSlot();
				}else item = accessory.createItemStack();
			}else item = type.getUnavailableItemSlot();
			inv.setItem(type.getSlot(), item);
		}
		
		return p.openInventory(inv).getTopInventory();
	}
	
	public boolean onClose(Player p, Inventory inv){
		gun.updateItemLore(editedItem, true);
		return true;
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){ // clic avec rien dans la main
		AccessoryType accessoryType = AccessoryType.getFromSlot(slot);
		if (accessoryType == null) return true; // si c'est pas un slot d'accessoire : cancel
		
		Material type = current.getType();
		if (type == Material.RED_STAINED_GLASS_PANE || type == Material.LIME_STAINED_GLASS_PANE) return true; // si c'est un slot avec rien dedans : cancel
		
		gun.accessories[accessoryType.ordinal()].remove(gun); // enlever les caractéristiques de l'accessoire sur l'arme
		gun.accessories[accessoryType.ordinal()] = null; // enlever l'accessoire de l'arme
		
		new BukkitRunnable(){
			public void run(){
				inv.setItem(slot, accessoryType.getAvailableItemSlot()); // remettre l'item "slot disponible"
			}
		}.runTask(OlympaZTA.getInstance());
		
		return false; // laisse le joueur prendre l'item
	}
	
	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot){ // clic avec quelque chose dans la main
		AccessoryType accessoryType = AccessoryType.getFromSlot(slot);
		if (accessoryType == null) return true; // si c'est pas un slot d'accessoire : cancel
		if (current.getType() == Material.RED_STAINED_GLASS_PANE) return true; // si le slot est indisponible : cancel
		
		ItemStackable object = ZTARegistry.getItemStackable(cursor);
		if (!(object instanceof Accessory)) return true; // si l'objet en main n'est pas un accessoire : cancel
		Accessory accessory = (Accessory) object;
		if (accessoryType != AccessoryType.getFromAccessory(accessory)) return true; // si le type d'accessoire en main n'est pas approprié avec le slot : cancel
		
		if (current.getType() == Material.LIME_STAINED_GLASS_PANE) {
			new BukkitRunnable(){
				public void run(){
					p.setItemOnCursor(null); // enlever l'item "slot disponible" de la main du joueur (il l'aura chopé automatiquement lors du swap d'items)
				}
			}.runTask(OlympaZTA.getInstance());
		}else gun.accessories[accessoryType.ordinal()].remove(gun); // enlever les caractéristiques de l'accessoire précédent de l'arme
		
		gun.accessories[accessoryType.ordinal()] = accessory; // mettre l'accessoire sur l'arme
		accessory.apply(gun); // mettre les caractéristiques de l'accessoire sur l'arme
		
		return false; // laisse le joueur swapper les items
	}
	
}
