package fr.olympa.zta.loot.pickers.serial;

public interface SerializableType {
	
	public String getID();
	
	public SerializablePicked load(String name);
	
	public SerializablePicked create();
	
}
