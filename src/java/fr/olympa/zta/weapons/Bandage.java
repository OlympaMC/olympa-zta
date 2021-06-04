package fr.olympa.zta.weapons;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.item.ImmutableItemStack;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Bandage implements ItemStackable, Weapon {
	
	private static final Material MATERIAL = Material.FEATHER;
	private static final int COOLDOWN = 15 * 20;

	public static final Bandage BANDAGE = new Bandage();
	
	private final ImmutableItemStack stack;
	private final NamespacedKey key;
	
	private Bandage() {
		key = ItemStackableManager.register(this);
		stack = new ImmutableItemStack(ItemStackableManager.processItem(ItemUtils.item(MATERIAL, "§cBandage", "§8➤ §7Utilisable toutes les 15 secondes.\n\n§8➤ §c Redonne 5x ❤"), this));
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
		return stack.toMutableStack(amount);
	}
	
	@Override
	public ItemStack createItem() {
		return stack.toMutableStack();
	}
	
	@Override
	public ImmutableItemStack getDemoItem() {
		return stack;
	}
	
	public NamespacedKey getKey() {
		return key;
	}
	
	@Override
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Player player = e.getPlayer();
		if (!player.hasCooldown(MATERIAL)) {
			if (!e.getItem().equals(player.getInventory().getItemInMainHand())) return;
			player.setHealth(e.getPlayer().getHealth() + 10);
			player.getWorld().spawnParticle(Particle.HEART, e.getPlayer().getLocation().add(0, 1.2, 0), 7, 0.5, 0.9, 0.5);
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§8➤ §cTu as été heal !"));
			player.setCooldown(MATERIAL, COOLDOWN);
			e.setCancelled(true);
		}
	}
	
}
