package fr.olympa.zta.settings;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;

public class PlayerSettingsGUI extends OlympaGUI {
	
	private static final int SLOT_CLAN_BOARD = 6;
	private static final int SLOT_QUESTS_BOARD = 4;
	private static final int SLOT_AMBIENT = 1;
	private OlympaPlayerZTA player;
	
	public PlayerSettingsGUI(OlympaPlayerZTA player) {
		super("Paramètres", 1);
		this.player = player;
		
		inv.setItem(SLOT_AMBIENT, ItemUtils.itemSwitch("Sons d'ambiance", player.parameterAmbient.get(), SpigotUtils.wrapAndAlign("Active les bruits de fond (tirs, explosions) pendant le jeu.", 35).toArray(String[]::new)));
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
