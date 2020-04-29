package fr.olympa.zta.lootchests.creators;

import java.lang.reflect.Constructor;
import java.util.Random;

import org.bukkit.entity.Player;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.ZTARegistry;

public class ItemStackableCreator<T extends ItemStackable> implements LootCreator {

	private Constructor<T> constructor = null;
	private double chance;

	public ItemStackableCreator(double chance, Class<T> clazz) {
		this.chance = chance;
		try {
			constructor = clazz.getDeclaredConstructor(int.class);
		}catch (NoSuchMethodException | SecurityException e) {
			OlympaZTA.getInstance().sendMessage("Impossible d'instancier l'ItemStackable " + clazz.getSimpleName());
		}
	}

	public double getChance() {
		return chance;
	}

	public Loot create(Player p, Random random) {
		try {
			return new ItemStackableLoot(constructor.newInstance(ZTARegistry.generateID()));
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	}

	static class ItemStackableLoot extends Loot {

		private ItemStackable stackable;

		public ItemStackableLoot(ItemStackable stackable) {
			super(ZTARegistry.createItem(stackable));
			this.stackable = stackable;
		}

		@Override
		public void onRemove() {
			super.onRemove();
			ZTARegistry.removeObject(stackable);
		}

	}

}
