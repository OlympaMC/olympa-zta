package fr.olympa.zta.weapons.guns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.Attribute;
import fr.olympa.zta.weapons.Weapon;
import fr.olympa.zta.weapons.guns.accessories.Accessory;
import fr.olympa.zta.weapons.guns.accessories.Scope;
import fr.olympa.zta.weapons.guns.bullets.Bullet;

public abstract class Gun extends Weapon{
	
	protected int ammos = 0;
	protected boolean ready = false;
	protected boolean zoomed = false;
	protected boolean secondaryMode = false;
	protected BukkitTask reloading = null;
	
	public float damageAdded = 0;
	public AttributeModifier zoomModifier = null;
	public final Attribute maxAmmos = new Attribute(getMaxAmmos());
	public final Attribute chargeTime = new Attribute(getChargeTime());
	public final Attribute bulletSpeed = new Attribute(getBulletSpeed());
	public final Attribute bulletSpread = new Attribute(getAccuracy().getBulletSpread());
	public final Attribute knockback = new Attribute(getKnockback());
	public final Attribute fireRate = new Attribute(getFireRate());
	public final Attribute fireVolume = new Attribute(getFireVolume());
	
	/**
	 * Accessoires attachés sur l'arme.
	 * <ol start="0">
	 * <li> Scope (lunette)
	 * <li> Cannon (canon)
	 * <li> Stock (crosse)
	 * </ol>
	 */
	public final Accessory[] accessories = new Accessory[3];
	
	public ItemStack createItemStack(){
		return createItemStack(true);
	}
	
	public ItemStack createItemStack(boolean accessories){
		ItemStack item = ItemUtils.item(getItemMaterial(), getName());
		updateItemName(item);
		updateItemLore(item, accessories);
		return item;
	}
	
	public void updateItemName(ItemStack item){
		ItemMeta im = item.getItemMeta();
		im.setDisplayName("§e" + (getSecondaryMode() == null ? "" : secondaryMode ? "ᐊ▶ " : "◀ᐅ ") + getName() + " [" + ammos + "/" + getMaxAmmos() + "] " + (ready ? "●" : "○") + (reloading == null ? "" : " recharge"));
		item.setItemMeta(im);
	}
	
	public void updateItemLore(ItemStack item, boolean accessories){
		ItemMeta im = item.getItemMeta();
		List<String> lore = new ArrayList<>(Arrays.asList(
				getFeatureLoreLine("Cadence de tir", getFireRate() / 20 + "s"),
				getFeatureLoreLine("Temps de recharge", getChargeTime() / 20 + "s"),
				getFeatureLoreLine("Munitions", getAmmoType().getName()),
				getFeatureLoreLine("Mode de tir", getPrimaryMode().getName() + (getSecondaryMode() == null ? "" : "/" + getSecondaryMode().getName()))));
		lore.addAll(getIDLoreLines());
		if (accessories) {
			lore.addAll(Arrays.asList(
					"",
					"§6§lAccessoires §r§6: §e[§n" + getAccessoriesAmount() + "§r§e/" + getAllowedAccessoriesAmount() + "]",
					"§e§oClic droit pour attacher des accessoires"));
		}
		im.setLore(lore);
		item.setItemMeta(im);
	}
	

	public void onEntityHit(EntityDamageByEntityEvent e){
		e.setDamage(getHitDamage());
	}
	
	public void itemHeld(Player p, ItemStack item){}
	
	public void itemNoLongerHeld(Player p, ItemStack item){
		if (zoomed) toggleZoom(p);
		if (reloading != null) {
			reloading.cancel();
			reloading = null;
			updateItemName(item);
		}
	}
	
	public boolean drop(Player p, ItemStack item){
		reload(p, item);
		return true;
	}
	
