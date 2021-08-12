package fr.olympa.zta.itemstackable;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.spigot.item.ImmutableItemStack;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.map.DynmapLink;
import fr.olympa.zta.weapons.Weapon;

public class Brouilleur implements ItemStackable, Weapon { // Carte Virtuelle Magnétique
	
	private static final Material MATERIAL = Material.SCUTE;
	private static final int HIDDEN_SECONDS = 24 * 3600;

	public static final Brouilleur BROUILLEUR = new Brouilleur();
	
	private final ImmutableItemStack item;
	private final NamespacedKey key;
	
	private Brouilleur() {
		key = ItemStackableManager.register(this);
		ItemStack item = new ItemStack(MATERIAL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("§bBrouilleur C.V.M.");
		List<String> lore = SpigotUtils.wrapAndAlign("Une fois activé, vous n'êtes plus visible sur la carte pendant une journée.", 35);
		lore.add("");
		lore.add("§7> §c§lObjet consommable!");
		meta.setLore(lore);
		meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 0);
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
		this.item = new ImmutableItemStack(item);
		
		Bukkit.addRecipe(new ShapelessRecipe(new NamespacedKey(OlympaZTA.getInstance(), "brouilleur"), item)
				.addIngredient(new RecipeChoice.ExactChoice(QuestItem.BOITIER_PROG.getDemoItem()))
				.addIngredient(new RecipeChoice.ExactChoice(QuestItem.CARTE_MERE.getDemoItem()))
				.addIngredient(new RecipeChoice.ExactChoice(QuestItem.PILE.getDemoItem())));
	}
	
	@Override
	public String getName() {
		return "Brouilleur C.V.M.";
	}
	
	@Override
	public String getId() {
		return "brouilleur";
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
			
			OlympaPlayerZTA playerZTA = OlympaPlayerZTA.get(player);
			if (playerZTA.isHidden()) {
				Prefix.ERROR.sendMessage(player, "Tu es déjà caché de la carte virtuelle !");
				return;
			}
			
			playerZTA.setHidden(System.currentTimeMillis() + HIDDEN_SECONDS * 1000);
			DynmapLink.ifEnabled(link -> link.setPlayerVisiblity(player, false));
			
			player.getWorld().spawnParticle(Particle.CLOUD, e.getPlayer().getLocation().add(0, 1, 0), 50, 0.2, 0.8, 0.2, 0.01, null, true);
			player.getWorld().spawnParticle(Particle.SPELL_WITCH, e.getPlayer().getLocation().add(0, 1, 0), 50, 0.2, 0.8, 0.2, 0.01, null, true);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.9f, 0.9f);
			player.setCooldown(MATERIAL, HIDDEN_SECONDS * 20);
			
			Prefix.DEFAULT_GOOD.sendMessage(player, "Tu es caché de la carte virtuelle pendant une journée !");

			int amount = e.getItem().getAmount();
			if (amount == 1) {
				player.getInventory().setItemInMainHand(null);
			}else e.getItem().setAmount(amount - 1);
			e.setCancelled(true);
		}
	}
	
}
