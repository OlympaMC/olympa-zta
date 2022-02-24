package fr.olympa.zta.weapons.guns.ambiance;

import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.ambiance.SoundAmbiance.ZTASound;

public class ExplosionScenario extends AmbianceScenario {
	
	private boolean done = false;
	
	public ExplosionScenario(float volume, float pitch) {
		super(volume, pitch);
	}
	
	@Override
	public void run(Player p) {
		playSound(p, ZTASound.EXPLOSION_CIVIL.getSound(), 0.08f, 0);
	}
	
	@Override
	public boolean shouldTerminate() {
		if (done) return true;
		done = true;
		return false;
	}
	
}
