package fr.olympa.zta.settings;

import org.bukkit.Material;

public enum ClanBoardSetting {
	
	ONLINE_FIVE(Material.YELLOW_DYE, "§eTous les joueurs", "Affiche les joueurs connectés et ceux hors-ligne, dans une limite de 5 joueurs."),
	ONLINE(Material.ORANGE_DYE, "§6Joueurs en ligne", "Affiche seulement les joueurs connectés, ainsi que leur emplacement et leurs PV."),
	NEVER(Material.RED_DYE, "§cx §odésactivé §cx", "N'affiche aucune information sur votre clan dans la tableau des scores."),
	;
	
	private Material material;
	private String itemName;
	private String itemLore;
	
	ClanBoardSetting(Material material, String itemName, String itemLore) {
		this.material = material;
		this.itemName = itemName;
		this.itemLore = itemLore;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public String getItemName() {
		return itemName;
	}
	
	public String getItemLore() {
		return itemLore;
	}
	
}
