package fr.olympa.zta.utils.quests;

import java.util.Arrays;
import java.util.function.Consumer;

import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import fr.skytasul.quests.api.mobs.MobFactory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.XMaterial;

public class ZTAMobFactory implements MobFactory<Zombies> {
	
	@Override
	public String getID() {
		return "ztaZombies";
	}
	
	@Override
	public ItemStack getFactoryItem() {
		return ItemUtils.item(XMaterial.ZOMBIE_SPAWN_EGG, "§eOlympa ZTA - zombie");
	}
	
	@Override
	public void itemClick(Player p, Consumer<Zombies> run) {
		new PagedGUI<>("Zombies - ZTA", DyeColor.GREEN, Arrays.asList(Zombies.values())) {
			
			@Override
			public ItemStack getItemStack(Zombies object) {
				return ItemUtils.item(XMaterial.mobItem(object.getBukkitType()), "§a" + object.getName());
			}
			
			@Override
			public void click(Zombies existing, ItemStack item, ClickType clickType) {
				Inventories.closeAndExit(p);
				run.accept(existing);
			}
			
		}.create(p);
	}
	
	@Override
	public Zombies fromValue(String value) {
		return Zombies.valueOf(value);
	}
	
	@Override
	public String getValue(Zombies data) {
		return data.name();
	}
	
	@Override
	public String getName(Zombies data) {
		return data.getName();
	}
	
	@Override
	public EntityType getEntityType(Zombies data) {
		return data.getBukkitType();
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();
		if (entity.getKiller() == null) return;
		if (!entity.hasMetadata("ztaZombieType")) return;
		Zombies zombie = (Zombies) entity.getMetadata("ztaZombieType").get(0).value();
		callEvent(e, zombie, entity, e.getEntity().getKiller());
	}
	
}
