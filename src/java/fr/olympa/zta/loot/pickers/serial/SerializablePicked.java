package fr.olympa.zta.loot.pickers.serial;

import fr.olympa.api.common.observable.Observable;

public interface SerializablePicked extends Observable {
	
	public SerializableType getSerialType();
	
	public String save();
	
	public default String serialize() {
		return getSerialType().getID() + "::" + save();
	}
	
}
