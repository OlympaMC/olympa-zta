package fr.olympa.zta.lootchests;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.AbstractRandomizedPicker;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.lootchests.creators.LootCreator;
import fr.olympa.zta.registry.Registrable;

public class LootChest extends AbstractRandomizedPicker<LootCreator> implements Registrable {
	
	public static List<LootCreator> creatorsSimple = new ArrayList<>();
	public static List<LootCreator> creatorsAlways = new ArrayList<>();

	private final int id = super.random.nextInt();

	private int minutsToWait = 60;
	private long nextOpen = 0;

	public void click(Player p) {
		long time = System.currentTimeMillis();
		if (time < nextOpen) {
			int minuts = (int) Math.ceil((nextOpen - time) / 60000);
			p.sendMessage(Prefix.BAD + "Vous devez encore attendre " + minuts + " minutes pour ouvrir ce coffre.");
			return;
		}
		nextOpen = time + minutsToWait * 60000;

		for (LootCreator creator : pick()) {
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
		return creatorsSimple;
	}

	public List<LootCreator> getAlwaysObjectList() {
		return creatorsAlways;
	}

	public static void addLootCreator(LootCreator creator) {
		if (creator.getChance() != -1) {
			creatorsSimple.add(creator);
		}else creatorsAlways.add(creator);
	}

}
