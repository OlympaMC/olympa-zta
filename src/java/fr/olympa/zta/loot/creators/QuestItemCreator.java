package fr.olympa.zta.loot.creators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ImmutableItemStack;
import fr.olympa.api.item.ItemUtils;

public class QuestItemCreator implements LootCreator {

	private double chance;
	private QuestItem questItem;

	public QuestItemCreator(double chance, QuestItem questItem) {
		this.chance = chance;
		this.questItem = questItem;
	}
	
	@Override
	public double getChance() {
		return chance;
	}

	@Override
	public Loot create(Random random) {
		return new Loot(questItem.getItem(1));
	}
	
	@Override
	public String getTitle() {
		return questItem.getName();
	}
	
	public enum QuestItem {
		DECHET(Material.IRON_NUGGET, "Déchet métallique", 1),
		PIECE(Material.IRON_INGOT, "Pièce métallique", 1),
		AIMANT(Material.CLAY_BALL, "Aimant", 1),
		PILE(Material.RABBIT_HIDE, "Pile magnétique", 1),
		IEM_MATERIEL(Material.LEATHER, "Matériel I.E.M.", 1),
		IEM_BROUILLEUR(Material.SCUTE, "Brouilleur I.E.M.", 1),
		BIOTECH_BROUILLER(Material.SLIME_BALL, "Brouilleur BioTech.", 1),

		AMAS(Material.GOLD_NUGGET, "Amas technologique", 2),
		COMPOSANT(Material.GOLD_INGOT, "Composant", 2),
		CARTE(Material.GLOWSTONE_DUST, "Carte", 2),
		BOITIER_ELEC(Material.PRISMARINE_CRYSTALS, "Boîtier éléctronique", 2),
		BOITIER_PROG(Material.PRISMARINE_SHARD, "Boîtier de programme", 2),
		GENERATEUR_ENCOD(Material.SHULKER_SHELL, "Générateur encodé", 2),
		GENERATEUR_CRED(Material.POPPED_CHORUS_FRUIT, "Générateur de crédit", 2);
		
		private final String name;
		private final int segment;
		private final ImmutableItemStack item;

		private QuestItem(Material type, String name, int cat, String... lore) {
			this.name = name;
			this.segment = cat;
			List<String> loreList = new ArrayList<>(Arrays.asList(lore));
			loreList.add("");
			loreList.add("§8> §7Ressource de catégorie §l" + cat);
			this.item = new ImmutableItemStack(ItemUtils.item(type, "§b" + name, loreList.toArray(String[]::new)));
		}

		public String getName() {
			return name;
		}
		
		public ItemStack getItem(int amount) {
			ItemStack item = this.item.toMutableStack();
			item.setAmount(amount);
			return item;
		}
		
		public ImmutableItemStack getOriginalItem() {
			return item;
		}

		public int getSegment() {
			return segment;
		}
	}

}
