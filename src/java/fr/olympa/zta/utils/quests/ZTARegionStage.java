package fr.olympa.zta.utils.quests;

import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

import fr.olympa.api.editor.RegionEditor;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.region.Region;
import fr.olympa.api.region.tracking.ActionResult;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;

public class ZTARegionStage extends AbstractStage {
	
	private static int regionID = 0;
	
	private TrackedRegion region;
	
	public ZTARegionStage(QuestBranch branch, Region region) {
		super(branch);
		
		this.region = OlympaCore.getInstance().getRegionManager().registerRegion(region, "questRegion" + regionID++, EventPriority.NORMAL, new Flag() {
			@Override
			public ActionResult enters(Player p, Set<TrackedRegion> to) {
				if (hasStarted(p) && canUpdate(p)) OlympaZTA.getInstance().getTask().runTask(() -> finishStage(p));
				return ActionResult.ALLOW;
			}
		});
	}
	
	@Override
	public void unload() {
		super.unload();
		region.unregister();
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) {
			if (region.getRegion().isIn(acc.getPlayer())) {
				finishStage(acc.getPlayer());
			}
		}
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return "Trouve la région " + region.getID();
	}
	
	@Override
	protected void serialize(Map<String, Object> map) {
		map.put("region", region.getRegion());
	}
	
	public static ZTARegionStage deserialize(Map<String, Object> map, QuestBranch branch) {
		return new ZTARegionStage(branch, (Region) map.get("region"));
	}
	
	public static class Creation extends StageCreation<ZTARegionStage> {
		
		private Region region;
		
		public Creation(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(Material.WOODEN_AXE, "§eChoisir la région"), (p, item) -> {
				fr.skytasul.quests.gui.Inventories.closeWithoutExit(p);
				new RegionEditor(p, newRegion -> {
					if (newRegion == null) return;
					setRegion(newRegion);
					reopenGUI(p, false);
				}).enterOrLeave();
			});
		}
		
		private void setRegion(Region region) {
			this.region = region;
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			fr.skytasul.quests.gui.Inventories.closeWithoutExit(p);
			new RegionEditor(p, newRegion -> {
				if (newRegion == null) {
					remove();
				}else {
					setRegion(newRegion);
				}
				reopenGUI(p, false);
			}).enterOrLeave();
		}
		
		@Override
		public void edit(ZTARegionStage stage) {
			super.edit(stage);
			setRegion(stage.region.getRegion());
		}
		
		@Override
		protected ZTARegionStage finishStage(QuestBranch branch) {
			return new ZTARegionStage(branch, region);
		}
		
	}
	
}
