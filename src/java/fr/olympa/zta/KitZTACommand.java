package fr.olympa.zta;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.Grenade;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunType;

public class KitZTACommand extends OlympaCommand {
	
	private static final NumberFormat numberFormat = new DecimalFormat("00");
	
	public KitZTACommand(Plugin plugin) {
		super(plugin, "kit", "Obtenir son kit.", (OlympaSpigotPermission) null);
		setAllowConsole(false);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		OlympaPlayerZTA p = getOlympaPlayer();
		long timeToWait = p.kitVIPTime.get() + TimeUnit.DAYS.toMillis(1) - System.currentTimeMillis();
		if (timeToWait > 0) {
			sendError("Tu dois encore attendre %s avant de pouvoir reprendre ton kit !", Utils.durationToString(numberFormat, timeToWait));
			return false;
		}
		if (ZTAPermissions.GROUP_LEGENDE.hasPermission(p)) {
			giveKit(p, "héros", GunType.M16.createItem(), Grenade.GRENADE.get(3), AmmoType.LIGHT.getAmmo(64, true), AmmoType.CARTRIDGE.getAmmo(32, true), AmmoType.HEAVY.getAmmo(32, true), Food.COOKED_RABBIT.get(16), ArmorType.MILITARY.get(ArmorSlot.BOOTS), ArmorType.MILITARY.get(ArmorSlot.LEGGINGS), ArmorType.ANTIRIOT.get(ArmorSlot.CHESTPLATE), ArmorType.ANTIRIOT.get(ArmorSlot.HELMET));
		}else if (ZTAPermissions.GROUP_HEROS.hasPermission(p)) {
			giveKit(p, "héros", GunType.M1897.createItem(), AmmoType.LIGHT.getAmmo(64, true), AmmoType.CARTRIDGE.getAmmo(32, true), AmmoType.HEAVY.getAmmo(32, true), Food.COOKED_RABBIT.get(16), ArmorType.ANTIRIOT.get(ArmorSlot.BOOTS), ArmorType.ANTIRIOT.get(ArmorSlot.LEGGINGS), ArmorType.ANTIRIOT.get(ArmorSlot.CHESTPLATE), ArmorType.ANTIRIOT.get(ArmorSlot.HELMET));
		}else if (ZTAPermissions.GROUP_SAUVEUR.hasPermission(p)) {
			giveKit(p, "sauveur", GunType.P22.createItem(), AmmoType.LIGHT.getAmmo(64, true), AmmoType.CARTRIDGE.getAmmo(32, true), Food.APPLE.get(16), ArmorType.ANTIRIOT.get(ArmorSlot.BOOTS), ArmorType.ANTIRIOT.get(ArmorSlot.LEGGINGS), ArmorType.ANTIRIOT.get(ArmorSlot.CHESTPLATE), ArmorType.ANTIRIOT.get(ArmorSlot.HELMET));
		}else if (ZTAPermissions.GROUP_RODEUR.hasPermission(p)) {
			giveKit(p, "rôdeur", Knife.SURIN.createItem(), AmmoType.LIGHT.getAmmo(64, true), Food.CARROT.get(16), ArmorType.GANGSTER.get(ArmorSlot.BOOTS), ArmorType.GANGSTER.get(ArmorSlot.LEGGINGS), ArmorType.GANGSTER.get(ArmorSlot.CHESTPLATE), ArmorType.GANGSTER.get(ArmorSlot.HELMET));
		}else if (ZTAPermissions.GROUP_SURVIVANT.hasPermission(p)) {
			giveKit(p, "survivant", Knife.BICHE.createItem(), AmmoType.LIGHT.getAmmo(32, true));
		}else {
			sendHoverAndURL(Prefix.DEFAULT_BAD, "Tu n'as pas de kit. Achètes un grade sur la boutique pour en obtenir un !", "§7Clique pour accéder à la boutique", "https://olympa.fr/shop");
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
	private void giveKit(OlympaPlayerZTA player, String name, ItemStack... items) {
		player.kitVIPTime.set(System.currentTimeMillis());
		Player p = (Player) player.getPlayer();
		SpigotUtils.giveItems(p, items);
		Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as reçu ton kit §l%s§a !", name);
	}
	
}
