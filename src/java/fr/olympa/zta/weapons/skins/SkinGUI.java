package fr.olympa.zta.weapons.skins;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.weapons.WeaponsListener;

public class SkinGUI extends OlympaGUI {
	
	private static final int SLOT_ITEM = 13;
	
	private static final ItemStack ITEM_LAY_WEAPON = ItemUtils.item(Material.RED_STAINED_GLASS_PANE, "§cDépose une arme");
	private static final ItemStack ITEM_SKIN_UNAVAILABLE = ItemUtils.item(Material.ORANGE_STAINED_GLASS_PANE, "§6Skin en cours de création...");
	
	private static final Map<Integer, Skin> SKINS_SLOT = ImmutableMap.<Integer, Skin>builder()
			.put(28, Skin.NORMAL)
			.put(30, Skin.GOLD)
			.put(31, null)
			.put(32, null)
			.put(33, null)
			.put(34, null)
			.build();
	
	private Skinable skinable;
	private int oldSkin;
	
	public SkinGUI() {
		super("Choisis le skin de ton arme", 5);
		ItemStack separator = ItemUtils.itemSeparator(DyeColor.GRAY);
		for (int i = 0; i < 45; i++) {
			if (i == SLOT_ITEM || SKINS_SLOT.containsKey(i)) continue;
			inv.setItem(i, separator);
		}
	}
	
	private void setItemsNoWeapon() {
		for (Integer skinSlot : SKINS_SLOT.keySet()) {
			inv.setItem(skinSlot, ITEM_LAY_WEAPON);
		}
		skinable = null;
	}
	
	private boolean setWeapon(Player p, ItemStack item) {
		if (WeaponsListener.getWeapon(item) instanceof Skinable skinable) {
			Skin skin = skinable.getSkinOfItem(item);
			this.skinable = skinable;
			for (Entry<Integer, Skin> skinEntry : SKINS_SLOT.entrySet()) {
				ItemStack slotItem;
				Skin slotSkin = skinEntry.getValue();
				if (slotSkin == null) {
					slotItem = ITEM_SKIN_UNAVAILABLE;
				}else {
					slotItem = skinable.getSkinItem(slotSkin);
					if (slotSkin == skin) {
						ItemUtils.addEnchant(slotItem, Enchantment.DURABILITY, 1, true);
						this.oldSkin = skinEntry.getKey();
					}
				}
				inv.setItem(skinEntry.getKey(), slotItem);
			}
			return false;
		}
		Prefix.DEFAULT_BAD.sendMessage(p, "On ne peut pas appliquer de skin sur cet objet.");
		return true;
	}
	
	private void removeWeapon() {
		setItemsNoWeapon();
	}
	
	@Override
	public boolean onMoveItem(Player p, ItemStack moved) {
		return setWeapon(p, moved);
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == SLOT_ITEM) {
			if (current != null) removeWeapon();
			return false;
		}else if (SKINS_SLOT.containsKey(slot)) {
			Skin skin = SKINS_SLOT.get(slot);
			if (skin == null) {
				Prefix.ERROR.sendMessage(p, "Ce skin est encore en création...");
			}else {
				skinable.setSkin(skin, inv.getItem(SLOT_ITEM));
				ItemUtils.removeEnchant(inv.getItem(oldSkin), Enchantment.DURABILITY);
				oldSkin = SLOT_ITEM;
				ItemUtils.addEnchant(current, Enchantment.DURABILITY, 1, true);
			}
		}
		return true;
	}
	
	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		if (slot == SLOT_ITEM) {
			if (current == null) {
				setWeapon(p, cursor);
				return false;
			}else {
				return !setWeapon(p, cursor);
			}
		}
		return true;
	}
	
}
