package fr.olympa.zta;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.zta.weapons.WeaponsRegistry;
import fr.olympa.zta.weapons.guns.Gun870;
import fr.olympa.zta.weapons.guns.GunCobra;
import fr.olympa.zta.weapons.guns.GunM16;
import fr.olympa.zta.weapons.guns.GunM1897;
import fr.olympa.zta.weapons.guns.GunM1911;
import fr.olympa.zta.weapons.guns.GunUZI;
import fr.tristiisch.olympa.api.objects.OlympaPlugin;
import fr.tristiisch.olympa.api.task.TaskManager;
import fr.tristiisch.olympa.api.utils.SpigotUtils;

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

		getCommand("gun").setExecutor(new GunCommand());
		
		this.sendMessage("§2" + this.getDescription().getName() + "§a (" + this.getDescription().getVersion() + ") is activated.");
		
		WeaponsRegistry.weaponsTypes.addAll(Arrays.asList(GunM1911.class, GunCobra.class, Gun870.class, GunUZI.class, GunM16.class, GunM1897.class));
		
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
