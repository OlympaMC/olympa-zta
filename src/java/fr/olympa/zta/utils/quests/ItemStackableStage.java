package fr.olympa.zta.utils.quests;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.zta.itemstackable.ItemStackable;
import fr.olympa.zta.itemstackable.ItemStackableManager;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.stages.StageBringBack;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class ItemStackableStage extends StageBringBack {
	
	private Map<ItemStackable, Integer> items;
	
	public ItemStackableStage(QuestBranch branch, Map<ItemStackable, Integer> items, String customMessage) {
		super(branch, items.entrySet().stream().map(x -> {
			ItemStack item = x.getKey().createItem();
			item.setAmount(x.getValue());
			return item;
		}).toArray(ItemStack[]::new), customMessage, new ItemComparisonMap());
		this.items = items;
	}
	
	@Override
	public boolean checkItems(Player p, boolean msg) {
		List<Entry<ItemStackable, Integer>> toRemove = items.entrySet().stream().map(AbstractMap.SimpleEntry::new).collect(Collectors.toList());
		for (ItemStack item : p.getInventory().getContents()) {
			ItemStackable stackable = ItemStackableManager.getStackable(item);
			if (stackable != null) {
				for (Iterator<Entry<ItemStackable, Integer>> iterator = toRemove.iterator(); iterator.hasNext();) {
					Entry<ItemStackable, Integer> entry = iterator.next();
					if (entry.getKey().equals(stackable)) {
						entry.setValue(entry.getValue() - item.getAmount());
						if (entry.getValue() <= 0) iterator.remove();
						break;
					}
				}
			}
		}
		if (toRemove.isEmpty()) return true;
		if (msg) Lang.NpcText.sendWP(p, npcName(), Utils.format(getMessage(), line), 1, 1);
		return false;
	}
	
	@Override
	public void removeItems(Player p) {
		List<Entry<ItemStackable, Integer>> toRemove = items.entrySet().stream().map(AbstractMap.SimpleEntry::new).collect(Collectors.toList());
		PlayerInventory inv = p.getInventory();
		ItemStack[] items = inv.getContents();
		for (int slot = 0; slot < items.length; slot++) {
			ItemStack item = items[slot];
			ItemStackable stackable = ItemStackableManager.getStackable(item);
			if (stackable != null) {
				for (Iterator<Entry<ItemStackable, Integer>> iterator = toRemove.iterator(); iterator.hasNext();) {
					Entry<ItemStackable, Integer> entry = iterator.next();
					if (entry.getKey().equals(stackable)) {
						if (entry.getValue() == item.getAmount()) {
							inv.setItem(slot, null);
							iterator.remove();
						}else if (entry.getValue() > item.getAmount()) {
							entry.setValue(entry.getValue() - item.getAmount());
							inv.setItem(slot, null);
						}else {
							item.setAmount(item.getAmount() - entry.getValue());
							iterator.remove();
						}
						break;
					}
				}
			}
		}
	}
	
	@Override
	public void serialize(Map<String, Object> map) {
		super.serialize(map);
		map.remove("items");
		map.put("itemStackables", items.entrySet().stream().collect(Collectors.toMap(x -> x.getKey().getUniqueId(), Entry<ItemStackable, Integer>::getValue)));
	}
	
	public static ItemStackableStage deserialize(Map<String, Object> map, QuestBranch branch) {
		String customMessage = (String) map.getOrDefault("customMessage", null);
		ItemStackableStage st = new ItemStackableStage(branch, ((Map<String, Integer>) map.get("itemStackables")).entrySet().stream().collect(Collectors.toMap(x -> ItemStackableManager.getStackable(x.getKey()), Entry<String, Integer>::getValue)), customMessage);
		st.loadDatas(map);
		return st;
	}
	
	public static class Creation extends StageBringBack.AbstractCreator<ItemStackableStage> {
		
		private Map<ItemStackable, Integer> itemsStackables;
		
		public Creation(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(12, ItemUtils.item(Material.NETHER_BRICK, "§aChoisir les items"), (p, item) -> openGUI(p, () -> line.gui.reopen(p, true)));
		}
		
		public void openGUI(Player p, Runnable end) {
			new ListGUI<Entry<ItemStackable, Integer>>("Items", DyeColor.PURPLE, itemsStackables.entrySet()) {
				
				@Override
				public void finish(List<Entry<ItemStackable, Integer>> objects) {
					Creation.this.itemsStackables = new HashMap<>();
					objects.forEach(x -> Creation.this.itemsStackables.put(x.getKey(), x.getValue()));
					end.run();
				}
				
				@Override
				public ItemStack getObjectItemStack(Entry<ItemStackable, Integer> object) {
					return object.getKey().getDemoItem();
				}
				
				@Override
				public void createObject(Function<Entry<ItemStackable, Integer>, ItemStack> callback) {
					Runnable reopen = this::reopen;
					new PagedGUI<>("Liste des items", DyeColor.LIGHT_BLUE, ItemStackableManager.stackables) {
						
						@Override
						public ItemStack getItemStack(ItemStackable object) {
							return object.getDemoItem();
						}
						
						@Override
						public void click(ItemStackable existing, ItemStack item, ClickType clickType) {
							new TextEditor<>(p, () -> {
								reopen.run();
							}, obj -> {
								callback.apply(new AbstractMap.SimpleEntry<>(existing, obj));
							}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
						}
						
						@Override
						public CloseBehavior onClose(Player p, Inventory inv) {
							return CloseBehavior.REOPEN;
						}
						
					}.create(p);
				}
			}.create(p);
		}
		
		@Override
		public void start(Player p) {
			itemsStackables = new HashMap<>();
			openGUI(p, () -> {
				if (itemsStackables.isEmpty()) {
					line.gui.deleteStageLine(line);
					line.gui.reopen(p, true);
				}else {
					super.start(p);
					Inventories.onClick(new InventoryClickEvent(p.getOpenInventory(), SlotType.CONTAINER, 8, ClickType.RIGHT, InventoryAction.PICKUP_ONE));
				}
			});
		}
		
		@Override
		public void edit(ItemStackableStage stage) {
			super.edit(stage);
			itemsStackables = new HashMap<>(stage.items);
		}
		
		@Override
		protected ItemStackableStage createStage(QuestBranch branch) {
			return new ItemStackableStage(branch, itemsStackables, message);
		}
		
	}
	
}
