package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.Prefix;

public class MoneyLoot implements LootCreator {

	public void give(Player p, Random random) {
		int amount = random.nextInt(50) + 5;
		// TODO don de l'argent
		p.sendMessage(Prefix.DEFAULT + "Vous avez trouvé " + amount + "Ω");
	}

	public double getChance() {
		return -1;
	}

}
