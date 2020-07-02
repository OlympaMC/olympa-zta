package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;

public class QuestItemCreator implements LootCreator {

	private double chance;
	private QuestItems questItem;

	public QuestItemCreator(double chance, QuestItems questItem) {
		this.chance = chance;
		this.questItem = questItem;
	}
	
	@Override
	public double getChance() {
		return chance;
	}

	@Override
	public Loot create(Player p, Random random) {
		return new Loot(questItem.getItem());
	}
	
	public enum QuestItems {
		DECHET(Material.IRON_NUGGET, "Déchet métallique"),
		PIECE(Material.IRON_INGOT, "Pièce métallique"),
		AIMANT(Material.CLAY_BALL, "Aimant"),
		PILE(Material.RABBIT_HIDE, "Pile magnétique"),
		IEM_MATERIEL(Material.LEATHER, "Matériel I.E.M."),
		IEM_BROUILLEUR(Material.SCUTE, "Brouilleur I.E.M."),
		SLIMEBALL(Material.CLAY_BALL, "Brouilleur BioTech"),

		AMAS(Material.GOLD_NUGGET, "Amas technologique"),
		COMPOSANT(Material.GOLD_INGOT, "Composant"),
		CARTE(Material.GLOWSTONE_DUST, "Carte"),
		BOITIER_ELEC(Material.PRISMARINE_CRYSTALS, "Boîtier éléctronique"),
		BOITIER_PROG(Material.PRISMARINE_SHARD, "Boîtier de programme"),
		GENERATEUR_ENCOD(Material.SHULKER_SHELL, "Générateur encodé"),
		GENERATEUR_CRED(Material.POPPED_CHORUS_FRUIT, "Générateur de crédit");
		
		private final ItemStack item;

		private QuestItems(Material type, String name, String... lore) {
			this.item = ItemUtils.item(type, "§b" + name, lore);
		}

		public ItemStack getItem() {
			return item;
		}
	}

}
