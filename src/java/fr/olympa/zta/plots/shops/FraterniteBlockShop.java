package fr.olympa.zta.plots.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public class FraterniteBlockShop extends BlockShop {

	private static final List<Article<Material>> materials = Arrays.asList(
			new Article<>(Material.IRON_BLOCK, 50),
			new Article<>(Material.IRON_TRAPDOOR, 50),
			new Article<>(Material.IRON_DOOR, 50),
			new Article<>(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, 50),
			new Article<>(Material.LEVER, 20),
			new Article<>(Material.ANVIL, 50),
			new Article<>(Material.FURNACE, 25),
			new Article<>(Material.IRON_BARS, 25),
			new Article<>(Material.LIME_BED, 25),
			new Article<>(Material.GREEN_BED, 25),
			new Article<>(Material.CAMPFIRE, 50),
			new Article<>(Material.SCAFFOLDING, 10),
			new Article<>(Material.SMOKER, 100),
			new Article<>(Material.BLAST_FURNACE, 100),
			new Article<>(Material.GRINDSTONE, 100),
			new Article<>(Material.STONECUTTER, 100),
			new Article<>(Material.SMITHING_TABLE, 100),
			new Article<>(Material.BLACK_STAINED_GLASS, 10),
			new Article<>(Material.GRAY_STAINED_GLASS, 10),
			new Article<>(Material.BLACK_STAINED_GLASS_PANE, 10),
			new Article<>(Material.GRAY_STAINED_GLASS_PANE, 10),
			new Article<>(Material.LIME_WOOL, 10),
			new Article<>(Material.GREEN_WOOL, 10),
			new Article<>(Material.LIME_CARPET, 10),
			new Article<>(Material.GREEN_CARPET, 10),
			new Article<>(Material.WHITE_CONCRETE, 20),
			new Article<>(Material.ORANGE_CONCRETE, 20),
			new Article<>(Material.MAGENTA_CONCRETE, 20),
			new Article<>(Material.LIGHT_BLUE_CONCRETE, 20),
			new Article<>(Material.YELLOW_CONCRETE, 20),
			new Article<>(Material.LIME_CONCRETE, 20),
			new Article<>(Material.PINK_CONCRETE, 20),
			new Article<>(Material.GRAY_CONCRETE, 20),
			new Article<>(Material.LIGHT_GRAY_CONCRETE, 20),
			new Article<>(Material.CYAN_CONCRETE, 20),
			new Article<>(Material.PURPLE_CONCRETE, 20),
			new Article<>(Material.BLUE_CONCRETE, 20),
			new Article<>(Material.BROWN_CONCRETE, 20),
			new Article<>(Material.GREEN_CONCRETE, 20),
			new Article<>(Material.RED_CONCRETE, 20),
			new Article<>(Material.BLACK_CONCRETE, 20),
			new Article<>(Material.WHITE_CONCRETE_POWDER, 20),
			new Article<>(Material.ORANGE_CONCRETE_POWDER, 20),
			new Article<>(Material.MAGENTA_CONCRETE_POWDER, 20),
			new Article<>(Material.LIGHT_BLUE_CONCRETE_POWDER, 20),
			new Article<>(Material.YELLOW_CONCRETE_POWDER, 20),
			new Article<>(Material.LIME_CONCRETE_POWDER, 20),
			new Article<>(Material.PINK_CONCRETE_POWDER, 20),
			new Article<>(Material.GRAY_CONCRETE_POWDER, 20),
			new Article<>(Material.LIGHT_GRAY_CONCRETE_POWDER, 20),
			new Article<>(Material.CYAN_CONCRETE_POWDER, 20),
			new Article<>(Material.PURPLE_CONCRETE_POWDER, 20),
			new Article<>(Material.BLUE_CONCRETE_POWDER, 20),
			new Article<>(Material.BROWN_CONCRETE_POWDER, 20),
			new Article<>(Material.GREEN_CONCRETE_POWDER, 20),
			new Article<>(Material.RED_CONCRETE_POWDER, 20),
			new Article<>(Material.BLACK_CONCRETE_POWDER, 20),
			new Article<>(Material.FISHING_ROD, 500),
			new Article<>(Material.TRAPPED_CHEST, 1000)
			);

	public FraterniteBlockShop() {
		super("blockshopfraternite", "Magasin de blocs - Fraternité", DyeColor.GREEN, materials);
	}
	
}
