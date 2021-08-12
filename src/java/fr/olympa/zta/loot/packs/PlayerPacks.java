package fr.olympa.zta.loot.packs;

import java.util.HashMap;
import java.util.Map;

import fr.olympa.api.common.observable.AbstractObservable;
import fr.olympa.core.spigot.OlympaCore;

public class PlayerPacks extends AbstractObservable {
	
	private Map<PackType, Integer> packs = new HashMap<>();
	
	public boolean hasPack(PackType type) {
		return packs.containsKey(type);
	}
	
	public int getPackAmount(PackType type) {
		Integer amount = packs.get(type);
		return amount == null ? 0 : amount.intValue();
	}
	
	public boolean removePack(PackType type) {
		Integer oldAmount = packs.remove(type);
		if (oldAmount == null) return false;
		int amount = oldAmount.intValue() - 1;
		if (amount > 0) packs.put(type, amount);
		update();
		return true;
	}
	
	public void givePack(PackType type) {
		packs.merge(type, 1, (o1, o2) -> o1 + o2);
		update();
	}
	
	@Override
	public String toString() {
		return OlympaCore.getInstance().getGson().toJson(packs);
	}
	
	public void loadFromString(String string) {
		if (string == null) return;
		this.packs = OlympaCore.getInstance().getGson().fromJson(string, Map.class);
	}
	
}
