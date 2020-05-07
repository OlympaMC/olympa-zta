package fr.olympa.zta.plots.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public class FraterniteBlockShop extends BlockShop {

	private static final List<Article<Material>> materials = Arrays.asList(
			new Article<>(Material.IRON_BARS, 20)
			);

	public FraterniteBlockShop() {
		super("civilblockshop", "Magasin de blocs de la Fraternité", DyeColor.GREEN, materials);
	}
	
}
