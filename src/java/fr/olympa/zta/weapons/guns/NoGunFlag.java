package fr.olympa.zta.weapons.guns;

import fr.olympa.api.region.tracking.flags.AbstractProtectionFlag;

public class NoGunFlag extends AbstractProtectionFlag {

	public NoGunFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}

	public boolean isFireEnabled() {
		return !protectedByDefault;
	}

}
