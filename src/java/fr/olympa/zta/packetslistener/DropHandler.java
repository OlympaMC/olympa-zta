package fr.olympa.zta.packetslistener;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.zta.registry.ItemStackable;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.weapons.Weapon;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_15_R1.PacketPlayInBlockDig;
import net.minecraft.server.v1_15_R1.PacketPlayInBlockDig.EnumPlayerDigType;

@Sharable
public class DropHandler extends ChannelDuplexHandler{
	
	DropHandler(){}
	
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
		if (msg instanceof PacketPlayInBlockDig) {
			PacketPlayInBlockDig packet = (PacketPlayInBlockDig) msg;
			if (packet.d() == EnumPlayerDigType.DROP_ITEM) {
				Player p = PacketHandlers.retrievePlayerFromChannel(ctx.channel());
				ItemStack item = p.getInventory().getItemInMainHand();
				ItemStackable object = ZTARegistry.getItemStackable(item);
				if (object instanceof Weapon) {
					if (((Weapon) object).drop(p, item)) {
						p.updateInventory();
						return; // super.channelRead pas appelé si l'action est annulée
					}
				}
			}
		}
		super.channelRead(ctx, msg);
	}
	
}
