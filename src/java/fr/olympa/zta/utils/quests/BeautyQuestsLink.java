package fr.olympa.zta.utils.quests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.customevents.ScoreboardCreateEvent;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.linesTimer.Line;
import fr.olympa.api.spigot.scoreboard.sign.Scoreboard;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.PlayerQuestResetEvent;
import fr.skytasul.quests.api.events.PlayerSetStageEvent;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.stages.StageType;
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
		String questName = quest.isRepeatable() ? "\n§7§lMission quotidienne:" : ("\n§7Mission: §6§l" + quest.getName());
		return String.join("\n", SpigotUtils.wordWrap(questName + "\n§7" + quest.getBranchesManager().getDescriptionLine(acc, Source.SCOREBOARD), 35));
	}, OlympaZTA.getInstance(), 100);
	
	public BeautyQuestsLink() {
		QuestsAPI.setHologramsManager(new BeautyQuestsHolograms());
		QuestsAPI.registerReward(new QuestObjectCreator<>(QuestItemReward.class, ItemUtils.item(Material.GOLD_INGOT, "§eOlympa ZTA - Item de quête"), QuestItemReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(MoneyItemReward.class, ItemUtils.item(Material.NETHER_BRICK, "§eOlympa ZTA - Billets de banque"), MoneyItemReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(GunReward.class, ItemUtils.item(Material.STICK, "§eOlympa ZTA - Arme"), GunReward::new));
		QuestsAPI.registerRequirement(new QuestObjectCreator<>(OlympaRegionRequirement.class, ItemUtils.item(Material.PAPER, "§eOlympa ZTA - région"), OlympaRegionRequirement::new));
		QuestsAPI.registerMobFactory(new ZTAMobFactory());
		QuestsAPI.registerStage(new StageType<>("ztaRegion", ZTARegionStage.class, "Trouver une région Olympa", ZTARegionStage::deserialize, ItemUtils.item(Material.WOODEN_AXE, "Trouver une région Olympa"), ZTARegionStage.Creation::new));
		QuestsAPI.registerStage(new StageType<>("ztaItems", ItemStackableStage.class, "Rapporter des items Olympa", ItemStackableStage::deserialize, ItemUtils.item(Material.NETHER_BRICK, "Rapporter des items Olympa"), ItemStackableStage.Creation::new));
		new BQCommand().register();
		
		OlympaGroup.PLAYER.setRuntimePermission("beautyquests.command", false);
		OlympaGroup.RESP_TECH.setRuntimePermission("beautyquests.*", true);
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
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		scoreboards.remove(e.getPlayer());
	}
	
	private synchronized void checkScoreboard(Player p, boolean forceCreation) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (acc == null) return;
		OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
		if (!player.parameterQuestsBoard.get()) return;
		
		if (!QuestsAPI.getQuestsStarteds(acc, true).isEmpty()) {
			if (!forceCreation && scoreboards.containsKey(p)) return;
			scoreboards.putIfAbsent(p, 0);
			Scoreboard<OlympaPlayerZTA> scoreboard = OlympaZTA.getInstance().scoreboards.getPlayerScoreboard(player);
			if (scoreboard == null) return;
			
			scoreboard.addLine(line);
		}else {
			if (!scoreboards.containsKey(p)) return;
			scoreboards.remove(p);
			OlympaZTA.getInstance().scoreboards.refresh(player); // reset le scoreboard
		}
	}
	
	public void updateBoardParameter(OlympaPlayerZTA player, boolean enabled) {
		if (!enabled) {
			if (scoreboards.containsKey(player.getPlayer())) {
				scoreboards.remove(player.getPlayer());
				OlympaZTA.getInstance().scoreboards.refresh(player);
			}
		}else {
			if (!scoreboards.containsKey(player.getPlayer())) {
				checkScoreboard(player.getPlayer(), true);
			}
		}
	}
	
}
