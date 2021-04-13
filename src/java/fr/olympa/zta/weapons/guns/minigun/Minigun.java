package fr.olympa.zta.weapons.guns.minigun;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import fr.olympa.api.utils.Point2D;
import fr.olympa.api.utils.observable.AbstractObservable;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.guns.CommonGunConstants;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.ambiance.SoundAmbiance.ZTASound;
import fr.olympa.zta.weapons.guns.bullets.BulletSimple;

public class Minigun extends AbstractObservable {
	
	private static final Vector NORMAL = new Vector(0, 1, 0);
	private static final EulerAngle INITIAL_ANGLE = new EulerAngle(3 * Math.PI / 2, 0, 0);
	private static final double MAX_ANGLE = Math.PI / 3;
	private static final double CRITICAL_ANGLE = Math.PI / 2;
	private static final int MAX_STATE = 75;
	private static final int WARNING_STATE = 48;
	private static final int FIRE_RATE = 3;
	private static final int RATE_UNTIL_UNLOAD = 10;

	public int id;
	
	public Map<Point2D, Chunk> chunks = new HashMap<>();
	
	public Location playerPosition;
	public Location standLocation;
	public BlockFace facing;
	public Vector facingDirection;
	
	protected boolean isSpawned = false;
	
	private ArmorStand gunStand;
	private ArmorStand sitStand;
	
	private Player inUse = null;
	private BossBar bar;
	private BukkitTask rotationTask;
	
	private int state = 0;
	private long lastClick = 0;
	private BukkitTask fireTask;
	private int timeUntilUnload = RATE_UNTIL_UNLOAD;
	private boolean lockUnload = false;
	
	private Location lastLocation;
	private double yAngle;
	private double angle;
	private Vector bulletDirection = null;
	
	public Minigun(Location playerPosition, BlockFace facing) {
		this.playerPosition = playerPosition;
		this.facing = facing;
		
		facingDirection = facing.getDirection();
		
		standLocation = playerPosition.clone();
		standLocation.subtract(facing.getModZ() * -0.375, 0.2, facing.getModX() * 0.375);
		
		Point2D point = Point2D.chunkPointFromLocation(playerPosition);
		Chunk chunk = null;
		if (playerPosition.getWorld().isChunkLoaded(point.x, point.z)) chunk = point.asChunk(playerPosition.getWorld());
		chunks.put(point, chunk);
		
		point = Point2D.chunkPointFromLocation(standLocation);
		chunk = null;
		if (playerPosition.getWorld().isChunkLoaded(point.x, point.z)) chunk = point.asChunk(playerPosition.getWorld());
		chunks.put(point, chunk);
		
		bar = Bukkit.createBossBar("§bMinigun", BarColor.BLUE, BarStyle.SOLID);
		bar.setProgress(1);
	}
	
	public int getID() {
		return id;
	}
	
	public void updateChunks() {
		boolean loaded = chunks.values().stream().allMatch(x -> x != null && x.isLoaded());
		if (isSpawned) {
			if (!loaded) destroy();
		}else {
			if (loaded) spawn();
		}
	}
	
	public boolean isInUse() {
		return inUse != null;
	}
	
	public Player getUser() {
		return inUse;
	}
	
	public void approach(Player p) {
		if (isInUse()) return;
		
		p.teleport(playerPosition);
		sitStand.addPassenger(p);
		inUse = p;
		bar.addPlayer(p);
		OlympaZTA.getInstance().miniguns.inUse.put(p, this);
		
		rotationTask = Bukkit.getScheduler().runTaskTimer(OlympaZTA.getInstance(), () -> {
			Location loc = p.getLocation();
			if (lastLocation == null || lastLocation.getPitch() != loc.getPitch() || lastLocation.getYaw() != loc.getYaw()) move(p.getLocation());
		}, 1, 1);
		
		if (lockUnload) p.sendTitle("§cSurchauffe!", "§7Patientez quelques instants...", 0, (state - WARNING_STATE) * FIRE_RATE - 20, 20);
	}
	
	public void leave(boolean eject) {
		if (!isInUse()) return;
		
		if (eject) {
			sitStand.removePassenger(inUse);
			return;
		}
		rotationTask.cancel();
		if (lockUnload) inUse.resetTitle();
		lastClick = 0;
		bar.removePlayer(inUse);
		//gunStand.teleport(standLocation);
		gunStand.setRightArmPose(INITIAL_ANGLE);
		OlympaZTA.getInstance().miniguns.inUse.remove(inUse);
		inUse = null;
	}
	
