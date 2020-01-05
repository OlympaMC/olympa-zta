package fr.olympa.zta.clans.gui;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.clans.Clan;
import fr.olympa.zta.clans.ClansManager;

public class InvitationsGUI extends PagedGUI<Clan> {

	protected InvitationsGUI(Player p) {
		super("§dMes invitations", DyeColor.MAGENTA, ClansManager.getPlayerInvitations(p));
	}

	public ItemStack getItemStack(Clan clan) {
		return ItemUtils.item(Material.PAPER, "§a" + clan.getName());
	}

	public void click(Clan existing, Player p) {
		if (existing.addPlayer(AccountProvider.get(p.getUniqueId()))) {
			Inventories.closeAndExit(p);
			ClansManager.clearPlayerInvitations(p);
		}else {
			Prefix.DEFAULT_BAD.sendMessage(p, "Ce clan n'a plus la place pour accueillir un nouveau membre.");
		}
	}

}
