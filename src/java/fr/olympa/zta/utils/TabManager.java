package fr.olympa.zta.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import fr.olympa.api.utils.Reflection;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_16_R3.PlayerConnection;

public class TabManager implements Listener {
	
	private DecimalFormat format = new DecimalFormat("00");
	
	private Map<Integer, String> texts = new HashMap<>(20);
	
	private List<Packet<?>> teamPackets = new ArrayList<>(5);
	private List<FakePlayer> fakePlayers = new ArrayList<>(80);
	
	private String npcTeamName;
	
	public TabManager() {}
	
	public TabManager addText(int slot, String text) {
		Validate.isTrue(npcTeamName == null, "Already built");
		Validate.isTrue(slot >= 0 && slot < 40, "Slot must be bound into 0 - 39");
		texts.put(slot, text);
		return this;
	}
	
	public TabManager build() {
		String randomized = Integer.toString(Math.abs(ThreadLocalRandom.current().nextInt()));
		
		List<String> players = new ArrayList<>(20);
		for (int i = 0; i < 20; i++) {
			String playerName = randomized + "é" + format.format(i);
			fakePlayers.add(createPlayer(playerName, texts.containsKey(i) ? new ChatComponentText(texts.get(i)) : ChatComponentText.d));
			players.add(playerName);
		}
		createTeam("+00AAA" + randomized, players).forEach(teamPackets::add);
		
		players = new ArrayList<>(59);
		for (int i = 20; i < 80; i++) {
			String playerName = randomized + "é" + format.format(i);
			fakePlayers.add(createPlayer(playerName, i >= 60 && texts.containsKey(i - 40) ? new ChatComponentText(texts.get(i - 40)) : ChatComponentText.d));
			players.add(playerName);
		}
		createTeam("A__ZZA" + randomized, players).forEach(teamPackets::add);
		
		npcTeamName = "___zzz" + randomized;
		//createTeam(npcTeamName, null).forEach(teamPackets::add);
		
		return this;
	}

	private List<PacketPlayOutScoreboardTeam> createTeam(String teamName, List<String> players) {
		Validate.isTrue(teamName.length() <= 16, "Team " + teamName + " length greater than 16 characters");
		PacketPlayOutScoreboardTeam packetTeam = new PacketPlayOutScoreboardTeam();
		Reflection.setFieldValue(packetTeam, "a", teamName);
		Reflection.setFieldValue(packetTeam, "e", "never");
		Reflection.setFieldValue(packetTeam, "f", "never");
		Reflection.setFieldValue(packetTeam, "g", EnumChatFormat.BLACK);
		if (players != null) {
			PacketPlayOutScoreboardTeam packetPlayers = new PacketPlayOutScoreboardTeam();
			Reflection.setFieldValue(packetPlayers, "i", 3);
			Reflection.setFieldValue(packetPlayers, "a", teamName);
			Reflection.setFieldValue(packetPlayers, "h", players);
			return Arrays.asList(packetTeam, packetPlayers);
		}
		return Arrays.asList(packetTeam);
	}
	