	public void move(Location to) {
		yAngle = -Math.toRadians(Math.min(8, Math.max(-25, to.getPitch())));
		
		to.setPitch(0);
		Vector direction = to.getDirection().normalize();
		double absangle = Math.acos(direction.dot(facingDirection));
		if (absangle >= CRITICAL_ANGLE) {
			leave(true);
			return;
		}
		absangle = Math.min(absangle, MAX_ANGLE);
		direction.crossProduct(facingDirection);
		angle = NORMAL.dot(direction) < 0 ? -absangle : absangle;
		gunStand.setRightArmPose(new EulerAngle(INITIAL_ANGLE.getX() - yAngle * 0.8, angle, 0));
		
		bulletDirection = null;
	}
	
	public void interact() {
		lastClick = System.currentTimeMillis();
		
		if (fireTask == null || fireTask.isCancelled()) {
			fireTask = Bukkit.getScheduler().runTaskTimer(OlympaZTA.getInstance(), () -> {
				if (!lockUnload && System.currentTimeMillis() - lastClick < 210) {
					timeUntilUnload = RATE_UNTIL_UNLOAD;
					
					if (bulletDirection == null) {
						bulletDirection = facingDirection.clone();
						bulletDirection.rotateAroundY(-angle);
						if (facing.getModX() == 0) {
							bulletDirection.rotateAroundX(yAngle);
						}else {
							bulletDirection.rotateAroundZ(yAngle);
						}
					}
					new BulletSimple(CommonGunConstants.BULLET_SPEED_MEDIUM_LOW, Gun.GunAccuracy.MEDIUM.getBulletSpread(), 3, 3).launchProjectile(inUse, bulletDirection);
					playerPosition.getWorld().playSound(playerPosition, ZTASound.GUN_AUTO.getSound(), SoundCategory.PLAYERS, 1f + 0.5f, 1);
					
					state++;
					if (state == MAX_STATE) {
						lockUnload = true;
						timeUntilUnload = RATE_UNTIL_UNLOAD / 2;
						inUse.sendTitle("§cSurchauffe!", "§7Patientez quelques instants...", 7, 90, 20);
					}
					updateState();
				}else {
					if (timeUntilUnload == 0) {
						state--;
						updateState();
						if (state == 0) {
							fireTask.cancel();
							fireTask = null;
						}else if (lockUnload && state < WARNING_STATE) {
							lockUnload = false;
						}
					}else timeUntilUnload--;
				}
			}, 0, FIRE_RATE);
		}
	}
	
	private void updateState() {
		BarColor newColor;
		String name;
		if (lockUnload) {
			newColor = BarColor.RED;
			name = "§cMinigun - §lsurchauffe";
		}else if (state >= WARNING_STATE) {
			newColor = BarColor.YELLOW;
			name = "§eMinigun";
		}else {
			newColor = BarColor.BLUE;
			name = "§bMinigun";
		}
		
		if (bar.getColor() != newColor) {
			bar.setColor(newColor);
			bar.setTitle(name);
		}
		bar.setProgress((double) state / (double) MAX_STATE);
	}
	
	public void spawn() {
		if (isSpawned) return;
		
		ItemStack item = new ItemStack(Material.IRON_HORSE_ARMOR);
		ItemMeta meta = item.getItemMeta();
		meta.setCustomModelData(1);
		item.setItemMeta(meta);
		
		gunStand = playerPosition.getWorld().spawn(standLocation, ArmorStand.class);
		gunStand.setInvisible(true);
		gunStand.setInvulnerable(true);
		gunStand.getEquipment().setItemInMainHand(item);
		gunStand.setRightArmPose(INITIAL_ANGLE);
		gunStand.setGravity(false);
		gunStand.setPersistent(false);
		gunStand.setMarker(false);
		
		sitStand = playerPosition.getWorld().spawn(playerPosition.clone().add(0, 0.3, 0), ArmorStand.class);
		sitStand.setInvisible(true);
		sitStand.setInvulnerable(true);
		sitStand.setGravity(false);
		sitStand.setPersistent(false);
		sitStand.setMarker(true);
		
		isSpawned = true;
	}
	
	protected void destroy() {
		if (!isSpawned) return;
		isSpawned = false;
		
		gunStand.remove();
		sitStand.remove();
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("playerPosition", SpigotUtils.convertLocationToString(playerPosition));
		map.put("facing", facing.name());
		
		return map;
	}
	
	public static Minigun deserialize(Map<String, Object> map) {
		return new Minigun(SpigotUtils.convertStringToLocation((String) map.get("playerPosition")), BlockFace.valueOf((String) map.get("facing")));
	}
	
}
