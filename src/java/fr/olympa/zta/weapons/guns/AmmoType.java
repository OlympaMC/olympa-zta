package fr.olympa.zta.weapons.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public enum AmmoType{
	
	LIGHT("Munitions légères", Material.CACTUS_GREEN, Material.LIME_DYE),
	HEAVY("Munitions lourdes", Material.LAPIS_LAZULI, Material.LIGHT_BLUE_DYE),
	HANDWORKED("Munitions artisanales", Material.ROSE_RED, Material.PINK_DYE),
	CARTRIDGE("Cartouches", Material.GRAY_DYE, Material.LIGHT_GRAY_DYE);
	
	private String name;
	private Material empty, fill;
	
	private AmmoType(String name, Material empty, Material fill){
		this.name = name;
		this.empty = empty;
		this.fill = fill;
	}
	
	public String getName(){
		return name;
	}
	
	public Material getEmptyAmmoType(){
		return empty;
	}
	
	public Material getFilledAmmoType(){
		return fill;
	}
	
	/**
	 * @param p Player sur qui compter les munitions
	 * @return Nombre de munitions que le joueur a dans son inventaire
	 */
	public int getAmmos(Player p){
		int i = 0;
		for (ItemStack is : p.getInventory().getContents()) {
			if (is != null && is.getType() == fill) i += is.getAmount();
		}
		return i;
	}
	
	/**
	 * Retirer la quantité données de munitions de l'inventaire du joueur.<br>
	 * <b>Aucune vérification de quantité n'est incluse, veuillez l'effectuer au préalable !</b>
	 * @param p Joueur à qui retirer les munitions
	 * @param amount Quantité de munitions à retirer
	 * @return Quantité de munitions retirées
	 */
	public int removeAmmos(Player p, int amount){
		int removed = 0;
		ItemStack[] contents = p.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack is = contents[i];
			if (is == null || is.getType() != fill) continue;
			if (is.getAmount() == amount) {
				p.getInventory().setItem(i, null);
				return amount;
			}else if (is.getAmount() > amount) {
				removed += amount;
				is.setAmount(is.getAmount() - amount);
				return removed;
			}else if (is.getAmount() < amount) {
				amount -= is.getAmount();
				removed += is.getAmount();
				p.getInventory().setItem(i, new ItemStack(Material.AIR));
			}
		}
		return removed;
	}
	
	/**
	 * @param amount Quantité de munitions
	 * @param filled <tt>true</tt> si les munitions sont pleines
	 * @return Item correspondant aux paramètres des munitions
	 */
	public ItemStack getAmmo(int amount, boolean filled) {
		return new ItemStack(filled ? fill : empty, amount);
	}
	
	public static ItemStack getPowder(int amount) {
		return new ItemStack(Material.BONE_MEAL, amount);
	}

}
