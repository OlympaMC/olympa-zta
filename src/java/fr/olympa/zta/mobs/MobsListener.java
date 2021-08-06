package fr.olympa.zta.mobs;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.Nullable;

import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalMultiPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.Conditioned;
import fr.olympa.api.common.randomized.RandomizedPickerBuilder;
import fr.olympa.zta.OlympaPlayerZTA;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.bank.PhysicalMoney;
import fr.olympa.zta.itemstackable.QuestItem;
import fr.olympa.zta.loot.RandomizedInventory.LootContext;
import fr.olympa.zta.loot.creators.AmmoCreator;
import fr.olympa.zta.loot.creators.FoodCreator;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.creators.LootCreator;
import fr.olympa.zta.loot.creators.MoneyCreator;
import fr.olympa.zta.loot.creators.QuestItemCreator;
import fr.olympa.zta.mobs.custom.Mobs.Zombies;
import fr.olympa.zta.weapons.guns.AmmoType;
import net.citizensnpcs.api.CitizensAPI;

public class MobsListener implements Listener {

	private ConditionalMultiPicker<LootCreator, ZombieLootContext> zombieLoots = RandomizedPickerBuilder.<LootCreator, ZombieLootContext>newConditionalBuilder()
			.add(22, new AmmoCreator(3, 4))
			.add(40, new MoneyCreator(PhysicalMoney.BANKNOTE_1, 1, 4))
			.add(15, new FoodCreator(Food.BAKED_POTATO, 2, 4))
			.add(12, new AmmoCreator(AmmoType.LIGHT, 2, 3, false))
			.add(12, new AmmoCreator(AmmoType.HEAVY, 2, 3, false))
			.add(12, new AmmoCreator(AmmoType.HANDWORKED, 2, 3, false))
			.add(8, new AmmoCreator(AmmoType.CARTRIDGE, 1, 2, false))
			.add(7, new QuestItemCreator(QuestItem.AMAS))
			.add(3, new ZombieTypeConditioned(new QuestItemCreator(QuestItem.PILE), Zombies.TANK))
			.add(2, new ZombieTypeConditioned(new QuestItemCreator(QuestItem.CARTE_MERE), Zombies.SPEED))
			.build(0, 2, 20.0);

	public static boolean removeEntities = false;
	
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
				if (zombie.isLooting()) {
					killer.killedZombies.increment();
					ZombieLootContext context = new ZombieLootContext(entity.getKiller(), zombie);
					for (LootCreator creator : zombieLoots.pickMulti(ThreadLocalRandom.current(), context)) {
						e.getDrops().add(creator.create(ThreadLocalRandom.current(), context).getItem());
					}
					return;
				}
			}
		}
		if (entity.hasMetadata("player")) return;
		
		e.getDrops().clear();
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		if (!removeEntities) return;
		int removed = 0;
		for (Entity entity : e.getChunk().getEntities()) {
			if (entity.getType() == EntityType.ZOMBIE) {
				entity.remove();
				removed++;
			}else if (entity instanceof Item item) {
				if (item.getPickupDelay() < 100) {
					item.remove();
					removed++;
				}else System.out.println("ITEM NOT PICKUP");
			}
		}
		if (removed > 0) System.out.println("Suppression de " + removed + " entités");
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
	
	class ZombieLootContext extends LootContext {
		
		private Zombies zombie;
		
		public ZombieLootContext(@Nullable Player player, Zombies zombie) {
			super(player);
			this.zombie = zombie;
		}
		
		public Zombies getZombie() {
			return zombie;
		}
		
	}
	
	class ZombieTypeConditioned implements Conditioned<LootCreator, ZombieLootContext> {
		
		private LootCreator creator;
		private Zombies[] zombies;
		
		public ZombieTypeConditioned(LootCreator creator, Zombies... zombies) {
			this.creator = creator;
			this.zombies = zombies;
		}
		
		@Override
		public LootCreator getObject() {
			return creator;
		}
		
		@Override
		public boolean isValid(ZombieLootContext context) {
			for (Zombies zombie : zombies) {
				if (zombie == context.zombie) return true;
			}
			return false;
		}
		
		@Override
		public boolean isValidWithNoContext() {
			return true;
		}
		
	}
	
}
