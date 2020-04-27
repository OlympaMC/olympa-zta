package fr.olympa.zta.utils;

public class AttributeModifier{
	
	private String name;
	private Operation operation;
	private float amount;
	
	public AttributeModifier(String name, Operation operation, float amount){
		this.name = name;
		this.operation = operation;
		this.amount = amount;
	}
	
	public String getName(){
		return name;
	}
	
	public Operation getOperation(){
		return operation;
	}
	
	public float getAmount(){
		return amount;
	}
	
	public enum Operation{
		ADD_NUMBER, ADD_MULTIPLICATOR, MULTIPLY_VALUE;
	}
	
	@Override
	public int hashCode() {
		int hash = name.hashCode();
		hash += 37 * operation.ordinal();
		hash += 37 * amount;
		return hash;
	}

}
