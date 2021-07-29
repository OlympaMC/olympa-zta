package fr.olympa.zta.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PlayerConnection;

public class Tab {
	
	private List<Packet<?>> packets = new ArrayList<>(80);
	
	/*public Tab() {
		String randomized = "f" + Integer.toString(ThreadLocalRandom.current().nextInt(6));
		for (int i = 0; i < 79; i++) {
			PacketPlayOutScoreboardTeam packetTeam = new PacketPlayOutScoreboardTeam();
			Reflection.setFieldValue(packetTeam, "a", "ZZZZZZ" + i + randomized);
			Reflection.setFieldValue(packetTeam, "e", "never");
			Reflection.setFieldValue(packetTeam, "f", "never");
			Reflection.setFieldValue(packetTeam, "g", EnumChatFormat.BLACK);
			packets.add(packetTeam);
			packetTeam = new PacketPlayOutScoreboardTeam();
			Reflection.setFieldValue(packetTeam, "i", 3);
			Reflection.setFieldValue(packetTeam, "a", i + randomized);
			Reflection.setFieldValue(packetTeam, "h", Arrays.asList(i + randomized));
			packets.add(packetTeam);
			GameProfile gameProfile = new GameProfile(UUID.randomUUID(), i + randomized);
			gameProfile.getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYxOTEyNjUwNTYxNCwKICAicHJvZmlsZUlkIiA6ICIwNjNhMTc2Y2RkMTU0ODRiYjU1MjRhNjQyMGM1YjdhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJkYXZpcGF0dXJ5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U5ODdjMzAzYWQ0NTQzM2I1Y2IwYzE5NzliNzU0NDU5Njg2OWViMmJhYzk4N2JjZjdjNjE3MTc4YjA1YzA5YWUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", "qXWUQvLTDtxI/SAXsiipNvAZ6OQaBgE0Z3ITREphrI8XlJJeqNAz/dJahUup4oSub4c5hW25/dzh9WnF5NQ73qwOtXuzvMbNRJ+z+m/GOISqWJKTBAn+P1u6R3rSs4DAVKw/FGZt61jxkrHPUbsPjTOB+9wRRWtekvneCvWkehTHGNtu6hwJpNarpZIsSPfl2kB7imupEd2byITxZR1kCo7XSVWwKMh90ie35ZF85vexlrfG5axly/NLtnx8sanhEMjSVb6qwaBpg2g04LsaZMJg8NpigEs5+2Ux1jhfjoaz5xXWpeLwIcpOD43hpXzCRbKILmcTF/F6wz6sOSFxV4zNsSlYKiptw3zZ6QTGuF29nO1d0j2XSU0Be3cryLgpAwRmqXEbBA5cBTz0bkE/hotT3hUISb0NRpANxvADYB0GdEEfSwvN1ruQCP7kt4OiHxPDoLuSOEFpmULIUiX8lKcqk6X2KYD7AjFavBm0BFh664dXtqpPJFbDpNTJdCXo6MDbzsTBj836v7Q6IT1TSph+BMMeNOlj/F/0cNRCwY59PypQPVeGcBOTANJzMfBicD4mtw4LZoNZOw8Erin9keoIeqNBwSG41eU8gM9U/SbK5rdCgmqJszfWSGZu+JYzRVQjfPOg7uWZ5xieqWECaHHbgUuJDO9ALFEKGW3w7ZY="));
			PacketPlayOutPlayerInfo packetInfo = new PacketPlayOutPlayerInfo();
			Reflection.setFieldValue(packetInfo, "a", EnumPlayerInfoAction.ADD_PLAYER);
			Reflection.setFieldValue(packetInfo, "b", Arrays.asList(packetInfo.new PlayerInfoData(gameProfile, 0, EnumGamemode.ADVENTURE, ChatComponentText.d)));
			packets.add(packetInfo);
		}
	}*/
	
	public void join(Player p) {
		PlayerConnection playerConnection = ((CraftPlayer) p).getHandle().playerConnection;
		packets.forEach(playerConnection::sendPacket);
	}
	
	public void show(String name) {
		
	}
	
}
