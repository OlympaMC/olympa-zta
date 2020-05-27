package fr.olympa.zta.weapons;

import java.util.Map.Entry;

import org.bukkit.command.CommandSender;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.ItemStackableInstantiator;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.registry.ZTARegistry.RegistryType;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;
import fr.olympa.zta.weapons.guns.AmmoType;

public class WeaponsCommand extends ComplexCommand {

	public WeaponsCommand() {
		super(OlympaZTA.getInstance(), "weapons", "Commande pour les armes", ZTAPermissions.WEAPONS_COMMAND, "armes");
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (player != null) {
			new WeaponsGiveGUI().create(player);
			return true;
		}else return false;
	}

	@Cmd (player = true, min = 1, syntax = "<nom de l'arme>")
	public void give(CommandContext cmd) {
		ItemStackableInstantiator<?> type = null;
		for (ItemStackableInstantiator<?> stackable : ZTARegistry.itemStackables) {
			if (stackable.clazz.getSimpleName().equalsIgnoreCase(cmd.getArgument(0))) {
				type = stackable;
				break;
			}
		}
		if (type == null) {
			sendError("Cette arme n'existe pas.");
			return;
		}
		try {
			getPlayer().getInventory().addItem(ZTARegistry.createItem(type.create()));
			sendSuccess("Vous avez obtenu une instance de " + cmd.getArgument(0) + ".");
		}catch (ReflectiveOperationException ex) {
			sendError("Une erreur est survenue lors du don de l'objet.");
			ex.printStackTrace();
		}
	}

	@Cmd (player = true, min = 2, args = { "light|heavy|handworked|cartridge|powder", "1|2|3|...", "true|false" }, syntax = "<type de munition> <quantité> [vide ?]")
	public void giveAmmo(CommandContext cmd) {
		try {
			boolean empty = cmd.args.length > 2 ? Boolean.parseBoolean(cmd.getArgument(2)) : false;
			int amount = Integer.parseInt(cmd.getArgument(1));
			if ("powder".equalsIgnoreCase(cmd.getArgument(0))) {
				getPlayer().getInventory().addItem(AmmoType.getPowder(amount));
			}else getPlayer().getInventory().addItem(AmmoType.valueOf(cmd.<String>getArgument(0).toUpperCase()).getAmmo(amount, !empty));
		}catch (NumberFormatException ex) {
			sendError(cmd.getArgument(1) + " n'est pas un nombre valide.");
		}catch (IllegalArgumentException ex) {
			sendError("Ce type de munition n'existe pas.");
		}
	}

	@Cmd (player = true, min = 1, args = { "civil|gangster|antiriot|military", "helmet|chestplate|leggings|boots" })
	public void giveArmor(CommandContext cmd) {
		try {
			ArmorType armor = ArmorType.valueOf(cmd.<String>getArgument(0).toUpperCase());
			if (cmd.args.length == 1) {
				for (ArmorSlot slot : ArmorSlot.values()) {
					getPlayer().getInventory().addItem(armor.get(slot));
				}
				sendSuccess("Vous avez reçu l'équipement §o" + armor.getName() + "§r§a complet !");
			}else {
				try {
					getPlayer().getInventory().addItem(armor.get(ArmorSlot.valueOf(cmd.<String>getArgument(1).toUpperCase())));
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
				sendMessage(Prefix.NONE, "§d● " + type.getKey());
			}
		}
	}

}
