package fr.olympa.zta.itemstackable;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.spigot.item.ImmutableItemStack;
import fr.olympa.api.spigot.utils.SpigotUtils;

public enum QuestItem implements ItemStackable {
	DECHET(Material.IRON_NUGGET, "Déchet métallique", 1),
	FERRAILLE(Material.IRON_INGOT, "Ferraille", 1),
	KEROSENE(Material.CLAY_BALL, "Kérosène", 1),
	PILE(Material.RABBIT_HIDE, "Pile magnétique", 1),
	ANTIDOTE(Material.LEATHER, "Antidote", 1),
	IEM_BROUILLEUR(Material.SCUTE, "Brouilleur I.E.M.", 1),
	BATTERIE(Material.QUARTZ, "Batterie", 1),

	AMAS(Material.GOLD_NUGGET, "Amas technologique", 2),
	CARTE_MERE(Material.GOLD_INGOT, "Carte mère", 2),
	INDICE(Material.FIREWORK_STAR, "Indice", 2),
	BOITIER_ELEC(Material.PRISMARINE_CRYSTALS, "Boîtier éléctronique", 2),
	BOITIER_PROG(Material.PRISMARINE_SHARD, "Boîtier de programme", 2),
	GENERATEUR_ENCOD(Material.SHULKER_SHELL, "Générateur encodé", 2),
	GENERATEUR_CRED(Material.POPPED_CHORUS_FRUIT, "Générateur de crédit", 2),
	;
	
	private final String name;
	private final int segment;
	private final NamespacedKey key;
	private final ImmutableItemStack item;

	private QuestItem(Material type, String name, int cat, String... lore) {
		this.name = name;
		this.segment = cat;
		
		key = ItemStackableManager.register(this);
		
		List<String> loreList = new ArrayList<>();
		for (String loreLine : lore) loreList.addAll(SpigotUtils.wrapAndAlign(loreLine, 35));
		loreList.add("");
		loreList.add("§8> §7Ressource de catégorie §l" + cat);
		ItemStack item = new ItemStack(type);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§b" + name);
		meta.setLore(loreList);
		meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 0);
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
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
	
	public ItemStack getItem(int amount) {
		return item.toMutableStack(amount);
	}
	
	@Override
	public ItemStack createItem() {
		return item.toMutableStack();
	}
	
	@Override
	public ImmutableItemStack getDemoItem() {
		return item;
	}

	public int getSegment() {
		return segment;
	}
	
	public int getItemAmount(Inventory inv) {
		int amount = 0;
		for (ItemStack item : inv.getContents()) {
			ItemStackable stackable = ItemStackableManager.getStackable(item);
			if (this.equals(stackable)) amount += item.getAmount();
		}
		return amount;
	}
	
	public boolean containsAmount(Inventory inv, int amount) {
		for (ItemStack item : inv.getContents()) {
			ItemStackable stackable = ItemStackableManager.getStackable(item);
			if (this.equals(stackable)) {
				if (item.getAmount() >= amount) return true;
				amount -= item.getAmount();
			}
		}
		return false;
	}
	
	public void removeItems(Inventory inv, int amount) {
		ItemStack[] items = inv.getContents();
		for (int slot = 0; slot < items.length; slot++) {
			ItemStack item = items[slot];
			ItemStackable stackable = ItemStackableManager.getStackable(item);
			if (this.equals(stackable)) {
				if (item.getAmount() > amount) {
					item.setAmount(item.getAmount() - amount);
					return;
				}else if (item.getAmount() == amount) {
					inv.setItem(slot, null);
					return;
				}else {
					amount -= item.getAmount();
					inv.setItem(slot, null);
				}
			}
		}
	}
}