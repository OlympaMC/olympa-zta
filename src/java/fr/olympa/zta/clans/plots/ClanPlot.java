package fr.olympa.zta.clans.plots;

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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.region.Region;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.region.tracking.flags.PlayerBlockInteractFlag;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.ClanZTA;

public class ClanPlot {

	public static final List<Material> CONTAINER_MATERIALS = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL);
	
	public static final int PAYMENT_DURATION_DAYS = 7;
	public static final long PAYMENT_DURATION_MILLIS = PAYMENT_DURATION_DAYS * 24 * 3600 * 1000;
	private static final DateFormat paymentDateFormat = new SimpleDateFormat("dd/MM");

	private final int id;
	private final TrackedRegion region;
	private final int price;
	private final String priceFormatted;
	private final Location sign;
	private final Location spawn;

	private ClanZTA clan;
	private long nextPayment = -1;
	private BukkitTask paymentExpiration;

	private final ClanPlotsManager manager;
	
	public ClanPlot(ClanPlotsManager manager, int id, Region region, int price, Location sign, Location spawn) {
		this.manager = manager;
		this.id = id;
		this.price = price;
		this.priceFormatted = OlympaMoney.format(price);
		this.sign = sign;
		this.spawn = spawn;
		
		this.region = OlympaCore.getInstance().getRegionManager().registerRegion(region, "clanPlot" + id, EventPriority.HIGH, new ClanPlotFlag());
	}

	public ClanZTA getClan() {
		return clan;
	}

	public void setClan(ClanZTA clan, boolean updateDB) {
		if (this.clan != null) {
			this.clan.setCachedPlot(null);
		}else setNextPayment(-1, updateDB, false);

		this.clan = clan;
		if (clan != null) clan.setCachedPlot(this);

		if (updateDB) manager.columnClan.updateAsync(this, clan == null ? -1 : clan.getID(), null, null);
	}

	public int getID() {
		return id;
	}

	public TrackedRegion getTrackedRegion() {
		return region;
	}

	public int getPrice() {
		return price;
	}
	
	public String getPriceFormatted() {
		return priceFormatted;
	}

	public Location getSign() {
		return sign;
	}

	public Location getSpawn() {
		return spawn;
	}

	public long getNextPayment() {
		return nextPayment;
	}

	public void setNextPayment(long nextPayment, boolean updateDB, boolean updateReset) {
		this.nextPayment = nextPayment;
		if (nextPayment != -1) {
			long timeBeforeExpiration = getSecondsBeforeExpiration();
			if (timeBeforeExpiration < 0) {
				if (clan != null) clan.setResetExpirationTime();
				setClan(null, true);
				updateSign();
				if (updateReset) updateDB = true;
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
			sign.setLine(3, "§evendre");
		}else {
			sign.setLine(2, "§6" + clan.getName());
			sign.setLine(3, "§eExpire le §n" + getExpirationDate());
		}
		sign.update();
	}

	public void signClick(Player p) {
		OlympaPlayerZTA player = OlympaPlayerZTA.get(p);
		ClanZTA targetClan = player.getClan();
		if (clan != null) {
			if (clan == targetClan) {
				if (clan.getChief() != player.getInformation()) {
					Prefix.DEFAULT_BAD.sendMessage(p, "Seul le chef du clan peut verser le montant de la location.");
					return;
				}
				if (nextPayment - System.currentTimeMillis() > PAYMENT_DURATION_MILLIS) {
					Prefix.DEFAULT_BAD.sendMessage(p, "La parcelle a déjà été payée cette semaine.");
					return;
				}
				if (!clan.getMoney().withdraw(price)) {
					Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a pas assez d'argent dans la cagnotte du clan pour payer la parcelle (" + clan.getMoney().getFormatted() + "/" + priceFormatted + ").");
					return;
				}
				setNextPayment(nextPayment + PAYMENT_DURATION_MILLIS, true, false);
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
			handleCancellable(event, null, clan != null && OlympaPlayerZTA.get(event.getPlayer()).getClan() != clan);
		}
		
		@Override
		protected void handleInventoryBlock(PlayerInteractEvent event) {
			if (OlympaPlayerZTA.get(event.getPlayer()).getClan() != clan) {
				handleCancellable(event, null, true);
				return;
			}
			if (CONTAINER_MATERIALS.contains(event.getClickedBlock().getType())) {
				ItemStack[] inventory = ((Container) event.getClickedBlock().getState()).getInventory().getContents();
				OlympaZTA.getInstance().getTask().runTaskAsynchronously(() -> {
					try {
						int items = OlympaZTA.getInstance().gunRegistry.loadFromItems(inventory);
						if (items != 0) OlympaZTA.getInstance().sendMessage("%d items chargés depuis un coffre du plot de clan %d de %s.", items, id, clan.getName());
					}catch (SQLException ex) {
						ex.printStackTrace();
					}
				});
				handleCancellable(event, null, false);
			}else handleCancellable(event, null, true);
		}
		
	}

}
