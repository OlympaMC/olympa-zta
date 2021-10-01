package fr.olympa.zta.loot.pickers.serial;

import java.util.HashMap;
import java.util.Map;

public class SerializableManager {
	
	private Map<String, SerializableType> types = new HashMap<>();
	
	public SerializableManager() {}
	
	public void addSerializableType(SerializableType type) {
		types.put(type.getID(), type);
	}
	
	public SerializablePicked deserialize(String string) {
		int index = string.indexOf("::");
		String typeID = string.substring(0, index);
		SerializableType type = types.get(typeID);
		if (type == null) throw new IllegalArgumentException("Unknown serializable type " + typeID);
		return type.load(string.substring(index + 2));
	}
	
}
