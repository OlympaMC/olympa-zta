package fr.olympa.zta.weapons.guns;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.utils.AttributeModifier;
import fr.olympa.zta.utils.AttributeModifier.Operation;
import fr.olympa.zta.weapons.ItemStackable;
import fr.olympa.zta.weapons.Weapon;
import fr.olympa.zta.weapons.guns.Accessory.AccessoryType;
import fr.olympa.zta.weapons.guns.Gun.GunAccuracy;
import fr.olympa.zta.weapons.guns.Gun.GunMode;
import fr.olympa.zta.weapons.guns.bullets.Bullet;
import fr.olympa.zta.weapons.guns.bullets.Bullet.BulletCreator;
import fr.olympa.zta.weapons.guns.bullets.BulletEffect.BulletEffectCreator;
import fr.olympa.zta.weapons.guns.bullets.BulletExplosive;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public enum GunType implements ItemStackable{
	
	REM_870(
			"Remington 870 Express",
			Material.WOODEN_HOE,
			AmmoType.CARTRIDGE,
			4,
			30,
			70,
			true,
			CommonGunConstants.KNOCKBACK_LOW,
			CommonGunConstants.BULLET_SPEED_LOW,
			GunAccuracy.MEDIUM,
			null,
			4,
			5,
			BulletSimple::new,
			1,
			GunMode.SINGLE,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.pump",
			null,
			AccessoryType.CANNON,
			AccessoryType.SCOPE),
	AK_20(
			"AK-20",
			Material.GOLDEN_PICKAXE,
			AmmoType.HANDWORKED,
			20,
			5,
			70,
			false,
			CommonGunConstants.KNOCKBACK_LOW,
			CommonGunConstants.BULLET_SPEED_MEDIUM,
			GunAccuracy.HIGH,
			null,
			6,
			3,
			BulletSimple::new,
			1,
			GunMode.AUTOMATIC,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.auto",
			null,
			AccessoryType.values()),
	BARRETT(
			"Barrett M109",
			Material.IRON_HOE,
			AmmoType.HEAVY,
			1,
			-1,
			80,
			false,
			CommonGunConstants.KNOCKBACK_HIGH,
			CommonGunConstants.BULLET_SPEED_ULTRA_HIGH,
			GunAccuracy.EXTREME,
			new AttributeModifier("zoom", Operation.ADD_MULTIPLICATOR, -3),
			9,
			10,
			new BulletEffectCreator(new PotionEffect(PotionEffectType.SLOW, 40, 1)),
			1,
			GunMode.SINGLE,
			null,
			CommonGunConstants.SOUND_VOLUME_HIGH,
			"zta.guns.barrett",
			null,
			AccessoryType.CANNON),
	BAZOOKA(
			"Bazooka",
			Material.GOLDEN_HOE,
			AmmoType.HEAVY,
			2,
			40,
			70,
			false,
			CommonGunConstants.KNOCKBACK_HIGH,
			CommonGunConstants.BULLET_SPEED_LOW,
			GunAccuracy.MEDIUM,
			null,
			0,
			0,
			(gun, playerDamage, entityDamage) -> new BulletExplosive(gun, 5),
			1,
			GunMode.SINGLE,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.generic",
			null),
	BENELLI(
			"Benelli M5 Super",
			Material.STONE_SHOVEL,
			AmmoType.CARTRIDGE,
			8,
			30,
			15,
			true,
			CommonGunConstants.KNOCKBACK_LOW,
			CommonGunConstants.BULLET_SPEED_LOW,
			GunAccuracy.LOW,
			null,
			5,
			8,
			new BulletEffectCreator(new PotionEffect(PotionEffectType.WITHER, 40, 1)),
			1,
			GunMode.SEMI_AUTOMATIC,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.pump",
			null),
	COBRA(
			"King Cobra",
			Material.STONE_AXE,
			AmmoType.HEAVY,
			6,
			15,
			80,
			false,
			CommonGunConstants.KNOCKBACK_LOW,
			CommonGunConstants.BULLET_SPEED_HIGH,
			GunAccuracy.EXTREME,
			null,
			4,
			4,
			new BulletEffectCreator(new PotionEffect(PotionEffectType.SLOW, 40, 1)),
			1,
			GunMode.SINGLE,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.generic",
			null),
	DRAGUNOV(
			"Dragunov",
			Material.GOLDEN_HOE,
			AmmoType.HANDWORKED,
			1,
			-1,
			60,
			false,
			CommonGunConstants.KNOCKBACK_HIGH,
			CommonGunConstants.BULLET_SPEED_ULTRA_HIGH,
			GunAccuracy.EXTREME,
			new AttributeModifier("zoom", Operation.ADD_MULTIPLICATOR, -1),
			14,
			10,
			new BulletEffectCreator(new PotionEffect(PotionEffectType.SLOW, 20, 1)),
			1,
			GunMode.SINGLE,
			null,
			CommonGunConstants.SOUND_VOLUME_HIGH,
			"zta.guns.barrett",
			null,
			AccessoryType.CANNON),
	G19(
			"Glock 19",
			Material.GOLDEN_AXE,
			AmmoType.HANDWORKED,
			15,
			10,
			40,
			false,
			CommonGunConstants.KNOCKBACK_ULTRA_LOW,
			CommonGunConstants.BULLET_SPEED_MEDIUM,
			GunAccuracy.HIGH,
			null,
			4,
			4,
			BulletSimple::new,
			1,
			GunMode.AUTOMATIC,
			GunMode.SEMI_AUTOMATIC,
			CommonGunConstants.SOUND_VOLUME_LOW,
			"zta.guns.generic",
			null,
			AccessoryType.CANNON),
	KSG(
			"KSG",
			Material.IRON_SHOVEL,
			AmmoType.CARTRIDGE,
			10,
			14,
			100,
			false,
			CommonGunConstants.KNOCKBACK_LOW,
			CommonGunConstants.BULLET_SPEED_ULTRA_LOW,
			GunAccuracy.MEDIUM,
			null,
			3,
			6,
			new BulletEffectCreator(new PotionEffect(PotionEffectType.WITHER, 40, 1)),
			5,
			GunMode.SEMI_AUTOMATIC,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.pump",
			null,
			AccessoryType.STOCK),
	LUPARA(
			"Lupara",
			Material.GOLDEN_SHOVEL,
			AmmoType.CARTRIDGE,
			1,
			-1,
			10,
			false,
			CommonGunConstants.KNOCKBACK_ULTRA_LOW,
			CommonGunConstants.BULLET_SPEED_LOW,
			GunAccuracy.LOW,
			null,
			5,
			8,
			new BulletEffectCreator(new PotionEffect(PotionEffectType.WITHER, 40, 1)),
			2,
			GunMode.AUTOMATIC,
			null,
			CommonGunConstants.SOUND_VOLUME_LOW,
			"zta.guns.pump",
			null,
			AccessoryType.STOCK),
	M16(
			"M16",
			Material.WOODEN_PICKAXE,
			AmmoType.HEAVY,
			20,
			8,
			60,
			false,
			CommonGunConstants.KNOCKBACK_MEDIUM,
			CommonGunConstants.BULLET_SPEED_HIGH,
			GunAccuracy.HIGH,
			null,
			4,
			5,
			BulletSimple::new,
			1,
			GunMode.AUTOMATIC,
			GunMode.BLAST,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.auto",
			null,
			AccessoryType.CANNON,
			AccessoryType.STOCK),
	M1897(
			"M1897",
			Material.WOODEN_SHOVEL,
			AmmoType.CARTRIDGE,
			5,
			30,
			15,
			true,
			CommonGunConstants.KNOCKBACK_MEDIUM,
			CommonGunConstants.BULLET_SPEED_ULTRA_LOW,
			GunAccuracy.LOW,
			null,
			7,
			10,
			new BulletEffectCreator(new PotionEffect(PotionEffectType.WITHER, 40, 1)),
			5,
			GunMode.SINGLE,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.pump",
			null,
			AccessoryType.STOCK),
	M1911(
			"Colt M1911",
			Material.WOODEN_AXE,
			AmmoType.LIGHT,
			7,
			10,
			60,
			false,
			CommonGunConstants.KNOCKBACK_ULTRA_LOW,
			CommonGunConstants.BULLET_SPEED_MEDIUM,
			GunAccuracy.HIGH,
			null,
			3,
			3,
			BulletSimple::new,
			1,
			GunMode.SINGLE,
			null,
			CommonGunConstants.SOUND_VOLUME_LOW,
			"zta.guns.generic",
			null,
			AccessoryType.CANNON),
	P22(
			"Walther P22",
			Material.IRON_AXE,
			AmmoType.LIGHT,
			10,
			10,
			30,
			false,
			CommonGunConstants.KNOCKBACK_ULTRA_LOW,
			CommonGunConstants.BULLET_SPEED_MEDIUM,
			GunAccuracy.HIGH,
			null,
			4,
			4,
			BulletSimple::new,
			1,
			GunMode.SEMI_AUTOMATIC,
			GunMode.BLAST,
			CommonGunConstants.SOUND_VOLUME_LOW,
			"zta.guns.generic",
			null,
			AccessoryType.CANNON),
	SDMR(
			"SDM-R",
			Material.IRON_PICKAXE,
			AmmoType.HEAVY,
			20,
			6,
			50,
			false,
			CommonGunConstants.KNOCKBACK_MEDIUM,
			CommonGunConstants.BULLET_SPEED_HIGH,
			GunAccuracy.HIGH,
			null,
			5,
			6,
			BulletSimple::new,
			1,
			GunMode.AUTOMATIC,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.auto",
			null,
			AccessoryType.values()),
	SKORPION(
			"Skorpion VZ64",
			Material.MAGMA_CREAM,
			AmmoType.HANDWORKED,
			24,
			4,
			50,
			false,
			CommonGunConstants.KNOCKBACK_ULTRA_LOW,
			CommonGunConstants.BULLET_SPEED_LOW,
			GunAccuracy.MEDIUM,
			null,
			3,
			7,
			BulletSimple::new,
			1,
			GunMode.AUTOMATIC,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.auto",
			null),
	STONER(
			"Stoner 24",
			Material.IRON_HORSE_ARMOR,
			AmmoType.HEAVY,
			55,
			15,
			70,
			false,
			CommonGunConstants.KNOCKBACK_HIGH,
			CommonGunConstants.BULLET_SPEED_MEDIUM,
			GunAccuracy.MEDIUM,
			null,
			2,
			3,
			new BulletEffectCreator(new PotionEffect(PotionEffectType.SLOW, 40, 1)),
			1,
			GunMode.AUTOMATIC,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.generic",
			new PotionEffect(PotionEffectType.SLOW, 99999999, 1),
			AccessoryType.CANNON,
			AccessoryType.STOCK),
	UZI(
			"UZI",
			Material.SLIME_BALL,
			AmmoType.LIGHT,
			25,
			3,
			50,
			false,
			CommonGunConstants.KNOCKBACK_ULTRA_LOW,
			CommonGunConstants.BULLET_SPEED_ULTRA_LOW,
			GunAccuracy.MEDIUM,
			null,
			2,
			7,
			BulletSimple::new,
			1,
			GunMode.AUTOMATIC,
			null,
			CommonGunConstants.SOUND_VOLUME_MEDIUM,
			"zta.guns.auto",
			null),
			;
	
	private static DecimalFormat attributeFormat = new DecimalFormat("##.##");
	
	private final String name;
	private final Material material;
	private final AmmoType ammoType;
	private final int maxAmmos;
	private final int fireRate;
	private final int chargeTime;
	private final boolean oneByOneCharge;
	private final float knockback;
	private final float bulletSpeed;
	private final GunAccuracy accuracy;
	private final AttributeModifier zoomModifier;
	private final int playerDamage;
	private final int entityDamage;
	private final BulletCreator bulletCreator;
	private final int firedBullets;
	private final GunMode primaryMode;
	private final GunMode secondaryMode;
	private final float fireVolume;
	private final String fireSound;
	private final PotionEffect heldEffect;
	private final List<AccessoryType> allowedAccessories;
	
	private final ItemStack demoItem;
	
	private GunType(String name, Material material, AmmoType ammoType, int maxAmmos, int fireRate, int chargeTime, boolean oneByOneCharge, float knockback, float bulletSpeed, GunAccuracy accuracy, AttributeModifier zoomModifier, int playerDamage, int entityDamage, BulletCreator bulletCreator, int firedBullets, GunMode primaryMode, GunMode secondaryMode, float fireVolume, String fireSound, PotionEffect heldEffect, AccessoryType... allowedAccessories) {
		this.name = name;
		this.material = material;
		this.ammoType = ammoType;
		this.maxAmmos = maxAmmos;
		this.fireRate = fireRate;
		this.chargeTime = chargeTime;
		this.oneByOneCharge = oneByOneCharge;
		this.knockback = knockback;
		this.bulletSpeed = bulletSpeed;
		this.accuracy = accuracy;
		this.zoomModifier = zoomModifier;
		this.playerDamage = playerDamage;
		this.entityDamage = entityDamage;
		this.bulletCreator = bulletCreator;
		this.firedBullets = firedBullets;
		this.primaryMode = primaryMode;
		this.secondaryMode = secondaryMode;
		this.fireVolume = fireVolume;
		this.fireSound = fireSound;
		this.heldEffect = heldEffect;
		this.allowedAccessories = Arrays.asList(allowedAccessories);
		
		demoItem = new ItemStack(material);
		ItemMeta meta = demoItem.getItemMeta();
		meta.setDisplayName("§e" + name);
		meta.setLore(getLore());
		meta.addItemFlags(ItemFlag.values());
		meta.setCustomModelData(1);
		demoItem.setItemMeta(meta);
	}
	
	public String getName() {
		return name;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	/**
	 * @return Type de munition
	 */
	public AmmoType getAmmoType() {
		return ammoType;
	}
	
	/**
	 * @return Maximum de munitions
	 */
	public int getMaxAmmos() {
		return maxAmmos;
	}
	
	/**
	 * @return Temps (en ticks) entre chaque coup de feu
	 */
	public int getFireRate() {
		return fireRate;
	}
	
	/**
	 * @return Temps (en ticks) avant la recharge complète
	 */
	public int getChargeTime() {
		return chargeTime;
	}
	
	/**
	 * @return <tt>true</tt> si la charge se fait munition par munition
	 */
	public boolean isOneByOneCharge() {
		return oneByOneCharge;
	}
	
	/**
	 * @return Puissance de recul en m/s
	 */
	public float getKnockback() {
		return knockback;
	}
	
	/**
	 * @return Vitesse de la balle en m/s
	 */
	public float getBulletSpeed() {
		return bulletSpeed;
	}
	
	/**
	 * @return Rayon du dispersement des balles (en m)
	 */
	public GunAccuracy getAccuracy() {
		return accuracy;
	}
	
	/**
	 * @return Mode de tir secondaire
	 */
	public AttributeModifier getZoomModifier() {
		return zoomModifier;
	}
	
	public boolean hasZoom() {
		return zoomModifier != null;
	}
	
	/**
	 * @return Dommages donnés aux joueurs par la balle tirée
	 */
	public int getPlayerDamage() {
		return playerDamage;
	}
	
	/**
	 * @return Dommages donnés aux entités par la balle tirée
	 */
	public int getEntityDamage() {
		return entityDamage;
	}
	
	public Bullet createBullet(Gun gun, float playerDamage, float entityDamage) {
		return bulletCreator.create(gun, playerDamage, entityDamage);
	}
	
	public int getFiredBullets() {
		return firedBullets;
	}
	
	/**
	 * @return Mode de tir principal
	 */
	public GunMode getPrimaryMode() {
		return primaryMode;
	}
	
	/**
	 * @return Mode de tir secondaire
	 */
	public GunMode getSecondaryMode() {
		return secondaryMode;
	}
	
	public boolean hasSecondaryMode() {
		return secondaryMode != null;
	}
	
	/**
	 * @return Volume lors du tir de la balle (distance = 16*x)
	 */
	public float getFireVolume() {
		return fireVolume;
	}
	
	public String getFireSound() {
		return fireSound;
	}
	
	public PotionEffect getHeldEffect() {
		return heldEffect;
	}
	
	public List<AccessoryType> getAllowedAccessories() {
		return allowedAccessories;
	}
	
	@Override
	public ItemStack getDemoItem() {
		return demoItem;
	}
	
	@Override
	public ItemStack createItem() {
		try {
			Gun gun = OlympaZTA.getInstance().gunRegistry.createGun(this);
			return gun.createItemStack();
		}catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<String> getLore(){
		return Arrays.asList(
				Weapon.getFeatureLoreLine("Cadence de tir", attributeFormat.format(getFireRate() / 20D) + "s"),
				Weapon.getFeatureLoreLine("Temps de recharge", attributeFormat.format(getChargeTime() / 20D) + "s"),
				Weapon.getFeatureLoreLine("Munitions", getAmmoType().getName()),
				Weapon.getFeatureLoreLine("Précision", getAccuracy().getName()),
				Weapon.getFeatureLoreLine("Mode de tir", getPrimaryMode().getName() + (hasSecondaryMode() ? "/" + getSecondaryMode().getName() : "")));
	}
	
	public List<String> getAccessoriesLore(int accessories){
		return Arrays.asList(
				"",
				"§6§lAccessoires §r§6: §e[§n" + accessories + "§r§e/" + allowedAccessories.size() + "]",
				"§e§oClic droit pour attacher des accessoires");
	}
	
}
