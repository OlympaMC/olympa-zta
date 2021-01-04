package fr.olympa.zta.loot.packs;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;

public class LootPackGUI extends OlympaGUI {
	
	private PackBlock packBlock;
	
	public LootPackGUI(PackBlock packBlock) {
		super("Choisissez votre pack", 5);
		this.packBlock = packBlock;
		for (PackType pack : PackType.values()) {
			inv.setItem(pack.getSlot(), ItemUtils.item(Material.CHEST, "§e" + pack.getName(), pack.getLootsDescription()));
		}
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		Arrays.stream(PackType.values()).filter(type -> type.getSlot() == slot).findAny().ifPresent(type -> packBlock.start(p, type));
		Inventories.closeAndExit(p);
		return true;
	}
	
}
