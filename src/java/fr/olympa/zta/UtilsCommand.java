package fr.olympa.zta;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;

import com.google.common.collect.Sets;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.region.RegionManager;
import fr.olympa.api.region.RegionManager.TrackedRegion;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.World;

public class UtilsCommand extends ComplexCommand {

	public UtilsCommand() {
		super(null, OlympaZTA.getInstance(), "utils", "Commandes diverses", ZTAPermissions.UTILS_COMMAND);
	}

	@Cmd (player = true)
	public void chunkfix(CommandContext cmd) {
		World world = ((CraftPlayer) sender).getHandle().world;
		Chunk oriChunk = getPlayer().getLocation().getChunk();
		for (int x = oriChunk.getX() - 16; x < oriChunk.getX() + 16; x++) {
			for (int z = oriChunk.getZ() - 16; z < oriChunk.getZ() + 16; z++) {
				net.minecraft.server.v1_15_R1.Chunk chunk = ((CraftChunk) getPlayer().getWorld().getChunkAt(x, z)).getHandle();
				for (Iterator<Entry<BlockPosition, TileEntity>> iterator = chunk.getTileEntities().entrySet().iterator(); iterator.hasNext();) {
					Entry<BlockPosition, TileEntity> tile = iterator.next();
					if (!tile.getValue().getTileType().isValidBlock(world.getType(tile.getKey()).getBlock())) {
						String str = "invalid tile entity: removing " + tile.getValue().getPosition().toString();
						System.out.println(str);
						sendSuccess(str);
						//world.removeTileEntity(tile.getValue().getPosition());
						//world.tileEntityList.remove(tile.getValue());
						world.tileEntityListTick.remove(tile.getValue());
						iterator.remove();
					}
				}
				//chunk.getTileEntities().clear();
			}
		}
	}

	@Cmd (player = true)
	public void regions(CommandContext cmd) {
		RegionManager regionManager = OlympaCore.getInstance().getRegionManager();
		Set<TrackedRegion> trackedRegions = regionManager.getTrackedRegions();

		sendInfo("Régions trackées : " + trackedRegions.size());
		sendInfo("Total de points : " + trackedRegions.stream().mapToInt(x -> x.getRegion().getLocations().size()).sum());

		Set<TrackedRegion> playerRegions = regionManager.getCachedPlayerRegions(cmd.player);
		StringJoiner joiner = new StringJoiner(", ", "Vous êtes actuellement dans les régions : [", "]");
		playerRegions.forEach(x -> joiner.add(x.getID()));
		sendInfo(joiner.toString());

		Set<TrackedRegion> applicable = trackedRegions.stream().filter(x -> x.getRegion().isIn(cmd.player)).collect(Collectors.toSet());
		sendInfo("Différences entre les régions en cache et les régions calculées : §l" + Sets.symmetricDifference(playerRegions, applicable));
	}

}
