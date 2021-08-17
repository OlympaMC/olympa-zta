package fr.olympa.zta.clans.plots;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.region.tracking.TrackedRegion;
import fr.olympa.api.spigot.region.tracking.flags.DamageFlag;
import fr.olympa.api.spigot.region.tracking.flags.PlayerBlockInteractFlag;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.ClanZTA;
import fr.olympa.zta.glass.GlassSmashFlag;
import fr.olympa.zta.weapons.guns.GunFlag;

public class ClanPlot {

	public static final List<Material> CONTAINER_MATERIALS = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL, Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER, Material.HOPPER, Material.DROPPER, Material.DISPENSER);
	public static final List<Material> CONTAINER_MATERIALS_ALLOWED = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL);
	
	public static final int PAYMENT_DURATION_DAYS = 7;
	public static final long PAYMENT_DURATION_MILLIS = PAYMENT_DURATION_DAYS * 24 * 3600 * 1000L;
	
	public static final long CHIEF_PAYMENT_DURATION_MILLIS = 28 * 24 * 3600 * 1000L;
	
	private static final DateFormat paymentDateFormat = new SimpleDateFormat("dd/MM");

	private final int id;
	private final TrackedRegion region;
	private final Location sign;
	private Location spawn;
	private int price;
	private String priceFormatted;

	private ClanZTA clan;
	private long nextPayment = -1;
	private long lastChiefPayment = 0;
	private BukkitTask paymentExpiration;

	private final ClanPlotsManager manager;
	
	public ClanPlot(ClanPlotsManager manager, int id, Region region, int price, Location sign, Location spawn) {
		this.manager = manager;
		this.id = id;
		this.sign = sign;
		this.spawn = spawn;
		setPrice(price, false);
		
		this.region = OlympaCore.getInstance().getRegionManager().registerRegion(region, "clanPlot" + id, EventPriority.HIGH, 
				new ClanPlotFlag(),
				new GlassSmashFlag(true),
				new DamageFlag(true, DamageCause.ENTITY_ATTACK, DamageCause.PROJECTILE),
				new GunFlag(true, false));
	}

	public ClanZTA getClan() {
		return clan;
	}

	public void setClan(ClanZTA clan, boolean updateDB) {
		ClanZTA old = this.clan;
		this.clan = clan;
		
		if (old != null) {
			old.setCachedPlot(null);
		}else {
			setNextPayment(-1, updateDB, false);
			setLastChiefPayment(-1, updateDB);
		}

		if (clan != null) clan.setCachedPlot(this);

		if (updateDB) manager.columnClan.updateAsync(this, clan == null ? -1 : clan.getID(), null, null);
	}

	public int getID() {
		return id;
	}

	public TrackedRegion getTrackedRegion() {
		return region;
	}
	
	public void setRegion(Region region) throws SQLException, IOException {
		this.region.updateRegion(region);
		manager.columnRegion.updateValue(this, SpigotUtils.serialize(region));
	}

	public int getPrice() {
		return price;
	}
	
	public String getPriceFormatted() {
		return priceFormatted;
	}

	public void setPrice(int price, boolean update) {
		this.price = price;
		this.priceFormatted = OlympaMoney.format(price);
		if (update) {
			updateSign();
			manager.columnPrice.updateAsync(this, price, null, null);
		}
	}
	
	public Location getSign() {
		return sign;
	}

	public Location getSpawn() {
		return spawn;
	}

	public void setSpawn(Location spawn, boolean update) {
		this.spawn = spawn;
		if (update) {
			manager.columnSpawn.updateAsync(this, SpigotUtils.convertLocationToString(spawn), null, null);
		}
	}
	
	public long getNextPayment() {
		return nextPayment;
	}

	public void setNextPayment(long nextPayment, boolean updateDB, boolean updateReset) {
		this.nextPayment = nextPayment;
		if (nextPayment != -1) {
			long timeBeforeExpiration = getSecondsBeforeExpiration();
			if (timeBeforeExpiration < 0) {
				if (clan != null && updateReset) clan.setResetExpirationTime();
				setClan(null, true);
				updateSign();
				return;
			}else {
				if (paymentExpiration != null) paymentExpiration.cancel();
				paymentExpiration = new BukkitRunnable() {
					@Override
					public void run() {
						clan.broadcast("Vous n'avez pas renouvelé le paiement, votre parcelle est donc arrivée à expiration.");
						clan.setResetExpirationTime();
						setClan(null, true);
						updateSign();
					}
				}.runTaskLater(OlympaZTA.getInstance(), timeBeforeExpiration * 20);
			}
		}
		
		if (updateDB) manager.columnNextPayment.updateAsync(this, nextPayment, null, null);
	}
	
	public void setLastChiefPayment(long time, boolean updateDB) {
		this.lastChiefPayment = time;
		
		if (updateDB) manager.columnLastChiefPayment.updateAsync(this, time, null, null);
	}
	
	public boolean canAnybodyPay() {
		return System.currentTimeMillis() - lastChiefPayment <= CHIEF_PAYMENT_DURATION_MILLIS;
	}

	public String getExpirationDate() {
		return paymentDateFormat.format(new Date(nextPayment));
	}

	public long getSecondsBeforeExpiration() {
		return (nextPayment - System.currentTimeMillis()) / 1000;
	}

	public void updateSign() {
		Sign sign = (Sign) this.sign.getBlock().getState();
		
		sign.getPersistentDataContainer().set(ClanPlotsManager.SIGN_KEY, PersistentDataType.INTEGER, id);

		sign.setLine(0, "§e[§l" + priceFormatted + "§e/semaine§e]");
		sign.setLine(1, "");
		if (clan == null) {
			sign.setLine(2, "§eParcelle à");
			sign.setLine(3, "§elouer");
		}else {
			sign.setLine(2, "§6" + clan.getName());
			sign.setLine(3, "§eExpire le §n" + getExpirationDate());
		}
		sign.update();
		manager.updateBook();
	}

	public void signClick(Player p) {
		OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
		ClanZTA targetClan = player.getClan();
		if (clan != null) {
			if (clan == targetClan) {
				if (nextPayment - System.currentTimeMillis() > PAYMENT_DURATION_MILLIS) {
					Prefix.DEFAULT_BAD.sendMessage(p, "La parcelle a déjà été payée cette semaine.");
					return;
				}
				boolean isChief = clan.getChief() == player.getInformation();
				if (!canAnybodyPay() && !isChief) {
					Prefix.DEFAULT_BAD.sendMessage(p, "Le chef du clan n'a pas payé la parcelle depuis 1 mois. Il est nécessaire qu'il actualise la location.");
					return;
				}
				if (!clan.getMoney().withdraw(price)) {
					Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a pas assez d'argent dans la cagnotte du clan pour payer la parcelle (" + clan.getMoney().getFormatted() + "/" + priceFormatted + ").");
					return;
				}
				setNextPayment(nextPayment + PAYMENT_DURATION_MILLIS, true, false);
				if (isChief) setLastChiefPayment(System.currentTimeMillis(), true);
				updateSign();
				clan.broadcast("La parcelle a été payée pour une nouvelle semaine !");
				return;
			}
			
			Prefix.DEFAULT_BAD.sendMessage(p, "Cette parcelle est déjà louée par un clan !");
			return;
		}
		if (targetClan == null) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Cette parcelle n'est louable qu'à un clan.");
			return;
		}
		if (targetClan.getCachedPlot() != null) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Ton clan loue déjà une parcelle !");
			return;
		}
		if (targetClan.getChief() != player.getInformation()) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Seul le chef du clan peut décider de louer une parcelle.");
			return;
		}
		if (targetClan.getMoney().withdraw(price)) {
			setClan(targetClan, true);
			targetClan.resetExpirationTime();
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.add(Calendar.DATE, PAYMENT_DURATION_DAYS);
			setNextPayment(calendar.getTimeInMillis(), true, false);
			setLastChiefPayment(calendar.getTimeInMillis(), true);
			updateSign();
			targetClan.broadcast("Le clan fait l'acquisition d'une parcelle.");
		}else Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a pas assez d'argent dans la cagnotte du clan pour louer cette parcelle.");
	}
	
	public class ClanPlotFlag extends PlayerBlockInteractFlag {
		
		public ClanPlotFlag() {
			super(true, true, true);
			//setMessages("Vous entrez dans la parcelle " + id, "Vous sortez de la parcelle " + id, ChatMessageType.CHAT);
		}
		
		@Override
		protected void handleOtherBlock(PlayerInteractEvent event) {
			handleCancellable(event, event.getPlayer(), clan != null && OlympaPlayerZTA.get(event.getPlayer()).getClan() != clan);
		}
		
		@Override
		protected void handleInventoryBlock(PlayerInteractEvent event) {
			if (clan == null || (OlympaPlayerZTA.get(event.getPlayer()).getClan() != clan)) {
				handleCancellable(event, event.getPlayer(), true);
				return;
			}
			if (CONTAINER_MATERIALS_ALLOWED.contains(event.getClickedBlock().getType())) {
				ItemStack[] inventory = ((Container) event.getClickedBlock().getState()).getInventory().getContents();
				OlympaZTA.getInstance().getTask().runTaskAsynchronously(() -> {
					try {
						int items = OlympaZTA.getInstance().gunRegistry.loadFromItems(inventory);
						if (items != 0) OlympaZTA.getInstance().sendMessage("%d items chargés depuis un coffre du plot de clan %d de %s.", items, id, clan.getName());
					}catch (SQLException ex) {
						ex.printStackTrace();
					}
				});
				handleCancellable(event, event.getPlayer(), false);
			}else handleCancellable(event, event.getPlayer(), true);
		}
		
	}

}
