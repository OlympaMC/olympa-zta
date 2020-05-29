package fr.olympa.zta.plots.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;

public class CorporationBlockShop extends BlockShop {

	private static final List<Article<Material>> materials = Arrays.asList(
			new Article<>(Material.NOTE_BLOCK, 50),
			new Article<>(Material.JUKEBOX, 100),
			new Article<>(Material.PAINTING, 500),
			new Article<>(Material.WHITE_BED, 25),
			new Article<>(Material.ORANGE_BED, 25),
			new Article<>(Material.MAGENTA_BED, 25),
			new Article<>(Material.LIGHT_BLUE_BED, 25),
			new Article<>(Material.YELLOW_BED, 25),
			new Article<>(Material.PINK_BED, 25),
			new Article<>(Material.GRAY_BED, 25),
			new Article<>(Material.LIGHT_GRAY_BED, 25),
			new Article<>(Material.CYAN_BED, 25),
			new Article<>(Material.PURPLE_BED, 25),
			new Article<>(Material.BLUE_BED, 25),
			new Article<>(Material.BROWN_BED, 25),
			new Article<>(Material.BLACK_BED, 25),
			new Article<>(Material.REDSTONE_TORCH, 50),
			new Article<>(Material.END_ROD, 50),
			new Article<>(Material.LANTERN, 50),
			new Article<>(Material.FLETCHING_TABLE, 100),
			new Article<>(Material.BARREL, 100),
			new Article<>(Material.LOOM, 100),
			new Article<>(Material.CARTOGRAPHY_TABLE, 100),
			new Article<>(Material.SMITHING_TABLE, 100),
			new Article<>(Material.QUARTZ_BLOCK, 10),
			new Article<>(Material.CHISELED_QUARTZ_BLOCK, 10),
			new Article<>(Material.SMOOTH_QUARTZ, 10),
			new Article<>(Material.QUARTZ_PILLAR, 10),
			new Article<>(Material.QUARTZ_STAIRS, 10),
			new Article<>(Material.QUARTZ_SLAB, 10),
			new Article<>(Material.WHITE_STAINED_GLASS, 10),
			new Article<>(Material.ORANGE_STAINED_GLASS, 10),
			new Article<>(Material.MAGENTA_STAINED_GLASS, 10),
			new Article<>(Material.LIGHT_BLUE_STAINED_GLASS, 10),
			new Article<>(Material.YELLOW_STAINED_GLASS, 10),
			new Article<>(Material.LIME_STAINED_GLASS, 10),
			new Article<>(Material.PINK_STAINED_GLASS, 10),
			new Article<>(Material.LIGHT_GRAY_STAINED_GLASS, 10),
			new Article<>(Material.CYAN_STAINED_GLASS, 10),
			new Article<>(Material.PURPLE_STAINED_GLASS, 10),
			new Article<>(Material.BLUE_STAINED_GLASS, 10),
			new Article<>(Material.BROWN_STAINED_GLASS, 10),
			new Article<>(Material.GREEN_STAINED_GLASS, 10),
			new Article<>(Material.RED_STAINED_GLASS, 10),
			new Article<>(Material.WHITE_STAINED_GLASS_PANE, 10),
			new Article<>(Material.ORANGE_STAINED_GLASS_PANE, 10),
			new Article<>(Material.MAGENTA_STAINED_GLASS_PANE, 10),
			new Article<>(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 10),
			new Article<>(Material.YELLOW_STAINED_GLASS_PANE, 10),
			new Article<>(Material.LIME_STAINED_GLASS_PANE, 10),
			new Article<>(Material.PINK_STAINED_GLASS_PANE, 10),
			new Article<>(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 10),
			new Article<>(Material.CYAN_STAINED_GLASS_PANE, 10),
			new Article<>(Material.PURPLE_STAINED_GLASS_PANE, 10),
			new Article<>(Material.BLUE_STAINED_GLASS_PANE, 10),
			new Article<>(Material.BROWN_STAINED_GLASS_PANE, 10),
			new Article<>(Material.GREEN_STAINED_GLASS_PANE, 10),
			new Article<>(Material.RED_STAINED_GLASS_PANE, 10),
			new Article<>(Material.ORANGE_WOOL, 10),
			new Article<>(Material.MAGENTA_WOOL, 10),
			new Article<>(Material.LIGHT_BLUE_WOOL, 10),
			new Article<>(Material.YELLOW_WOOL, 10),
			new Article<>(Material.PINK_WOOL, 10),
			new Article<>(Material.GRAY_WOOL, 10),
			new Article<>(Material.LIGHT_GRAY_WOOL, 10),
			new Article<>(Material.CYAN_WOOL, 10),
			new Article<>(Material.PURPLE_WOOL, 10),
			new Article<>(Material.BLUE_WOOL, 10),
			new Article<>(Material.BROWN_WOOL, 10),
			new Article<>(Material.RED_WOOL, 10),
			new Article<>(Material.BLACK_WOOL, 10),
			new Article<>(Material.ORANGE_CARPET, 10),
			new Article<>(Material.MAGENTA_CARPET, 10),
			new Article<>(Material.LIGHT_BLUE_CARPET, 10),
			new Article<>(Material.YELLOW_CARPET, 10),
			new Article<>(Material.PINK_CARPET, 10),
			new Article<>(Material.GRAY_CARPET, 10),
			new Article<>(Material.LIGHT_GRAY_CARPET, 10),
			new Article<>(Material.CYAN_CARPET, 10),
			new Article<>(Material.PURPLE_CARPET, 10),
			new Article<>(Material.BLUE_CARPET, 10),
			new Article<>(Material.BROWN_CARPET, 10),
			new Article<>(Material.RED_CARPET, 10),
			new Article<>(Material.BLACK_CARPET, 10),
			new Article<>(Material.WHITE_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.ORANGE_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.MAGENTA_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.YELLOW_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.LIME_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.PINK_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.GRAY_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.CYAN_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.PURPLE_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.BLUE_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.BROWN_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.GREEN_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.RED_GLAZED_TERRACOTTA, 50),
			new Article<>(Material.BLACK_GLAZED_TERRACOTTA, 50)
			);
	
	

	public CorporationBlockShop() {
		super("blockshopcorporation", "Magasin de blocs - Corporation", DyeColor.RED, materials);
	}
	
}
