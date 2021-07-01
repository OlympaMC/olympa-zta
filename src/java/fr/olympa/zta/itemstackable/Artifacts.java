package fr.olympa.zta.itemstackable;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.spigot.item.ImmutableItemStack;
import fr.olympa.api.spigot.utils.SpigotUtils;

public enum Artifacts implements ItemStackable {
	
	PARACHUTE(
			Material.DIAMOND_CHESTPLATE,
			"Parachute",
			"Si vous le portez, vous serez en mesure de planer dès le saut depuis un endroit élevé.",
			"Item rarissime, prenez-en soin."),
	BOOTS(
			Material.DIAMOND_BOOTS,
			"Bottes à ressort",
			"Produites par Aperture Science, Inc., il s'agit de bottes empêchant tout dégât de chute et permettant de sauter haut.",
			"Item de très grande valeur."),
			;
	
	private final String name;
	private final NamespacedKey key;
	private final ImmutableItemStack item;
	
	private Artifacts(Material type, String name, String... lore) {
		this.name = name;
		this.key = ItemStackableManager.register(this);
		
		List<String> loreList = new ArrayList<>();
		for (String loreLine : lore) loreList.addAll(SpigotUtils.wrapAndAlign(loreLine, 35));
		loreList.add("");
		loreList.add("§8> §bCet objet reste dans votre");
		loreList.add("§b  inventaire quand vous mourez.");
		
		ItemStack item = new ItemStack(type);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§b" + name);
		meta.setLore(loreList);
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
		ItemStackableManager.processItem(item, this);
		
		this.item = new ImmutableItemStack(item);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getId() {
		return name();
	}
	
	@Override
	public NamespacedKey getKey() {
		return key;
	}
	
	@Override
	public ItemStack createItem() {
		return item.toMutableStack();
	}
	
	@Override
	public ImmutableItemStack getDemoItem() {
		return item;
	}
	
	@Override
	public ItemDropBehavior loot(Player p, ItemStack item) {
		return ItemDropBehavior.KEEP;
	}
	
}
