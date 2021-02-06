package fr.olympa.zta.weapons.guns.ambiance;

import org.bukkit.entity.Player;

public abstract class AmbianceScenario {

	protected float volume;
	protected float pitch;
	
	public AmbianceScenario(float volume, float pitch) {
		this.volume = volume;
		this.pitch = pitch;
	}
	
	public void run(Player p) {}
	
	public abstract boolean shouldTerminate();
	
}
