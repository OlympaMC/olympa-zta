package fr.olympa.zta.clans.plots;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.region.Region;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.ClanZTA;

public class ClanPlot {

	private static final int PAYMENT_DURATION_DAYS = 7;
	private static final long PAYMENT_DURATION_MILLIS = PAYMENT_DURATION_DAYS * 24 * 3600 * 1000;
	private static final DateFormat paymentDateFormat = new SimpleDateFormat("dd/MM");

	private final int id;
	private final Region region;
	private final int price;
	private final String priceFormatted;
	private final Location sign;
	private final Location spawn;

	private ClanZTA clan;
	private long nextPayment = -1;
	private BukkitTask paymentExpiration;

	public ClanPlot(int id, Region region, int price, Location sign, Location spawn) {
		this.id = id;
		this.region = region;
		this.price = price;
		this.priceFormatted = OlympaMoney.format(price);
		this.sign = sign;
		this.spawn = spawn;
	}

	public ClanZTA getClan() {
		return clan;
	}

	public void setClan(ClanZTA clan, boolean updateDB) {
		if (this.clan != null) {
			this.clan.cachedPlot = null;
		}else setNextPayment(-1, true);

		this.clan = clan;
		if (clan != null) clan.cachedPlot = this;

		if (updateDB) {
			try {
				PreparedStatement statement = ClanPlotsManager.updatePlotClan.getStatement();
				statement.setLong(1, clan == null ? -1 : clan.getID());
				statement.setInt(2, id);
				statement.executeUpdate();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int getID() {
		return id;
	}

	public Region getRegion() {
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

	public void setNextPayment(long nextPayment, boolean updateDB) {
		this.nextPayment = nextPayment;
		if (nextPayment != -1) {
			long timeBeforeExpiration = getSecondsBeforeExpiration();
			if (timeBeforeExpiration < 0) {
				setClan(null, true);
				updateSign();
			}else {
				if (paymentExpiration != null) paymentExpiration.cancel();
				paymentExpiration = new BukkitRunnable() {
					@Override
					public void run() {
						clan.broadcast("Vous n'avez pas renouvelé le paiement, votre parcelle est donc arrivée à expiration.'");
						setClan(null, true);
						updateSign();
					}
				}.runTaskLater(OlympaZTA.getInstance(), timeBeforeExpiration * 20);
			}
		}
		
		if (updateDB) {
			try {
				PreparedStatement statement = ClanPlotsManager.updatePlotNextPayment.getStatement();
				statement.setLong(1, nextPayment);
				statement.setInt(2, id);
				statement.executeUpdate();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public String getExpirationDate() {
		return paymentDateFormat.format(new Date(nextPayment));
	}

	public long getSecondsBeforeExpiration() {
		return (nextPayment - System.currentTimeMillis()) / 1000;
	}

	public boolean onInteract(Player player) {
		return OlympaPlayerZTA.get(player).getClan() != clan;
	}

	public void updateSign() {
		Sign sign = (Sign) this.sign.getBlock().getState();

		sign.setLine(0, "[" + priceFormatted + "/semaine]");
		if (clan == null) {
			sign.setLine(2, "Parcelle à");
			sign.setLine(3, "vendre");
		}else {
			sign.setLine(2, clan.getName());
			sign.setLine(3, "Expire le " + getExpirationDate());
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
				setNextPayment(nextPayment + PAYMENT_DURATION_MILLIS, true);
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
		if (targetClan.cachedPlot != null) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Ton clan loue déjà une parcelle !");
			return;
		}
		if (targetClan.getChief() != player.getInformation()) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Seul le chef du clan peut décider de louer une parcelle.");
			return;
		}
		if (targetClan.getMoney().withdraw(price)) {
			setClan(targetClan, true);
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.add(Calendar.DATE, PAYMENT_DURATION_DAYS);
			setNextPayment(calendar.getTimeInMillis(), true);
			updateSign();
			targetClan.broadcast("Le clan fait l'acquisition d'une parcelle.");
		}else Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a pas assez d'argent dans la cagnotte du clan pour louer cette parcelle.");
	}

}
