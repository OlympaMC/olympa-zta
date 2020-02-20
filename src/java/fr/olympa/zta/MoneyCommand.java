package fr.olympa.zta;

import org.bukkit.entity.Player;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.objects.OlympaMoney;
import fr.olympa.api.provider.AccountProvider;

public class MoneyCommand extends ComplexCommand {

	public MoneyCommand() {
		super(null, OlympaZTA.getInstance(), "money", "Gérer son porte-monnaie.", ZTAPermissions.MONEY_COMMAND, "monnaie");
		noArgs = (sender) -> {
			if (sender instanceof Player) {
				get(new CommandContext(this, sender, new String[0], "money"));
				return true;
			}else return false;
		};
	}

	@Cmd (args = "PLAYERS")
	public void get(CommandContext cmd) {
		if (cmd.args.length == 0) {
			if (cmd.isPlayer()) {
				sendSuccess("Vous disposez de " + getGameMoney(cmd.player).getFormatted());
			}else sendImpossibleWithConsole();
		}else if (ZTAPermissions.MONEY_COMMAND_OTHER.hasPermission(cmd.sender)) {
			sendSuccess("Le joueur dispose de " + getGameMoney((Player) cmd.args[0]).getFormatted());
		}else sendDoNotHavePermission();
	}

	@Cmd (permissionName = "MONEY_COMMAND_MANAGE", min = 2, args = { "PLAYERS", "DOUBLE" })
	public void set(CommandContext cmd) {
		OlympaMoney money = getGameMoney((Player) cmd.args[0]);
		money.set((double) cmd.args[1]);
		sendSuccess("Le joueur dispose maintenant de " + money.getFormatted());
	}

	@Cmd (permissionName = "MONEY_COMMAND_MANAGE", min = 2, args = { "PLAYERS", "DOUBLE" })
	public void give(CommandContext cmd) {
		OlympaMoney money = getGameMoney((Player) cmd.args[0]);
		money.give((double) cmd.args[1]);
		sendSuccess("Le joueur dispose maintenant de " + money.getFormatted());
	}

	@Cmd (permissionName = "MONEY_COMMAND_MANAGE", min = 2, args = { "PLAYERS", "DOUBLE" })
	public void withdraw(CommandContext cmd) {
		OlympaMoney money = getGameMoney((Player) cmd.args[0]);
		money.withdraw((double) cmd.args[1]);
		sendSuccess("Le joueur dispose maintenant de " + money.getFormatted());
	}

	private OlympaMoney getGameMoney(Player p) {
		return AccountProvider.<OlympaPlayerZTA>get(p).getGameMoney();
	}

}
