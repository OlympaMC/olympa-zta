package fr.olympa.zta.utils.map;

import org.bukkit.Color;

public class DynmapZoneConfig {
	private final Color color;
	private final String htmlColor;
	private final String name;
	private final String description;
	
	public DynmapZoneConfig(Color color, String htmlColor, String name, String description) {
		this.color = color;
		this.htmlColor = htmlColor;
		this.name = name;
		this.description = description;
	}
	
	public Color color() {
		return color;
	}
	
	public String htmlColor() {
		return htmlColor;
	}
	
	public String name() {
		return name;
	}
	
	public String description() {
		return description;
	}
}
