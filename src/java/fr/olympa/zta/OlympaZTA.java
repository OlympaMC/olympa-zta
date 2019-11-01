package fr.olympa.zta;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaPlugin;
import fr.olympa.zta.lootchests.ChestCommand;
import fr.olympa.zta.lootchests.ChestsListener;
import fr.olympa.zta.lootchests.LootChest;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.weapons.WeaponsListener;
import fr.olympa.zta.weapons.guns.Gun870;
import fr.olympa.zta.weapons.guns.GunAK;
import fr.olympa.zta.weapons.guns.GunBarrett;
import fr.olympa.zta.weapons.guns.GunBenelli;
import fr.olympa.zta.weapons.guns.GunCobra;
import fr.olympa.zta.weapons.guns.GunDragunov;
import fr.olympa.zta.weapons.guns.GunG19;
import fr.olympa.zta.weapons.guns.GunKSG;
import fr.olympa.zta.weapons.guns.GunLupara;
import fr.olympa.zta.weapons.guns.GunM16;
import fr.olympa.zta.weapons.guns.GunM1897;
import fr.olympa.zta.weapons.guns.GunM1911;
import fr.olympa.zta.weapons.guns.GunP22;
import fr.olympa.zta.weapons.guns.GunSDMR;
import fr.olympa.zta.weapons.guns.GunSkorpion;
import fr.olympa.zta.weapons.guns.GunStoner;
import fr.olympa.zta.weapons.guns.GunUZI;
import fr.olympa.zta.weapons.guns.accessories.CannonDamage;
import fr.olympa.zta.weapons.guns.accessories.CannonPower;
import fr.olympa.zta.weapons.guns.accessories.CannonSilent;
import fr.olympa.zta.weapons.guns.accessories.CannonStabilizer;
import fr.olympa.zta.weapons.guns.accessories.ScopeLight;
import fr.olympa.zta.weapons.guns.accessories.ScopeStrong;
import fr.olympa.zta.weapons.guns.accessories.StockLight;
import fr.olympa.zta.weapons.guns.accessories.StockStrong;
import fr.olympa.zta.weapons.knives.KnifeBatte;
import fr.olympa.zta.weapons.knives.KnifeBiche;
import fr.olympa.zta.weapons.knives.KnifeSurin;

public class OlympaZTA extends OlympaPlugin{

	private static OlympaZTA instance;

	public static OlympaZTA getInstance() {
		return (OlympaZTA) instance;
	}
	
	private WeaponsListener weaponListener = new WeaponsListener();
	private ChestsListener chestsListener = new ChestsListener();

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		OlympaPermission.registerPermissions(ZTAPermissions.class);
		
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(weaponListener, this);
		pluginManager.registerEvents(chestsListener, this);
		//pluginManager.registerEvents(new Inventories(), this); // temporaire : la classe Inventories sera register dans le plugin Core

		getCommand("gun").setExecutor(new GunCommand());
		new ChestCommand().register();
		
		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
		
		Arrays.asList(
				GunM1911.class, GunCobra.class, Gun870.class, GunUZI.class, GunM16.class, GunM1897.class, GunG19.class, GunSkorpion.class, GunAK.class, GunBenelli.class, GunDragunov.class, GunLupara.class, GunP22.class, GunSDMR.class, GunStoner.class, GunBarrett.class, GunKSG.class,
				KnifeBatte.class, KnifeBiche.class, KnifeSurin.class,
				CannonDamage.class, CannonPower.class, CannonSilent.class, CannonStabilizer.class, ScopeLight.class, ScopeStrong.class, StockLight.class, StockStrong.class,
				LootChest.class).forEach(ZTARegistry::registerObjectType);
		

		for (Player p : getServer().getOnlinePlayers()) {
			weaponListener.onJoin(new PlayerJoinEvent(p.getPlayer(), "random join message"));
		}
	}

	@Override
	public void onDisable(){
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
		
		for (Player p : getServer().getOnlinePlayers()) {
			weaponListener.onQuit(new PlayerQuitEvent(p.getPlayer(), "random quit message"));
		}
	}
	
}
