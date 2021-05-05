package fr.olympa.zta.loot.creators;
import java.util.Random;

import fr.olympa.zta.itemstackable.QuestItem;

public class QuestItemCreator implements LootCreator {

	private double chance;
	private QuestItem questItem;

	public QuestItemCreator(double chance, QuestItem questItem) {
		this.chance = chance;
		this.questItem = questItem;
	}
	
	@Override
	public double getChance() {
		return chance;
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
