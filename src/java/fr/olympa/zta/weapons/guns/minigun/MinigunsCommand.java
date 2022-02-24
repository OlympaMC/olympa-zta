package fr.olympa.zta.weapons.guns.minigun;

import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class MinigunsCommand extends ComplexCommand {

	private static final HoverEvent HOVER_EVENT = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("§eClique pour suggérer la commande !")));

	private MinigunsManager miniguns;

	public MinigunsCommand(MinigunsManager miniguns) {
		super(OlympaZTA.getInstance(), "miniguns", "Permet de gérer les miniguns.", ZTAPermissions.MINIGUNS_COMMAND);
		this.miniguns = miniguns;
		super.addArgumentParser("MINIGUN", (sender, arg) -> miniguns.getMiniguns().keySet().stream().map(String::valueOf).collect(Collectors.toList()), arg -> {
			try {
				Minigun minigun = miniguns.getMinigun(Integer.parseInt(arg));
				if (minigun != null) return minigun;
			}catch (NumberFormatException ex) {}
			return null;
		}, x -> String.format("Il n'y a pas de minigun avec l'ID %d.", x));
	}

	@Cmd (player = true)
	public void create(CommandContext cmd) {
		BlockFace facing = getPlayer().getFacing();
		Location location = getPlayer().getLocation();
		location = location.getBlock().getLocation().add(0.5, 0, 0.5);
		location.setDirection(facing.getDirection());
		Minigun minigun = new Minigun(location, facing);
		sendSuccess("Le minigun %d a été créé.", miniguns.addMinigun(minigun));
	}

	private TextComponent createCommandComponent(String legacyText, String command, String after, Minigun minigun) {
		TextComponent compo = new TextComponent();
		compo.setHoverEvent(HOVER_EVENT);
		compo.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/miniguns " + command + " " + minigun.getID() + after));
		for (BaseComponent baseComponent : TextComponent.fromLegacyText(legacyText)) compo.addExtra(baseComponent);
		return compo;
	}

	@Cmd (args = "MINIGUN")
	public void info(CommandContext cmd) {
		Minigun minigun = cmd.getArgument(0);

		sender.spigot().sendMessage(getMinigunInfo(minigun));

		/*sender.spigot().sendMessage(createCommandComponent(Prefix.DEFAULT.formatMessage("§eVitesse : §6§l%d§e (§6%s§e)", minigun.getSpeed(), minigun.getSpeed() * 0.9 + " blocks/seconde"), "setSpeed", " ", minigun));
		sender.spigot().sendMessage(createCommandComponent(Prefix.DEFAULT.formatMessage("§eBlockdata : §6%s", minigun.getBlockData().getAsString()), "setBlock", " ", minigun));

		sendSuccess("Étages : (%d)", minigun.getFloors().size());

		int id = 0;
		for (Floor floor : minigun.getFloors()) {
			TextComponent floorCompo = new TextComponent();
			floorCompo.setColor(ChatColor.YELLOW);
			floorCompo.addExtra(createCommandComponent(Prefix.DEFAULT.formatMessage("§eY : §6§l%d§e | ", floor.getY()), "setY", " " + id + " ", minigun));
			floorCompo.addExtra(createButtonComponent(minigun, id, floor.getButtonUp(), "Montée", "UP"));
			floorCompo.addExtra(new TextComponent(" | "));
			floorCompo.addExtra(createButtonComponent(minigun, id, floor.getButtonDown(), "Descente", "DOWN"));

			sender.spigot().sendMessage(floorCompo);
			id++;
		}

		sender.spigot().sendMessage(createCommandComponent(Prefix.DEFAULT_GOOD.formatMessage("Créer un nouvel étage..."), "addFloor", " ", minigun));*/
	}

	@Cmd (args = "MINIGUN")
	public void remove(CommandContext cmd) {
		Minigun minigun = cmd.getArgument(0);

		miniguns.removeMinigun(minigun);
		sendSuccess("Le minigun %d a été supprimé.", minigun.getID());
	}

	@Cmd
	public void list(CommandContext cmd) {
		for (Minigun minigun : miniguns.getMiniguns().values())
			sender.spigot().sendMessage(getMinigunInfo(minigun));
	}

	private TextComponent getMinigunInfo(Minigun minigun) {
		return createCommandComponent(Prefix.DEFAULT_GOOD.formatMessage("Minigun §l#%d§a aux coordonnées §l%s§a, dans la direction §l%s§a.%s", minigun.getID(), SpigotUtils.convertLocationToHumanString(minigun.standLocation), minigun.facing.name(), minigun.isSpawned ? "" : "§c | Déchargé"), "info", "", minigun);
	}

	/*@Cmd (args = { "MINIGUN", "INTEGER" }, min = 2, syntax = "<minigun id> <floor y>")
	public void addFloor(CommandContext cmd) {
		cmd.<Minigun>getArgument(0).addFloor(cmd.getArgument(1));
		sendSuccess("Vous avez ajouté un étage à l'ascenseur.");
	}

	@Cmd (player = true, args = { "MINIGUN", "INTEGER", "UP|DOWN" }, min = 3, syntax = "<minigun id> <floor id> <button type>")
	public void setButton(CommandContext cmd) {
		Integer floorID = cmd.<Integer>getArgument(1);
		Floor floor = cmd.<Minigun>getArgument(0).getFloors().get(floorID);
		if (floor == null) {
			sendError("Il n'y a pas d'étage avec l'ID %d.", floorID);
			return;
		}

		Block target = getPlayer().getTargetBlockExact(3);
		if (target == null) {
			sendError("Tu dois regarder un bloc situé à moins de 3 blocs de distance de toi.");
			return;
		}
		String button = cmd.getArgument(2);
		if (button.equalsIgnoreCase("up")) {
			floor.setButtonUp(target.getLocation());
			sendSuccess("Tu as modifié la position du bouton de montée.");
		}else if (button.equalsIgnoreCase("down")) {
			floor.setButtonDown(target.getLocation());
			sendSuccess("Tu as modifié la position du bouton de descente.");
		}else {
			sendError("Argument %s inconnu (requis : UP / DOWN)", button);
		}
	}

	@Cmd (player = true, args = { "MINIGUN", "INTEGER", "INTEGER" }, min = 3, syntax = "<minigun id> <floor id> <floor y>")
	public void setY(CommandContext cmd) {
		Integer floorID = cmd.<Integer>getArgument(1);
		Floor floor = cmd.<Minigun>getArgument(0).getFloors().get(floorID);
		if (floor == null) {
			sendError("Il n'y a pas d'étage avec l'ID %d.", floorID);
			return;
		}
		floor.setY(cmd.getArgument(2));
		sendSuccess("Tu as modifié la hauteur de l'étage.");
	}

	@Cmd (player = true, args = { "MINIGUN" }, min = 2, syntax = "<minigun id> <block data>")
	public void setBlock(CommandContext cmd) {
		try {
			BlockData blockData = Bukkit.createBlockData(cmd.<String>getArgument(1));
			cmd.<Minigun>getArgument(0).setBlockData(blockData);
			sendSuccess("Tu as modifié la blockdata du minigun (%s)", blockData.getAsString());
		}catch (IllegalArgumentException ex) {
			sendError("La blockdata spécifiée est invalide.");
		}
	}

	@Cmd (player = true, args = { "MINIGUN", "INTEGER" }, min = 2, syntax = "<minigun id> <speed>")
	public void setSpeed(CommandContext cmd) {
		Minigun minigun = cmd.getArgument(0);
		try {
			minigun.setSpeed(cmd.getArgument(1));
			sendSuccess("Tu as modifié la vitesse du minigun (%d).", minigun.getSpeed());
		}catch (IllegalArgumentException ex) {
			sendError("La vitesse spécifiée doit être supérieure ou égale à 1.");
		}
	}*/

}
