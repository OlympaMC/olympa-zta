package fr.olympa.zta.plots.clans;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.region.Region;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.clans.ClanZTA;

public class ClanPlot {

	private static final long PAYMENT_DURATION_MILLIS = 7 * 24 * 3600 * 1000;

	private final int id;
	private final Region region;
	private final int price;
	private final Location sign;
	private final Location spawn;

	private ClanZTA clan;
	private long nextPayment;
	private BukkitTask paymentExpiration;

	public ClanPlot(int id, Region region, int price, Location sign, Location spawn) {
		this.id = id;
		this.region = region;
		this.price = price;
		this.sign = sign;
		this.spawn = spawn;
	}

	public ClanZTA getClan() {
		return clan;
	}

	public void setClan(ClanZTA clan, boolean update) {
		if (this.clan != null) {
			this.clan.cachedPlot = null;
		}

		this.clan = clan;
		
		if (update) {
			Sign sign = (Sign) this.sign.getBlock().getState();

			sign.setLine(0, "[" + price + "/semaine]");
			if (clan == null) {
				sign.setLine(2, "Parcelle à vendre");
			}else {
				clan.cachedPlot = this;
				sign.setLine(2, clan.getName());
			}

			try {
				PreparedStatement statement = ClanPlotsManager.updatePlotClan.getStatement();
				statement.setLong(1, clan.getID());
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

	public Location getSign() {
		return sign;
	}

	public Location getSpawn() {
		return spawn;
	}

	public long getNextPayment() {
		return nextPayment;
	}

	public void setNextPayment(long nextPayment, boolean update) {
		this.nextPayment = nextPayment;
		if (nextPayment != -1) {
			long timeBeforeExpiration = getSecondsBeforeExpiration();
			if (timeBeforeExpiration < 0) {
				setClan(null, true);
			}else {
				if (paymentExpiration != null) paymentExpiration.cancel();
				paymentExpiration = new BukkitRunnable() {
					@Override
					public void run() {
						clan.broadcast("Vous n'avez pas renouvelé le payement, votre parcelle est donc arrivée à expiration.'");
						setClan(null, true);
					}
				}.runTaskLater(OlympaZTA.getInstance(), timeBeforeExpiration * 20);
			}
		}
		
		if (update) {
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

	public long getSecondsBeforeExpiration() {
		return (nextPayment - System.currentTimeMillis()) / 1000;
	}

	public boolean onInteract(Player player) {
		return OlympaPlayerZTA.get(player).getClan() != clan;
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
				if (nextPayment - System.currentTimeMillis() < PAYMENT_DURATION_MILLIS) {
					Prefix.DEFAULT_BAD.sendMessage(p, "La parcelle a déjà été payée cette semaine.");
					return;
				}
				if (!clan.getMoney().withdraw(price)) {
					Prefix.DEFAULT_BAD.sendMessage(p, "Il n'y a pas assez d'argent dans la cagnotte du clan pour payer la parcelle (" + clan.getMoney().get() + "/" + price + ").");
					return;
				}
				setNextPayment(nextPayment + PAYMENT_DURATION_MILLIS, true);
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
			setNextPayment(System.currentTimeMillis() + PAYMENT_DURATION_MILLIS, true);
			targetClan.broadcast("Le clan fait l'acquisition d'une parcelle.");
		}
	}

}
