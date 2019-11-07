package fr.olympa.zta.lootchests.creators;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.utils.AbstractRandomizedPicker.Chanced;

public interface LootCreator extends Chanced {

	public abstract ItemStack create(Player p, Random random);

}
