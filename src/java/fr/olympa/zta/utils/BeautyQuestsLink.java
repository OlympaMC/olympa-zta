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
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;

public class BeautyQuestsLink {
	
	public static void initialize() {
		QuestsAPI.registerReward(QuestItemReward.class, ItemUtils.item(Material.GOLD_INGOT, "§eOlympa ZTA - Item de quête"), QuestItemReward::new);
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
		public AbstractReward clone() {
			return new QuestItemReward(item);
		}
		
		@Override
		public String[] getLore() {
			return new String[] { "§8> §7" + item.getName() };
		}
		
		@Override
		public void itemClick(Player p, QuestObjectGUI gui, ItemStack clicked) {
			new PagedGUI<QuestItems>("Liste des items de quête", DyeColor.ORANGE, Arrays.asList(QuestItems.values())) {
				
				@Override
				public ItemStack getItemStack(QuestItems object) {
					return object.getItem();
				}
				
				@Override
				public void click(QuestItems existing) {
					item = existing;
					ItemUtils.lore(clicked, getLore());
					gui.reopen(p);
				}
				
			}.create(p);
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
	
}
