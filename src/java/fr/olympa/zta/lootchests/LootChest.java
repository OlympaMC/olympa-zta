package fr.olympa.zta.lootchests;

import java.util.Random;

import org.bukkit.entity.Player;

import fr.olympa.zta.registry.Registrable;

public class LootChest implements Registrable{
	
	private final int id = new Random().nextInt();
	public int getID(){
		return id;
	}
	
	public void click(Player p) {

	}

}
