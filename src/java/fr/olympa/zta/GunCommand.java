package fr.olympa.zta;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;

import fr.olympa.zta.weapons.Weapon;
import fr.olympa.zta.weapons.WeaponsRegistry;
import fr.olympa.zta.weapons.guns.AmmoType;

public class GunCommand implements CommandExecutor{
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cYou must be a player to perform this command.");
			return true;
		}
		Player p = (Player) sender;
		
		if (args.length == 1) {
			int speed = Integer.parseInt(args[0]);
			p.launchProjectile(Snowball.class, p.getLocation().getDirection().multiply(speed));
		}else {
			for (AmmoType ammo : AmmoType.values()) {
				ammo.give(p, 50, true);
			}
			for (Class<? extends Weapon> clazz : WeaponsRegistry.weaponsTypes) {
				try {
					WeaponsRegistry.giveWeapon(p, clazz.newInstance());
				}catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		return true;
	}
	
}
