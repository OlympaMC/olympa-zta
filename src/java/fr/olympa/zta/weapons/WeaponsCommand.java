package fr.olympa.zta.weapons;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.Registrable;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.weapons.guns.AmmoType;

public class WeaponsCommand extends ComplexCommand {

	public WeaponsCommand() {
		super(null, OlympaZTA.getInstance(), "weapons", "Commande pour les armes", ZTAPermissions.WEAPONS_COMMAND, "armes");
	}

	@Cmd (player = true, min = 1)
	public void give(CommandContext cmd) {
		Class<? extends Registrable> clazz = ZTARegistry.registrable.get(cmd.args[0]);
		if (clazz == null) {
			sendError("Cette arme n'existe pas.");
			return;
		}
		try {
			if (ItemStackable.class.isAssignableFrom(clazz)) {
				cmd.player.getInventory().addItem(ZTARegistry.createItem(((Class<? extends ItemStackable>) clazz).newInstance()));
				sendSuccess("Vous avez obtenu une instance de " + cmd.args[0] + ".");
			}else {
				sendError("L'objet spécifié ne correspond pas à une arme.");
			}
		}catch (InstantiationException | IllegalAccessException ex) {
			sendError("Une erreur est survenue lors du don de l'objet.");
			ex.printStackTrace();
		}
	}

	@Cmd (player = true, min = 3, args = { "LIGHT|HEAVY|HANDWORKED|CARTRIDGE", "true|false", "1|2|3|..." })
	public void giveAmmo(CommandContext cmd) {
		try {
			boolean filled = Boolean.parseBoolean((String) cmd.args[1]);
			int amount = Integer.parseInt((String) cmd.args[2]);
			cmd.player.getInventory().addItem(AmmoType.valueOf(((String) cmd.args[0]).toUpperCase()).getAmmo(amount, filled));
		}catch (NumberFormatException ex) {
			sendError(cmd.args[2] + " n'est pas un nombre valide.");
		}catch (IllegalArgumentException ex) {
			sendError("Ce type de munition n'existe pas.");
		}
	}

	@Cmd
	public void list(CommandContext cmd) {
		for (Class<? extends Registrable> clazz : ZTARegistry.registrable.values()) {
			if (ItemStackable.class.isAssignableFrom(clazz)) {
				sendMessage("§d● " + clazz.getSimpleName());
			}
		}
	}

}
