package fr.olympa.zta;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.economy.OlympaMoney;

public class MoneyCommand extends ComplexCommand {

	public MoneyCommand() {
		super(OlympaZTA.getInstance(), "money", "Gérer son porte-monnaie.", ZTAPermissions.MONEY_COMMAND, "monnaie");
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (sender instanceof Player) {
			get(new CommandContext(this, new String[0], "money"));
			return true;
		}else return false;
	}

	@Cmd (args = "PLAYERS")
	public void get(CommandContext cmd) {
		if (cmd.args.length == 0) {
			if (player != null) {
				sendSuccess("Vous disposez de " + getGameMoney(getPlayer()).getFormatted());
			}else sendImpossibleWithConsole();
		}else if (ZTAPermissions.MONEY_COMMAND_OTHER.hasSenderPermission(getSender())) {
			sendSuccess("Le joueur dispose de " + getGameMoney(cmd.getArgument(0)).getFormatted());
		}else sendDoNotHavePermission();
	}

	@Cmd (permissionName = "MONEY_COMMAND_MANAGE", min = 2, args = { "PLAYERS", "DOUBLE" })
	public void set(CommandContext cmd) {
		OlympaMoney money = getGameMoney(cmd.getArgument(0));
		money.set(cmd.getArgument(1));
		sendSuccess("Le joueur dispose maintenant de " + money.getFormatted());
	}

	@Cmd (permissionName = "MONEY_COMMAND_MANAGE", min = 2, args = { "PLAYERS", "DOUBLE" })
	public void give(CommandContext cmd) {
		OlympaMoney money = getGameMoney(cmd.getArgument(0));
		money.give(cmd.getArgument(1));
		sendSuccess("Le joueur dispose maintenant de " + money.getFormatted());
	}

	@Cmd (permissionName = "MONEY_COMMAND_MANAGE", min = 2, args = { "PLAYERS", "DOUBLE" })
	public void withdraw(CommandContext cmd) {
		OlympaMoney money = getGameMoney(cmd.getArgument(0));
		money.withdraw(cmd.getArgument(1));
		sendSuccess("Le joueur dispose maintenant de " + money.getFormatted());
	}

	private OlympaMoney getGameMoney(Player p) {
		return OlympaPlayerZTA.get(p).getGameMoney();
	}

}
