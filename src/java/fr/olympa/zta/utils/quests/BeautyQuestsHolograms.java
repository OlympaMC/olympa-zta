package fr.olympa.zta.utils.quests;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.api.spigot.holograms.Hologram;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.core.spigot.OlympaCore;
import fr.skytasul.quests.api.AbstractHolograms;

public class BeautyQuestsHolograms extends AbstractHolograms<Hologram> {
	
	@Override
	public boolean supportPerPlayerVisibility() {
		return true;
	}
	
	@Override
	public boolean supportItems() {
		return false;
	}
	
	@Override
	public AbstractHolograms<Hologram>.BQHologram createHologram(Location lc, boolean defaultVisible) {
		return new OHologram(OlympaCore.getInstance().getHologramsManager().createHologram(lc, false, defaultVisible));
	}
	
	public class OHologram extends BQHologram {
		
		protected OHologram(Hologram hologram) {
			super(hologram);
		}
		
		@Override
		public void setPlayerVisibility(Player p, boolean visible) {
			hologram.setVisibility(p, visible);
		}
		
		@Override
		public void appendTextLine(String text) {
			hologram.addLine(new FixedLine<>(text));
		}
		
		@Override
		public void teleport(Location lc) {
			hologram.move(lc);
		}
		
		@Override
		public void delete() {
			hologram.remove();
		}
		
	}
	
}
