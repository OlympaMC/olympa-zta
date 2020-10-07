package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.weapons.guns.accessories.Accessory;
import fr.olympa.zta.weapons.guns.accessories.Accessory.AccessoryType;

public class AccessoriesGUI extends OlympaGUI{
	
	private static final ItemStack separator = ItemUtils.item(Material.GRAY_STAINED_GLASS_PANE, "§7");
	
	private Gun gun;
	private ItemStack editedItem;
	
	public AccessoriesGUI(Gun gun, ItemStack editedItem) {
		super("Modifier son arme", 5);
		this.gun = gun;
		this.editedItem = editedItem;
		
		for (int i = 0; i < 45; i++) inv.setItem(i, separator);
		inv.setItem(22, gun.createItemStack(false));
		for (AccessoryType type : AccessoryType.values()) {
			ItemStack item = null;
			if (type.isEnabled(gun)) {
				Accessory accessory = type.get(gun);
				if (accessory == null) {
					item = type.getAvailableItemSlot();
				}else item = accessory.createItemStack();
			}else item = type.getUnavailableItemSlot();
			inv.setItem(type.getSlot(), item);
		}
	}
	
	public boolean onClose(Player p) {
		gun.updateItemLore(editedItem, true);
		return true;
	}
	
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) { // clic avec rien dans la main
		AccessoryType accessoryType = AccessoryType.getFromSlot(slot);
		if (accessoryType == null) return true; // si c'est pas un slot d'accessoire : cancel
		
		Material type = current.getType();
		if (type == Material.RED_STAINED_GLASS_PANE || type == Material.LIME_STAINED_GLASS_PANE) return true; // si c'est un slot avec rien dedans : cancel
		
		gun.setAccessory(accessoryType, null);
		
		new BukkitRunnable(){
			public void run(){
				inv.setItem(slot, accessoryType.getAvailableItemSlot()); // remettre l'item "slot disponible"
			}
		}.runTask(OlympaZTA.getInstance());
		
		return false; // laisse le joueur prendre l'item
	}
	
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) { // clic avec quelque chose dans la main
		AccessoryType accessoryType = AccessoryType.getFromSlot(slot);
		if (accessoryType == null) return true; // si c'est pas un slot d'accessoire : cancel
		if (current.getType() == Material.RED_STAINED_GLASS_PANE) return true; // si le slot est indisponible : cancel
		
		ItemStackable object = ZTARegistry.get().getItemStackable(cursor);
		if (!(object instanceof Accessory)) return true; // si l'objet en main n'est pas un accessoire : cancel
		Accessory accessory = (Accessory) object;
		if (accessoryType != accessory.getType()) return true; // si le type d'accessoire en main n'est pas approprié avec le slot : cancel
		
		if (current.getType() == Material.LIME_STAINED_GLASS_PANE) {
			new BukkitRunnable(){
				public void run(){
					p.setItemOnCursor(null); // enlever l'item "slot disponible" de la main du joueur (il l'aura chopé automatiquement lors du swap d'items)
				}
			}.runTask(OlympaZTA.getInstance());
		}
		
		gun.setAccessory(accessoryType, accessory);
		accessory.apply(gun); // mettre les caractéristiques de l'accessoire sur l'arme
		
		return false; // laisse le joueur swapper les items
	}
	
}
