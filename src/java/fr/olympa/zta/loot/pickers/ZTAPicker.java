package fr.olympa.zta.loot.pickers;

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

public interface ZTAPicker<T> extends RandomizedPickerBase<T> {
	
	public String getName();
	
	public void setName(String name);
	
	public void save(JsonObject json);
	
	public void load(JsonObject json);
	
	public default String serialize() {
		JsonObject json = new JsonObject();
		json.addProperty("type", getClass().getSimpleName());
		save(json);
		return LinkSpigotBungee.getInstance().getGson().toJson(json);
	}
	
	public default void load(String string) {
		JsonObject json = LinkSpigotBungee.getInstance().getGson().fromJson(string, JsonObject.class);
		String type = json.get("type").getAsString();
		if (!type.equals(getClass().getSimpleName())) {
			new IllegalArgumentException("Incompatible type " + type + " with " + getClass().getSimpleName()).printStackTrace();
		}else {
			load(json);
		}
	}
	
	public class FixedPickerZTA<T> extends RandomizedPickerBase.FixedPicker<T> implements ZTAPicker<T> {
		
		private String name;
		
		public FixedPickerZTA(Map<T, Double> values, double emptyChance) {
			super(values, emptyChance);
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public void setName(String name) {
			this.name = name;
		}
		
		@Override
		public void save(JsonObject json) {
			json.addProperty("emptyChance", super.emptyChance);
			
			Map<String, Double> serialized = super.values.entrySet().stream().flatMap(x -> x.getKey()instanceof SerializablePicked picked ? Stream.of(Map.entry(picked.serialize(), x.getValue())) : Stream.empty()).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			json.add("values", LinkSpigotBungee.getInstance().getGson().toJsonTree(serialized));
		}
		
		@Override
		public void load(JsonObject json) {
			super.emptyChance = json.get("emptyChance").getAsDouble();
			
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
			super.values = (Map<T, Double>) newValues;
		}
		
	}
	
	public class FixedMultiPickerZTA<T> extends RandomizedPickerBase.FixedMultiPicker<T> implements ZTAPicker<T> {
		
		private String name;

		public FixedMultiPickerZTA(Map<T, Double> values, List<T> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance) {
			super(values, valuesAlways, amountProvider, emptyChance);
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	public class FixedConditionalPickerZTA<T, C extends ConditionalContext<T>> extends RandomizedPickerBase.FixedConditionalPicker<T, C> implements ZTAPicker<T> {
		
		private String name;
		
		public FixedConditionalPickerZTA(Map<Conditioned<T, C>, Double> values, double emptyChance) {
			super(values, emptyChance);
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	public class FixedConditionalMultiPickerZTA<T, C extends ConditionalContext<T>> extends RandomizedPickerBase.FixedConditionalMultiPicker<T, C> implements ZTAPicker<T> {
		
		private String name;
		
		public FixedConditionalMultiPickerZTA(Map<Conditioned<T, C>, Double> values, List<Conditioned<T, C>> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance) {
			super(values, valuesAlways, amountProvider, emptyChance);
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
}
