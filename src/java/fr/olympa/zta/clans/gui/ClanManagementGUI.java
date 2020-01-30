package fr.olympa.zta.clans.gui;

import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.editor.TextEditor;
import fr.olympa.api.editor.parsers.PlayerParser;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.gui.templates.ConfirmGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.clans.Clan;
import fr.olympa.zta.clans.ClansCommand;
import fr.olympa.zta.clans.ClansManager;

public class ClanManagementGUI extends OlympaGUI {

	private static ItemStack noMember = ItemUtils.skullCustom("§cPas de membre", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDIzZWFlZmJkNTgxMTU5Mzg0Mjc0Y2RiYmQ1NzZjZWQ4MmViNzI0MjNmMmVhODg3MTI0ZjllZDMzYTY4NzJjIn19fQ==");
	private static ItemStack noMemberInvite = ItemUtils.skullCustom("§bInviter un nouveau membre", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDIzZWFlZmJkNTgxMTU5Mzg0Mjc0Y2RiYmQ1NzZjZWQ4MmViNzI0MjNmMmVhODg3MTI0ZjllZDMzYTY4NzJjIn19fQ==");

	private static ItemStack leave = ItemUtils.item(Material.OAK_DOOR, "§cQuitter le clan");
	private static ItemStack leaveChief = ItemUtils.item(Material.OAK_DOOR, "§c§mQuitter le clan", "§7§oPour pouvoir quitter votre clan,", "§7§ovous devez tout d'abord", "§7§otransmettre la direction de celui-ci", "§7§oà un autre membre.");

	private static ItemStack disband = ItemUtils.item(Material.BARRIER, "§cDémenteler le clan");

	private OlympaPlayer player;
	private Clan clan;
	private boolean isChief;

	public ClanManagementGUI(OlympaPlayer p) {
		super("Gérer son clan", 2);
		this.player = p;
		this.clan = ClansManager.getPlayerClan(p);
		isChief = clan.getChief() == player;

		inv.setItem(4, ItemUtils.item(Material.FILLED_MAP, "§eInformations sur le clan §6" + clan.getName(), "§e§lNombre de membres §r§6: §e§o" + clan.getMembersAmount()));
		inv.setItem(17, isChief ? leaveChief : leave);
		if (isChief) inv.setItem(16, disband);

		for (int id = 0; id < clan.getMaxSize(); id++) {
			OlympaPlayer member = clan.getMember(id);
			ItemStack item;
			if (isChief) {
				String[] lore = member == player ? new String[] { "§6§lChef" } : new String[] { "§7Clic §lgauche§r§7 : §cÉjecter", "§7Clic §ldroit§r§7 : §6Transférer la direction" };
				item = member == null ? noMemberInvite : ItemUtils.skull("§a" + member.getName(), member.getName(), lore);
			}else {
				item = member == null ? noMember : ItemUtils.skull("§a" + member.getName(), member.getName(), clan.getChief() == member ? "§6§lChef" : "§eMembre");
			}
			inv.setItem(9 + id, item);
		}
	}

	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 17) {
			if (!isChief) {
				clan.removePlayer(player);
				p.closeInventory();
			}
		}else if (isChief && slot >= 9 && slot < 14) {
			OlympaPlayer member = clan.getMember((byte) (slot - 9));
			if (member == null){
				Prefix.DEFAULT.sendMessage(p, "Entrez le nom du joueur à inviter.");
				new TextEditor<Player>(p, (target) -> {
					ClansCommand.invite(clan, p, target);
					this.create(p);
				}, () -> this.create(p), false, new PlayerParser()).enterOrLeave(p);
			}else if (member != player) { // pas le chef
				BiConsumer<Clan, OlympaPlayer> consumer;
				String msg;
				if (click == ClickType.LEFT){
					consumer = Clan::removePlayer;
					msg = "§7Voulez-vous vraiment éjecter le joueur " + member.getName() + " ?";
				}else if (click == ClickType.RIGHT) {
					consumer = Clan::setChief;
					msg = "§7Voulez-vous vraiment donner la direction au joueur " + member.getName() + " ?";
				}else return false;
				new ConfirmGUI(() -> {
					consumer.accept(clan, member);
					new ClanManagementGUI(player).create(p); // pour update les items
				}, () -> {
					this.create(p);
				}, msg).create(p);
			}
		}else if (slot == 16) {
			new ConfirmGUI(() -> clan.disband(), () -> this.create(p), "§7Voulez-vous vraiment supprimer le clan ?", "§cCette action sera définitive.").create(p);
			clan.disband();
		}
		return true;
	}

}