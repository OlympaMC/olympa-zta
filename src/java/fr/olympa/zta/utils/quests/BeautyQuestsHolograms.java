package fr.olympa.zta.utils.quests;

import org.bukkit.Location;

import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.core.spigot.OlympaCore;
import fr.skytasul.quests.api.AbstractHolograms;

public class BeautyQuestsHolograms extends AbstractHolograms<Hologram> {
	
	@Override
	public boolean supportPerPlayerVisibility() {
		return false;
	}
	
	@Override
	public boolean supportItems() {
		return false;
	}
	
	@Override
	public AbstractHolograms<Hologram>.BQHologram createHologram(Location lc, boolean defaultVisible) {
		Hologram hologram = OlympaCore.getInstance().getHologramsManager().createHologram(lc, false);
		return new OHologram(hologram);
	}
	
	public class OHologram extends BQHologram {
		
		protected OHologram(Hologram hologram) {
			super(hologram);
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