	public void onInteract(PlayerInteractEvent e){
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		e.setCancelled(true);
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) { // clic droit : tir
			if (reloading != null) return;
			if (ammos == 0) { // tentative de tir alors que le barillet est vide
				reload(p, item);
			}else if (ready) {
				fire(p);
				ready = false;
				updateItemName(item);
				
				if (ammos > 0) { // si encore des balles dans le barillet
					OlympaZTA.getInstance().getTask().runTaskLater(() -> {
						ready = true;
						updateItemName(item);
						playReadySound(p.getLocation());
					}, (int) fireRate.getValue());
				}
			}
		}else { // clic gauche : zoom
			if (getLeftClickAction() == null) return;
			switch (getLeftClickAction()){
			case CHANGE_MODE:
				secondaryMode = !secondaryMode;
				updateItemName(item);
				break;
			case ZOOM:
				toggleZoom(p);
				break;
			}
		}
	}
	
	public void itemClick(Player p, ItemStack item){
		new AccessoriesGUI(this, item).create(p);
	}
	
	private void fire(Player p){
		Bullet bullet = getFiredBullet(p);
		launchBullet(bullet, p); // première balle
		ammos--;
		if (ammos == 0) return;
		switch (getCurrentMode()){
		case AUTOMATIC:
			Bukkit.getScheduler().runTaskLater(OlympaZTA.getInstance(), () -> {
				launchBullet(bullet, p);
				ammos--;
			}, (int) fireRate.getValue() / GunMode.AUTOMATIC_BULLETS_AMOUNT);
			break;
		case BLAST:
			int time = (int) ((fireRate.getValue() / 2) / (GunMode.BLAST_BULLETS_AMOUNT - 1));
			new BukkitRunnable(){
				int amount = GunMode.BLAST_BULLETS_AMOUNT - 1;
				public void run(){
					launchBullet(bullet, p);
					ammos--;
					amount--;
					if (amount == 0 || ammos == 0) cancel();
				}
			}.runTaskTimer(OlympaZTA.getInstance(), time, time);
			break;
		}
	}
	
	private void reload(Player p, ItemStack item){
		if (reloading != null) return;
		
		int max = (int) maxAmmos.getValue();
		int toCharge;
		if (max == ammos) { // déjà le max de munitions
			toCharge = 0;
		}else if (isOneByOneCharge()) {
			toCharge = Math.min(1, getAmmoType().getAmmos(p));
		}else toCharge = Math.min(max - ammos, getAmmoType().getAmmos(p));
		if (toCharge == 0) return;
		
		reloading = OlympaZTA.getInstance().getTask().runTaskLater(() -> {
			reloading = null;
			ammos += getAmmoType().removeAmmos(p, toCharge);
			if (ammos != 0) ready = true;
			updateItemName(item);
			playChargeCompleteSound(p.getLocation());
			
			if (isOneByOneCharge()) reload(p, item); // relancer une charge
		}, (int) chargeTime.getValue());
		
		updateItemName(item);
		playChargeSound(p.getLocation());
	}
	
	private void toggleZoom(Player p){
		if (zoomed) {
			p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(getZoomModifier());
		}else {
			p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).addModifier(getZoomModifier());
		}
		zoomed = !zoomed;
		if (accessories[0] != null) ((Scope) accessories[0]).zoomToggled(p, zoomed);
	}
	
	private void launchBullet(Bullet bullet, Player p){
		bullet.launchProjectile(p);
		playFireSound(p.getLocation());
		
		float knockback = this.knockback.getValue();
		if (p.isSneaking()) knockback /= 2;
		p.setVelocity(p.getLocation().getDirection().multiply(-knockback));
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
	protected boolean isOneByOneCharge(){
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
	 * @param p Player qui tire la balle
	 * @return instance de {@link Bullet}
	 */
	public abstract Bullet getFiredBullet(Player p);
	
	/**
	 * @return Mode de tir principal
	 */
	public abstract GunMode getPrimaryMode();
	
	/**
	 * @return Mode de tir secondaire
	 */
	public GunMode getSecondaryMode(){
		return null;
	}
	
	public GunMode getCurrentMode(){
		return secondaryMode ? getSecondaryMode() : getPrimaryMode();
	}
	
	/**
	 * @return Action effectuée lors du clic gauche
	 */
	public GunAction getLeftClickAction(){
		return getSecondaryMode() != null ? GunAction.CHANGE_MODE : getZoomModifier() != null ? GunAction.ZOOM : null;
	}
	
	/**
	 * @return Dommages sur un coup à la main avec l'arme
	 */
	public float getHitDamage(){
		return 2;
	}
	
	/**
	 * @return AttributModifier sur l'attribut {@link Attribute#GENERIC_MOVEMENT_SPEED}
	 */
	public AttributeModifier getZoomModifier(){
		return zoomModifier == null ? null : zoomModifier;
	}
	
	/**
	 * @return Est-ce qu'un canon peut être attaché à l'arme
	 */
	public boolean isCannonAllowed(){
		return false;
	}
	
	/**
	 * @return Est-ce qu'une lunette peut être attachée à l'arme
	 */
	public boolean isScopeAllowed(){
		return false;
	}
	
	/**
	 * @return Est-ce qu'une crosse peut être attachée à l'arme
	 */
	public boolean isStockAllowed(){
		return false;
	}
	
	public int getAccessoriesAmount(){
		int i = 0;
		for (Accessory access : accessories) {
			if (access != null) i++;
		}
		return i;
	}
	
	public int getAllowedAccessoriesAmount(){
		int i = 0;
		if (isScopeAllowed()) i++;
		if (isCannonAllowed()) i++;
		if (isStockAllowed()) i++;
		return i;
	}
	
	/**
	 * @return Volume lors du tir de la balle (distance = 16*x)
	 */
	protected float getFireVolume(){
		return 4;
	}
	
	/**
	 * Jouer le son de tir
	 * @param lc location où est jouée le son
	 */
	public void playFireSound(Location lc){
		lc.getWorld().playSound(lc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, fireVolume.getValue(), 1);
	}
	
	/**
	 * Jouer le son de recharge
	 * @param lc location où est jouée le son
	 */
	public void playChargeSound(Location lc){
		lc.getWorld().playSound(lc, Sound.BLOCK_PISTON_EXTEND, SoundCategory.PLAYERS, 1, 1);
	}
	
	/**
	 * Jouer le son de recharge complète
	 * @param lc location où est jouée le son
	 */
	public void playChargeCompleteSound(Location lc){
		lc.getWorld().playSound(lc, Sound.BLOCK_PISTON_CONTRACT, SoundCategory.PLAYERS, 1, 1);
	}
	
	/**
	 * Jouer le son de paré au tir
	 * @param lc location où est jouée le son
	 */
	public void playReadySound(Location lc){
		lc.getWorld().playSound(lc, Sound.BLOCK_STONE_HIT, SoundCategory.PLAYERS, 1, 1);
	}
	
	public enum GunAction{
		ZOOM, CHANGE_MODE;
	}
	
	public enum GunMode{
		/**
		 * 1 balle tirée dans l'intervalle de temps donné
		 */
		SINGLE("tir unique"),
		/**
		 * 1 balle tirée dans l'intervalle de temps donné
		 */
		SEMI_AUTOMATIC("semi-automatique"),
		/**
		 * {@link GunMode#AUTOMATIC_BULLETS_AMOUNT} balles tirées dans l'intervalle de temps donné
		 */
		AUTOMATIC("automatique"),
		/**
		 * {@link GunMode#BLAST_BULLETS_AMOUNT} balles tirées dans la moitié de l'intervalle de temps donné
		 */
		BLAST("rafales");
		
		public static final int AUTOMATIC_BULLETS_AMOUNT = 2;
		public static final int BLAST_BULLETS_AMOUNT = 3;
		
		private String name;
		
		private GunMode(String name){
			this.name = name;
		}
		
		public String getName(){
			return name;
		}
	}
	
	public enum GunAccuracy{
		EXTREME("Exrême", 0), HIGH("Bonne", 0.05f), MEDIUM("Moyenne", 0.2f), LOW("Faible", 0.7f);
		
		private String name;
		private float spread;
		
		private GunAccuracy(String name, float spread){
			this.name = name;
			this.spread = spread;
		}
		
		public String getName(){
			return name;
		}
		
		public float getBulletSpread(){
			return spread;
		}
		
	}
	
}