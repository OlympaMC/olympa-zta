package fr.olympa.zta.utils.npcs;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;

import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.core.spigot.OlympaCore;
import net.citizensnpcs.api.event.NPCTeleportEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

public abstract class HologramTrait extends Trait {

	private Hologram hologram;

	public HologramTrait(String name) {
		super(name);
	}

	protected void showHologram() {
		removeHologram();
		hologram = OlympaCore.getInstance().getHologramsManager().createHologram(getHologramLocation(npc.getStoredLocation()), false, getLines());
	}
	
	private Location getHologramLocation(Location npcLocation) {
		return npcLocation.add(0, npc.getEntity().getHeight() + (Boolean.valueOf(npc.data().get(NPC.NAMEPLATE_VISIBLE_METADATA).toString()) ? 0.2 : 0), 0);
	}
	
	protected abstract AbstractLine<HologramLine>[] getLines();
	
	protected void removeHologram() {
		if (hologram != null) {
			hologram.remove();
			hologram = null;
		}
	}
	
	@EventHandler
	public void onNPCTeleport(NPCTeleportEvent e) {
		if (e.getNPC() == npc) {
			if (hologram != null) hologram.move(getHologramLocation(e.getTo()));
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