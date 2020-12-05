package fr.olympa.zta.utils.quests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.customevents.ScoreboardCreateEvent;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.lines.TimerLine;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.utils.spigot.SpigotUtils;
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
import fr.skytasul.quests.players.events.PlayerAccountJoinEvent;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Utils;

public class BeautyQuestsLink implements Listener {
	
	private Map<Player, Integer> scoreboards = new HashMap<>();
	private TimerLine<Scoreboard<OlympaPlayerZTA>> line = new TimerLine<Scoreboard<OlympaPlayerZTA>>(scoreboard -> {
		Player player = scoreboard.getOlympaPlayer().getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);
		List<Quest> started = QuestsAPI.getQuestsStarteds(acc, true);
		int id = scoreboards.get(player).intValue();
		if (id >= started.size()) id = 0;
		Quest quest = started.get(id++);
		scoreboards.put(player, id);
		return String.join("\n", SpigotUtils.wordWrap("\n§7Mission: §6§l" + quest.getName() + "\n§7" + quest.getBranchesManager().getPlayerBranch(acc).getDescriptionLine(acc, Source.SCOREBOARD), 30));
	}, OlympaZTA.getInstance(), 100);
	
	public BeautyQuestsLink() {
		QuestsAPI.setHologramsManager(new BeautyQuestsHolograms());
		QuestsAPI.registerReward(new QuestObjectCreator<>(QuestItemReward.class, ItemUtils.item(Material.GOLD_INGOT, "§eOlympa ZTA - Item de quête"), QuestItemReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(MoneyItemReward.class, ItemUtils.item(Material.NETHER_BRICK, "§eOlympa ZTA - Billets de banque"), MoneyItemReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(GunReward.class, ItemUtils.item(Material.STICK, "§eOlympa ZTA - Item custom"), GunReward::new));
		QuestsAPI.registerRequirement(new QuestObjectCreator<>(OlympaRegionRequirement.class, ItemUtils.item(Material.PAPER, "§eOlympa ZTA - région"), OlympaRegionRequirement::new));
		QuestsAPI.registerMobFactory(new ZTAMobFactory());
	}
	
	public boolean isQuestItem(ItemStack item) {
		return Utils.isQuestItem(item);
	}
	
	@EventHandler
	public void onBQLoad(PlayerAccountJoinEvent e) {
		checkScoreboard(e.getPlayer(), false);
	}
	
	@EventHandler
	public void onQuestStart(QuestLaunchEvent e) {
		checkScoreboard(e.getPlayer(), false);
	}
	
	@EventHandler
	public void onQuestFinish(QuestFinishEvent e) {
		checkScoreboard(e.getPlayer(), false);
	}
	
	@EventHandler
	public void onQuestReset(PlayerQuestResetEvent e) {
		checkScoreboard(e.getPlayerAccount().getPlayer(), false);
	}
	
	@EventHandler
	public void onQuestSetStage(PlayerSetStageEvent e) {
		checkScoreboard(e.getPlayerAccount().getPlayer(), false);
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onScoreboardCreate(ScoreboardCreateEvent<OlympaPlayerZTA> e) {
		checkScoreboard(e.getPlayer(), true);
	}
	
	private void checkScoreboard(Player p, boolean forceCreation) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (acc == null) return;
		if (QuestsAPI.getQuestsStarteds(acc, true).size() >= 1) {
			if (!forceCreation && scoreboards.containsKey(p)) return;
			scoreboards.putIfAbsent(p, 0);
			OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
			Scoreboard<OlympaPlayerZTA> scoreboard = OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(player);
			if (scoreboard == null) return;
			
			scoreboard.addLine(line);
		}else {
			if (!scoreboards.containsKey(p)) return;
			scoreboards.remove(p);
			OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
			OlympaZTA.getInstance().scoreboards.create(player); // reset le scoreboard
		}
	}
	
}
