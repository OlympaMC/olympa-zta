package fr.olympa.zta.plots.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public class CivilBlockShop extends BlockShop {

	private static final List<Article<Material>> materials = Arrays.asList(
			new Article<>(Material.OAK_LOG, 10),
			new Article<>(Material.OAK_DOOR, 15),
			new Article<>(Material.OAK_STAIRS, 15),
			new Article<>(Material.OAK_SLAB, 15),
			new Article<>(Material.OAK_FENCE, 20),
			new Article<>(Material.CHEST, 50),
			new Article<>(Material.GLASS, 25),
			new Article<>(Material.GLASS_PANE, 30),
			new Article<>(Material.LADDER, 20),
			new Article<>(Material.FLOWER_POT, 30)
			);
	
	public CivilBlockShop() {
		super("blockshopcivil", "Magasin de blocs", DyeColor.CYAN, materials);
	}

}
