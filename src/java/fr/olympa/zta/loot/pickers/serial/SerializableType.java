package fr.olympa.zta.loot.pickers.serial;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

public interface SerializableType {
	
	public String getID();
	
	public Class<? extends SerializablePicked> getPickedClass();
	
	public SerializablePicked load(String data);
	
	public void create(Player p, Consumer<SerializablePicked> callback);
	
	//public SerializablePicked create();
	
}
