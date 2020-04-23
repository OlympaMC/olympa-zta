package fr.olympa.zta.clans;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.clans.gui.ClanManagementGUI;
import fr.olympa.zta.clans.gui.NoClanGUI;
import fr.olympa.zta.registry.ZTARegistry;

public class ClansCommand extends ComplexCommand {

	public ClansCommand() {
		super(OlympaZTA.getInstance(), "clans", "Commande de gestion des clans", ZTAPermissions.CLANS_PLAYERS_COMMAND, "clan");
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			OlympaPlayerZTA olp = AccountProvider.get(p.getUniqueId());
			Clan clan = olp.getClan();
			if (clan == null) {
				new NoClanGUI(p).create(p);
			}else new ClanManagementGUI(olp).create(p);
			return true;
		}
		return false;
	}

	@Cmd (player = true, min = 1, syntax = "<nom du clan>")
	public void create(CommandContext cmd) { // syntax: create <name>
		if (OlympaPlayerZTA.get(cmd.player).getClan() != null) {
			sendError("Vous faites déjà partie d'un clan !");
			return;
		}
		String name = (String) cmd.args[0];
		if (Clan.exists(name)) {
			sendError("Un clan avec ce nom existe déjà !");
			return;
		}
		createClan(name, cmd.player);
	}
	public static Clan createClan(String name, Player p) {
		Clan clan = new Clan(name);
		OlympaPlayerZTA op = AccountProvider.get(p.getUniqueId());
		clan.addPlayer(op);
		clan.setChief(op.getInformation());
		ZTARegistry.registerObject(clan);
		Prefix.DEFAULT_GOOD.sendMessage(p, "Vous venez de créer votre clan !");
		return clan;
	}

	@Cmd (player = true, min = 1, args = "PLAYERS", syntax = "<nom du joueur>")
	public void invite(CommandContext cmd) {
		Clan clan = getPlayerClan(true);
		if (clan == null) return;
		
		Player targetPlayer = (Player) cmd.args[0];
		invite(clan, cmd.player, targetPlayer);
	}
	public static void invite(Clan clan, Player inviter, Player targetPlayer) {
		OlympaPlayerZTA target = AccountProvider.get(targetPlayer.getUniqueId());
		if (target.getClan() != null) {
			Prefix.DEFAULT_BAD.sendMessage(inviter, "Ce joueur est déjà dans un clan.");
			return;
		}

		if (ClansManager.getPlayerInvitations(targetPlayer).contains(clan)) {
			Prefix.DEFAULT_BAD.sendMessage(inviter, "Vous avez déjà invité ce joueur.");
			return;
		}

		ClansManager.addInvitation(targetPlayer, clan);
		Prefix.DEFAULT_GOOD.sendMessage(inviter, "Vous avez invité le joueur a rejoindre votre clan !");
		Prefix.DEFAULT_GOOD.sendMessage(targetPlayer, "§l" + inviter.getName() + "§r§a vous a invité à rejoindre son clan : \"§l" + clan.getName() + "§r§a\" ! §oTapez \"/clans accept " + clan.getName() + "\" ou acceptez l'invitation depuis le menu.");
	}
	
	@Cmd (player = true, min = 1, syntax = "<nom du clan>")
	public void accept(CommandContext cmd) {
		Clan clan = ClansManager.getPlayerInvitations(cmd.player).stream().filter(x -> x.getName().equals(cmd.args[0])).findFirst().orElse(null);
		if (clan == null) {
			sendError("Vous n'avez pas reçu d'invitation du le clan \"" + cmd.args[0] + "\".");
			return;
		}

		if (clan.addPlayer(getOlympaPlayer())) {
			sendSuccess("Vous venez de rejoindre le clan \"§l" + clan.getName() + "§r§a\" !");
			ClansManager.clearPlayerInvitations(cmd.player);
		}else {
			sendError("Ce clan n'a plus la place pour accueillir un nouveau membre.");
			ClansManager.getPlayerInvitations(cmd.player).remove(clan);
		}
	}

	@Cmd (player = true)
	public void quit(CommandContext cmd) {
		Clan clan = getPlayerClan(false);
		if (clan == null) return;
		
		OlympaPlayer p = getOlympaPlayer();
		if (clan.getChief() == p.getInformation()) {
			if (clan.getMembersAmount() == 1) {
				clan.disband();
			}else	sendError("Vous ne pouvez pas quitter votre clan en en étant le chef. Veuillez transférer la direction de celui-ci à un autre joueur.");
			return;
		}
		
		clan.removePlayer(p.getInformation(), true);
	}

	@Cmd (player = true, min = 1, args = "PLAYERS", syntax = "<nom du joueur>")
	public void chief(CommandContext cmd) {
		Clan clan = getPlayerClan(false);
		if (clan == null) return;

		if (cmd.args[0] == cmd.player) {
			sendError("Vous ne pouvez pas transférer la direction du clan à vous-même.");
			return;
		}

		OlympaPlayer target = AccountProvider.get(((Player) cmd.args[0]).getUniqueId());
		if (!clan.contains(target)) {
			sendError("Le joueur " + target.getName() + " ne fait pas partie de votre clan.");
			return;
		}

		clan.setChief(target.getInformation());
	}

	private Clan getPlayerClan(boolean chief) {
		OlympaPlayerZTA p = getOlympaPlayer();
		Clan clan = p.getClan();
		if (clan == null){
			sendError("Vous devez appartenir à un clan pour effectuer cette commande.");
			return null;
		}
		if (chief && clan.getChief() != p.getInformation()) {
			sendError("Vous devez être le chef du clan pour effectuer cette commande.");
			return null;
		}
		return clan;
	}

}
