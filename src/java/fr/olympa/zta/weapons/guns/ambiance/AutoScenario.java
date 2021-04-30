package fr.olympa.zta.weapons.guns.ambiance;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.ambiance.SoundAmbiance.ZTASound;

public class AutoScenario extends AmbianceScenario {
	
	private final int fireRate = ThreadLocalRandom.current().nextInt(3, 7);
	private int fireRateWait = -1;
	private int shotsAmount = 0;
	private int salves = ThreadLocalRandom.current().nextInt(1, 4);
	private int salveWait = -1;
	private boolean shouldFire = false;
	
	public AutoScenario(float volume, float pitch) {
		super(volume, pitch);
	}
	
	@Override
	public void run(Player p) {
		if (shouldFire) {
			playSound(p, ZTASound.GUN_AUTO.getFarSound());
		}
	}
	
	@Override
	public boolean shouldTerminate() {
		shouldFire = false;
		if (salveWait-- >= 0) {
			return false;
		}
		if (fireRateWait >= 0) {
			if (fireRateWait-- == 0) {
				shouldFire = true;
			}
			return false;
		}else {
			if (shotsAmount >= 0) {
				shotsAmount--;
				fireRateWait = fireRate;
			}else {
				if (salves-- == 0) return true;
				salveWait = ThreadLocalRandom.current().nextInt(5, 25);
				shotsAmount = ThreadLocalRandom.current().nextInt(2, 7);
			}
		}
		return false;
	}
	
}
