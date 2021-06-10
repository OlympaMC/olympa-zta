package fr.olympa.zta.weapons.guns.ambiance;

import java.util.concurrent.ThreadLocalRandom;

public class SilentScenario extends AmbianceScenario {
	
	private int duration = ThreadLocalRandom.current().nextInt(250);
	
	public SilentScenario(float volume, float pitch) {
		super(volume, pitch);
	}
	
	@Override
	public boolean shouldTerminate() {
		return duration-- <= 0;
	}
	
}
