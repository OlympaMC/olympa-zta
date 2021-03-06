package fr.olympa.zta.weapons.guns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.Attribute;
import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.weapons.Weapon;
import fr.olympa.zta.weapons.guns.Accessory.AccessoryType;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.skins.Skin;
import fr.olympa.zta.weapons.skins.Skinable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Gun implements Weapon, Skinable {

	public static final List<ChatColor> TIERS = Arrays.asList(ChatColor.GREEN, ChatColor.AQUA, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.GOLD);
	public static final UUID ZOOM_UUID = UUID.fromString("8a1c6742-3f54-44c2-ac6f-90fa7491ebef");
	
	private static DecimalFormat timeFormat = new DecimalFormat("#0.0");

	protected final int id;
	protected final GunType type;
	
	protected Skin skin = Skin.NORMAL;
	
	protected int beforeTrainingAmmos = -1;
	
	protected int ammos = 0;
	protected boolean ready = false;
	protected boolean zoomed = false;
	protected boolean secondaryMode = false;
	protected BukkitTask reloading = null;

	public float damageAdded = 0;
	public float damageCaC = 0;
	public AttributeModifier zoomModifier = null;
	public final Attribute maxAmmos;
	public final Attribute chargeTime;
	public final Attribute bulletSpeed;
	public final Attribute bulletSpread;
	public final Attribute knockback;
	public final Attribute fireRate;
	public final Attribute fireVolume;

	public Accessory scope;
	public Accessory cannon;
	public Accessory stock;
	
	public float customDamagePlayer, customDamageEntity;

	Gun(int id, GunType type) {
		this.id = id;
		this.type = type;
		
		maxAmmos = new Attribute(type.getMaxAmmos());
		chargeTime = new Attribute(type.getChargeTime());
		bulletSpeed = new Attribute(type.getBulletSpeed());
		bulletSpread = new Attribute(type.getAccuracy().getBulletSpread());
		knockback = new Attribute(type.getKnockback());
		fireRate = new Attribute(type.getFireRate());
		fireVolume = new Attribute(type.getFireVolume());
	}
	
	public int getID() {
		return id;
	}
	
	public GunType getType() {
		return type;
	}
	
	public void saveBeforeTrainingAmmos() {
		beforeTrainingAmmos = ammos;
	}
	
	public void restoreBeforeTrainingAmmos(Player p, ItemStack item) {
		if (beforeTrainingAmmos != -1) {
			cancelReload(p, item);
			ammos = beforeTrainingAmmos;
			beforeTrainingAmmos = -1;
			if (ammos != 0) ready = true;
			updateItemName(item);
		}
	}
	
	public ItemStack createItemStack() {
		return createItemStack(true);
	}

	public ItemStack createItemStack(boolean accessories) {
		ItemStack item = new ItemStack(type.getMaterial());
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.values());
		meta.getPersistentDataContainer().set(getKey(), PersistentDataType.INTEGER, getID());
		meta.getPersistentDataContainer().set(type.getKey(), PersistentDataType.BYTE, (byte) 0);
		meta.setCustomModelData(1);
		meta.setLore(getLore(accessories));
		item.setItemMeta(meta);
		updateItemName(item);
		return item;
	}

	public void updateItemName(ItemStack item) {
		try {
			ItemMeta im = item.getItemMeta();
			im.setDisplayName("??e" + (!type.hasSecondaryMode() ? "" : secondaryMode ? "?????? " : "?????? ") + type.getName() + " [" + ammos + "/" + (int) maxAmmos.getValue() + "] " + (ready ? "???" : "???") + (reloading == null ? "" : " recharge"));
			item.setItemMeta(im);
		}catch (Exception ex) {
			OlympaZTA.getInstance().sendMessage("??cUne erreur est survenue lors de la mise ?? jour d'un item d'arme.");
			ex.printStackTrace();
		}
	}

	public void updateItemCustomModel(ItemStack item) {
		try {
			ItemMeta im = item.getItemMeta();
			im.setCustomModelData(skin.getId() * 2 + (zoomed ? 2 : 1));
			item.setItemMeta(im);
		}catch (Exception ex) {
			OlympaZTA.getInstance().sendMessage("??cUne erreur est survenue lors de la mise ?? jour d'un item d'arme.");
			ex.printStackTrace();
		}
	}

	public List<String> getLore(boolean accessories) {
		List<String> lore = new ArrayList<>(type.getLore());
		if (accessories) lore.addAll(type.getAccessoriesLore(getAccessoriesAmount()));
		lore.add("");
		lore.add("??8Arme immatricul??e > ??7[G" + id + "]");
		return lore;
	}

	public NamespacedKey getKey() {
		return GunRegistry.GUN_KEY;
	}
	
	@Override
	public ItemStack getSkinItem(Skin skin) {
		Skin oldSkin = this.skin;
		this.skin = skin;
		ItemStack item = createItemStack(false);
		this.skin = oldSkin;
		return item;
	}
	
	@Override
	public Skin getSkinOfItem(ItemStack item) {
		return skin;
	}
	
	@Override
	public void setSkin(Skin skin, ItemStack item) {
		this.skin = skin;
		updateItemCustomModel(item);
	}
	
	@Override
	public void onEntityHit(EntityDamageByEntityEvent e) {
		Player damager = (Player) e.getDamager();
		if (damageCaC == 0) {
			onInteract(new PlayerInteractEvent(damager, Action.LEFT_CLICK_AIR, damager.getInventory().getItemInMainHand(), null, null));
			e.setCancelled(true);
		}else {
			e.setDamage(damageCaC);
		}
	}

	@Override
	public void itemHeld(Player p, ItemStack item, Weapon previous) {
		showAmmos(p);
		p.setCooldown(item.getType(), 0);
		if (type.hasHeldEffect()) p.addPotionEffect(type.getHeldEffect());
		int readyTime = -1;
		float thisPotential = fireRate.getValue();
		if (thisPotential <= 0) {
			if (ammos == 0) return;
			thisPotential = chargeTime.getValue();
		}
		if (previous instanceof Gun prev && !prev.ready) {
			float prevPotential = prev.fireRate.getValue();
			if (prevPotential <= 0) prevPotential = prev.chargeTime.getValue();
			readyTime = (int) Math.min(prevPotential, thisPotential);
		}
		if (readyTime == -1 && !ready) readyTime = (int) thisPotential;
		if (readyTime != -1) {
			ready = false;
			updateItemName(item);
			setCooldown(p, readyTime);
			task = new BukkitRunnable() {
				@Override
				public void run() {
					setReady(p, item);
					cancel();
				}
				@Override
				public synchronized void cancel() throws IllegalStateException {
					super.cancel();
					task = null;
				}
			}.runTaskLater(OlympaZTA.getInstance(), readyTime);
		}
	}

	private void setCooldown(Player p, int readyTime) {
		if (readyTime > 5) p.setCooldown(type.getMaterial(), readyTime);
	}

	@Override
	public void itemNoLongerHeld(Player p, ItemStack item) {
		if (zoomed) toggleZoom(p, item);
		if (reloading != null) cancelReload(p, item);
		if (type.hasHeldEffect()) p.removePotionEffect(type.getHeldEffect().getType());
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	public void drop(Player p, ItemStack item) {
		if (Bukkit.isPrimaryThread()) {
			reload(p, item);
		}else
			Bukkit.getScheduler().runTask(OlympaZTA.getInstance(), () -> reload(p, item));
	}

	private BukkitTask task;
	private long lastClick;

	@Override
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		e.setCancelled(true);
		
		lastClick = System.currentTimeMillis();
		
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) { // clic droit : tir
			if (reloading != null) {
				if (type.isOneByOneCharge() && ammos > 0) {
					cancelReload(p, item);
				}else return;
			}
			if (ammos == 0) { // tentative de tir alors que le barillet est vide
				reload(p, item);
			}else if (ready && isFireEnabled(p) && task == null) {
				if (getCurrentMode() == GunMode.BLAST) {
					ready = false;
					int rate = (int) (fireRate.getValue() / 2);
					task = new BukkitRunnable() {
						byte left = 3;
						@Override
						public void run() {
							if (left-- > 0) {
								fire(p);
								updateItemName(item);
								if (ammos == 0) {
									cancel();
								}
								if (left == 0) p.setCooldown(type.getMaterial(), rate * 3);
							}else if (left == -4) {
								setReady(p, item);
								cancel();
							}
						}
						@Override
						public synchronized void cancel() throws IllegalStateException {
							super.cancel();
							task = null;
						}
					}.runTaskTimer(OlympaZTA.getInstance(), 0, rate);
				}else if (fireRate.getValue() == -1) {
					ready = false;
					fire(p);
					updateItemName(item);
				}else {
					ready = false;
					int rate = (int) fireRate.getValue();
					task = new BukkitRunnable() {
						@Override
						public void run() {
							if (ammos == 0) {
								updateItemName(item);
								cancel();
								return;
							}
							if (System.currentTimeMillis() - lastClick < 210) {
								fire(p);
								setCooldown(p, rate);
								updateItemName(item);
							}else {
								setReady(p, item);
								cancel();
							}
						}

						@Override
						public synchronized void cancel() throws IllegalStateException {
							super.cancel();
							task = null;
						}
					}.runTaskTimer(OlympaZTA.getInstance(), 0, rate);
				}
			}
		}else if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) { // clic gauche : tir
			if (reloading == null) secondaryClick(p, item);
		}
	}
	
	private void setReady(Player p, ItemStack item) {
		ready = true;
		playReadySound(p.getLocation());
		updateItemName(item);
	}

	public boolean isFireEnabled(Player p) {
		GunFlag gunFlag = getGunFlag(p);
		if (gunFlag == null) return false;
		return gunFlag.isFireEnabled(p, true);
	}
	
	public GunFlag getGunFlag(Player p) {
		return OlympaCore.getInstance().getRegionManager().getMostImportantFlag(p.getLocation(), GunFlag.class);
	}

	public void itemClick(Player p, ItemStack item) {
		new AccessoriesGUI(this, item).create(p);
	}

	private void secondaryClick(Player p, ItemStack item) {
		GunAction action = getSecondClickAction();
		if (action == null) return;
		switch (action) {
		case CHANGE_MODE:
			secondaryMode = !secondaryMode;
			updateItemName(item);
			break;
		case ZOOM:
			toggleZoom(p, item);
			break;
		}
	}

	private void fire(Player p) {
		Bullet bullet = type.createBullet(this, (customDamagePlayer == 0 ? type.getPlayerDamage() : customDamagePlayer) + damageAdded, (customDamageEntity == 0 ? type.getEntityDamage() : customDamageEntity) + damageAdded);
		for (int i = 0; i < type.getFiredBullets(); i++) {
			bullet.launchProjectile(p, p.getLocation().getDirection());
		}
		
		float knockback = this.knockback.getValue();
		if (knockback != 0) {
			if (p.isSneaking()) knockback /= 2;
			Vector velocity = p.getLocation().getDirection().multiply(-knockback).add(p.getVelocity());
			velocity.setY(velocity.getY() / 3);
			p.setVelocity(velocity);
		}
		ammos--;
		
		playFireSound(p.getLocation());
		float distance = (fireVolume.getValue() - 0.5f) * 10;
		for (Entity en : p.getWorld().getNearbyEntities(p.getLocation(), distance, distance, distance, x -> x instanceof Zombie)) {
			Zombie zombie = (Zombie) en;
			if (zombie.getTarget() == null && ThreadLocalRandom.current().nextBoolean()) zombie.setTarget(p);
		}
	}

	public void showAmmos(Player p) {
		int availableAmmos = shouldTakeItems(p) ? type.getAmmoType().getAmmos(p) : -1;
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(type.getAmmoType().getColoredName() + "??7: " + (availableAmmos == -1 ? "??c???" : (availableAmmos == 0 ? "??c0" : availableAmmos))));
	}
	
	private void cancelReload(Player p, ItemStack item) {
		if (reloading != null) {
			reloading.cancel();
			reloading = null;
		}
		updateItemName(item);
		//p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
		showAmmos(p);
	}
	
	protected boolean shouldTakeItems(Player p) {
		GunFlag gunFlag = getGunFlag(p);
		return p.getGameMode() != GameMode.CREATIVE && (gunFlag == null || !gunFlag.isFreeAmmos());
	}

	private boolean reload(Player p, ItemStack item) {
		if (reloading != null) return false;
		if (zoomed) toggleZoom(p, item);

		int max = (int) maxAmmos.getValue();
		if (max <= ammos) return false;

		int toCharge;
		int availableAmmos = shouldTakeItems(p) ? type.getAmmoType().getAmmos(p) : Integer.MAX_VALUE;
		if (availableAmmos == 0) {
			playOutOfAmmosSound(p.getLocation());
			showAmmos(p);
			return false;
		}
		if (type.isOneByOneCharge()) {
			toCharge = 1;
		}else toCharge = Math.min((int) Math.ceil((max - ammos) / (double) type.getAmmoType().getAmmosPerItem()), availableAmmos);

		reloading = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), new Runnable() {
			final short animationMax = 13;
			final char character = '???'; //'???';

			short time = (short) chargeTime.getValue();
			float add = (float) animationMax / (float) time;
			float current = 0;

			@Override
			public void run() {
				if (time == 0) {
					ammos = shouldTakeItems(p) ? Math.min(ammos + type.getAmmoType().removeAmmos(p, toCharge) * type.getAmmoType().getAmmosPerItem(), max) : Math.min(max, ammos + (type.isOneByOneCharge() ? 1 : max));
					if (ammos != 0) ready = true;
					playChargeCompleteSound(p.getLocation());

					if (type.isOneByOneCharge() && maxAmmos.getValue() > ammos) {
						reloading.cancel();
						reloading = null;
						if (!reload(p, item)) { // relancer une charge
							updateItemName(item); // update si plus assez de munitions = recharge termin??e
						}
						return;
					}
					cancelReload(p, item);
					return;
				}
				StringBuilder status = new StringBuilder("??bRechargement... ");
				boolean changed = false;
				for (int i = 0; i < animationMax; i++) {
					if (!changed && i >= current) {
						status.append("??c");
						changed = true;
					}
					status.append(character);
				}
				status.append("??b " + timeFormat.format(time / 20D) + "s");
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(status.toString()));
				current += add;
				time--;
			}
		}, 0, 1);

		updateItemName(item);
		playChargeSound(p.getLocation());
		return true;
	}

	private void toggleZoom(Player p, ItemStack item) {
		AttributeInstance attribute = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED);
		if (zoomed) {
			attribute.removeModifier(getZoomModifier().getBukkitModifier());
		}else {
			try {
				attribute.addModifier(getZoomModifier().getBukkitModifier());
			}catch (IllegalArgumentException ex) {
				OlympaZTA.getInstance().sendMessage("??cZoom d??j?? appliqu?? sur un gun.");
			}
		}
		zoomed = !zoomed;
		if (scope != null) scope.zoomToggled(p, zoomed);
		updateItemCustomModel(item);
	}

	public GunMode getCurrentMode() {
		return secondaryMode ? type.getSecondaryMode() : type.getPrimaryMode();
	}

	/**
	 * @return Action effectu??e lors du clic secondaire
	 */
	public GunAction getSecondClickAction() {
		return type.hasSecondaryMode() ? GunAction.CHANGE_MODE : hasZoom() ? GunAction.ZOOM : null;
	}
	
	public boolean hasZoom() {
		return type.hasZoom() || (zoomModifier != null);
	}
	
	public AttributeModifier getZoomModifier() {
		return zoomModifier == null ? type.getZoomModifier() : zoomModifier;
	}

	public int getAccessoriesAmount() {
		int i = 0;
		if (scope != null) i++;
		if (cannon != null) i++;
		if (stock != null) i++;
		return i;
	}
	
	public boolean setAccessory(Accessory accessory) {
		return setAccessory(accessory.getType(), accessory);
	}

	public boolean setAccessory(AccessoryType type, Accessory accessory) {
		Accessory old = null;
		switch (type) {
		case SCOPE:
			old = scope;
			scope = accessory;
			break;
		case CANNON:
			old = cannon;
			cannon = accessory;
			break;
		case STOCK:
			old = stock;
			stock = accessory;
			break;
		}
		if (old != null) old.remove(this);
		if (accessory != null) accessory.apply(this);
		return old != null;
	}

	/**
	 * Jouer le son de tir
	 * @param lc location o?? est jou??e le son
	 */
	public void playFireSound(Location lc) {
		lc.getWorld().playSound(lc, type.getFireSound().getSound(), SoundCategory.PLAYERS, fireVolume.getValue() + 0.5f, 1);
	}

	/**
	 * Jouer le son de recharge
	 * @param lc location o?? est jou??e le son
	 */
	public void playChargeSound(Location lc) {
		lc.getWorld().playSound(lc, Sound.BLOCK_PISTON_EXTEND, SoundCategory.PLAYERS, 1, 1);
	}

	/**
	 * Jouer le son de recharge compl??te
	 * @param lc location o?? est jou??e le son
	 */
	public void playChargeCompleteSound(Location lc) {
		lc.getWorld().playSound(lc, Sound.BLOCK_PISTON_CONTRACT, SoundCategory.PLAYERS, 1, 1);
	}

	/**
	 * Jouer le son de par?? au tir
	 * @param lc location o?? est jou??e le son
	 */
	public void playReadySound(Location lc) {
		lc.getWorld().playSound(lc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.PLAYERS, 0.5f, 1);
	}

	/**
	 * Jouer le son de chargeur vide
	 * @param lc location o?? est jou??e le son
	 */
	public void playOutOfAmmosSound(Location lc) {
		lc.getWorld().playSound(lc, Sound.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 0.8f, 1);
	}

	public synchronized void updateDatas(PreparedStatement statement) throws SQLException {
		int i = 1;
		statement.setInt(i++, ammos);
		statement.setBoolean(i++, ready);
		statement.setBoolean(i++, zoomed);
		statement.setBoolean(i++, secondaryMode);
		statement.setInt(i++, scope == null ? -1 : scope.ordinal());
		statement.setInt(i++, cannon == null ? -1 : cannon.ordinal());
		statement.setInt(i++, stock == null ? -1 : stock.ordinal());
		statement.setInt(i++, skin.getId());
		statement.setInt(i++, getID());
	}
	
	public void loadDatas(ResultSet set) throws Exception {
		ammos = set.getInt("ammos");
		ready = set.getBoolean("ready");
		zoomed = set.getBoolean("zoomed");
		secondaryMode = set.getBoolean("secondary_mode");
		int scopeType = set.getInt("scope_id");
		int cannonType = set.getInt("cannon_id");
		int stockType = set.getInt("stock_id");
		if (scopeType != -1) setAccessory(Accessory.values()[scopeType]);
		if (cannonType != -1) setAccessory(Accessory.values()[cannonType]);
		if (stockType != -1) setAccessory(Accessory.values()[stockType]);
		skin = Skin.getFromId(set.getInt("skin"));
	}
	
	public enum GunAction {
		ZOOM, CHANGE_MODE;
	}

	public enum GunMode {
		SINGLE("tir unique"),
		SEMI_AUTOMATIC("semi-automatique"),
		AUTOMATIC("automatique"),
		BLAST("rafales");

		private String name;

		private GunMode(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public enum GunAccuracy {
		EXTREME("Extr??me", 0), HIGH("Bonne", 0.05f), MEDIUM("Moyenne", 0.2f), LOW("Faible", 0.7f);

		private String name;
		private float spread;

		private GunAccuracy(String name, float spread) {
			this.name = name;
			this.spread = spread;
		}

		public String getName() {
			return name;
		}

		public float getBulletSpread() {
			return spread;
		}

	}

}