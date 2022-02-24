package fr.olympa.zta.loot.creators;

import java.util.EnumMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import fr.olympa.api.common.randomized.RandomizedPickerBase;
import fr.olympa.api.common.randomized.RandomizedPickerBase.Conditioned;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.loot.RandomizedInventory.LootContext;
import fr.olympa.zta.loot.pickers.serial.SerializablePicked;
import fr.olympa.zta.weapons.guns.AmmoType;

public class AmmoCreator implements LootCreator/*, JsonSerializablePicked*/ {

	//private static final SerializableType SERIAL_TYPE = new LazySerialType("ammoCreator", data -> JsonSerializablePicked.load(data, AmmoCreator.class), AmmoCreator::create);
	
	private AmmoType type;
	private int min;
	private int max;
	private double filledChance;
	
	public AmmoCreator(int min, int max) {
		this(null, min, max, false);
	}
	
	public AmmoCreator(AmmoType type, int min, int max, boolean filled) {
		this(type, min, max, filled ? 1 : 0);
	}

	public AmmoCreator(AmmoType type, int min, int max, double filledChance) {
		this.type = type;
		this.min = min;
		this.max = max;
		this.filledChance = filledChance;
	}

	@Override
	public Loot create(Random random, LootContext context) {
		int amount = Utils.getRandomAmount(random, min, max);
		return new Loot(type == null ? AmmoType.getPowder(amount) : type.getAmmo(amount, random.nextDouble() <= filledChance));
	}
	
	@Override
	public String getTitle() {
		if (type == null) return "Poudre à canon";
		return type.getName() + (filledChance == 0 ? " vides" : "");
	}
	
	/*@Override
	public SerializableType getSerialType() {
		return SERIAL_TYPE;
	}*/
	
	/*@Override
	public String save() {
		StringJoiner joiner = new StringJoiner(";");
		joiner.add(type == null ? null : type.name());
		joiner.add(Integer.toString(min));
		joiner.add(Integer.toString(max));
		joiner.add(Double.toString(filledChance));
		return joiner.toString();
	}
	
	public static AmmoCreator load(String data) {
		String[] args = data.split(";");
		return new AmmoCreator(args[0].equals("null") ? null : AmmoType.valueOf(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]));
	}*/
	
	public static void create(Player p, Consumer<SerializablePicked> callback) {
		
	}
	
	public static class BestCreator implements LootCreator/*, JsonSerializablePicked */ {
		
		//private static final SerializableType BEST_SERIAL_TYPE = new LazySerialType("bestAmmoCreator", data -> JsonSerializablePicked.load(data, BestCreator.class));
		
		private int min;
		private int max;
		private int emptyMax;
		private double filledChance;
		private double powderChancePerGun;
		private double absentChancePerGun;
		
		public BestCreator(int min, int max, int emptyMax, double filledChance, double powderChancePerGun, double absentChancePerGun) {
			this.min = min;
			this.max = max;
			this.emptyMax = emptyMax;
			this.filledChance = filledChance;
			this.powderChancePerGun = powderChancePerGun;
			this.absentChancePerGun = absentChancePerGun;
		}
		
		@Override
		public Loot create(Random random, @Nullable LootContext context) {
			Validate.notNull(context);
			EnumMap<AmmoType, Double> types = new EnumMap<>(AmmoType.class);
			context.getCarriedGuns().forEach(gun -> types.merge(gun.getAmmoType(), 1D, (oldValue, value) -> oldValue + value));
			double empty = context.getCarriedGuns().size() * powderChancePerGun;
			if (absentChancePerGun > 0) {
				double absent = context.getCarriedGuns().size() * absentChancePerGun;
				for (AmmoType newType : AmmoType.values()) types.putIfAbsent(newType, absent);
			}
			AmmoType type = RandomizedPickerBase.pickWithEmpty(random, types, empty);
			int max = this.max;
			boolean filled = false;
			if (type != null) {
				filled = random.nextDouble() <= filledChance;
				if (!filled) max = emptyMax;
			}
			int amount = Utils.getRandomAmount(random, min, max);
			return new Loot(type == null ? AmmoType.getPowder(amount) : type.getAmmo(amount, filled));
		}
		
		@Override
		public String getTitle() {
			return "Munition aléatoire";
		}
		
		/*@Override
		public SerializableType getSerialType() {
			return BEST_SERIAL_TYPE;
		}*/
	}
	
	public static class AmmoConditionned implements Conditioned<LootCreator, LootContext> {

		private AmmoCreator ammo;
		private double threshold;
		
		public AmmoConditionned(AmmoCreator ammo, double threshold) {
			this.ammo = ammo;
			this.threshold = threshold;
		}
		
		@Override
		public LootCreator getObject() {
			return ammo;
		}

		@Override
		public boolean isValid(LootContext context) {
			if (threshold > 0 && ThreadLocalRandom.current().nextDouble() <= threshold) return true;
			return context.getCarriedGuns().stream().anyMatch(x -> x.getAmmoType() == ammo.type);
		}

		@Override
		public boolean isValidWithNoContext() {
			return true;
		}
		
	}

}
