package fr.olympa.zta.utils.quests;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.editor.RegionEditor;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.region.tracking.ActionResult;
import fr.olympa.api.spigot.region.tracking.RegionEvent.EntryEvent;
import fr.olympa.api.spigot.region.tracking.RegionEvent.ExitEvent;
import fr.olympa.api.spigot.region.tracking.TrackedRegion;
import fr.olympa.api.spigot.region.tracking.flags.Flag;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.OlympaZTA;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;

public class ZTARegionStage extends AbstractStage {
	
	private static int regionID = 0;
	
	private final Region regionShape;
	private TrackedRegion region;
	private boolean exit;
	
	public ZTARegionStage(QuestBranch branch, Region regionShape, boolean exit) {
		super(branch);
		
		this.exit = exit;
		this.regionShape = regionShape;
	}
	
	@Override
	public void unload() {
		super.unload();
		if (region != null) region.unregister();
	}
	
	@Override
	public void load() {
		super.load();
		region = OlympaCore.getInstance().getRegionManager().registerRegion(regionShape, "questRegion" + branch.getQuest().getID() + "b" + branch.getID() + "s" + branch.getID(this) + "i" + regionID++, EventPriority.NORMAL, new Flag() {
			@Override
			public ActionResult enters(EntryEvent event) {
				if (!exit) {
					if (hasStarted(event.getPlayer()) && canUpdate(event.getPlayer())) OlympaZTA.getInstance().getTask().runTask(() -> finishStage(event.getPlayer()));
				}
				return ActionResult.ALLOW;
			}
			
			@Override
			public ActionResult leaves(ExitEvent event) {
				if (exit) {
					if (hasStarted(event.getPlayer()) && canUpdate(event.getPlayer())) OlympaZTA.getInstance().getTask().runTask(() -> finishStage(event.getPlayer()));
				}
				return ActionResult.ALLOW;
			}
		});
	}
	
	@Override
	public void start(PlayerAccount acc) {
		super.start(acc);
		if (acc.isCurrent()) {
			if (region.getRegion().isIn(acc.getPlayer()) != exit) {
				finishStage(acc.getPlayer());
			}
		}
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return "Trouve la r??gion " + region.getID();
	}
	
	@Override
	protected void serialize(Map<String, Object> map) {
		map.put("region", regionShape);
		if (exit) map.put("exit", exit);
	}
	
	public static ZTARegionStage deserialize(Map<String, Object> map, QuestBranch branch) {
		return new ZTARegionStage(branch, (Region) map.get("region"), ((Boolean) map.getOrDefault("exit", Boolean.FALSE)).booleanValue());
	}
	
	public static class Creation extends StageCreation<ZTARegionStage> {
		
		private Region region;
		private boolean exit = false;
		
		public Creation(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(Material.WOODEN_AXE, "??eChoisir la r??gion"), (p, item) -> {
				fr.skytasul.quests.gui.Inventories.closeWithoutExit(p);
				new RegionEditor(p, newRegion -> {
					if (newRegion == null) return;
					setRegion(newRegion);
					reopenGUI(p, false);
				}).enterOrLeave();
			});
			line.setItem(6, ItemUtils.itemSwitch(Lang.stageRegionExit.toString(), exit), (p, item) -> setExit(ItemUtils.toggle(item)));
		}
		
		private void setRegion(Region region) {
			this.region = region;
		}
		
		private void setExit(boolean exit) {
			if (this.exit != exit) {
				this.exit = exit;
				ItemStack item = line.getItem(6);
				ItemUtils.set(item, exit);
				line.editItem(6, item);
			}
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
			setExit(stage.exit);
		}
		
		@Override
		protected ZTARegionStage finishStage(QuestBranch branch) {
			return new ZTARegionStage(branch, region, exit);
		}
		
	}
	
}
