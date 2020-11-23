package fr.olympa.zta.utils.quests;

import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.core.spigot.OlympaCore;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OlympaRegionRequirement extends AbstractRequirement {
	
	private String regionID;
	
	protected OlympaRegionRequirement() {
		super("olympaRegion");
	}
	
	protected OlympaRegionRequirement(String regionID) {
		this();
		this.regionID = regionID;
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8Région: §7" + regionID, "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new PagedGUI<TrackedRegion>("Régions - Olympa", DyeColor.ORANGE, OlympaCore.getInstance().getRegionManager().getTrackedRegions().values()) {
			
			@Override
			public ItemStack getItemStack(TrackedRegion object) {
				return ItemUtils.item(XMaterial.PAPER, object.getID(), "§8> §7" + object.getFlags().size() + " flags", "§8> §7Priorité : " + object.getPriority().name(), "§8> §7Type de région : " + object.getRegion().getClass().getSimpleName());
			}
			
			@Override
			public void click(TrackedRegion existing, ItemStack item, ClickType clickType) {
				regionID = existing.getID();
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}
		}.create(p);
	}
	
	@Override
	public boolean test(Player p) {
		return OlympaCore.getInstance().getRegionManager().getCachedPlayerRegions(p).stream().anyMatch(region -> region.getID().equals(regionID));
	}
	
	@Override
	public AbstractRequirement clone() {
		return new OlympaRegionRequirement(regionID);
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		datas.put("region", regionID);
	}
	
	@Override
	protected void load(Map<String, Object> savedDatas) {
		regionID = (String) savedDatas.get("region");
	}
	
}
