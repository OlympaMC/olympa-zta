package fr.olympa.zta.weapons.guns;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.utils.Attribute;
import fr.olympa.zta.weapons.Weapon;
import fr.olympa.zta.weapons.guns.accessories.Accessory;
import fr.olympa.zta.weapons.guns.accessories.Accessory.AccessoryType;
import fr.olympa.zta.weapons.guns.accessories.Cannon;
import fr.olympa.zta.weapons.guns.accessories.Scope;
import fr.olympa.zta.weapons.guns.accessories.Stock;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class Gun extends Weapon {

	private static DecimalFormat attributeFormat = new DecimalFormat("##.##");
	private static DecimalFormat timeFormat = new DecimalFormat("#0.0");

	public static final String TABLE_NAME = "`zta_guns`";

	protected int ammos = 0;
	protected boolean ready = false;
	protected boolean zoomed = false;
	protected boolean secondaryMode = false;
	protected BukkitTask reloading = null;

	public float damageAdded = 0;
	public float damageCaC = 0;
	public AttributeModifier zoomModifier = null;
	public final Attribute maxAmmos = new Attribute(getMaxAmmos());
	public final Attribute chargeTime = new Attribute(getChargeTime());
	public final Attribute bulletSpeed = new Attribute(getBulletSpeed());
	public final Attribute bulletSpread = new Attribute(getAccuracy().getBulletSpread());
	public final Attribute knockback = new Attribute(getKnockback());
	public final Attribute fireRate = new Attribute(getFireRate());
	public final Attribute fireVolume = new Attribute(getFireVolume());

	public Scope scope;
	public Cannon cannon;
	public Stock stock;
	
	public float customDamagePlayer, customDamageEntity;

	public Gun(int id) {
		super(id);
	}

	public ItemStack createItemStack() {
		return createItemStack(true);
	}

	public ItemStack createItemStack(boolean accessories) {
		ItemStack item = ItemUtils.item(getItemMaterial(), getName());
		updateItemName(item);
		updateItemLore(item, accessories);
		return addIdentifier(item);
	}

	public void updateItemName(ItemStack item) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName("§e" + (getSecondaryMode() == null ? "" : secondaryMode ? "◁▶ " : "◀▷ ") + getName() + " [" + ammos + "/" + (int) maxAmmos.getValue() + "] " + (ready ? "●" : "○") + (reloading == null ? "" : " recharge"));
		item.setItemMeta(im);
	}

	public void updateItemCustomModel(ItemStack item) {
		ItemMeta im = item.getItemMeta();
		im.setCustomModelData(zoomed ? 2 : 1);
		item.setItemMeta(im);
	}

	public void updateItemLore(ItemStack item, boolean accessories) {
		ItemMeta im = item.getItemMeta();
		List<String> lore = new ArrayList<>(Arrays.asList(
				getFeatureLoreLine("Cadence de tir", attributeFormat.format(getFireRate() / 20D) + "s"), getFeatureLoreLine("Temps de recharge", attributeFormat.format(getChargeTime() / 20D)
						+ "s"),
				getFeatureLoreLine("Munitions", getAmmoType().getName()),
				getFeatureLoreLine("Précision", getAccuracy().getName()),
				getFeatureLoreLine("Mode de tir", getPrimaryMode().getName() + (getSecondaryMode() == null ? "" : "/" + getSecondaryMode().getName()))));
		if (accessories) {
			lore.addAll(Arrays.asList(
					"",
					"§6§lAccessoires §r§6: §e[§n" + getAccessoriesAmount() + "§r§e/" + getAllowedAccessoriesAmount() + "]",
					"§e§oClic droit pour attacher des accessoires"));
		}
		lore.addAll(getIDLoreLines());
		im.setLore(lore);
		item.setItemMeta(im);
	}

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
	public void itemHeld(Player p, ItemStack item) {
		p.setCooldown(item.getType(), 0);
	}

	@Override
	public void itemNoLongerHeld(Player p, ItemStack item) {
		if (zoomed) toggleZoom(p, item);
		if (reloading != null) {
			cancelReload(p, item);
		}
	}

	public boolean drop(Player p, ItemStack item) {
		reload(p, item);
		return true;
	}

	private BukkitTask task;
	private long lastClick;

	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		e.setCancelled(true);

		lastClick = System.currentTimeMillis();

		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) { // clic droit : tir
			if (reloading != null) {
				if (isOneByOneCharge() && ammos > 0) {
					cancelReload(p, item);
				}else return;
			}
			if (ammos == 0) { // tentative de tir alors que le barillet est vide
				reload(p, item);
			}else if (ready && fireEnabled(p) && task == null) {
				if (getCurrentMode() == GunMode.BLAST) {
					ready = false;
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
							}else if (left == -3) {
								ready = true;
								playReadySound(p.getLocation());
								updateItemName(item);
								cancel();
							}
						}
						@Override
						public synchronized void cancel() throws IllegalStateException {
							super.cancel();
							task = null;
						}
					}.runTaskTimer(OlympaZTA.getInstance(), 0, (int) (fireRate.getValue() / 3L));
				}else if (fireRate.getValue() == -1) {
					ready = false;
					fire(p);
					updateItemName(item);
				}else {
					ready = false;
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
								updateItemName(item);
							}else {
								ready = true;
								playReadySound(p.getLocation());
								updateItemName(item);
								cancel();
							}
						}

						public synchronized void cancel() throws IllegalStateException {
							super.cancel();
							task = null;
						}
					}.runTaskTimer(OlympaZTA.getInstance(), 0, (long) fireRate.getValue());
				}
			}
		}else 	if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) { // clic gauche : tir
			if (reloading == null) secondaryClick(p, item);
		}
	}

	private boolean fireEnabled;
	public boolean fireEnabled(Player p) {
		fireEnabled = true;
		OlympaCore.getInstance().getRegionManager().fireEvent(p.getLocation(), NoGunFlag.class, x -> fireEnabled = x.isFireEnabled());
		return fireEnabled;
		//return !OlympaZTA.getInstance().hub.isInHub(p.getLocation()) && OlympaZTA.getInstance().mobSpawning.world == p.getWorld();
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
		Bullet bullet = getFiredBullet(p, (customDamagePlayer == 0 ? getBulletPlayerDamage() : customDamagePlayer) + damageAdded, (customDamageEntity == 0 ? getBulletEntityDamage() : customDamageEntity) + damageAdded);
		for (int i = 0; i < getFiredBulletsAmount(); i++) {
			bullet.launchProjectile(p);
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
		float distance = (fireVolume.getValue() - 0.5f) * 16;
		for (Entity en : p.getWorld().getNearbyEntities(p.getLocation(), distance, distance, distance, x -> x instanceof Zombie)) {
			Zombie zombie = (Zombie) en;
			if (zombie.getTarget() == null) zombie.setTarget(p);
		}
	}

	private void cancelReload(Player p, ItemStack item) {
		reloading.cancel();
		reloading = null;
		updateItemName(item);
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent());
	}

	private void reload(Player p, ItemStack item) {
		if (reloading != null) return;
		if (zoomed) toggleZoom(p, item);

		int max = (int) maxAmmos.getValue();
		if (max >= ammos) return;

		int toCharge;
		int availableAmmos = getAmmoType().getAmmos(p);
		if (availableAmmos == 0) {
			playOutOfAmmosSound(p.getLocation());
			return;
		}
		if (isOneByOneCharge()) {
			toCharge = 1;
		}else toCharge = Math.min((int) Math.ceil((max - ammos) / (double) getAmmoType().getAmmosPerItem()), availableAmmos);

		reloading = Bukkit.getScheduler().runTaskTimerAsynchronously(OlympaZTA.getInstance(), new Runnable() {
			final short animationMax = 13;
			final char character = '░'; //'█';

			short time = (short) chargeTime.getValue();
			float add = (float) animationMax / (float) time;
			float current = 0;

			@Override
			public void run() {
				if (time == 0) {
					ammos = Math.min(ammos + getAmmoType().removeAmmos(p, toCharge) * getAmmoType().getAmmosPerItem(), max);
					if (ammos != 0) ready = true;
					playChargeCompleteSound(p.getLocation());

					if (isOneByOneCharge() && maxAmmos.getValue() > ammos) {
						reloading.cancel();
						reloading = null;
						reload(p, item); // relancer une charge
					}else {
						cancelReload(p, item);
					}
					return;
				}
				StringBuilder status = new StringBuilder("§bRechargement... ");
				boolean changed = false;
				for (int i = 0; i < animationMax; i++) {
					if (i >= current && !changed) {
						status.append("§c");
						changed = true;
					}
					status.append(character);
				}
				status.append("§b " + timeFormat.format(time / 20D) + "s");
				p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(status.toString()));
				current += add;
				time--;
			}
		}, 0, 1);

		updateItemName(item);
		playChargeSound(p.getLocation());
	}

	private void toggleZoom(Player p, ItemStack item) {
		if (zoomed) {
			p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(getZoomModifier());
		}else {
			p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).addModifier(getZoomModifier());
		}
		zoomed = !zoomed;
		if (scope != null) scope.zoomToggled(p, zoomed);
		updateItemCustomModel(item);
	}

	/**
	 * @return Type de munition
	 */
	public abstract AmmoType getAmmoType();

	/**
	 * @return Maximum de munitions
	 */
	protected abstract int getMaxAmmos();

	/**
	 * @return Temps (en ticks) entre chaque coup de feu
	 */
	protected abstract int getFireRate();

	/**
	 * @return Temps (en ticks) avant la recharge complète
	 */
	protected abstract int getChargeTime();

	/**
	 * @return <tt>true</tt> si la charge se fait munition par munition
	 */
	protected boolean isOneByOneCharge() {
		return false;
	}

	/**
	 * @return Puissance de recul en m/s
	 */
	protected abstract float getKnockback();

	/**
	 * @return Vitesse de la balle en m/s
	 */
	protected abstract float getBulletSpeed();

	/**
	 * @return Rayon du dispersement des balles (en m)
	 */
	protected abstract GunAccuracy getAccuracy();

	/**
	 * @return Dommages donnés aux joueurs par la balle tirée
	 */
	protected abstract float getBulletPlayerDamage();
	
	/**
	 * @return Dommages donnés aux entités par la balle tirée
	 */
	protected abstract float getBulletEntityDamage();
	
	/**
	 * @param p Player qui tire la balle
	 * @param playerDamage Dommages aux joueurs
	 * @param entityDamage Dommages aux entités
	 * @return instance de {@link Bullet}
	 */
	public abstract Bullet getFiredBullet(Player p, float playerDamage, float entityDamage);

	public int getFiredBulletsAmount() {
		return 1;
	}
	
	/**
	 * @return Mode de tir principal
	 */
	public abstract GunMode getPrimaryMode();

	/**
	 * @return Mode de tir secondaire
	 */
	public GunMode getSecondaryMode() {
		return null;
	}

	public GunMode getCurrentMode() {
		return secondaryMode ? getSecondaryMode() : getPrimaryMode();
	}

	/**
	 * @return Action effectuée lors du clic secondaire
	 */
	public GunAction getSecondClickAction() {
		return getSecondaryMode() != null ? GunAction.CHANGE_MODE : getZoomModifier() != null ? GunAction.ZOOM : null;
	}

	/**
	 * @return AttributModifier sur l'attribut {@link Attribute#GENERIC_MOVEMENT_SPEED}
	 */
	public AttributeModifier getZoomModifier() {
		return zoomModifier == null ? null : zoomModifier;
	}

	/**
	 * @return Est-ce qu'un canon peut être attaché à l'arme
	 */
	public boolean isCannonAllowed() {
		return false;
	}

	/**
	 * @return Est-ce qu'une lunette peut être attachée à l'arme
	 */
	public boolean isScopeAllowed() {
		return false;
	}

	/**
	 * @return Est-ce qu'une crosse peut être attachée à l'arme
	 */
	public boolean isStockAllowed() {
		return false;
	}

	public int getAccessoriesAmount() {
		int i = 0;
		if (scope != null) i++;
		if (cannon != null) i++;
		if (stock != null) i++;
		return i;
	}

	public int getAllowedAccessoriesAmount() {
		int i = 0;
		if (isScopeAllowed()) i++;
		if (isCannonAllowed()) i++;
		if (isStockAllowed()) i++;
		return i;
	}

	public void setAccessory(AccessoryType type, Accessory accessory) {
		Accessory old = null;
		switch (type) {
		case SCOPE:
			old = scope;
			scope = (Scope) accessory;
			break;
		case CANNON:
			old = cannon;
			cannon = (Cannon) accessory;
			break;
		case STOCK:
			old = stock;
			stock = (Stock) accessory;
			break;
		}
		if (old != null) old.remove(this);
		if (accessory != null) accessory.apply(this);
	}

	/**
	 * @return Volume lors du tir de la balle (distance = 16*x)
	 */
	protected abstract float getFireVolume();
	
	protected String getFireSound() {
		return "zta.guns.generic";
	}

	/**
	 * Jouer le son de tir
	 * @param lc location où est jouée le son
	 */
	public void playFireSound(Location lc) {
		lc.getWorld().playSound(lc, getFireSound(), SoundCategory.PLAYERS, fireVolume.getValue(), 1);
	}

	/**
	 * Jouer le son de recharge
	 * @param lc location où est jouée le son
	 */
	public void playChargeSound(Location lc) {
		lc.getWorld().playSound(lc, Sound.BLOCK_PISTON_EXTEND, SoundCategory.PLAYERS, 1, 1);
	}

	/**
	 * Jouer le son de recharge complète
	 * @param lc location où est jouée le son
	 */
	public void playChargeCompleteSound(Location lc) {
		lc.getWorld().playSound(lc, Sound.BLOCK_PISTON_CONTRACT, SoundCategory.PLAYERS, 1, 1);
	}

	/**
	 * Jouer le son de paré au tir
	 * @param lc location où est jouée le son
	 */
	public void playReadySound(Location lc) {
		lc.getWorld().playSound(lc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.PLAYERS, 0.5f, 1);
	}

	/**
	 * Jouer le son de chargeur vide
	 * @param lc location où est jouée le son
	 */
	public void playOutOfAmmosSound(Location lc) {
		lc.getWorld().playSound(lc, Sound.UI_BUTTON_CLICK, SoundCategory.PLAYERS, 1, 1);
	}

	private static OlympaStatement createStatement = new OlympaStatement("INSERT INTO " + TABLE_NAME + " (`id`) VALUES (?)");
	private static OlympaStatement updateStatement = new OlympaStatement("UPDATE " + TABLE_NAME + " SET "
			+ "`ammos` = ?, "
			+ "`ready` = ?, "
			+ "`zoomed` = ?, "
			+ "`secondary_mode` = ?, "
			+ "`scope_id` = ?, "
			+ "`cannon_id` = ?, "
			+ "`stock_id` = ? "
			+ "WHERE (`id` = ?)");

	public void createDatas() throws SQLException {
		PreparedStatement statement = createStatement.getStatement();
		statement.setInt(1, getID());
		statement.executeUpdate();
	}

	public synchronized void updateDatas() throws SQLException {
		PreparedStatement statement = updateStatement.getStatement();
		statement.setInt(1, ammos);
		statement.setBoolean(2, ready);
		statement.setBoolean(3, zoomed);
		statement.setBoolean(4, secondaryMode);
		statement.setInt(5, scope == null ? -1 : scope.getID());
		statement.setInt(6, cannon == null ? -1 : cannon.getID());
		statement.setInt(7, stock == null ? -1 : stock.getID());
		statement.setInt(8, getID());
		statement.executeUpdate();
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
		EXTREME("Extrême", 0), HIGH("Bonne", 0.05f), MEDIUM("Moyenne", 0.2f), LOW("Faible", 0.7f);

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

	public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
			"  `id` INT NOT NULL," +
			"  `ammos` SMALLINT(2) UNSIGNED DEFAULT 0," +
			"  `ready` TINYINT(1) DEFAULT 1," +
			"  `zoomed` TINYINT(1) DEFAULT 0," +
			"  `secondary_mode` TINYINT(1) DEFAULT 0," +
			"  `scope_id` INT(11) DEFAULT -1," +
			"  `cannon_id` INT(11) DEFAULT -1," +
			"  `stock_id` INT(11) DEFAULT -1," +
			"  PRIMARY KEY (`id`))";

	public static <T extends Gun> T deserializeGun(ResultSet set, int id, Class<?> clazz) throws Exception {
		T gun = (T) clazz.getConstructor(int.class).newInstance(id);
		gun.ammos = set.getInt("ammos");
		gun.ready = set.getBoolean("ready");
		gun.zoomed = set.getBoolean("zoomed");
		gun.secondaryMode = set.getBoolean("secondary_mode");
		int scopeID = set.getInt("scope_id");
		int cannonID = set.getInt("cannon_id");
		int stockID = set.getInt("stock_id");
		new BukkitRunnable() {
			public void run() {
				ZTARegistry registry = ZTARegistry.get();
				if (scopeID != -1) gun.setAccessory(AccessoryType.SCOPE, registry.getObject(scopeID));
				if (cannonID != -1) gun.setAccessory(AccessoryType.CANNON, registry.getObject(cannonID));
				if (stockID != -1) gun.setAccessory(AccessoryType.STOCK, registry.getObject(stockID));
			}
		}.runTaskLater(OlympaZTA.getInstance(), 20L);
		return gun;
	}

	public static ItemStack buildDemoStack(Material type, String name) {
		return ItemUtils.item(type, "§e" + name);
	}

}