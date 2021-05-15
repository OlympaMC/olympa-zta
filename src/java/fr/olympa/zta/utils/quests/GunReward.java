package fr.olympa.zta.utils.quests;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;

public class GunReward extends AbstractReward {
	
	private ItemStackable type;
	private int amount = 1;
	
	public GunReward() {
		super("ztaGun");
	}
	
	public GunReward(ItemStackable type, int amount) {
		this();
		this.type = type;
		this.amount = amount;
	}
	
	@Override
	public List<String> give(Player p) {
		ItemStack item = null;
		for (int i = 0; i < amount; i++) {
			item = type.createItem();
			if (item == null) return Arrays.asList("Error");
			SpigotUtils.giveItems(p, item);
		}
		return Arrays.asList("§a" + type.getName() + (amount == 1 ? "§e" : " §ex" + amount));
	}
	
	@Override
	public AbstractReward clone() {
		return new GunReward(type, amount);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + (type == null ? null : type.getName() + " (" + type.getId() + ") x" + amount) };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new PagedGUI<>("Liste des armes", DyeColor.LIGHT_BLUE, ItemStackableManager.stackables) {
			
			@Override
			public ItemStack getItemStack(ItemStackable object) {
				return object.getDemoItem();
			}
			
			@Override
			public void click(ItemStackable existing, ItemStack item, ClickType clickType) {
				new TextEditor<>(p, () -> {
					if (type == null) gui.remove(GunReward.this);
					gui.reopen();
				}, x -> {
					type = existing;
					amount = x;
					ItemUtils.lore(clicked, getLore());
					gui.reopen();
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			}
			
			@Override
			public CloseBehavior onClose(Player p, Inventory inv) {
				gui.reopen();
				return CloseBehavior.NOTHING;
			}
			
		}.create(p);
	}
	
	@Override
	protected void load(Map<String, Object> map) {
		String typeID = (String) map.get("type");
		type = ItemStackableManager.stackables.stream()
				.filter(x -> x.getId().equals(typeID))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown item stackable type: " + typeID));
		if (map.containsKey("amount")) amount = (int) map.get("amount");
	}
	
	@Override
	protected void save(Map<String, Object> map) {
		map.put("type", type.getId());
		if (amount != 1) map.put("amount", amount);
	}
	
}