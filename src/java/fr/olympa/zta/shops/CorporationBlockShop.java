package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CorporationBlockShop extends BlockShop {

	private static final List<AbstractArticle<ItemStack>> materials = Arrays.asList(
			prepare(Material.NOTE_BLOCK, 50),
			prepare(Material.JUKEBOX, 100),
			prepare(Material.PAINTING, 500),
			prepare(Material.WHITE_BED, 25),
			prepare(Material.ORANGE_BED, 25),
			prepare(Material.MAGENTA_BED, 25),
			prepare(Material.LIGHT_BLUE_BED, 25),
			prepare(Material.YELLOW_BED, 25),
			prepare(Material.PINK_BED, 25),
			prepare(Material.GRAY_BED, 25),
			prepare(Material.LIGHT_GRAY_BED, 25),
			prepare(Material.CYAN_BED, 25),
			prepare(Material.PURPLE_BED, 25),
			prepare(Material.BLUE_BED, 25),
			prepare(Material.BROWN_BED, 25),
			prepare(Material.BLACK_BED, 25),
			prepare(Material.REDSTONE_TORCH, 50),
			prepare(Material.END_ROD, 50),
			prepare(Material.LANTERN, 50),
			prepare(Material.FLETCHING_TABLE, 100),
			prepare(Material.BARREL, 800),
			prepare(Material.LOOM, 100),
			prepare(Material.CARTOGRAPHY_TABLE, 100),
			prepare(Material.SMITHING_TABLE, 100),
			prepare(Material.QUARTZ_BLOCK, 10),
			prepare(Material.CHISELED_QUARTZ_BLOCK, 10),
			prepare(Material.SMOOTH_QUARTZ, 10),
			prepare(Material.QUARTZ_PILLAR, 10),
			prepare(Material.QUARTZ_STAIRS, 10),
			prepare(Material.QUARTZ_SLAB, 10),
			prepare(Material.WHITE_STAINED_GLASS, 10),
			prepare(Material.ORANGE_STAINED_GLASS, 10),
			prepare(Material.MAGENTA_STAINED_GLASS, 10),
			prepare(Material.LIGHT_BLUE_STAINED_GLASS, 10),
			prepare(Material.YELLOW_STAINED_GLASS, 10),
			prepare(Material.LIME_STAINED_GLASS, 10),
			prepare(Material.PINK_STAINED_GLASS, 10),
			prepare(Material.LIGHT_GRAY_STAINED_GLASS, 10),
			prepare(Material.CYAN_STAINED_GLASS, 10),
			prepare(Material.PURPLE_STAINED_GLASS, 10),
			prepare(Material.BLUE_STAINED_GLASS, 10),
			prepare(Material.BROWN_STAINED_GLASS, 10),
			prepare(Material.GREEN_STAINED_GLASS, 10),
			prepare(Material.RED_STAINED_GLASS, 10),
			prepare(Material.WHITE_STAINED_GLASS_PANE, 10),
			prepare(Material.ORANGE_STAINED_GLASS_PANE, 10),
			prepare(Material.MAGENTA_STAINED_GLASS_PANE, 10),
			prepare(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 10),
			prepare(Material.YELLOW_STAINED_GLASS_PANE, 10),
			prepare(Material.LIME_STAINED_GLASS_PANE, 10),
			prepare(Material.PINK_STAINED_GLASS_PANE, 10),
			prepare(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 10),
			prepare(Material.CYAN_STAINED_GLASS_PANE, 10),
			prepare(Material.PURPLE_STAINED_GLASS_PANE, 10),
			prepare(Material.BLUE_STAINED_GLASS_PANE, 10),
			prepare(Material.BROWN_STAINED_GLASS_PANE, 10),
			prepare(Material.GREEN_STAINED_GLASS_PANE, 10),
			prepare(Material.RED_STAINED_GLASS_PANE, 10),
			prepare(Material.ORANGE_WOOL, 10),
			prepare(Material.MAGENTA_WOOL, 10),
			prepare(Material.LIGHT_BLUE_WOOL, 10),
			prepare(Material.YELLOW_WOOL, 10),
			prepare(Material.PINK_WOOL, 10),
			prepare(Material.GRAY_WOOL, 10),
			prepare(Material.LIGHT_GRAY_WOOL, 10),
			prepare(Material.CYAN_WOOL, 10),
			prepare(Material.PURPLE_WOOL, 10),
			prepare(Material.BLUE_WOOL, 10),
			prepare(Material.BROWN_WOOL, 10),
			prepare(Material.RED_WOOL, 10),
			prepare(Material.BLACK_WOOL, 10),
			prepare(Material.ORANGE_CARPET, 10),
			prepare(Material.MAGENTA_CARPET, 10),
			prepare(Material.LIGHT_BLUE_CARPET, 10),
			prepare(Material.YELLOW_CARPET, 10),
			prepare(Material.PINK_CARPET, 10),
			prepare(Material.GRAY_CARPET, 10),
			prepare(Material.LIGHT_GRAY_CARPET, 10),
			prepare(Material.CYAN_CARPET, 10),
			prepare(Material.PURPLE_CARPET, 10),
			prepare(Material.BLUE_CARPET, 10),
			prepare(Material.BROWN_CARPET, 10),
			prepare(Material.RED_CARPET, 10),
			prepare(Material.BLACK_CARPET, 10),
			prepare(Material.WHITE_GLAZED_TERRACOTTA, 50),
			prepare(Material.ORANGE_GLAZED_TERRACOTTA, 50),
			prepare(Material.MAGENTA_GLAZED_TERRACOTTA, 50),
			prepare(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, 50),
			prepare(Material.YELLOW_GLAZED_TERRACOTTA, 50),
			prepare(Material.LIME_GLAZED_TERRACOTTA, 50),
			prepare(Material.PINK_GLAZED_TERRACOTTA, 50),
			prepare(Material.GRAY_GLAZED_TERRACOTTA, 50),
			prepare(Material.LIGHT_GRAY_GLAZED_TERRACOTTA, 50),
			prepare(Material.CYAN_GLAZED_TERRACOTTA, 50),
			prepare(Material.PURPLE_GLAZED_TERRACOTTA, 50),
			prepare(Material.BLUE_GLAZED_TERRACOTTA, 50),
			prepare(Material.BROWN_GLAZED_TERRACOTTA, 50),
			prepare(Material.GREEN_GLAZED_TERRACOTTA, 50),
			prepare(Material.RED_GLAZED_TERRACOTTA, 50),
			prepare(Material.BLACK_GLAZED_TERRACOTTA, 50)
			);
	
	

	public CorporationBlockShop() {
		super("blockshopcorporation", "Le Capitole", DyeColor.RED, materials);
	}
	
}
