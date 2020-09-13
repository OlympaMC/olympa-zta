package fr.olympa.zta.utils.quests;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.lootchests.creators.QuestItemCreator.QuestItem;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;

public class QuestItemReward extends AbstractReward {
	
	private QuestItem item;
	private int amount;
	
	public QuestItemReward() {
		super("ztaItem");
	}
	
	public QuestItemReward(QuestItem item, int amount) {
		this();
		this.item = item;
		this.amount = amount;
	}
	
	@Override
	public String give(Player p) {
		SpigotUtils.giveItems(p, item.getItem(amount));
		return amount + " " + item.getName();
	}
	
	@Override
	public AbstractReward clone() {
		return new QuestItemReward(item, amount);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + (item == null ? null : item.getName()), Lang.Amount.format(amount) };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new PagedGUI<QuestItem>("Liste des items de quête", DyeColor.ORANGE, Arrays.asList(QuestItem.values())) {
			
			@Override
			public ItemStack getItemStack(QuestItem object) {
				return object.getOriginalItem();
			}
			
			@Override
			public void click(QuestItem existing) {
				Lang.CHOOSE_ITEM_AMOUNT.send(p);
				new TextEditor<>(p, () -> {
					if (item == null) gui.remove(QuestItemReward.this);
					gui.reopen();
				}, obj -> {
					item = existing;
					amount = obj;
					ItemUtils.lore(clicked, getLore());
					gui.reopen();
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enterOrLeave(p);
			}
			
		}.create(p);
	}
	
	@Override
	protected void load(Map<String, Object> map) {
		item = QuestItem.valueOf((String) map.get("item"));
		amount = (int) map.get("amount");
	}
	
	@Override
	protected void save(Map<String, Object> map) {
		map.put("item", item.name());
		map.put("amount", amount);
	}
	
}