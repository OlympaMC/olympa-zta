package fr.olympa.zta.loot.creators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ImmutableItemStack;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;

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
	
	public enum QuestItem implements ItemStackable {
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
		GENERATEUR_CRED(Material.POPPED_CHORUS_FRUIT, "Générateur de crédit", 2),
		
		PARACHUTE(Material.DIAMOND_CHESTPLATE, "Parachute", -1, "Si vous le portez, vous serez en mesure de planer dès le saut depuis un endroit élevé.", "Item rarissime, prenez-en soin."),
		BOOTS(Material.DIAMOND_BOOTS, "Bottes à ressort", -1, "Produites par Aperture Science, Inc., il s'agit de bottes empêchant tout dégât de chute et permettant de sauter haut.", "Item de très grande valeur."),
		;
		
		private final String name;
		private final int segment;
		private final ImmutableItemStack item;

		private QuestItem(Material type, String name, int cat, String... lore) {
			this.name = name;
			this.segment = cat;
			List<String> loreList = new ArrayList<>();
			for (String loreLine : lore) loreList.addAll(SpigotUtils.wrapAndAlign(loreLine, 35));
			if (cat != -1) {
				loreList.add("");
				loreList.add("§8> §7Ressource de catégorie §l" + cat);
			}
			this.item = new ImmutableItemStack(ItemStackableManager.processItem(ItemUtils.item(type, "§b" + name, loreList.toArray(String[]::new)), this));
		}

		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public String getId() {
			return name();
		}
		
		public ItemStack getItem(int amount) {
			ItemStack item = this.item.toMutableStack();
			item.setAmount(amount);
			return item;
		}
		
		@Override
		public ItemStack createItem() {
			return getItem(1);
		}
		
		@Override
		public ImmutableItemStack getDemoItem() {
			return item;
		}

		public int getSegment() {
			return segment;
		}
	}

}
