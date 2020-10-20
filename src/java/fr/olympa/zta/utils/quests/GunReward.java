package fr.olympa.zta.utils.quests;

import java.util.Arrays;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.weapons.guns.GunType;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;

public class GunReward extends AbstractReward {
	
	private GunType type;
	
	public GunReward() {
		super("ztaGun");
	}
	
	public GunReward(GunType type) {
		this();
		this.type = type;
	}
	
	@Override
	public String give(Player p) {
		ItemStack item = type.createItem();
		if (item == null) return "Error";
		SpigotUtils.giveItems(p, item);
		return ItemUtils.getName(item);
	}
	
	@Override
	public AbstractReward clone() {
		return new GunReward(type);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + type.getName() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new PagedGUI<>("Liste des armes", DyeColor.LIGHT_BLUE, Arrays.asList(GunType.values())) {
			
			@Override
			public ItemStack getItemStack(GunType object) {
				return object.getDemoItem();
			}
			
			@Override
			public void click(GunType existing) {
				type = existing;
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}
			
		};
	}
	
	@Override
	protected void load(Map<String, Object> map) {
		String typeName = (String) map.get("type");
		try {
			type = GunType.valueOf(typeName);
		}catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown item stackable type: " + typeName);
		}
	}
	
	@Override
	protected void save(Map<String, Object> map) {
		map.put("type", type.name());
	}
	
}