package fr.olympa.zta;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.objects.OlympaPlugin;
import fr.olympa.api.task.TaskManager;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.zta.weapons.guns.Gun870;
import fr.olympa.zta.weapons.guns.GunBarrett;
import fr.olympa.zta.weapons.guns.GunCobra;
import fr.olympa.zta.weapons.guns.GunM16;
import fr.olympa.zta.weapons.guns.GunM1897;
import fr.olympa.zta.weapons.guns.GunM1911;
import fr.olympa.zta.weapons.guns.GunUZI;
import fr.olympa.zta.weapons.guns.accessories.CannonDamage;
import fr.olympa.zta.weapons.guns.accessories.CannonPower;
import fr.olympa.zta.weapons.guns.accessories.CannonSilent;
import fr.olympa.zta.weapons.guns.accessories.CannonStabilizer;
import fr.olympa.zta.weapons.guns.accessories.ScopeLight;
import fr.olympa.zta.weapons.guns.accessories.ScopeStrong;
import fr.olympa.zta.weapons.guns.accessories.StockLight;
import fr.olympa.zta.weapons.guns.accessories.StockStrong;
import fr.olympa.zta.weapons.knives.KnifeBat;

public class OlympaZTA extends JavaPlugin implements OlympaPlugin{
	protected static OlympaZTA instance;
	
	private TaskManager taskManager = new TaskManager(this);
	private ZTAListener listener = new ZTAListener();
	
	public static OlympaZTA getInstance() {
		return instance;
	}

	private String getPrefixConsole() {
		return "&f[&6" + this.getDescription().getName() + "&f] &e";
	}

	@Override
	public void onEnable() {
		instance = this;

		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(listener, this);
		pluginManager.registerEvents(new Inventories(), this); // temporaire : la classe Inventories sera register dans le plugin Core

		getCommand("gun").setExecutor(new GunCommand());
		
		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
		
		Arrays.asList(
				GunM1911.class, GunCobra.class, Gun870.class, GunUZI.class, GunM16.class, GunM1897.class, GunBarrett.class,
				KnifeBat.class,
				CannonDamage.class, CannonPower.class, CannonSilent.class, CannonStabilizer.class, ScopeLight.class, ScopeStrong.class, StockLight.class, StockStrong.class).forEach((x) -> ZTARegistry.registerObjectType(x));
		
		for (Player p : getServer().getOnlinePlayers()) {
			listener.onJoin(new PlayerJoinEvent(p.getPlayer(), "random join message"));
		}
	}

	@Override
	public void onDisable(){
		this.sendMessage("§4" + this.getDescription().getName() + "§c (" + this.getDescription().getVersion() + ") is disabled.");
		
		for (Player p : getServer().getOnlinePlayers()) {
			listener.onQuit(new PlayerQuitEvent(p.getPlayer(), "random quit message"));
		}
	}
	
	public void sendMessage(final String message) {
		this.getServer().getConsoleSender().sendMessage(SpigotUtils.color(this.getPrefixConsole() + message));
	}
	
	public TaskManager getTaskManager(){
		return taskManager;
	}
}
