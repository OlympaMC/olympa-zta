package fr.olympa.zta.utils.quests;

import org.bukkit.Material;

import fr.olympa.api.item.ItemUtils;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectCreator;

public class BeautyQuestsLink {
	
	public static void initialize() {
		QuestsAPI.registerReward(new QuestObjectCreator<>(QuestItemReward.class, ItemUtils.item(Material.GOLD_INGOT, "§eOlympa ZTA - Item de quête"), QuestItemReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(MoneyItemReward.class, ItemUtils.item(Material.NETHER_BRICK, "§eOlympa ZTA - Billets de banque"), MoneyItemReward::new));
		QuestsAPI.registerReward(new QuestObjectCreator<>(ItemStackableReward.class, ItemUtils.item(Material.STICK, "§eOlympa ZTA - Item custom"), ItemStackableReward::new));
	}
	
}
