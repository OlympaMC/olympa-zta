package fr.olympa.zta.clans.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.editor.TextEditor;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.clans.ClansCommand;
import fr.olympa.zta.clans.ClansManager;

public class NoClanGUI extends OlympaGUI {

	public NoClanGUI(Player p) {
		super("Rejoindre un clan", InventoryType.HOPPER);

		inv.setItem(1, ItemUtils.item(Material.CRAFTING_TABLE, "§eCréer mon clan"));
		inv.setItem(3, ItemUtils.item(Material.PAPER, "§aVoir mes invitations", "§7§o" + ClansManager.getPlayerInvitations(p).size() + " invitations en attente"));
	}

	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		switch (slot) {
		case 1:
			Prefix.DEFAULT.sendMessage(p, "§aChoisis le nom de ton clan :");
			new TextEditor<String>(p, (msg) -> {
				ClansCommand.createClan(msg, p);
				new ClanManagementGUI(AccountProvider.get(p)).create(p);
			}, () -> {}, false, (player, msg) -> {
				if (ClansManager.exists(msg)) {
					Prefix.DEFAULT_BAD.sendMessage(player, "Un clan avec ce nom existe déjà !");
					return null;
				}
				return msg;
			}).enterOrLeave(p);
			break;
		case 3:
			new InvitationsGUI(p).create(p);
			break;
		}
		return true;
	}

}
