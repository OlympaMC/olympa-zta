package fr.olympa.zta.plots.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public class CorporationBlockShop extends BlockShop {

	private static final List<Article<Material>> materials = Arrays.asList(
			new Article<>(Material.GRAVEL, 30)
			);

	public CorporationBlockShop() {
		super("blockshopcorporation", "Magasin de blocs de la Corporation", DyeColor.RED, materials);
	}
	
}
