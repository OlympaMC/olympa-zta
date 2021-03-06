package fr.olympa.zta.packetslistener;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.guns.Gun;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig.EnumPlayerDigType;

@Sharable
public class DropHandler extends ChannelDuplexHandler{
	
	private Map<Channel, Integer> toCancel = new HashMap<>();
	
	DropHandler(){}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
		if (msg instanceof PacketPlayInBlockDig) {
			PacketPlayInBlockDig packet = (PacketPlayInBlockDig) msg;
			if (packet.d() == EnumPlayerDigType.DROP_ITEM) {
				Player p = ctx.channel().attr(PacketHandlers.PLAYER_KEY).get();
				if (p == null) {
					OlympaZTA.getInstance().sendMessage("§cImpossible de trouver un joueur par son channel.");
				}else {
					ItemStack item = p.getInventory().getItemInMainHand();
					Gun gun = OlympaZTA.getInstance().gunRegistry.getGun(item);
					if (gun != null) {
						gun.drop(p, item);
						p.updateInventory();
						toCancel.put(ctx.channel(), MinecraftServer.currentTick);
						return; // super.channelRead pas appelé si l'action est annulée
					}
				}
			}
		}
		if (msg instanceof PacketPlayInArmAnimation) {
			if (!toCancel.isEmpty()) {
				Integer tick = toCancel.remove(ctx.channel());
				if (tick != null && MinecraftServer.currentTick - tick.intValue() < 2) return;
			}
		}
		super.channelRead(ctx, msg);
	}
	
}
