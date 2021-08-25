package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.guns.Accessory.AccessoryType;

public class AccessoriesGUI extends OlympaGUI{
	
	public static final NamespacedKey ACCESSORY_KEY = new NamespacedKey(OlympaZTA.getInstance(), "accessory");
	
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
				}else item = accessory.createItem();
			}else item = type.getUnavailableItemSlot();
			inv.setItem(type.getSlot(), item);
		}
	}
	
	@Override
	public boolean onClose(Player p) {
		ItemUtils.setRawLore(editedItem, gun.getLore(true));
		return true;
	}

	@Override
	public boolean onMoveItem(Player p, ItemStack moved, boolean isFromInv, int slot) {
		if (moved == null)
			return true;
		Accessory accessoryMoved = null;
		ItemMeta meta = moved.getItemMeta();
		if (meta.getPersistentDataContainer().has(ACCESSORY_KEY, PersistentDataType.INTEGER)) accessoryMoved = Accessory.values()[meta.getPersistentDataContainer().get(ACCESSORY_KEY, PersistentDataType.INTEGER)];
		if (accessoryMoved == null) return true; // si l'objet sur le slot n'est pas un accessoire : cancel
		AccessoryType accessoryType;
		if (!isFromInv) {
			accessoryType = AccessoryType.getFromSlot(slot);
			if (accessoryType == null) return true;
			ItemStack item = new ItemStack(moved);
			item.setAmount(1);
			gun.setAccessory(accessoryType, null);
			inv.setItem(slot, accessoryMoved.getType().getItemSlot(gun)); // met available le slot du gui
			SpigotUtils.giveItems(p, item);
			return true;
		}
		ItemStack current = null;
		int correctSlotInGui = -1;
		for (int i = 0; inv.getSize() > i; i++) {
			accessoryType = AccessoryType.getFromSlot(i);
			if (accessoryType == null || accessoryType != accessoryMoved.getType()) continue;
			if (!accessoryType.isEnabled(gun)) return true;
			current = inv.getItem(i);
			if (current == null) continue;
			correctSlotInGui = i;
			break;
		}
		if (correctSlotInGui == -1 || current == null)
			return true;
		if (moved.isSimilar(current)) return true;
		boolean already = gun.setAccessory(accessoryMoved);
		if (moved.getAmount() > 1) {
			ItemStack item = new ItemStack(moved);
			item.setAmount(1);
			moved.setAmount(moved.getAmount() - 1);
			inv.setItem(correctSlotInGui, item);
			if (already) {
				SpigotUtils.giveItems(p, current);
			}
		} else {
			p.getInventory().setItem(slot, null);
			inv.setItem(correctSlotInGui, moved);
			if (already)
				SpigotUtils.giveItems(p, current);
		}
		return true;
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) { // clic avec rien dans la main
		AccessoryType accessoryType = AccessoryType.getFromSlot(slot);
		if (accessoryType == null) return true; // si c'est pas un slot d'accessoire : cancel
		
		Material type = current.getType();
		if (type == Material.RED_STAINED_GLASS_PANE || type == Material.LIME_STAINED_GLASS_PANE) return true; // si c'est un slot avec rien dedans : cancel
		
		gun.setAccessory(accessoryType, null);

		p.getOpenInventory().setCursor(current); // met l'accessoire dans la souris d'inventaire du joueur
		inv.setItem(slot, accessoryType.getItemSlot(gun)); // met available le slot du gui
		return true; // annule l'action vanilla (c'est à dire prendre l'item)
	}
	
	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) { // clic avec quelque chose dans la main
		if (current == null) return true;
		AccessoryType accessoryType = AccessoryType.getFromSlot(slot);
		if (accessoryType == null) return true; // si c'est pas un slot d'accessoire : cancel
		if (!accessoryType.isEnabled(gun)) return true; // si le gun n'utilise pas l'accessoire : cancel
		
		Accessory accessory = null;
		ItemMeta meta = cursor.getItemMeta();
		if (meta.getPersistentDataContainer().has(ACCESSORY_KEY, PersistentDataType.INTEGER)) accessory = Accessory.values()[meta.getPersistentDataContainer().get(ACCESSORY_KEY, PersistentDataType.INTEGER)];
		if (accessory == null) return true; // si l'objet en main n'est pas un accessoire : cancel
		if (accessoryType != accessory.getType()) return true; // si le type d'accessoire en main n'est pas approprié avec le slot : cancel
		
		if (cursor.isSimilar(current)) return true;
		
		boolean already = gun.setAccessory(accessory);
		if (cursor.getAmount() > 1) {
			ItemStack item = new ItemStack(cursor);
			item.setAmount(1);
			cursor.setAmount(cursor.getAmount() - 1);
			if (already) {
				p.getOpenInventory().setCursor(current);
				SpigotUtils.giveItems(p, cursor);
			} else
				p.getOpenInventory().setCursor(cursor);
			inv.setItem(slot, item);
		} else {
			if (already)
				p.getOpenInventory().setCursor(current);
			else
				p.getOpenInventory().setCursor(null);
			inv.setItem(slot, cursor);
		}
		return true; // ne laisse le joueur swapper les items
	}
	
	@Override
	public boolean noMiddleClick() {
		return false;
	}
	
	@Override
	public boolean noDropClick() { // Désative le drop complètement
		return true; // TODO Cancel uniquement si l'item drop est le gun
	}
}
