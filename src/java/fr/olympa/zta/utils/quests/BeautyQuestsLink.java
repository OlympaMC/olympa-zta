package fr.olympa.zta.utils.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.PlayerQuestResetEvent;
import fr.skytasul.quests.api.events.PlayerSetStageEvent;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Utils;

public class BeautyQuestsLink implements Listener {
	
	private List<Player> scoreboards = new ArrayList<>();
	
	public BeautyQuestsLink() {
		QuestsAPI.registerReward(new QuestObjectCreator<>(QuestItemReward.class, ItemUtils.item(Material.GOLD_INGOT, "§eOlympa ZTA - Item de quête"), QuestItemReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(MoneyItemReward.class, ItemUtils.item(Material.NETHER_BRICK, "§eOlympa ZTA - Billets de banque"), MoneyItemReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(ItemStackableReward.class, ItemUtils.item(Material.STICK, "§eOlympa ZTA - Item custom"), ItemStackableReward::new));
		QuestsAPI.setHologramsManager(new BeautyQuestsHolograms());
	}
	
	public boolean isQuestItem(ItemStack item) {
		return Utils.isQuestItem(item);
	}
	
	@EventHandler
	public void onQuestStart(QuestLaunchEvent e) {
		checkScoreboard(e.getPlayer());
	}
	
	@EventHandler
	public void onQuestFinish(QuestFinishEvent e) {
		checkScoreboard(e.getPlayer());
	}
	
	@EventHandler
	public void onQuestReset(PlayerQuestResetEvent e) {
		checkScoreboard(e.getPlayerAccount().getPlayer());
	}
	
	@EventHandler
	public void onQuestSetStage(PlayerSetStageEvent e) {
		checkScoreboard(e.getPlayerAccount().getPlayer());
	}
	
	private void checkScoreboard(Player p) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (QuestsAPI.getQuestsStarteds(acc).size() > 1) {
			if (scoreboards.contains(p)) return;
			OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
			Scoreboard<OlympaPlayerZTA> scoreboard = OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(player);
			
			scoreboard.addLine(new TimerLine<Scoreboard<OlympaPlayerZTA>>(new Function<Scoreboard<OlympaPlayerZTA>, String>() {
				int id = 0;
				
				@Override
				public String apply(Scoreboard<OlympaPlayerZTA> scoreboard) {
					List<Quest> started = QuestsAPI.getQuestsStarteds(acc);
					if (id >= started.size()) id = 0;
					return "§7Quête" + (started.size() > 1 ? "s" : "") + "§e" + started.get(id++).getName();
				}
			}, OlympaZTA.getInstance(), 100));
		}else {
			if (!scoreboards.remove(p)) return;
			OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
			OlympaZTA.getInstance().scoreboards.create(player); // reset le scoreboard
		}
	}
	
}
