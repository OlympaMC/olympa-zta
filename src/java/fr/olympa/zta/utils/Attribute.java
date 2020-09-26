package fr.olympa.zta.utils;

import java.util.HashMap;
import java.util.Map;

public class Attribute{
	
	private float baseValue;
	private final Map<String, AttributeModifier> modifiers = new HashMap<>();
	
	private float cachedValue = -666;

	public Attribute(float baseValue){
		this.baseValue = baseValue;
	}
	
	public float getBaseValue(){
		return baseValue;
	}
	
	public void setBaseValue(float newValue) {
		this.baseValue = newValue;
	}
	
	public void addModifier(AttributeModifier modifier){
		modifiers.put(modifier.getName(), modifier);
		cachedValue = -666;
	}
	
	public void removeModifier(String name) {
		modifiers.remove(name);
		cachedValue = -666;
	}
	
	public float getValue(){
		if (cachedValue != -666) return cachedValue;
		if (modifiers.isEmpty()) return cachedValue = baseValue;
		float value = baseValue;
		float multiplicator = 1;
		for (AttributeModifier modifier : modifiers.values()) {
			switch (modifier.getOperation()){
			case ADD_NUMBER:
				value += modifier.getAmount();
				break;
			case MULTIPLY_VALUE:
				value *= modifier.getAmount();
				break;
			case ADD_MULTIPLICATOR:
				multiplicator += modifier.getAmount();
				break;
			}
		}
		value *= multiplicator;
		cachedValue = value;
		return value;
	}
	
}
