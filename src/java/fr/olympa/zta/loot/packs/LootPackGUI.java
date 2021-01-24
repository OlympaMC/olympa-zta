package fr.olympa.zta.loot.packs;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.OlympaPlayerZTA;

public class LootPackGUI extends OlympaGUI {
	
	private PackBlock packBlock;
	
	public LootPackGUI(PackBlock packBlock, Player p) {
		super("Choisissez votre pack", 6);
		this.packBlock = packBlock;
		OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
		for (PackType pack : PackType.values()) {
			inv.setItem(pack.getSlot(), ItemUtils.item(Material.CHEST, "§ePack " + pack.getName(), pack.getLootsDescription()));
		}
		inv.setItem(50, ItemUtils.item(Material.BRICK, "§eMon porte-feuille", player.getGameMoney().getFormatted()));
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		Arrays.stream(PackType.values()).filter(type -> type.getSlot() == slot).findAny().ifPresent(type -> packBlock.start(p, type));
		Inventories.closeAndExit(p);
		return true;
	}
	
}
