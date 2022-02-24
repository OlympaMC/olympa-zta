package fr.olympa.zta.weapons.skins;

import java.util.Arrays;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.zta.ZTAPermissions;

public enum Skin {
	
	NORMAL(
			0,
			"normal"),
	GOLD(
			1,
			"doré"),
	INCOMING(
			-1,
			"soon...");

	private int id;
	private String name;
	
	private Skin(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public static Skin getFromId(int id) {
		return Arrays.stream(values()).filter(x -> x.getId() == id).findAny().orElse(NORMAL);
	}
	
	public static Skin getAvailable(OlympaPlayer player) {
		if (ZTAPermissions.GROUP_SURVIVANT.hasPermission(player)) return GOLD;
		return NORMAL;
	}
	
}
