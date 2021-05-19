package fr.olympa.zta.settings;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;

public class PlayerSettingsGUI extends OlympaGUI {
	
	private static final int SLOT_CLAN_BOARD = 7;
	private static final int SLOT_QUESTS_BOARD = 5;
	private static final int SLOT_ZONE_TITLE = 3;
	private static final int SLOT_BLOOD = 2;
	private static final int SLOT_AMBIENT = 1;
	private OlympaPlayerZTA player;
	
	public PlayerSettingsGUI(OlympaPlayerZTA player) {
		super("Paramètres", 1);
		this.player = player;
		
		inv.setItem(SLOT_AMBIENT, ItemUtils.itemSwitch("Sons d'ambiance", player.parameterAmbient.get(), SpigotUtils.wrapAndAlign("Active les bruits de fond (tirs, explosions) pendant le jeu.", 35).toArray(String[]::new)));
		inv.setItem(SLOT_BLOOD, ItemUtils.itemSwitch("Effets de sang", player.parameterBlood.get(), SpigotUtils.wrapAndAlign("Affiche des particules de sang lorsqu'un joueur ou un zombie se fait frapper.", 35).toArray(String[]::new)));
		inv.setItem(SLOT_ZONE_TITLE, ItemUtils.itemSwitch("Titres de zones", player.parameterZoneTitle.get(), SpigotUtils.wrapAndAlign("Affiche un titre sur l'écran lorsque tu rentres dans une zone de jeu.", 35).toArray(String[]::new)));
		inv.setItem(SLOT_QUESTS_BOARD, ItemUtils.itemSwitch("Quêtes en scoreboard", player.parameterQuestsBoard.get(), SpigotUtils.wrapAndAlign("Affiche une description des quêtes en cours dans le scoreboard, à droite de l'écran.", 35).toArray(String[]::new)));
		updateClanBoard();
	}
	
	private void updateClanBoard() {
		ClanBoardSetting setting = player.parameterClanBoard.get();
		inv.setItem(SLOT_CLAN_BOARD, ItemUtils.item(setting.getMaterial(), setting.getItemName(), SpigotUtils.wrapAndAlign(setting.getItemLore(), 35).toArray(String[]::new)));
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		switch (slot) {
		case SLOT_AMBIENT:
			player.parameterAmbient.set(ItemUtils.toggle(current));
			break;
		case SLOT_BLOOD:
			player.parameterBlood.set(ItemUtils.toggle(current));
			break;
		case SLOT_ZONE_TITLE:
			player.parameterZoneTitle.set(ItemUtils.toggle(current));
			break;
		case SLOT_QUESTS_BOARD:
			player.parameterQuestsBoard.set(ItemUtils.toggle(current));
			break;
		case SLOT_CLAN_BOARD:
			int setting = player.parameterClanBoard.get().ordinal() + 1;
			if (setting == ClanBoardSetting.values().length) setting = 0;
			player.parameterClanBoard.set(ClanBoardSetting.values()[setting]);
			updateClanBoard();
			break;
		}
		return true;
	}
	
}
