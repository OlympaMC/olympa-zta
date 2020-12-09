package fr.olympa.zta.utils.npcs;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.core.spigot.OlympaCore;
import net.citizensnpcs.api.event.NPCTeleportEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

public abstract class HologramTrait extends Trait {

	private int hologramID = -1;

	public HologramTrait(String name) {
		super(name);
	}

	protected void showHologram() {
		removeHologram();
		hologramID = OlympaCore.getInstance().getHologramsManager().createHologram(getHologramLocation(npc.getStoredLocation()), false, true, getLines()).getID();
	}
	
	private Location getHologramLocation(Location npcLocation) {
		return npcLocation.add(0, npc.getEntity().getHeight() + (Boolean.valueOf(Objects.toString(npc.data().get(NPC.NAMEPLATE_VISIBLE_METADATA))) ? 0.2 : 0), 0);
	}
	
	protected abstract AbstractLine<HologramLine>[] getLines();
	
	protected void removeHologram() {
		if (hologramID != -1) {
			OlympaCore.getInstance().getHologramsManager().deleteHologram(hologramID);
			hologramID = -1;
		}
	}
	
	@EventHandler
	public void onNPCTeleport(NPCTeleportEvent e) {
		if (e.getNPC() == npc) {
			if (hologramID != -1) OlympaCore.getInstance().getHologramsManager().getHologram(hologramID).move(getHologramLocation(e.getTo()));
		}
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		showHologram();
	}

	@Override
	public void onDespawn() {
		super.onDespawn();
		removeHologram();
	}

	@Override
	public void onRemove() {
		super.onRemove();
		removeHologram();
	}

}