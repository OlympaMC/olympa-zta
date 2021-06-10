package fr.olympa.zta.weapons;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.spigot.item.ImmutableItemStack;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Bandage implements ItemStackable, Weapon {
	
	private static final Material MATERIAL = Material.FEATHER;
	private static final int COOLDOWN = 15 * 20;

	public static final Bandage BANDAGE = new Bandage();
	
	private final ImmutableItemStack item;
	private final NamespacedKey key;
	
	private Bandage() {
		key = ItemStackableManager.register(this);
		ItemStack item = new ItemStack(MATERIAL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§cBandage");
		meta.setLore(Arrays.asList("§8> §7Utilisable toutes les 15 secondes.", "", "§8> §cRedonne 5x ❤"));
		meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 0);
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
		this.item = new ImmutableItemStack(item);
	}
	
	@Override
	public String getName() {
		return "Bandage";
	}
	
	@Override
	public String getId() {
		return "bandage";
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
	
	@Override
	public NamespacedKey getKey() {
		return key;
	}
	
	@Override
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Player player = e.getPlayer();
		if (!player.hasCooldown(MATERIAL)) {
			if (!e.getItem().equals(player.getInventory().getItemInMainHand())) return;
			double health = player.getHealth();
			double maxHealth = player.getMaxHealth();
			if (health == maxHealth) return;
			player.setHealth(Math.min(health + 10, maxHealth));
			player.getWorld().spawnParticle(Particle.HEART, e.getPlayer().getLocation().add(0, 1, 0), 13, 1, 0.5, 1, 1, null, true);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.9f, 0.9f);
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§c§lTu as été heal !"));
			player.setCooldown(MATERIAL, COOLDOWN);
			
			int amount = e.getItem().getAmount();
			if (amount == 1) {
				player.getInventory().setItemInMainHand(null);
			}else e.getItem().setAmount(amount - 1);
			e.setCancelled(true);
		}
	}
	
}
