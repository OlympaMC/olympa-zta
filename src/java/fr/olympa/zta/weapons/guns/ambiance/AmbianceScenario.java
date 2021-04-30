package fr.olympa.zta.weapons.guns.ambiance;

import org.bukkit.SoundCategory;
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
	
	protected void playSound(Player p, String sound) {
		playSound(p, sound, 0, 0);
	}
	
	protected void playSound(Player p, String sound, float deltaVolume, float deltaPitch) {
		p.playSound(p.getLocation(), sound, SoundCategory.AMBIENT, volume + deltaVolume, pitch + deltaPitch);
	}
	
}
