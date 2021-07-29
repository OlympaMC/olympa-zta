package fr.olympa.zta.loot.creators;
import java.util.Random;

import fr.olympa.api.utils.Utils;
import fr.olympa.zta.itemstackable.QuestItem;
import fr.olympa.zta.loot.RandomizedInventory.LootContext;

public class QuestItemCreator implements LootCreator {

	private QuestItem questItem;
	private int min;
	private int max;

	public QuestItemCreator(QuestItem questItem) {
		this(questItem, 1, 1);
	}
	
	public QuestItemCreator(QuestItem questItem, int min, int max) {
		this.questItem = questItem;
		this.min = min;
		this.max = max;
	}

	@Override
	public Loot create(Random random, LootContext context) {
		return new Loot(questItem.getItem(Utils.getRandomAmount(random, min, max)));
	}
	
	@Override
	public String getTitle() {
		return questItem.getName();
	}

}
