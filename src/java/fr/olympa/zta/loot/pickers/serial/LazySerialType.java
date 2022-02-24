package fr.olympa.zta.loot.pickers.serial;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.entity.Player;

public class LazySerialType implements SerializableType {
	
	private String id;
	private Class<? extends SerializablePicked> pickedClass;
	private Function<String, SerializablePicked> load;
	private BiConsumer<Player, Consumer<SerializablePicked>> create;
	
	public LazySerialType(String id, Class<? extends SerializablePicked> pickedClass, Function<String, SerializablePicked> load, BiConsumer<Player, Consumer<SerializablePicked>> create) {
		this.id = id;
		this.pickedClass = pickedClass;
		this.load = load;
		this.create = create;
	}
	
	@Override
	public String getID() {
		return id;
	}
	
	@Override
	public Class<? extends SerializablePicked> getPickedClass() {
		return pickedClass;
	}
	
	@Override
	public SerializablePicked load(String data) {
		return load.apply(data);
	}
	
	@Override
	public void create(Player p, Consumer<SerializablePicked> callback) {
		create.accept(p, callback);
	}
	
}
