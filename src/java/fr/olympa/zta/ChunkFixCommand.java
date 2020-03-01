package fr.olympa.zta;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;

import fr.olympa.api.command.OlympaCommand;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.TileEntity;
import net.minecraft.server.v1_15_R1.World;

public class ChunkFixCommand extends OlympaCommand {

	public ChunkFixCommand() {
		super(OlympaZTA.getInstance(), "chunkfix", ZTAPermissions.CHUNKFIX_COMMAND);
		super.allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		World world = ((CraftPlayer) sender).getHandle().world;
		Chunk oriChunk = getPlayer().getLocation().getChunk();
		for (int x = oriChunk.getX() - 16; x < oriChunk.getX() + 16; x++) {
			for (int z = oriChunk.getZ() - 16; z < oriChunk.getZ() + 16; z++) {
				net.minecraft.server.v1_15_R1.Chunk chunk = ((CraftChunk) getPlayer().getWorld().getChunkAt(x, z)).getHandle();
				for (Iterator<Entry<BlockPosition, TileEntity>> iterator = chunk.getTileEntities().entrySet().iterator(); iterator.hasNext();) {
					Entry<BlockPosition, TileEntity> tile = iterator.next();
					if (!tile.getValue().getTileType().isValidBlock(world.getType(tile.getKey()).getBlock())) {
						System.out.println("invalid tile entity: removing " + tile.getValue().getPosition().toString());
						//world.removeTileEntity(tile.getValue().getPosition());
						//world.tileEntityList.remove(tile.getValue());
						world.tileEntityListTick.remove(tile.getValue());
						iterator.remove();
					}
				}
				//chunk.getTileEntities().clear();
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
