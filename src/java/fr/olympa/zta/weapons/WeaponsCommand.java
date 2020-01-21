package fr.olympa.zta.weapons;

import java.util.Map.Entry;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.registry.ZTARegistry.RegistryType;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;
import fr.olympa.zta.weapons.guns.AmmoType;

public class WeaponsCommand extends ComplexCommand {

	public WeaponsCommand() {
		super(null, OlympaZTA.getInstance(), "weapons", "Commande pour les armes", ZTAPermissions.WEAPONS_COMMAND, "armes");
	}

	@Cmd (player = true, min = 1, syntax = "<nom de l'arme>")
	public void give(CommandContext cmd) {
		RegistryType<?> type = ZTARegistry.registrable.get(cmd.args[0]);
		if (type == null) {
			sendError("Cette arme n'existe pas.");
			return;
		}
		try {
			if (ItemStackable.class.isAssignableFrom(type.clazz)) {
				cmd.player.getInventory().addItem(ZTARegistry.createItem(((Class<? extends ItemStackable>) type.clazz).getDeclaredConstructor(int.class).newInstance(ZTARegistry.generateID())));
				sendSuccess("Vous avez obtenu une instance de " + cmd.args[0] + ".");
			}else {
				sendError("L'objet spécifié ne peut pas être matérialisé comme item.");
			}
		}catch (ReflectiveOperationException ex) {
			sendError("Une erreur est survenue lors du don de l'objet.");
			ex.printStackTrace();
		}
	}

	@Cmd (player = true, min = 2, args = { "light|heavy|handworked|cartridge", "1|2|3|...", "true|false" }, syntax = "<type de munition> <quantité> <vide ?>")
	public void giveAmmo(CommandContext cmd) {
		try {
			boolean empty = cmd.args.length > 2 ? Boolean.parseBoolean((String) cmd.args[2]) : false;
			int amount = Integer.parseInt((String) cmd.args[1]);
			cmd.player.getInventory().addItem(AmmoType.valueOf(((String) cmd.args[0]).toUpperCase()).getAmmo(amount, !empty));
		}catch (NumberFormatException ex) {
			sendError(cmd.args[1] + " n'est pas un nombre valide.");
		}catch (IllegalArgumentException ex) {
			sendError("Ce type de munition n'existe pas.");
		}
	}

	@Cmd (player = true, min = 1, args = { "civil|gangster|antiriot|military", "helmet|chestplate|leggings|boots" })
	public void giveArmor(CommandContext cmd) {
		try {
			ArmorType armor = ArmorType.valueOf(((String) cmd.args[0]).toUpperCase());
			if (cmd.args.length == 1) {
				for (ArmorSlot slot : ArmorSlot.values()) {
					cmd.player.getInventory().addItem(armor.get(slot));
				}
				sendSuccess("Vous avez reçu l'équipement §o" + armor.getName() + "§r§a complet !");
			}else {
				try {
					cmd.player.getInventory().addItem(armor.get(ArmorSlot.valueOf(((String) cmd.args[1]).toUpperCase())));
					sendSuccess("Vous avez reçu une pièce de l'équipement §o" + armor.getName() + "§r§a !");
				}catch (IllegalArgumentException ex) {
					sendError("Cet emplacement n'existe pas.");
				}
			}
		}catch (IllegalArgumentException ex) {
			sendError("Ce type d'armure n'existe pas.");
		}
	}

	@Cmd
	public void list(CommandContext cmd) {
		for (Entry<String, RegistryType<?>> type : ZTARegistry.registrable.entrySet()) {
			if (ItemStackable.class.isAssignableFrom(type.getValue().clazz)) {
				sendMessage("§d● " + type.getKey());
			}
		}
	}

}
