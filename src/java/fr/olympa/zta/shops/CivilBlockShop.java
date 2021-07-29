package fr.olympa.zta.shops;

import java.util.Arrays;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;

public class CivilBlockShop extends BlockShop {

	public static final NamespacedKey TOOL_KEY = new NamespacedKey(OlympaZTA.getInstance(), "tool");
	
	private static final List<AbstractArticle<ItemStack>> materials =
			Arrays.asList(
			prepare(Material.COBBLESTONE, 10),
			prepare(Material.GRANITE, 10),
			prepare(Material.DIORITE, 10),
			prepare(Material.ANDESITE, 10),
			prepare(Material.COBBLESTONE_STAIRS, 10),
			prepare(Material.BRICKS, 10),
			prepare(Material.BRICK_STAIRS, 10),
			prepare(Material.STONE, 15),
			prepare(Material.POLISHED_GRANITE, 15),
			prepare(Material.POLISHED_DIORITE, 15),
			prepare(Material.POLISHED_ANDESITE, 15),
			prepare(Material.STONE_STAIRS, 15),
			prepare(Material.STONE_BRICKS, 15),
			prepare(Material.STONE_BRICK_STAIRS, 15),
			prepare(Material.GRASS_BLOCK, 5),
			prepare(Material.COARSE_DIRT, 5),
			prepare(Material.DIRT, 5),
			prepare(Material.SAND, 10),
			prepare(Material.RED_SAND, 10),
			prepare(Material.OAK_TRAPDOOR, 20),
			prepare(Material.OAK_SIGN, 20),
			prepare(Material.OAK_LOG, 20),
			prepare(Material.OAK_PLANKS, 5),
			prepare(Material.CRAFTING_TABLE, 20),
			prepare(Material.CHEST, 1000),
			prepare(Material.OAK_STAIRS, 5),
			prepare(Material.LADDER, 10),
			prepare(Material.OAK_DOOR, 20),
			prepare(Material.SANDSTONE, 10),
			prepare(Material.CUT_SANDSTONE, 10),
			prepare(Material.CHISELED_SANDSTONE, 10),
			prepare(Material.SMOOTH_SANDSTONE, 10),
			prepare(Material.SANDSTONE_STAIRS, 10),
			prepare(Material.SANDSTONE_SLAB, 10),
			prepare(Material.CUT_SANDSTONE_SLAB, 10),
			prepare(Material.RED_SANDSTONE, 10),
			prepare(Material.CUT_RED_SANDSTONE, 10),
			prepare(Material.CHISELED_RED_SANDSTONE, 10),
			prepare(Material.SMOOTH_RED_SANDSTONE, 10),
			prepare(Material.RED_SANDSTONE_STAIRS, 10),
			prepare(Material.RED_SANDSTONE_SLAB, 10),
			prepare(Material.CUT_RED_SANDSTONE_SLAB, 10),
			prepare(Material.OAK_SAPLING, 50),
			prepare(Material.SPRUCE_SAPLING, 50),
			prepare(Material.BIRCH_SAPLING, 50),
			prepare(Material.JUNGLE_SAPLING, 50),
			prepare(Material.ACACIA_SAPLING, 50),
			prepare(Material.DARK_OAK_SAPLING, 50),
			prepare(Material.DANDELION, 5),
			prepare(Material.POPPY, 5),
			prepare(Material.BLUE_ORCHID, 5),
			prepare(Material.ALLIUM, 5),
			prepare(Material.AZURE_BLUET, 5),
			prepare(Material.RED_TULIP, 5),
			prepare(Material.ORANGE_TULIP, 5),
			prepare(Material.WHITE_TULIP, 5),
			prepare(Material.PINK_TULIP, 5),
			prepare(Material.OXEYE_DAISY, 5),
			prepare(Material.CORNFLOWER, 5),
			prepare(Material.LILY_OF_THE_VALLEY, 5),
			prepare(Material.ROSE_BUSH, 5),
			prepare(Material.PEONY, 5),
			prepare(Material.LILAC, 5),
			prepare(Material.SUNFLOWER, 5),
			prepare(Material.SEAGRASS, 5),
			prepare(Material.SEA_PICKLE, 10),
			prepare(Material.FLOWER_POT, 5),
			prepare(Material.BUCKET, 25),
			prepare(Material.WATER_BUCKET, 50),
			prepare(Material.VINE, 100),
			prepare(Material.CACTUS, 100),
			prepare(Material.BAMBOO, 100),
			prepare(Material.SUGAR_CANE, 100),
			prepare(Material.COCOA_BEANS, 100),
			prepare(Material.WHEAT_SEEDS, 100),
			prepare(Material.CARROT, 100),
			prepare(Material.POTATO, 100),
			prepare(Material.BEETROOT, 100),
			prepare(Material.PUMPKIN_SEEDS, 100),
			prepare(Material.MELON_SEEDS, 100),
			prepare(Material.RED_BED, 25),
			prepare(Material.TORCH, 5),
			prepare(Material.GLASS, 10),
			prepare(Material.GLASS_PANE, 10),
			prepare(Material.WHITE_WOOL, 10),
			prepare(Material.WHITE_CARPET, 10),
			prepare(Material.TERRACOTTA, 10),
			prepare(Material.WHITE_TERRACOTTA, 10),
			prepare(Material.ORANGE_TERRACOTTA, 10),
			prepare(Material.MAGENTA_TERRACOTTA, 10),
			prepare(Material.LIGHT_BLUE_TERRACOTTA, 10),
			prepare(Material.YELLOW_TERRACOTTA, 10),
			prepare(Material.LIME_TERRACOTTA, 10),
			prepare(Material.PINK_TERRACOTTA, 10),
			prepare(Material.GRAY_TERRACOTTA, 10),
			prepare(Material.LIGHT_GRAY_TERRACOTTA, 10),
			prepare(Material.CYAN_TERRACOTTA, 10),
			prepare(Material.PURPLE_TERRACOTTA, 10),
			prepare(Material.BLUE_TERRACOTTA, 10),
			prepare(Material.BROWN_TERRACOTTA, 10),
			prepare(Material.GREEN_TERRACOTTA, 10),
			prepare(Material.RED_TERRACOTTA, 10),
			prepare(Material.BLACK_TERRACOTTA, 10),
			prepare(Material.COBBLESTONE_WALL, 15),
			prepare(generateTool(Material.DIAMOND_SHOVEL, "§bPelle en diamant"), 100),
			prepare(generateTool(Material.DIAMOND_PICKAXE, "§bPioche en diamant"), 100),
			prepare(generateTool(Material.DIAMOND_AXE, "§bHache en diamant"), 100),
			prepare(generateTool(Material.DIAMOND_HOE, "§bHoue en diamant"), 100)
			);
	
	private static ItemStack generateTool(Material material, String name) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(SpigotUtils.wrapAndAlign("Cet outil est uniquement utilisable sur une parcelle de construction.", 30));
		meta.setCustomModelData(1);
		meta.getPersistentDataContainer().set(TOOL_KEY, PersistentDataType.BYTE, (byte) 0);
		item.setItemMeta(meta);
		return item;
	}
	
	public CivilBlockShop() {
		super("blockshopcivil", null, DyeColor.CYAN, materials);
	}

}