	private FakePlayer createPlayer(String playerName, IChatBaseComponent component) {
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), playerName);
		gameProfile.getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYxOTEyNjUwNTYxNCwKICAicHJvZmlsZUlkIiA6ICIwNjNhMTc2Y2RkMTU0ODRiYjU1MjRhNjQyMGM1YjdhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJkYXZpcGF0dXJ5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U5ODdjMzAzYWQ0NTQzM2I1Y2IwYzE5NzliNzU0NDU5Njg2OWViMmJhYzk4N2JjZjdjNjE3MTc4YjA1YzA5YWUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", "qXWUQvLTDtxI/SAXsiipNvAZ6OQaBgE0Z3ITREphrI8XlJJeqNAz/dJahUup4oSub4c5hW25/dzh9WnF5NQ73qwOtXuzvMbNRJ+z+m/GOISqWJKTBAn+P1u6R3rSs4DAVKw/FGZt61jxkrHPUbsPjTOB+9wRRWtekvneCvWkehTHGNtu6hwJpNarpZIsSPfl2kB7imupEd2byITxZR1kCo7XSVWwKMh90ie35ZF85vexlrfG5axly/NLtnx8sanhEMjSVb6qwaBpg2g04LsaZMJg8NpigEs5+2Ux1jhfjoaz5xXWpeLwIcpOD43hpXzCRbKILmcTF/F6wz6sOSFxV4zNsSlYKiptw3zZ6QTGuF29nO1d0j2XSU0Be3cryLgpAwRmqXEbBA5cBTz0bkE/hotT3hUISb0NRpANxvADYB0GdEEfSwvN1ruQCP7kt4OiHxPDoLuSOEFpmULIUiX8lKcqk6X2KYD7AjFavBm0BFh664dXtqpPJFbDpNTJdCXo6MDbzsTBj836v7Q6IT1TSph+BMMeNOlj/F/0cNRCwY59PypQPVeGcBOTANJzMfBicD4mtw4LZoNZOw8Erin9keoIeqNBwSG41eU8gM9U/SbK5rdCgmqJszfWSGZu+JYzRVQjfPOg7uWZ5xieqWECaHHbgUuJDO9ALFEKGW3w7ZY="));
		
		PacketPlayOutPlayerInfo packetInfoCreate = new PacketPlayOutPlayerInfo();
		Reflection.setFieldValue(packetInfoCreate, "a", EnumPlayerInfoAction.ADD_PLAYER);
		Reflection.setFieldValue(packetInfoCreate, "b", Arrays.asList(packetInfoCreate.new PlayerInfoData(gameProfile, 0, EnumGamemode.ADVENTURE, component)));
		
		PacketPlayOutPlayerInfo packetInfoRemove = new PacketPlayOutPlayerInfo();
		Reflection.setFieldValue(packetInfoRemove, "a", EnumPlayerInfoAction.REMOVE_PLAYER);
		Reflection.setFieldValue(packetInfoRemove, "b", Arrays.asList(packetInfoRemove.new PlayerInfoData(gameProfile, 0, EnumGamemode.ADVENTURE, component)));
		
		return new FakePlayer(gameProfile.getId(), packetInfoCreate, packetInfoRemove);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		//if (true) return;
		Player p = e.getPlayer();
		
		PlayerConnection playerConnection = ((CraftPlayer) p).getHandle().playerConnection;
		teamPackets.forEach(playerConnection::sendPacket);
		
		int online = Bukkit.getOnlinePlayers().size();
		List<Packet<?>> globalPackets = new ArrayList<>();
		if (online >= 60) {
			if (online == 60) { // = on vient de dépasser la limite, il faut remove le reste des fake players
				for (int i = 0; i < 20; i++) {
					globalPackets.add(fakePlayers.get(i).removePacket);
				}
			}
		}else if (online >= 40) {
			if (online == 40) {
				for (int i = 60; i < 80; i++) {
					globalPackets.add(fakePlayers.get(i).removePacket);
				}
			}else {
				for (int i = 0; i < 20; i++) playerConnection.sendPacket(fakePlayers.get(i).createPacket);
			}
		}else {
			globalPackets.add(fakePlayers.get(19 + online).removePacket);
			for (int i = 0; i < 80; i++) {
				if (!(i > 19 && i < 20 + online)) playerConnection.sendPacket(fakePlayers.get(i).createPacket);
			}
		}
		if (!globalPackets.isEmpty()) {
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (onlinePlayer == p) continue;
				playerConnection = ((CraftPlayer) onlinePlayer).getHandle().playerConnection;
				globalPackets.forEach(playerConnection::sendPacket);
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		int online = Bukkit.getOnlinePlayers().size() - 1;
		if (online == 0) return;
		
		List<Packet<?>> globalPackets = new ArrayList<>();
		if (online == 40) {
			for (int i = 60; i < 80; i++) {
				globalPackets.add(fakePlayers.get(i).createPacket);
			}
		}else if (online == 60) {
			for (int i = 0; i < 20; i++) {
				globalPackets.add(fakePlayers.get(i).createPacket);
			}
		}else {
			globalPackets.add(fakePlayers.get(20 + online).createPacket);
		}
		
		if (!globalPackets.isEmpty()) {
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (onlinePlayer == e.getPlayer()) continue;
				PlayerConnection playerConnection = ((CraftPlayer) onlinePlayer).getHandle().playerConnection;
				globalPackets.forEach(playerConnection::sendPacket);
			}
		}
	}
	
	/*@EventHandler
	public void onNPCSpawn(NPCSpawnEvent e) {
		if (e.getNPC().getTraitNullable(MobType.class).getType() == EntityType.PLAYER) {
			Util.generateTeamFor(e.getNPC(), e.getNPC().getEntity().getName(), npcTeamName);
		}
	}*/
	
	class FakePlayer {
		UUID uuid;
		PacketPlayOutPlayerInfo createPacket;
		PacketPlayOutPlayerInfo removePacket;
		
		public FakePlayer(UUID uuid, PacketPlayOutPlayerInfo createPacket, PacketPlayOutPlayerInfo removePacket) {
			this.uuid = uuid;
			this.createPacket = createPacket;
			this.removePacket = removePacket;
		}
	}
	
}
