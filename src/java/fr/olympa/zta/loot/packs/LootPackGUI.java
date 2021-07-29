package fr.olympa.zta.loot.packs;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.zta.OlympaPlayerZTA;

public class LootPackGUI extends OlympaGUI {
	
	private PackBlock packBlock;
	
	public LootPackGUI(PackBlock packBlock, Player p) {
		super("Choisissez votre pack", 6);
		this.packBlock = packBlock;
		OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
		for (PackType pack : PackType.values()) {
			inv.setItem(pack.getSlot(), pack.getItem());
		}
		inv.setItem(49, ItemUtils.item(Material.PHANTOM_MEMBRANE, 1, "§eMa banque", "§7➤ §6" + player.getGameMoney().getFormatted()));
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 49) return true;
		Arrays.stream(PackType.values()).filter(type -> type.getSlot() == slot).findAny().ifPresent(type -> packBlock.start(p, type));
		Inventories.closeAndExit(p);
		return true;
	}
	
}
