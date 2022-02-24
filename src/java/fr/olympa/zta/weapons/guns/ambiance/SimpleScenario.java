package fr.olympa.zta.weapons.guns.ambiance;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.ambiance.SoundAmbiance.ZTASound;

public class SimpleScenario extends AmbianceScenario {
	
	private int shots = ThreadLocalRandom.current().nextInt(2, 5);
	private int fireRate = ThreadLocalRandom.current().nextInt(8, 22);
	private int fireRateWait = -1;
	private boolean shouldFire = false;
	
	public SimpleScenario(float volume, float pitch) {
		super(volume, pitch);
	}
	
	@Override
	public void run(Player p) {
		if (shouldFire) {
			playSound(p, ZTASound.GUN_GENERIC.getFarSound());
		}
	}
	
	@Override
	public boolean shouldTerminate() {
		shouldFire = false;
		if (fireRateWait >= 0) {
			if (fireRateWait-- == 0) {
				shouldFire = true;
			}
			return false;
		}else {
			if (shots-- == 0) {
				return true;
			}else fireRateWait = fireRate;
		}
		return false;
	}
	
}
