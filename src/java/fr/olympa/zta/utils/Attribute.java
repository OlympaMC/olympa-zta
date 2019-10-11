package fr.olympa.zta.utils;

import java.util.ArrayList;
import java.util.List;

public class Attribute{
	
	private final float baseValue;
	private final List<AttributeModifier> modifiers = new ArrayList<>(3);
	
	public Attribute(float baseValue){
		this.baseValue = baseValue;
	}
	
	public float getBaseValue(){
		return baseValue;
	}
	
	public List<AttributeModifier> getModifiers(){
		return modifiers;
	}
	
	public void addModifier(AttributeModifier modifier){
		modifiers.add(modifier);
	}
	
	public void removeModifier(AttributeModifier modifier){
		modifiers.remove(modifier);
	}
	
	public float getValue(){
		if (modifiers.isEmpty()) return baseValue;
		float value = baseValue;
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
		return value * multiplicator;
	}
	
}
