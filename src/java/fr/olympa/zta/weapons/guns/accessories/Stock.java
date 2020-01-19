package fr.olympa.zta.weapons.guns.accessories;

public abstract class Stock extends Accessory{
	
	public Stock(int id) {
		super(id);
	}

	public AccessoryType getType() {
		return AccessoryType.STOCK;
	}

}
