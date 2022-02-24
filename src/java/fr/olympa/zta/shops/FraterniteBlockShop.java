package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FraterniteBlockShop extends BlockShop {

	private static final List<AbstractArticle<ItemStack>> materials = Arrays.asList(
			prepare(Material.IRON_BLOCK, 50),
			prepare(Material.IRON_TRAPDOOR, 50),
			prepare(Material.IRON_DOOR, 50),
			prepare(Material.HEAVY_WEIGHTED_PRESSURE_PLATE, 50),
			prepare(Material.LEVER, 20),
			prepare(Material.ANVIL, 50),
			prepare(Material.FURNACE, 25),
			prepare(Material.IRON_BARS, 25),
			prepare(Material.LIME_BED, 25),
			prepare(Material.GREEN_BED, 25),
			prepare(Material.CAMPFIRE, 50),
			prepare(Material.SCAFFOLDING, 10),
			prepare(Material.SMOKER, 100),
			prepare(Material.BLAST_FURNACE, 100),
			prepare(Material.GRINDSTONE, 100),
			prepare(Material.STONECUTTER, 100),
			prepare(Material.SMITHING_TABLE, 100),
			prepare(Material.BLACK_STAINED_GLASS, 10),
			prepare(Material.GRAY_STAINED_GLASS, 10),
			prepare(Material.BLACK_STAINED_GLASS_PANE, 10),
			prepare(Material.GRAY_STAINED_GLASS_PANE, 10),
			prepare(Material.LIME_WOOL, 10),
			prepare(Material.GREEN_WOOL, 10),
			prepare(Material.LIME_CARPET, 10),
			prepare(Material.GREEN_CARPET, 10),
			prepare(Material.WHITE_CONCRETE, 20),
			prepare(Material.ORANGE_CONCRETE, 20),
			prepare(Material.MAGENTA_CONCRETE, 20),
			prepare(Material.LIGHT_BLUE_CONCRETE, 20),
			prepare(Material.YELLOW_CONCRETE, 20),
			prepare(Material.LIME_CONCRETE, 20),
			prepare(Material.PINK_CONCRETE, 20),
			prepare(Material.GRAY_CONCRETE, 20),
			prepare(Material.LIGHT_GRAY_CONCRETE, 20),
			prepare(Material.CYAN_CONCRETE, 20),
			prepare(Material.PURPLE_CONCRETE, 20),
			prepare(Material.BLUE_CONCRETE, 20),
			prepare(Material.BROWN_CONCRETE, 20),
			prepare(Material.GREEN_CONCRETE, 20),
			prepare(Material.RED_CONCRETE, 20),
			prepare(Material.BLACK_CONCRETE, 20),
			prepare(Material.WHITE_CONCRETE_POWDER, 20),
			prepare(Material.ORANGE_CONCRETE_POWDER, 20),
			prepare(Material.MAGENTA_CONCRETE_POWDER, 20),
			prepare(Material.LIGHT_BLUE_CONCRETE_POWDER, 20),
			prepare(Material.YELLOW_CONCRETE_POWDER, 20),
			prepare(Material.LIME_CONCRETE_POWDER, 20),
			prepare(Material.PINK_CONCRETE_POWDER, 20),
			prepare(Material.GRAY_CONCRETE_POWDER, 20),
			prepare(Material.LIGHT_GRAY_CONCRETE_POWDER, 20),
			prepare(Material.CYAN_CONCRETE_POWDER, 20),
			prepare(Material.PURPLE_CONCRETE_POWDER, 20),
			prepare(Material.BLUE_CONCRETE_POWDER, 20),
			prepare(Material.BROWN_CONCRETE_POWDER, 20),
			prepare(Material.GREEN_CONCRETE_POWDER, 20),
			prepare(Material.RED_CONCRETE_POWDER, 20),
			prepare(Material.BLACK_CONCRETE_POWDER, 20),
			prepare(Material.FISHING_ROD, 500),
			prepare(Material.TRAPPED_CHEST, 1000)
			);

	public FraterniteBlockShop() {
		super("blockshopfraternite", "La Milice", DyeColor.GREEN, materials);
	}
	
}
