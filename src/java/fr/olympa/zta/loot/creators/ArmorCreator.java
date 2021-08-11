package fr.olympa.zta.loot.creators;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.zta.loot.RandomizedInventory.LootContext;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;

public class ArmorCreator implements LootCreator {

	private ArmorType type;
	private double bestThreshold;

	public ArmorCreator(ArmorType type) {
		this(type, 0);
	}
	
	public ArmorCreator(ArmorType type, double bestThreshold) {
		this.type = type;
		this.bestThreshold = bestThreshold;
	}

	@Override
	public Loot create(Random random, LootContext context) {
		ArmorSlot slot = null;
		if (context != null && bestThreshold > 0 && ThreadLocalRandom.current().nextDouble() <= bestThreshold) {
			PlayerInventory inventory = context.getPlayer().getInventory();
			List<ArmorSlot> slots = Arrays.stream(ArmorSlot.values())
					.filter(tempSlot -> {
						ItemStack item = inventory.getItem(tempSlot.getSlot());
						return item == null || item.getType() != type.getImmutable(tempSlot).getType();
					})
					.collect(Collectors.toList());
			if (!slots.isEmpty()) slot = slots.get(random.nextInt(slots.size()));
		}
		if (slot == null) slot = ArmorSlot.values()[random.nextInt(4)]; // pièce d'armure aléatoire
		return new Loot(type.get(slot));
	}
	
	@Override
	public String getTitle() {
		return type.getName() + " (1 pc)";
	}

}
