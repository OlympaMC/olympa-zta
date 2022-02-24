package fr.olympa.zta.loot.pickers;

import java.util.List;
import java.util.Map;

import fr.olympa.api.common.randomized.RandomValueProvider;
import fr.olympa.api.common.randomized.RandomizedFactory;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalContext;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.Conditioned;
import fr.olympa.api.common.randomized.RandomizedPickerBase.RandomizedMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.RandomizedPicker;

public class PickersFactoryZTA implements RandomizedFactory {
	
	@Override
	public <T> RandomizedPicker<T> newPicker(Map<T, Double> values, double emptyChance) {
		return new ZTAPicker.FixedPickerZTA<>(values, emptyChance);
	}
	
	@Override
	public <T> RandomizedMultiPicker<T> newMultiPicker(Map<T, Double> values, List<T> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance) {
		return new ZTAPicker.FixedMultiPickerZTA<>(values, valuesAlways, amountProvider, emptyChance);
	}
	
	@Override
	public <T, C extends ConditionalContext<T>> ConditionalPicker<T, C> newConditionalPicker(Map<Conditioned<T, C>, Double> values, double emptyChance) {
		return new ZTAPicker.FixedConditionalPickerZTA<>(values, emptyChance);
	}
	
	@Override
	public <T, C extends ConditionalContext<T>> ConditionalMultiPicker<T, C> newConditionalMultiPicker(Map<Conditioned<T, C>, Double> values, List<Conditioned<T, C>> valuesAlways, RandomValueProvider<Integer> amountProvider, double emptyChance) {
		return new ZTAPicker.FixedConditionalMultiPickerZTA<>(values, valuesAlways, amountProvider, emptyChance);
	}
	
}
