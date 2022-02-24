package fr.olympa.zta.loot.creators;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.olympa.api.common.randomized.RandomizedPickerBase.Conditioned;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.loot.RandomizedInventory.LootContext;
import fr.olympa.zta.weapons.WeaponsListener;
import fr.olympa.zta.weapons.guns.GunType;
import fr.olympa.zta.weapons.skins.Skin;
import fr.olympa.zta.weapons.skins.Skinable;

public class ItemStackableCreator implements LootCreator {

	private ItemStackable stackable;
	private @Nullable Player player;
	
	public ItemStackableCreator(ItemStackable stackable) {
		this.stackable = stackable;
	}

	@Override
	public Loot create(Random random, LootContext context) {
		player = context.getPlayer();
		return new StackableLoot();
	}
	
	@Override
	public String getTitle() {
		return stackable.getName();
	}
	
	public ItemStackable getStackable() {
		return stackable;
	}

	class StackableLoot extends Loot {

		public StackableLoot() {
			super(stackable.getDemoItem());
		}
		
		@Override
		public ItemStack getRealItem() {
			ItemStack item = stackable.createItem();
			try {
				if (player != null) {
					Skinable skinable = null;
					if (stackable instanceof Skinable x) {
						skinable = x;
					}else if (WeaponsListener.getWeapon(item)instanceof Skinable x) {
						skinable = x;
					}
					if (skinable != null) {
						skinable.setSkin(Skin.getAvailable(OlympaPlayerZTA.get(player)), item);
					}
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
			return item;
		}
		
		@Override
		public boolean isStackable() {
			return !(stackable instanceof GunType);
		}

	}
	
	public static class GunConditionned implements Conditioned<LootCreator, LootContext> {
		
		private ItemStackableCreator creator;
		private double scalar;
		private double min;
		
		public GunConditionned(GunType gun, double scalar, double min) {
			this.creator = new ItemStackableCreator(gun);
			this.scalar = scalar;
			this.min = min;
		}
		
		@Override
		public LootCreator getObject() {
			return creator;
		}
		
		@Override
		public boolean isValid(LootContext context) {
			double odds = Math.max(min, 1D - scalar * context.getCarriedGuns().size());
			return ThreadLocalRandom.current().nextDouble() <= odds;
		}
		
		@Override
		public boolean isValidWithNoContext() {
			return true;
		}
		
	}

}
