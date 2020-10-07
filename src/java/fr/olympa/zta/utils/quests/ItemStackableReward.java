package fr.olympa.zta.utils.quests;

import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.registry.ItemStackableInstantiator;
import fr.olympa.zta.registry.ZTARegistry;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;

public class ItemStackableReward extends AbstractReward {
	
	private ItemStackableInstantiator<?> instantiator;
	
	public ItemStackableReward() {
		super("ztaMoneyItem");
	}
	
	public ItemStackableReward(ItemStackableInstantiator<?> instantiator) {
		this();
		this.instantiator = instantiator;
	}
	
	@Override
	public String give(Player p) {
		try {
			ItemStack item = ZTARegistry.get().createItem(instantiator.create());
			SpigotUtils.giveItems(p, item);
			return ItemUtils.getName(item);
		}catch (ReflectiveOperationException e) {
			e.printStackTrace();
			return "Error: " + e.getMessage();
		}
	}
	
	@Override
	public AbstractReward clone() {
		return new ItemStackableReward(instantiator);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + instantiator.clazz.getSimpleName() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new PagedGUI<>("Liste des items", DyeColor.LIGHT_BLUE, ZTARegistry.get().itemStackables) {
			
			@Override
			public ItemStack getItemStack(ItemStackableInstantiator<?> object) {
				return object.getDemoItem();
			}
			
			@Override
			public void click(ItemStackableInstantiator<?> existing) {
				instantiator = existing;
				ItemUtils.lore(clicked, getLore());
				gui.reopen();
			}
			
		};
	}
	
	@Override
	protected void load(Map<String, Object> map) {
		Object type = map.get("instantiator");
		for (ItemStackableInstantiator<?> stackable : ZTARegistry.get().itemStackables) {
			if (stackable.clazz.getSimpleName().equals(type)) {
				instantiator = stackable;
				return;
			}
		}
		throw new IllegalArgumentException("Unknown item stackable type: " + type);
	}
	
	@Override
	protected void save(Map<String, Object> map) {
		map.put("instantiator", instantiator.clazz.getSimpleName());
	}
	
}