package fr.olympa.zta.weapons.guns;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.item.OlympaItemBuild;
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
	public final Attribute bulletSpread = new Attribute(getBulletSpread());
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
		ItemStack item = new OlympaItemBuild(getItemMaterial(), getName())
				.unbreakable()
				.flag(ItemFlag.values())
				.lore("Cadence de tir: " + getFireRate() / 20 + "s",
						"Temps de recharge: " + getChargeTime() / 20 + "s",
						"Recul de l'arme: " + getKnockback(),
						"Munitions: " + getAmmoType().getName(),
						"Mode de tir: " + getPrimaryMode().getName() + (getSecondaryMode() == null ? "" : "/" + getSecondaryMode().getName()),
						"",
						"Arme immatriculée:",
						"[I" + id + "]",
						"",
						"Accessoires: [" + getCurrentMode() + "/" + getAllowedAccessoriesAmount() + "]",
						"Clic central pour attacher des accessoires")
				.build();
		setItemName(item);
		return item;
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
			setItemName(item);
		}
	}
	
	public void onDrop(PlayerDropItemEvent e){
		reload(e.getPlayer(), e.getItemDrop().getItemStack());
		e.setCancelled(true);
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
				setItemName(item);
				
				if (ammos > 0) { // si encore des balles dans le barillet
					OlympaZTA.getInstance().getTaskManager().runTaskLater(() -> {
						ready = true;
						setItemName(item);
						playReadySound(p);
					}, (int) fireRate.getValue());
				}
			}
		}else { // clic gauche : zoom
			if (getLeftClickAction() == null) return;
			switch (getLeftClickAction()){
			case CHANGE_MODE:
				secondaryMode = !secondaryMode;
				setItemName(item);
				break;
			case ZOOM:
				toggleZoom(p);
				break;
			}
		}
	}
	
	public void itemClick(Player p){
		new AccessoriesGUI(this).create(p);
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
		int toCharge = Math.min((int) maxAmmos.getValue(), getAmmoType().getAmmos(p));
		if (toCharge == 0) return;
		
		reloading = OlympaZTA.getInstance().getTaskManager().runTaskLater(() -> {
			ammos = getAmmoType().removeAmmos(p, toCharge);
			reloading = null;
			if (ammos != 0) ready = true;
			setItemName(item);
			playChargeCompleteSound(p);
		}, (int) chargeTime.getValue());
		
		setItemName(item);
		playChargeSound(p);
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
		playFireSound(p);
		
		float knockback = this.knockback.getValue();
		if (p.isSneaking()) knockback /= 2;
		p.setVelocity(p.getLocation().getDirection().multiply(-knockback));
	}
	
	private void setItemName(ItemStack item){
		ItemMeta im = item.getItemMeta();
		im.setDisplayName("§e" + (getSecondaryMode() == null ? "" : secondaryMode ? "ᐊ▶ " : "◀ᐅ ") + getName() + " [" + ammos + "/" + getMaxAmmos() + "] " + (ready ? "●" : "○") + (reloading == null ? "" : " recharge"));
		item.setItemMeta(im);
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
	protected abstract float getBulletSpread();
	
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
	 * Mettre un accessoire sur un item. Cette méthode ne vérifie pas si l'accessoire est accepté par l'arme.
	 * @param index Index de l'accessoire, précisions ici: {@link Gun#accessories}
	 * @param accessory Accessoire à mettre. Peut être <i>null</i>.
	 */
	public void setAccessory(byte index, Accessory accessory){
		if (accessories[index] != null) accessories[index].remove(this);
		accessories[index] = accessory;
		accessory.apply(this);
	}
	
	/**
	 * @return Volume lors du tir de la balle (distance = 16*x)
	 */
	protected float getFireVolume(){
		return 4;
	}
	
	/**
	 * Jouer le son de tir
	 * @param p joueur
	 */
	public void playFireSound(Player p){
		p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, fireVolume.getValue(), 1);
	}
	
	/**
	 * Jouer le son de recharge
	 * @param p joueur
	 */
	public void playChargeSound(Player p){
		p.playSound(p.getLocation(), Sound.BLOCK_PISTON_EXTEND, SoundCategory.PLAYERS, 1, 1);
	}
	
	/**
	 * Jouer le son de recharge complète
	 * @param p joueur
	 */
	public void playChargeCompleteSound(Player p){
		p.playSound(p.getLocation(), Sound.BLOCK_PISTON_CONTRACT, SoundCategory.PLAYERS, 1, 1);
	}
	
	/**
	 * Jouer le son de paré au tir
	 * @param p joueur
	 */
	public void playReadySound(Player p){
		p.playSound(p.getLocation(), Sound.BLOCK_STONE_HIT, SoundCategory.PLAYERS, 1, 1);
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
	
}