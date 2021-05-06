package fr.olympa.zta.primes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.sql.SQLTable;
import fr.olympa.api.utils.Prefix;
import fr.olympa.zta.OlympaPlayerZTA;

public class PrimesManager {
	
	private SQLTable<Prime> table;
	
	private final SQLColumn<Prime> columnID = new SQLColumn<Prime>("id", "int(11) NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(Prime::getID);
	private final SQLColumn<Prime> columnBuyer = new SQLColumn<>("buyer", "BIGINT NOT NULL", Types.BIGINT);
	private final SQLColumn<Prime> columnTarget = new SQLColumn<>("target", "BIGINT NOT NULL", Types.BIGINT);
	private final SQLColumn<Prime> columnBounty = new SQLColumn<>("bounty", "DOUBLE NOT NULL", Types.DOUBLE);

	private List<Prime> primes;
	
	public PrimesManager() throws SQLException {
		table = new SQLTable<>("zta_primes", Arrays.asList(columnID, columnBuyer, columnTarget, columnBounty), resultSet -> {
			int id = resultSet.getInt("id");
			OlympaPlayerInformations buyer = AccountProvider.getPlayerInformations(resultSet.getLong("buyer"));
			OlympaPlayerInformations target = AccountProvider.getPlayerInformations(resultSet.getLong("target"));
			double bounty = resultSet.getDouble("bounty");
			return new Prime(id, buyer, target, bounty);
		});
		table.createOrAlter();
		
		primes = table.selectAll();
	}
	
	public List<Prime> getPrimes() {
		return primes;
	}
	
	public void openPrimesGUI(Player p) {
		new PrimesGUI(primes).create(p);
	}
	
	public void addPrime(OlympaPlayerZTA buyer, OlympaPlayerZTA target, double bounty) throws SQLException {
		ResultSet resultSet = table.insert(buyer.getId(), target.getId(), bounty);
		int id = resultSet.getInt("id");
		resultSet.close();
		primes.add(new Prime(id, buyer.getInformation(), target.getInformation(), bounty));
		Prefix.DEFAULT.sendMessage(Bukkit.getOnlinePlayers(), "§l%s a mis a prix la tête de %s à %s !", buyer.getName(), target.getName(), bounty);
	}
	
}
