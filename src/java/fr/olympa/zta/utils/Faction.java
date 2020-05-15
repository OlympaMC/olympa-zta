package fr.olympa.zta.utils;

public enum Faction {

	CORPORATION("Corporation"),
	FRATERINTE("Fraternité");

	private final String name;

	private Faction(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
