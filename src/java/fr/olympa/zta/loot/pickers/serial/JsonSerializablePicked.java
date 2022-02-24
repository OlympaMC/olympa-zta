package fr.olympa.zta.loot.pickers.serial;

import fr.olympa.api.LinkSpigotBungee;

public interface JsonSerializablePicked extends SerializablePicked {
	
	@Override
	default String save() {
		return LinkSpigotBungee.getInstance().getGson().toJson(this);
	}
	
	public static <T extends JsonSerializablePicked> T load(String data, Class<T> clazz) {
		return LinkSpigotBungee.getInstance().getGson().fromJson(data, clazz);
	}
	
}
