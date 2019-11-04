package fr.olympa.zta.lootchests;

import java.util.List;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.AbstractRandomizedPicker;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.registry.Registrable;

public class LootChest extends AbstractRandomizedPicker<LootCreator> implements Registrable {

	private final int id = super.random.nextInt();

	public LootChestType type = null;
	private int minutesToWait = 60;
	private long nextOpen = 0;

	public void click(Player p) {
		long time = System.currentTimeMillis();
		if (time < nextOpen) {
			int minutes = (int) Math.ceil((nextOpen - time) / 60000);
			p.sendMessage(Prefix.BAD + "Vous devez encore attendre " + minutes + " minutes pour ouvrir ce coffre.");
			return;
		}
		nextOpen = time + minutesToWait * 60000;

		for (LootCreator creator : pick()) {
			System.out.println(creator.getClass().getName() + " " + creator.getChance());
			creator.give(p, super.random);
		}
	}

	public int getID() {
		return id;
	}

	public int getMinItems() {
		return 1;
	}

	public int getMaxItems() {
		return 4;
	}

	public List<LootCreator> getObjectList() {
		return type.getCreatorsSimple();
	}

	public List<LootCreator> getAlwaysObjectList() {
		return type.getCreatorsAlways();
	}

}
