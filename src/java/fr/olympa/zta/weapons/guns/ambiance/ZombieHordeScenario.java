package fr.olympa.zta.weapons.guns.ambiance;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import fr.olympa.zta.weapons.guns.ambiance.SoundAmbiance.ZTASound;

public class ZombieHordeScenario extends AmbianceScenario {
	
	private int zombies = ThreadLocalRandom.current().nextInt(1, 4);
	private int[] zombiesTicks = new int[zombies];
	private int timeUntilZombieDeath = -1;
	private boolean zombieMoaning = false;
	
	public ZombieHordeScenario(float volume, float pitch) {
		super(volume, pitch);
		Arrays.fill(zombiesTicks, -1);
	}
	
	@Override
	public void run(Player p) {
		if (zombieMoaning) {
			p.playSound(p.getLocation(), ZTASound.ZOMBIE_AMBIENT.getFarSound(), SoundCategory.AMBIENT, volume, pitch);
		}
	}
	
	@Override
	public boolean shouldTerminate() {
		zombieMoaning = false;
		if (timeUntilZombieDeath == -1) {
			timeUntilZombieDeath = ThreadLocalRandom.current().nextInt(20, 250);
		}else if (timeUntilZombieDeath-- == 0) {
			if (--zombies == 0) return true;
		}
		for (int i = 0; i < zombies; i++) {
			if (zombiesTicks[i] == -1) {
				zombiesTicks[i] = ThreadLocalRandom.current().nextInt(50, 230);
			}else if (zombiesTicks[i]-- == 0) zombieMoaning = true;
		}
		return false;
	}
	
}
