package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.AbstractRandomizedPicker.Chanced;

public interface LootCreator extends Chanced {

	public abstract void give(Player p, Random random);

}
