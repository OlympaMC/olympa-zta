package fr.olympa.zta.weapons.guns.accessories;

public abstract class Cannon extends Accessory{
	
	public Cannon(int id) {
		super(id);
	}

	public AccessoryType getType() {
		return AccessoryType.CANNON;
	}

}
