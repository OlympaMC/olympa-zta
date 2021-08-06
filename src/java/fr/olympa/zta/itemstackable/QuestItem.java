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
	DECHET(
			Material.IRON_NUGGET,
			"Déchet métallique",
			"N'a aucune utilité tout seul, mais vous pouvez le revendre à un ferrailleur."),
	FERRAILLE(
			Material.IRON_INGOT,
			"Ferraille"),
	KEROSENE(
			Material.CLAY_BALL,
			"Kérosène"),
	PILE(
			Material.RABBIT_HIDE,
			"Pile magnétique",
			"Pièce de ferraille rare, pouvant être combinées à plusieurs matériaux pour créer des objets.",
			"Les zombies tank en possèdent parfois.",
			"",
			"À combiner avec une carte mère et un boîtier de programme pour crafter un §oBrouilleur C.V.M."),
	ANTIDOTE(
			Material.LEATHER,
			"Antidote"),
	BATTERIE(
			Material.QUARTZ,
			"Batterie"),
	AMAS(
			Material.GOLD_NUGGET,
			"Amas technologique",
			"Renferme des composants utiles. Certains ferrailleurs peuvent en racheter."),
	CARTE_MERE(
			Material.GOLD_INGOT,
			"Carte mère",
			"Utilisé dans un boîtier programmable, elle permet de créer des circuits.",
			"Certains zombies rapides peuvent en avoir sur eux.",
			"",
			"À combiner avec un boîtier et une pile magnétique pour crafter un §oBrouilleur C.V.M."),
	INDICE(
			Material.FIREWORK_STAR,
			"Indice"),
	BOITIER_ELEC(
			Material.PRISMARINE_CRYSTALS,
			"Boîtier éléctronique"),
	BOITIER_PROG(
			Material.PRISMARINE_SHARD,
			"Boîtier de programme",
			"Peut recevoir des composants électroniques pour créer des circuits.",
			"",
			"À combiner avec une carte mère et une pile magnétique pour crafter un §oBrouilleur C.V.M."),
	GENERATEUR_ENCOD(
			Material.SHULKER_SHELL,
			"Générateur encodé"),
	GENERATEUR_CRED(
			Material.POPPED_CHORUS_FRUIT,
			"Générateur de crédit"),
	;
	
	private final String name;
	private final NamespacedKey key;
	private final ImmutableItemStack item;

	private QuestItem(Material type, String name, String... lore) {
		this.name = name;
		
		key = ItemStackableManager.register(this);
		
		List<String> loreList = new ArrayList<>();
		for (String loreLine : lore) {
			if (loreLine == null || loreLine.isEmpty())
				loreList.add("");
			else
				loreList.addAll(SpigotUtils.wrapAndAlign(loreLine, 35));
		}
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