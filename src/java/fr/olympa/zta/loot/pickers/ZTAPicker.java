package fr.olympa.zta.loot.pickers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.randomized.RandomValueProvider;
import fr.olympa.api.common.randomized.RandomizedPickerBase;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.pickers.serial.SerializablePicked;

public abstract class ZTAPicker<T> implements RandomizedPickerBase<T> {
	
	private String name;
	private Class<? extends SerializablePicked> serialClass;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Class<? extends SerializablePicked> getSerialClass() {
		return serialClass;
	}
	
	public void setSerialClass(Class<? extends SerializablePicked> serialClass) {
		this.serialClass = serialClass;
	}
	
	public abstract List<String> getDescription();
	
	public abstract void save(JsonObject json);
	
	public abstract void load(JsonObject json);
	
	public String serialize() {
		JsonObject json = new JsonObject();
		json.addProperty("type", getClass().getSimpleName());
		save(json);
		return LinkSpigotBungee.getInstance().getGson().toJson(json);
	}
	
	public void load(String string) {
		System.out.println("loading " + getName() + " with " + string);
		JsonObject json = LinkSpigotBungee.getInstance().getGson().fromJson(string, JsonObject.class);
		String type = json.get("type").getAsString();
		if (!type.equals(getClass().getSimpleName())) {
			new IllegalArgumentException("Incompatible type " + type + " with " + getClass().getSimpleName()).printStackTrace();
		}else {
			load(json);
		}
	}
	
	public abstract static class ZTAConditionalPicker<T, C extends ConditionalContext<T>> extends ZTAPicker<T> implements RandomizedPickerBase.ConditionalPicker<T, C> {
		

	}
	
	public static class FixedPickerZTA<T> extends ZTAPicker<T> implements RandomizedPickerBase.RandomizedPicker<T> {
		
		protected Map<T, Double> values;
		protected double emptyChance;
		
		public FixedPickerZTA(Map<T, Double> values, double emptyChance) {
			this.values = values;
			this.emptyChance = emptyChance;
		}
		
		@Override
		public Map<T, Double> getObjectList() {
			return values;
		}
		
		@Override
		public double getEmptyChance() {
			return emptyChance;
		}
		
		@Override
		public List<String> getDescription() {
			return Arrays.asList(values.size() + " values", "Empty chance: " + emptyChance);
		}
		
		@Override
		public void save(JsonObject json) {
			json.addProperty("emptyChance", emptyChance);
			
			Map<String, Double> serialized = values.entrySet().stream().flatMap(x -> x.getKey()instanceof SerializablePicked picked ? Stream.of(Map.entry(picked.serialize(), x.getValue())) : Stream.empty()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			json.add("values", LinkSpigotBungee.getInstance().getGson().toJsonTree(serialized));
		}
		
		@Override
		public void load(JsonObject json) {
			emptyChance = json.get("emptyChance").getAsDouble();
			
			Map<?, ?> serialized = LinkSpigotBungee.getInstance().getGson().fromJson(json.get("values"), Map.class);
			Map<SerializablePicked, Double> newValues = serialized.entrySet().stream().flatMap(x -> {
				if (x.getKey()instanceof String data) {
					SerializablePicked picked = OlympaZTA.getInstance().pickers.serializable.deserialize(data);
					if (x.getValue()instanceof Number chance) {
						return Stream.of(Map.entry(picked, chance.doubleValue()));
					}else OlympaZTA.getInstance().sendMessage("§cMauvaise donnée \"chance\": %s", x.getValue());
				}else OlympaZTA.getInstance().sendMessage("Mauvaise donnée \"picker\": %s", x.getKey());
				return Stream.empty();
			}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			values = (Map<T, Double>) newValues;
		}
		
	}
	
	public static class FixedMultiPickerZTA<T> extends FixedPickerZTA<T> implements RandomizedPickerBase.RandomizedMultiPicker<T> {
		
		protected List<T> valuesAlways;
		protected RandomValueProvider<Integer> amountProvider;

		public FixedMultiPickerZTA(Map<T, Double> values, List<T> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance) {
			super(values, emptyChance);
			this.valuesAlways = valuesAlways;
			this.amountProvider = amountProvider;
		}
		
		@Override
		public List<T> getAlwaysObjectList() {
			return valuesAlways;
		}
		
		@Override
		public RandomValueProvider<Integer> getAmountProvider() {
			return amountProvider;
		}
		
		@Override
		public List<String> getDescription() {
			return Arrays.asList(values.size() + " values", valuesAlways.size() + " always values", "Empty chance: " + emptyChance, "Amount provider: " + amountProvider.getClass().getSimpleName());
		}
		
		@Override
		public void save(JsonObject json) {
			super.save(json);
			
			json.add("valuesAlways", LinkSpigotBungee.getInstance().getGson().toJsonTree(valuesAlways.stream().flatMap(x -> x instanceof SerializablePicked picked ? Stream.of(picked.serialize()) : Stream.empty()).toList()));
		}
		
		@Override
		public void load(JsonObject json) {
			super.load(json);
			
			// TODO
		}
		
	}
	
	public static class FixedConditionalPickerZTA<T, C extends ConditionalContext<T>> extends ZTAConditionalPicker<T, C> implements RandomizedPickerBase.ConditionalPicker<T, C> {
		
		protected Map<Conditioned<T, C>, Double> values;
		protected double emptyChance;
		
		public FixedConditionalPickerZTA(Map<Conditioned<T, C>, Double> values, double emptyChance) {
			this.values = values;
			this.emptyChance = emptyChance;
		}
		
		@Override
		public Map<Conditioned<T, C>, Double> getConditionedObjectsList() {
			return values;
		}
		
		@Override
		public double getEmptyChance() {
			return emptyChance;
		}
		
		@Override
		public List<String> getDescription() {
			return Arrays.asList(values.size() + " values", "Empty chance: " + emptyChance);
		}

		@Override
		public void save(JsonObject json) {
			// TODO
		}

		@Override
		public void load(JsonObject json) {
			// TODO
		}
		
	}
	
	public static class FixedConditionalMultiPickerZTA<T, C extends ConditionalContext<T>> extends FixedConditionalPickerZTA<T, C> implements RandomizedPickerBase.ConditionalMultiPicker<T, C> {
		
		private List<Conditioned<T, C>> valuesAlways;
		private RandomValueProvider<Integer> amountProvider;
		
		public FixedConditionalMultiPickerZTA(Map<Conditioned<T, C>, Double> values, List<Conditioned<T, C>> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance) {
			super(values, emptyChance);
			this.valuesAlways = valuesAlways;
			this.amountProvider = amountProvider;
		}
		
		@Override
		public RandomValueProvider<Integer> getAmountProvider() {
			return amountProvider;
		}
		
		@Override
		public List<Conditioned<T, C>> getConditionedAlwaysObjects() {
			return valuesAlways;
		}
		
		@Override
		public List<String> getDescription() {
			return Arrays.asList(values.size() + " values", valuesAlways.size() + " always values", "Empty chance: " + emptyChance, "Amount provider: " + amountProvider.getClass().getSimpleName());
		}

		@Override
		public void save(JsonObject json) {
			super.save(json);
			
			// TODO
		}

		@Override
		public void load(JsonObject json) {
			super.load(json);
			
			// TODO
		}
		
	}
	
}
