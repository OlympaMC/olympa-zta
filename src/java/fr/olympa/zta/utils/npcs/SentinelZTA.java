package fr.olympa.zta.utils.npcs;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.mcmonkey.sentinel.SentinelIntegration;
import org.mcmonkey.sentinel.SentinelTrait;

import fr.olympa.zta.weapons.guns.PersistentGun;

public class SentinelZTA extends SentinelIntegration implements Listener {
	
	@Override
	public boolean tryAttack(SentinelTrait st, LivingEntity ent) {
		if (!(st.getLivingEntity() instanceof Player)) return false;
		Player p = (Player) st.getLivingEntity();
		
		ItemStack item = p.getInventory().getItemInMainHand();
		PersistentGun gun = PersistentGun.getGun(item);
		if (gun == null) return false;
		
		Vector faceAcc = ent.getEyeLocation().toVector().subtract(st.getLivingEntity().getEyeLocation().toVector());
		if (faceAcc.lengthSquared() > 0.0) {
			faceAcc = faceAcc.normalize();
		}
		faceAcc = st.fixForAcc(faceAcc);
		st.faceLocation(st.getLivingEntity().getEyeLocation().clone().add(faceAcc.multiply(10)));
		
		gun.onInteract(new PlayerInteractEvent(p, Action.RIGHT_CLICK_AIR, item, null, BlockFace.SOUTH, EquipmentSlot.HAND));
		if (st.rangedChase) {
			st.attackHelper.rechase();
		}
		return true;
	}
	
	@EventHandler
	public void onVelocity(PlayerVelocityEvent e) {
		System.out.println("PlayersListener.onVelocity() " + e.getPlayer());
	}
	
}
