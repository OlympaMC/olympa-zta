package fr.olympa.zta.weapons.guns.minigun;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherObject;

public class DelegateDataWatcher<T> extends DataWatcher {
	
	private List<Item<?>> items;
	
	public DelegateDataWatcher(DataWatcherObject<T> dataObject, T value) {
		super(null);
		this.items = Arrays.asList(new DataWatcher.Item<T>(dataObject, value));
	}
	
	@Override
	public @Nullable List<Item<?>> b() {
		return items;
	}
	
}
