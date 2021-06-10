package fr.olympa.zta.utils.quests;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.utils.PhysicalMoney;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;

public class MoneyItemReward extends AbstractReward {
	
	private int amount = -1;
	
	public MoneyItemReward() {
		super("ztaMoneyItem");
	}
	
	public MoneyItemReward(int amount) {
		this();
		this.amount = amount;
	}
	
	@Override
	public List<String> give(Player p) {
		PhysicalMoney.give(p, amount);
		return Arrays.asList(OlympaMoney.format(amount));
	}
	
	@Override
	public AbstractReward clone() {
		return new MoneyItemReward(amount);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + OlympaMoney.format(amount) };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.CHOOSE_MONEY_REWARD.send(p);
		new TextEditor<>(p, () -> {
			if (amount == -1) gui.remove(MoneyItemReward.this);
			gui.reopen();
		}, obj -> {
			amount = obj;
			ItemUtils.lore(clicked, getLore());
			gui.reopen();
		}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enterOrLeave(p);
	}
	
	@Override
	protected void load(Map<String, Object> map) {
		amount = (int) map.get("amount");
	}
	
	@Override
	protected void save(Map<String, Object> map) {
		map.put("amount", amount);
	}
	
}