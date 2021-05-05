package fr.olympa.zta.mobs;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;

import fr.olympa.api.utils.RandomizedPicker;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.itemstackable.QuestItem;
import fr.olympa.zta.loot.creators.AmmoCreator;
import fr.olympa.zta.loot.creators.FoodCreator;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.MoneyCreator;
import fr.olympa.zta.loot.creators.QuestItemCreator;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import fr.olympa.zta.utils.PhysicalMoney;
import fr.olympa.zta.weapons.guns.AmmoType;
import net.citizensnpcs.api.CitizensAPI;

public class MobsListener implements Listener {

	private RandomizedPicker<LootCreator> zombieLoots = new RandomizedPicker.FixedPicker<>(0, 2, 20,
			new AmmoCreator(22, 3, 4),
			new MoneyCreator(45, PhysicalMoney.BANKNOTE_1, 3, 9),
			new FoodCreator(15, Food.BAKED_POTATO, 3, 5),
			new AmmoCreator(12, AmmoType.LIGHT, 2, 3, false),
			new AmmoCreator(12, AmmoType.HEAVY, 2, 3, false),
			new AmmoCreator(12, AmmoType.HANDWORKED, 2, 3, false),
			new AmmoCreator(5, AmmoType.CARTRIDGE, 1, 2, false),
			new QuestItemCreator(7, QuestItem.AMAS)
			);

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		LivingEntity entity = e.getEntity();
		
		if (entity.getKiller() != null && !CitizensAPI.getNPCRegistry().isNPC(entity.getKiller())) {
			OlympaPlayerZTA killer = OlympaPlayerZTA.get(entity.getKiller());
			if (entity instanceof Player && !CitizensAPI.getNPCRegistry().isNPC(entity)) {
				killer.killedPlayers.increment();
			}else {
				if (!entity.hasMetadata("ztaZombieType")) return;
				Zombies zombie = (Zombies) entity.getMetadata("ztaZombieType").get(0).value();
				if (zombie == Zombies.COMMON || zombie == Zombies.DROWNED) {
					killer.killedZombies.increment();
					for (LootCreator creator : zombieLoots.pick(ThreadLocalRandom.current())) {
						e.getDrops().add(creator.create(ThreadLocalRandom.current()).getItem());
					}
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDamage(EntityDamageEvent e) {
		if (e.isCancelled()) return;
		if (e.getCause() == DamageCause.FALL) {
			if (e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				if (p.getInventory().getBoots() != null && p.getInventory().getBoots().getType() == Material.DIAMOND_BOOTS) {
					e.setCancelled(true);
					return;
				}
			}
			e.setDamage(e.getDamage() / 1.5);
		}else if (e.getCause() == DamageCause.ENTITY_EXPLOSION) {
			if (e.getEntity() instanceof Item) {
				e.setCancelled(true);
				return;
			}/*else if (e.getEntity() instanceof Player) {
				e.setDamage(e.getDamage() / 2);
			}*/
		}
		if (e.getEntity() instanceof Item) {
			OlympaZTA.getInstance().gunRegistry.ifGun(((Item) e.getEntity()).getItemStack(), OlympaZTA.getInstance().gunRegistry::removeObject);
		}
	}

	@EventHandler
	public void onItemRemove(ItemDespawnEvent e) {
		OlympaZTA.getInstance().getTask().runTaskAsynchronously(() -> OlympaZTA.getInstance().gunRegistry.itemRemove(e.getEntity().getItemStack()));
	}
	
}
