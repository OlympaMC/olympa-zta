package fr.olympa.zta.weapons;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import fr.olympa.zta.utils.Attribute;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.GunRegistry;
import fr.olympa.zta.weapons.guns.GunType;
import fr.olympa.zta.weapons.guns.PersistentGun;

public class WeaponsCommand extends ComplexCommand {

	private DateFormat evictionFormat = new SimpleDateFormat("HH:mm:ss");

	public WeaponsCommand() {
		super(OlympaZTA.getInstance(), "weapons", "Commande pour les armes", ZTAPermissions.WEAPONS_COMMAND, "armes");
		addArgumentParser(
				"GUN",
				sender -> Collections.EMPTY_LIST,
				x -> OlympaZTA.getInstance().gunRegistry.getGun(Integer.parseInt(x)),
				x -> "L'objet avec l'ID " + x + " est introuvable dans le registre.");
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (player != null) {
			new WeaponsGiveGUI().create(player);
			return true;
		}else return false;
	}
	
	@Cmd (player = true)
	public void givePersistent(CommandContext cmd) {
		new PagedGUI<GunType>("Liste des armes", DyeColor.ORANGE, Arrays.asList(GunType.values()), 3) {
			
			@Override
			public ItemStack getItemStack(GunType object) {
				return object.getDemoItem();
			}
			
			@Override
			public void click(GunType existing, Player p, ClickType clickType) {
				p.getInventory().addItem(PersistentGun.create(existing).createItemStack());
			}

		}.create(getPlayer());
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
	public void gunAttribute(CommandContext cmd) {
		ItemStack item = player.getInventory().getItemInMainHand();
		Gun gun = OlympaZTA.getInstance().gunRegistry.getGun(item);
		if (gun != null) {
			String attributeName = cmd.getArgument(0);
			try {
				Field attributeField = gun.getClass().getField(attributeName);
				if (attributeField.getType() == Attribute.class) {
					Attribute attribute = (Attribute) attributeField.get(gun);
					if (cmd.getArgumentsLength() == 1) {
						sendSuccess("La valeur de base de l'attribut %s est %f. Valeur calculée (%d modificateurs) : %f.", attributeName, attribute.getBaseValue(), attribute.getModifiersSize(), attribute.getValue());
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
		sendError("L'objet que tu tiens en main n'est pas une arme.");
	}
	
	@Cmd (player = true, min = 1, args = { "DOUBLE", "player|entity" })
	public void gunDamage(CommandContext cmd) {
		ItemStack item = player.getInventory().getItemInMainHand();
		Gun gun = OlympaZTA.getInstance().gunRegistry.getGun(item);
		if (gun != null) {
			boolean entity = cmd.getArgument(1, "player").equalsIgnoreCase("entity");
			float damage = cmd.<Double>getArgument(0).floatValue();
			if (entity) {
				gun.customDamageEntity = damage;
			}else gun.customDamagePlayer = damage;
			sendSuccess("Votre arme fait désormais un dégât de %f aux %s.", damage, entity ? "entités" : "joueurs");
			return;
		}
		sendError("L'objet que tu tiens en main n'est pas une arme à feu.");
	}
	
	@Cmd (syntax = "<id>", args = "GUN")
	public void gunRegistryInfo(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			GunRegistry registry = OlympaZTA.getInstance().gunRegistry;
			sendInfo("Types d'armes disponible : §l" + GunType.values().length);
			sendInfo("Armes chargés dans le registre : §l" + registry.registry.size());
			sendInfo("Armes en attente d'être déchargées : §l" + registry.toEvict.size());
			sendInfo("Prochain déchargement : §l" + evictionFormat.format(registry.nextEviction));
		}else {
			Gun obj = cmd.getArgument(0);
			sendInfo("Identifiant de l'objet : §l" + obj.getID());
			sendInfo("Type d'objet : §l" + obj.getClass().getSimpleName());
		}
	}

	@Cmd (player = true, min = 1, syntax = "<id>", args = "GUN")
	public void gunItem(CommandContext cmd) {
		Gun gun = cmd.getArgument(0);
		getPlayer().getInventory().addItem(gun.createItemStack());
		sendSuccess("Vous venez de recevoir une copie de l'objet " + gun.getID() + ". §c§lAttention ! Il est probable que ce même objet soit employé ailleurs dans le jeu, ce qui peut mener à des comportements simultanés et imprévisibles.");
	}

	@Cmd (min = 1, syntax = "<id>", args = "GUN")
	public void gunRemove(CommandContext cmd) {
		Gun gun = cmd.getArgument(0);
		if (OlympaZTA.getInstance().gunRegistry.removeObject(gun)) {
			sendSuccess("L'objet a été correctement supprimé du registre.");
		}else sendError("Il y a eu un problème lors de la suppression de l'objet.");
	}

	@Cmd (args = "PLAYERS")
	public void gunsLoad(CommandContext cmd) {
		Player target;
		if (cmd.getArgumentsLength() == 0) {
			target = getPlayer();
			if (target == null) {
				sendIncorrectSyntax();
				return;
			}
		}else target = cmd.getArgument(0);
		
		try {
			sendSuccess("§2%d §aguns chargés depuis l'inventaire de §2%s§a.", OlympaZTA.getInstance().gunRegistry.loadFromItems(target.getInventory().getContents()), target.getName());
		}catch (SQLException e) {
			e.printStackTrace();
			sendError(e);
		}
	}
	
}
