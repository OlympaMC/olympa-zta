package fr.olympa.zta.weapons.guns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;
import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;

public enum Accessory implements ItemStackable {

	CANNON_CAC(AccessoryType.CANNON, Material.GLOWSTONE_DUST, "Baïonnette", "augmente les dégâts au corps-à-corps (+3)"){
		@Override
		public void apply(Gun gun) {
			gun.damageCaC += 3;
		}
		
		@Override
		public void remove(Gun gun) {
			gun.damageCaC -= 3;
		}
	},
	CANNON_DAMAGE(AccessoryType.CANNON, Material.SUGAR, "Canon V2", "augmente le dégât des balles (+1)"){
		
		@Override
		public void apply(Gun gun) {
			gun.damageAdded += 1;
		}
		
		@Override
		public void remove(Gun gun) {
			gun.damageAdded -= 1;
		}
	},
	CANNON_POWER(AccessoryType.CANNON, Material.GUNPOWDER, "Canon lourd", "augmente la vitesse des balles"){
		private final AttributeModifier modifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 1.2f);
		
		@Override
		public void apply(Gun gun) {
			gun.bulletSpeed.addModifier(modifier);
		}
		
		@Override
		public void remove(Gun gun) {
			gun.bulletSpeed.removeModifier(modifier.getName());
		}
	},
	CANNON_SILENT(AccessoryType.CANNON, Material.REDSTONE, "Silencieux", "réduit le volume de la détonation"){
		private final AttributeModifier modifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 0.25f);
		
		@Override
		public void apply(Gun gun) {
			gun.fireVolume.addModifier(modifier);
		}
		
		@Override
		public void remove(Gun gun) {
			gun.fireVolume.removeModifier(modifier.getName());
		}
	},
	CANNON_STABILIZER(AccessoryType.CANNON, Material.GHAST_TEAR, "Stabilisateur", "augmente la cadence de tir", "diminue la dispersion des balles"){
		private final AttributeModifier rateModifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 1.2f);
		private final AttributeModifier spreadModifier = new AttributeModifier("cannon", Operation.MULTIPLY_VALUE, 0.8f);
		
		@Override
		public void apply(Gun gun) {
			gun.fireRate.addModifier(rateModifier);
			gun.bulletSpread.addModifier(spreadModifier);
		}
		
		@Override
		public void remove(Gun gun) {
			gun.fireRate.removeModifier(rateModifier.getName());
			gun.bulletSpread.removeModifier(spreadModifier.getName());
		}
	},
	SCOPE_LIGHT(AccessoryType.SCOPE, Material.BRICK, "Mire V2", "dispose d'un zoom faible", "donne la vision nocturne"){
		private final AttributeModifier zoomModifier = new AttributeModifier("zoom", Operation.ADD_MULTIPLICATOR, -1);
		private final PotionEffect effect = new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 0);
		
		@Override
		public void apply(Gun gun) {
			gun.zoomModifier = zoomModifier;
		}
		
		@Override
		public void remove(Gun gun) {
			gun.zoomModifier = null;
		}
		
		@Override
		public void zoomToggled(Player p, boolean zoom) {
			if (zoom) {
				p.addPotionEffect(effect);
			}else p.removePotionEffect(effect.getType());
		}
	},
	SCOPE_STRONG(AccessoryType.SCOPE, Material.NETHER_BRICK, "Lunette x3", "dispose d'un zoom puissant"){
		private final AttributeModifier zoomModifier = new AttributeModifier("zoom", Operation.ADD_MULTIPLICATOR, -3);
		
		@Override
		public void apply(Gun gun) {
			gun.zoomModifier = zoomModifier;
		}
		
		@Override
		public void remove(Gun gun) {
			gun.zoomModifier = null;
		}
	},
	STOCK_LIGHT(AccessoryType.STOCK, Material.SPRUCE_FENCE_GATE, "Crosse légère", "réduit le recul"){
		private final AttributeModifier modifier = new AttributeModifier("stock", Operation.MULTIPLY_VALUE, 0.8f);
		
		@Override
		public void apply(Gun gun) {
			gun.knockback.addModifier(modifier);
		}
		
		@Override
		public void remove(Gun gun) {
			gun.knockback.removeModifier(modifier.getName());
		}
	},
	STOCK_STRONG(AccessoryType.STOCK, Material.BIRCH_FENCE_GATE, "Crosse lourde", "réduit la dispersion des balles"){
		private final AttributeModifier modifier = new AttributeModifier("stock", Operation.MULTIPLY_VALUE, 0.8f);
		
		@Override
		public void apply(Gun gun) {
			gun.bulletSpread.addModifier(modifier);
		}
		
		@Override
		public void remove(Gun gun) {
			gun.bulletSpread.removeModifier(modifier.getName());
		}
	},
	;
	
	private final AccessoryType type;
	private final String name;
	
	private final ItemStack item;
	
	private Accessory(AccessoryType type, Material material, String name, String... effects) {
		this.type = type;
		this.name = name;
		
		item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§a" + name);
		List<String> lore = new ArrayList<>();
		lore.add("§8> Objet de type §7" + type.name);
		lore.add("§8> " + (effects.length < 2 ? "Effet" : "Effets") + ":");
		for (String effect : effects) {
			lore.add("§7● " + effect);
		}
		meta.setLore(lore);
		meta.setCustomModelData(1);
		meta.getPersistentDataContainer().set(AccessoriesGUI.ACCESSORY_KEY, PersistentDataType.INTEGER, ordinal());
		item.setItemMeta(meta);
		ItemStackableManager.processItem(item, this);
	}
	
	public AccessoryType getType() {
		return type;
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
	public ItemStack createItem() {
		return item.clone();
	}

	public abstract void apply(Gun gun);
	
	public abstract void remove(Gun gun);
	
	public void zoomToggled(Player p, boolean zoom) {}
	
	public enum AccessoryType{
		
		SCOPE("Lunette", 13), CANNON("Canon", 20), STOCK("Crosse", 33);
		
		private String name;
		private int slot;
		private ItemStack available;
		private ItemStack unavailable;
		
		private AccessoryType(String name, int slot){
			this.name = name;
			this.slot = slot;
			this.available = ItemUtils.item(Material.LIME_STAINED_GLASS_PANE, "§aEmplacement disponible : " + name);
			this.unavailable = ItemUtils.item(Material.RED_STAINED_GLASS_PANE, "§cEmplacement indisponible : " + name);
		}
		
		public String getName(){
			return name;
		}
		
		public int getSlot(){
			return slot;
		}
		
		public ItemStack getAvailableItemSlot(){
			return available;
		}
		
		public ItemStack getUnavailableItemSlot(){
			return unavailable;
		}
		
		public boolean isEnabled(Gun gun){
			return gun.getType().getAllowedAccessories().contains(this);
		}
		
		public Accessory get(Gun gun) {
			switch (this) {
			case SCOPE:
				return gun.scope;
			case CANNON:
				return gun.cannon;
			case STOCK:
				return gun.stock;
			}
			return null;
		}

		public static AccessoryType getFromSlot(int slot){
			for (AccessoryType type : values()) {
				if (type.slot == slot) return type;
			}
			return null;
		}
	}
	
}
