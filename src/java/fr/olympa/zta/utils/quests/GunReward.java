package fr.olympa.zta.utils.quests;

import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.weapons.guns.GunRegistry.GunInstantiator;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;

public class GunReward extends AbstractReward {
	
	private GunInstantiator<?> instantiator;
	
	public GunReward() {
		super("ztaMoneyItem");
	}
	
	public GunReward(GunInstantiator<?> instantiator) {
		this();
		this.instantiator = instantiator;
	}
	
	@Override
	public String give(Player p) {
		ItemStack item = instantiator.createItem();
		if (item == null) return "Error";
		SpigotUtils.giveItems(p, item);
		return ItemUtils.getName(item);
	}
	
	@Override
	public AbstractReward clone() {
		return new GunReward(instantiator);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + instantiator.getClazz().getSimpleName() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new PagedGUI<>("Liste des items", DyeColor.LIGHT_BLUE, OlympaZTA.getInstance().gunRegistry.getInstantiators()) {
			
			@Override
			public ItemStack getItemStack(GunInstantiator<?> object) {
				return object.getDemoItem();
			}
			
			@Override
			public void click(GunInstantiator<?> existing) {
				instantiator = existing;
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}
			
		};
	}
	
	@Override
	protected void load(Map<String, Object> map) {
		String type = (String) map.get("instantiator");
		try {
			instantiator = OlympaZTA.getInstance().gunRegistry.getInstantiator(type);
		}catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Unknown item stackable type: " + type);
		}
	}
	
	@Override
	protected void save(Map<String, Object> map) {
		map.put("instantiator", instantiator.getClazz().getSimpleName());
	}
	
}