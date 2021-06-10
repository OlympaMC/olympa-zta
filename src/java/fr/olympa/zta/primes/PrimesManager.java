package fr.olympa.zta.primes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.common.observable.ObservableList;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import net.citizensnpcs.api.CitizensAPI;

public class PrimesManager implements Listener {
	
	public static final long BOUNTY_EXPIRATION = 7 * 24 * 60 * 60 * 1000;
	
	private SQLTable<Prime> table;
	
	private final SQLColumn<Prime> columnID = new SQLColumn<Prime>("id", "int(11) NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(Prime::getID);
	private final SQLColumn<Prime> columnBuyer = new SQLColumn<>("buyer", "BIGINT NOT NULL", Types.BIGINT);
	private final SQLColumn<Prime> columnTarget = new SQLColumn<>("target", "BIGINT NOT NULL", Types.BIGINT);
	private final SQLColumn<Prime> columnBounty = new SQLColumn<>("bounty", "DOUBLE NOT NULL", Types.DOUBLE);
	private final SQLColumn<Prime> columnExpiration = new SQLColumn<>("expiration", "BIGINT NOT NULL", Types.BIGINT);

	private ObservableList<Prime> primes;
	
	public PrimesManager() throws SQLException {
		table = new SQLTable<>("zta_primes", Arrays.asList(columnID, columnBuyer, columnTarget, columnBounty, columnExpiration), resultSet -> {
			int id = resultSet.getInt("id");
			OlympaPlayerInformations buyer = AccountProvider.getter().getPlayerInformations(resultSet.getLong("buyer"));
			OlympaPlayerInformations target = AccountProvider.getter().getPlayerInformations(resultSet.getLong("target"));
			double bounty = resultSet.getDouble("bounty");
			long expiration = resultSet.getLong("expiration");
			return new Prime(id, buyer, target, bounty, expiration);
		});
		table.createOrAlter();
		
		List<Prime> primes = table.selectAll(null);
		List<Prime> toRemove = new ArrayList<>();
		for (Iterator<Prime> iterator = primes.iterator(); iterator.hasNext();) {
			Prime prime = iterator.next();
			if (prime.isExpired()) {
				iterator.remove();
				toRemove.add(prime);
			}
		}
		if (!toRemove.isEmpty()) {
			table.deleteMultiAsync(null, null, toRemove.toArray(Prime[]::new));
			OlympaZTA.getInstance().sendMessage("§cSuppression de §4%d§c primes expirées.", toRemove.size());
		}
		this.primes = new ObservableList<>(primes);
		OlympaZTA.getInstance().sendMessage("%d primes chargées.", primes.size());
	}
	
	public ObservableList<Prime> getPrimes() {
		return primes;
	}
	
	public void openPrimesGUI(Player p) {
		new PrimesGUI(OlympaPlayerZTA.get(p), primes).create(p);
	}
	
	public void addPrime(OlympaPlayerZTA buyer, OlympaPlayerZTA target, double bounty) throws SQLException {
		long expiration = System.currentTimeMillis() + BOUNTY_EXPIRATION;
		ResultSet resultSet = table.insert(buyer.getId(), target.getId(), bounty, expiration);
		resultSet.next();
		int id = resultSet.getInt("id");
		resultSet.close();
		Prime prime = new Prime(id, buyer.getInformation(), target.getInformation(), bounty, expiration);
		primes.add(prime);
		Prefix.BROADCAST_SERVER.sendMessage(Bukkit.getOnlinePlayers(), "§l%s§7 a mis à prix la tête de §a§l%s§7 pour §a§l%s§7 !", buyer.getName(), target.getName(), prime.getBountyFormatted());
	}
	
	public void removePrime(Prime prime, Runnable success, Consumer<SQLException> fail) {
		primes.remove(prime);
		prime.removed();
		table.deleteAsync(prime, success, fail);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (e.isCancelled()) return;
		if (CitizensAPI.getNPCRegistry().isNPC(e.getEntity())) return;
		Player p = e.getEntity();
		Player killer = p.getKiller();
		if (killer == null) return;
		OlympaPlayerZTA deadP = OlympaPlayerZTA.get(p);
		
		for (Prime prime : primes) {
			if (prime.getTarget().getId() == deadP.getId()) {
				OlympaPlayerZTA killerP = OlympaPlayerZTA.get(killer);
				if (killerP.getClan() != null && killerP.getClan() == deadP.getClan()) {
					Prefix.DEFAULT_BAD.sendMessage(killer, "Tu ne peux pas récupérer la prime sur la tête de %s, il s'agit d'un membre de ton clan.", p.getName());
				}else {
					removePrime(prime, null, ex -> ZTAPermissions.PROBLEM_MONITORING.sendMessage(Prefix.ERROR.toString() + "Une erreur est survenue lors de la terminaison d'une prime."));
					double paid = OlympaZTA.getInstance().taxManager.pay(killerP, prime.getBounty());
					Prefix.BROADCAST_SERVER.sendMessage(Bukkit.getOnlinePlayers(), "§aLa prime de §l%s§a placée sur la tête de §l%s§a par %s a été remportée par §l%s§a !", prime.getBountyFormatted(), p.getName(), prime.getBuyer().getName(), killer.getName());
					Prefix.DEFAULT_GOOD.sendMessage(killer, "§eTu as reçu la prime de %s TTC (%s hors taxes de l'État).", OlympaMoney.format(paid), prime.getBountyFormatted());
				}
				return;
			}
		}
	}
	
}
