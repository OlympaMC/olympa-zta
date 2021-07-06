package fr.olympa.zta.itemstackable;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent.SlotType;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.module.SpigotModule;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.PlayerAbilities;

public class ParachuteModule extends ComplexCommand implements ModuleApi<OlympaZTA>, Listener {
	
	private OlympaZTA plugin;
	
	private Map<Player, Parachuting> players = new HashMap<>();
	
	private double speed = 0.6;
	private double fallspeed = 0.65;
	
	public ParachuteModule(OlympaZTA plugin) throws Exception {
		super(plugin, "parachute", "Gère le système de parachutes.", ZTAPermissions.PARACHUTE_MANAGE_COMMAND);
		SpigotModule<ParachuteModule, ParachuteModule, OlympaZTA, ParachuteModule> module = new SpigotModule<ParachuteModule, ParachuteModule, OlympaZTA, ParachuteModule>(plugin, "parachute", x -> this);
		module.listener(getClass());
		module.cmd(getClass());
		module.enableModule();
		module.registerModule();
	}
	
	@Override
	public boolean disable(OlympaZTA plugin) {
		this.plugin = null;
		return true;
	}
	
	@Override
	public boolean enable(OlympaZTA plugin) {
		this.plugin = plugin;
		return isEnabled();
	}
	
	@Override
	public boolean setToPlugin(OlympaZTA plugin) {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return plugin != null;
	}
	
	@Cmd (min = 2, args = { "DOUBLE", "DOUBLE" }, syntax = "<speed> <fall speed>")
	public void setSpeed(CommandContext cmd) {
		speed = cmd.getArgument(0);
		fallspeed = cmd.getArgument(1);
		sendSuccess("Vitesse de chute des parachutes mise à jour.");
	}
	
	public boolean hasParachute(Player p) {
		ItemStack chestplate = p.getInventory().getChestplate();
		return chestplate != null && (chestplate.getType() == Material.DIAMOND_CHESTPLATE) && (ItemStackableManager.getStackable(chestplate) == Artifacts.PARACHUTE);
	}
	
	public boolean isInAir(Location location) {
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		if (!location.getWorld().getBlockAt(x, y, z).isEmpty()) return false;
		if (!location.getWorld().getBlockAt(x, --y, z).isEmpty()) return false;
		double deltaX = location.getX() - x;
		int newX = x;
		if (deltaX < 0.3) {
			newX--;
			if (!location.getWorld().getBlockAt(newX, y, z).isEmpty()) return false;
		}else if (deltaX > 0.7) {
			newX++;
			if (!location.getWorld().getBlockAt(newX, y, z).isEmpty()) return false;
		}
		double deltaZ = location.getZ() - z;
		int newZ = z;
		if (deltaZ < 0.3) {
			newZ--;
			if (!location.getWorld().getBlockAt(x, y, newZ).isEmpty()) return false;
		}else if (deltaZ > 0.7) {
			newZ++;
			if (!location.getWorld().getBlockAt(x, y, newZ).isEmpty()) return false;
		}
		return location.getWorld().getBlockAt(newX, y, newZ).isEmpty();
	}
	
	public void removeParachute(@NotNull Player player) {
		Parachuting parachuting = players.remove(player);
		if (parachuting != null) parachuting.disable();
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		removeParachute(e.getPlayer());
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntityType() != EntityType.CHICKEN) return;
		for (Parachuting para : players.values()) {
			if (para.chicken.getEntityId() == e.getEntity().getEntityId()) {
				para.forceDisable = true;
				para.disable();
				para.p.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cTon parachute s'est fait toucher !"));
				break;
			}
		}
		e.getDrops().clear();
	}
	
	@EventHandler
	public void onToggleSneak(PlayerToggleSneakEvent e) {
		if (!e.isSneaking()) return;
		Parachuting parachuting = players.get(e.getPlayer());
		if (parachuting != null) {
			if (parachuting.enabled)
				parachuting.disable();
			else
				parachuting.enable();
		}
	}
	
	@EventHandler
	public void onFly(PlayerToggleFlightEvent e) {
		if (e.isFlying()) removeParachute(e.getPlayer());
	}
	
	@EventHandler
	public void onArmor(PlayerArmorChangeEvent e) {
		if (e.getSlotType() != SlotType.CHEST) return;
		boolean parachuteOld = e.getOldItem() != null && e.getOldItem().getType() == Material.DIAMOND_CHESTPLATE;
		boolean parachuteNew = e.getNewItem() != null && e.getNewItem().getType() == Material.DIAMOND_CHESTPLATE;
		if (parachuteOld == parachuteNew) return;
		if (parachuteOld) removeParachute(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onTeleport(PlayerTeleportEvent e) {
		if (e.isCancelled()) return;
		removeParachute(e.getPlayer());
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		
		if (players.containsKey(p)) {
			if (isInAir(e.getTo())) {
				if (players.get(p).enabled) {
					Vector direction = /*e.getFrom()*/p.getLocation().getDirection();
					p.setVelocity(new Vector(direction.getX() * speed, p.getVelocity().getY() * fallspeed, direction.getZ() * speed));
					p.setFallDistance(0.0F);
				}
				return;
			}
			players.remove(p).disable();
		}else {
			if (SpigotUtils.isSameLocation(e.getFrom(), e.getTo())) return;
			if (p.getFallDistance() >= 4 && hasParachute(p) && isInAir(e.getTo())) {
				players.put(p, new Parachuting(p).enable());
			}
		}
	}
	
	class Parachuting {
		Chicken chicken;
		boolean enabled;
		boolean forceDisable = false;
		
		Player p;
		
		PlayerAbilities abilities;
		float oldWalkSpeed;
		
		Parachuting(Player p) {
			this.p = p;
			abilities = ((CraftPlayer) p).getHandle().abilities;
		}
		
		Parachuting enable() {
			if (enabled) return this;
			if (forceDisable) return this;
			enabled = true;
			Location location = p.getLocation();
			chicken = p.getWorld().spawn(location, Chicken.class, x -> {
				x.setAware(false);
				x.setPersistent(false);
				x.setSilent(true);
				x.setCustomName("§7Parachute de " + p.getName());
				x.setMaxHealth(6);
			});
			p.addPassenger(chicken);
			oldWalkSpeed = abilities.walkSpeed;
			abilities.walkSpeed = 10;
			p.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.6f, 1f);
			p.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§b▶ ▷ §e§lParachute déployé§b ◁ ◀"));
			return this;
		}
		
		Parachuting disable() {
			if (enabled) {
				enabled = false;
				if (chicken != null) {
					chicken.remove();
					chicken = null;
				}
				abilities.walkSpeed = oldWalkSpeed;
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1f);
				//p.teleport(p.getLocation());
			}
			return this;
		}
	}
	
}
