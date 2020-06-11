package fr.olympa.zta.utils;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.lootchests.creators.QuestItemCreator.QuestItems;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreationRunnables;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;

public class BeautyQuestsLink {

	public static void initialize() {
		QuestsAPI.registerReward(QuestItemReward.class, ItemUtils.item(Material.GOLD_INGOT, "§eOlympa ZTA - Item de quête"), new QuestItemRunnables());
	}

	public static class QuestItemReward extends AbstractReward {

		private QuestItems item;

		public QuestItemReward() {
			super("ztaItem");
		}

		public QuestItemReward(QuestItems item) {
			this();
			this.item = item;
		}

		@Override
		public String give(Player p) {
			SpigotUtils.giveItems(p, item.getItem());
			return null;
		}

		@Override
		protected void load(Map<String, Object> map) {
			item = QuestItems.valueOf((String) map.get("item"));
		}

		@Override
		protected void save(Map<String, Object> map) {
			map.put("item", item.name());
		}

	}

	public static class QuestItemRunnables implements RewardCreationRunnables<QuestItemReward> {

		@Override
		public void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked) {
			new PagedGUI<QuestItems>("Liste des items de quête", DyeColor.ORANGE, Arrays.asList(QuestItems.values())) {

				@Override
				public ItemStack getItemStack(QuestItems object) {
					return object.getItem();
				}

				@Override
				public void click(QuestItems existing) {
					datas.put("item", existing);
					gui.reopen(p, true);
				}

			}.create(p);
		}

		@Override
		public void edit(Map<String, Object> datas, QuestItemReward reward, ItemStack item) {
			datas.put("item", reward.item);
		}

		@Override
		public QuestItemReward finish(Map<String, Object> datas) {
			return new QuestItemReward((QuestItems) datas.get("item"));
		}

	}

}
