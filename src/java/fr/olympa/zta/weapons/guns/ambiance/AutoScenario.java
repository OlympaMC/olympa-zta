package fr.olympa.zta.weapons.guns.ambiance;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.ambiance.SoundAmbiance.ZTASound;

public class AutoScenario extends AmbianceScenario {
	
	private final int fireRate = ThreadLocalRandom.current().nextInt(4, 8);
	private int fireRateWait = -1;
	private int shotsAmount = 0;
	private int salves = ThreadLocalRandom.current().nextInt(1, 6);
	private int salveWait = -1;
	private boolean shouldFire = false;
	
	public AutoScenario(float volume, float pitch) {
		super(volume, pitch);
	}
	
	@Override
	public void run(Player p) {
		if (shouldFire) {
			p.playSound(p.getLocation(), ZTASound.GUN_AUTO.getFarSound(), SoundCategory.AMBIENT, volume, pitch);
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
				salveWait = ThreadLocalRandom.current().nextInt(5, 20);
				shotsAmount = ThreadLocalRandom.current().nextInt(2, 9);
			}
		}
		return false;
	}
	
}
