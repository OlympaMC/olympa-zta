package fr.olympa.zta.loot.creators;
import java.util.Random;

import fr.olympa.zta.itemstackable.QuestItem;

public class QuestItemCreator implements LootCreator {

	private QuestItem questItem;

	public QuestItemCreator(QuestItem questItem) {
		this.questItem = questItem;
	}

	@Override
	public Loot create(Random random) {
		return new Loot(questItem.getItem(1));
	}
	
	@Override
	public String getTitle() {
		return questItem.getName();
	}

}
