package fr.olympa.zta.weapons;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

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
import fr.olympa.zta.utils.Attribute;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.Gun;

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

	@Cmd (player = true, args = { "light|heavy|handworked|cartridge|powder", "INTEGER", "BOOLEAN" }, syntax = "[type de munition] [quantité] [vide ?]")
	public void giveAmmo(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			new WeaponsAmmosGUI().create(player);
		}else {
			try {
				boolean empty = cmd.getArgument(2, false);
				int amount = cmd.getArgument(1, 1);
				if ("powder".equalsIgnoreCase(cmd.getArgument(0))) {
					getPlayer().getInventory().addItem(AmmoType.getPowder(amount));
				}else getPlayer().getInventory().addItem(AmmoType.valueOf(cmd.<String>getArgument(0).toUpperCase()).getAmmo(amount, !empty));
			}catch (IllegalArgumentException ex) {
				sendError("Ce type de munition n'existe pas.");
			}
		}
	}

	@Cmd (player = true, min = 1, args = { "civil|gangster|antiriot|military", "helmet|chestplate|leggings|boots" })
	public void giveArmor(CommandContext cmd) {
		try {
			ArmorType armor = ArmorType.valueOf(cmd.<String>getArgument(0).toUpperCase());
			if (cmd.getArgumentsLength() == 1) {
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
	
	@Cmd (player = true, min = 1, args = { "maxAmmos|chargeTime|bulletSpeed|bulletSpread|knockback|fireRate|fireVolume", "DOUBLE" }, syntax = "<attribut> [valeur]")
	public void attribute(CommandContext cmd) {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item != null) {
			ItemStackable stackable = ZTARegistry.getItemStackable(item);
			if (stackable != null) {
				String attributeName = cmd.getArgument(0);
				try {
					Field attributeField = stackable.getClass().getField(attributeName);
					if (attributeField.getType() == Attribute.class) {
						Attribute attribute = (Attribute) attributeField.get(stackable);
						if (cmd.getArgumentsLength() == 1) {
							sendSuccess("La valeur de base de l'attribut %s est %f.", attributeName, attribute.getBaseValue());
						}else {
							float old = attribute.getBaseValue();
							attribute.setBaseValue(cmd.<Double>getArgument(1).floatValue());
							sendSuccess("La valeur de base de l'attribut %s a été modifiée (%f à %f).", attributeName, old, attribute.getBaseValue());
						}
						return;
					}
				}catch (ReflectiveOperationException e) {
					sendError("Une erreur est survenue: %s", e.toString());
				}
				sendError("L'attribut %s n'existe pas.", attributeName);
				return;
			}
		}
		sendError("L'objet que tu tiens en main n'est pas une arme.");
	}
	
	@Cmd (player = true, min = 1, args = { "DOUBLE", "player|entity" })
	public void damage(CommandContext cmd) {
		ItemStack item = player.getInventory().getItemInMainHand();
		if (item != null) {
			ItemStackable stackable = ZTARegistry.getItemStackable(item);
			if (stackable != null && stackable instanceof Gun) {
				Gun gun = (Gun) stackable;
				boolean entity = cmd.getArgument(1, "player").equalsIgnoreCase("entity");
				float damage = cmd.<Double>getArgument(0).floatValue();
				if (entity) {
					gun.customDamageEntity = damage;
				}else gun.customDamagePlayer = damage;
				sendSuccess("Votre arme fait désormais un dégât de %f aux %s.", damage, entity ? "entités" : "joueurs");
				return;
			}
		}
		sendError("L'objet que tu tiens en main n'est pas une arme à feu.");
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
