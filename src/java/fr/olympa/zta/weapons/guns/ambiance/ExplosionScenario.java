package fr.olympa.zta.weapons.guns.ambiance;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.ambiance.SoundAmbiance.ZTASound;

public class ExplosionScenario extends AmbianceScenario {
	
	private boolean done = false;
	
	public ExplosionScenario(float volume, float pitch) {
		super(volume, pitch);
	}
	
	@Override
	public void run(Player p) {
		p.playSound(p.getLocation(), ZTASound.EXPLOSION_CIVIL.getSound(), SoundCategory.AMBIENT, volume + 0.08f, pitch);
	}
	
	@Override
	public boolean shouldTerminate() {
		if (done) return true;
		done = true;
		return false;
	}
	
}
