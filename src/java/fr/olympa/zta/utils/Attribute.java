package fr.olympa.zta.utils;

import java.util.ArrayList;
import java.util.List;

public class Attribute{
	
	private final float baseValue;
	private final List<AttributeModifier> modifiers = new ArrayList<>(3);
	
	private float cachedValue = -666;

	public Attribute(float baseValue){
		this.baseValue = baseValue;
	}
	
	public float getBaseValue(){
		return baseValue;
	}
	
	/*public List<AttributeModifier> getModifiers(){
		return modifiers;
	}*/
	
	public void addModifier(AttributeModifier modifier){
		modifiers.add(modifier);
		cachedValue = -666;
	}
	
	public void removeModifier(AttributeModifier modifier){
		modifiers.remove(modifier);
		cachedValue = -666;
	}
	
	public float getValue(){
		if (cachedValue != -666) return cachedValue;
		float value = baseValue;
		if (modifiers.isEmpty()) return value;
		float multiplicator = 1;
		for (AttributeModifier modifier : modifiers) {
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
