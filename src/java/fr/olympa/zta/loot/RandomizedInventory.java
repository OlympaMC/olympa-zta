package fr.olympa.zta.loot;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalContext;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBuilder;
import fr.olympa.api.common.randomized.RandomizedPickerBuilder.IConditionalBuilder;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.creators.ItemStackableCreator;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.LootCreator.Loot;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.GunType;

public abstract class RandomizedInventory extends OlympaGUI {

	private Map<Integer, Loot> currentLoots = new HashMap<>();
	
	public RandomizedInventory(String name, InventoryType type) {
		super(name, type);
	}
	
	public RandomizedInventory(String name, int rows) {
		super(name, rows);
	}
	
	protected void fillInventory(@Nullable Player player) {
		clearInventory();
		LootContext context = new LootContext(player);
		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (LootCreator creator : getLootPicker().pickMulti(random, context)) {
			int slot;
			do {
				slot = random.nextInt(inv.getSize());
			}while (inv.getItem(slot) != null);

			Loot loot = creator.create(random, context);
			currentLoots.put(slot, loot);
			inv.setItem(slot, loot.getItem());
		}
	}
	
	protected void clearInventory() {
		currentLoots.values().forEach(Loot::onRemove);
		currentLoots.clear();
		inv.clear();
	}
	
	protected abstract ConditionalMultiPicker<LootCreator, LootContext> getLootPicker();
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) return true;
		Loot loot = currentLoots.get(slot);
		if (loot == null) {
			OlympaZTA.getInstance().sendMessage("Pas de loot au slot %d, item %s, pour %s.", slot, current.getType().name(), p.getName());
			Prefix.ERROR.sendMessage(p, "Une erreur est survenue avec ce loot.");
			inv.setItem(slot, null);
			return true;
		}
		if (click.isShiftClick()) {
			if (p.getInventory().firstEmpty() == -1) {
				boolean valid = false;
				if (loot.isStackable() && current.getMaxStackSize() > 1) {
					int amount = current.getAmount();
					for (ItemStack item : p.getInventory().getStorageContents()) {
						if (item.isSimilar(current)) {
							amount -= item.getMaxStackSize() - item.getAmount(); // remove available amount in this slot
							if (amount <= 0) {
								valid = true;
								break;
							}
						}
					}
				}
				if (!valid) {
					Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a pas d'espace pour cet item dans ton inventaire...");
					return true;
				}
			}
		}
		ItemStack realItem = loot.getRealItem();
		if (realItem != null) inv.setItem(slot, realItem);
		if (click != ClickType.RIGHT || current.getAmount() == 1) currentLoots.remove(slot);
		return false;
	}

	@Override
	public boolean noDoubleClick() {
		return true;
	}
	
	public boolean onMoveItem(Player p, ItemStack moved, boolean isFromPlayerInv, int slot) {
		return isFromPlayerInv;
	}
	
	public static class LootContext extends ConditionalContext<LootCreator> {
		
		private final @Nullable Player player;
		
		private List<GunType> carriedGuns;
		
		public LootContext(@Nullable Player player) {
			this.player = player;
		}
		
		public @Nullable Player getPlayer() {
			return player;
		}
		
		public List<GunType> getCarriedGuns() {
			if (carriedGuns == null) {
				if (player == null) {
					carriedGuns = Collections.emptyList();
				}else {
					carriedGuns = Arrays.stream(player.getInventory().getStorageContents()).map(item -> OlympaZTA.getInstance().gunRegistry.getGun(item)).filter(Objects::nonNull).map(Gun::getType).collect(Collectors.toList());
				}
			}
			return carriedGuns;
		}
		
		@Override
		public void addPicked(LootCreator picked) {
			super.addPicked(picked);
			if (player == null) return;
			if (picked instanceof ItemStackableCreator creator) {
				if (creator.getStackable()instanceof GunType gun) {
					getCarriedGuns().add(gun);
				}
			}
		}
		
	}
	
	public static IConditionalBuilder<LootCreator, LootContext> newBuilder() {
		return RandomizedPickerBuilder.newConditionalBuilder();
	}
	
}
