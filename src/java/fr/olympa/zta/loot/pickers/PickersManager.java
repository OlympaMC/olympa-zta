package fr.olympa.zta.loot.pickers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;

import fr.olympa.api.common.randomized.RandomizedFactory;
import fr.olympa.api.common.randomized.RandomizedPickerBase;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalContext;
import fr.olympa.api.common.randomized.RandomizedPickerBase.ConditionalPicker;
import fr.olympa.api.common.randomized.RandomizedPickerBase.RandomizedPicker;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.utils.Hierarchy;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.pickers.editor.PickersCommand;
import fr.olympa.zta.loot.pickers.serial.SerializableManager;

public class PickersManager {
	
	private final SQLColumn<ZTAPicker<?>> COLUMN_NAME = new SQLColumn<ZTAPicker<?>>("name", "VARCHAR(45) NOT NULL", Types.VARCHAR).setPrimaryKey(ZTAPicker::getName);
	private final SQLColumn<ZTAPicker<?>> COLUMN_DATA = new SQLColumn<ZTAPicker<?>>("data", "BLOB NOT NULL", Types.BLOB);
	private final SQLTable<ZTAPicker<?>> table;
	
	private Map<String, RandomizedPickerBase<?>> pickers = new HashMap<>();
	private Hierarchy<RandomizedPickerBase<?>> pickersHierarchy = new Hierarchy<>("");
	
	public final SerializableManager serializable = new SerializableManager();
	
	public PickersManager() throws SQLException {
		table = new SQLTable<>(OlympaZTA.getInstance().getServerNameID() + "_pickers", Arrays.asList(COLUMN_NAME, COLUMN_DATA));
		table.createOrAlter();
		
		RandomizedFactory.setDefaultFactory(new PickersFactoryZTA());
		
		new PickersCommand().register();
	}
	
	public <T, P extends RandomizedPicker<T>> P registerPicker(P picker, String name, Class<T> type) {
		if (picker == null) return null;
		if (!(picker instanceof ZTAPicker<?> ztaPicker)) {
			new IllegalArgumentException("Picker " + name + " invalide: class " + picker.getClass().getName()).printStackTrace();
			return picker;
		}
		registerPicker(ztaPicker, name);
		return picker;
	}
	
	public <T, C extends ConditionalContext<T>, P extends ConditionalPicker<T, C>> P registerPicker(P picker, String name, Class<T> objectType, Class<C> contextType) {
		if (picker == null) return null;
		if (!(picker instanceof ZTAPicker<?> ztaPicker)) {
			new IllegalArgumentException("Picker " + name + " invalide: class " + picker.getClass().getName()).printStackTrace();
			return picker;
		}
		registerPicker(ztaPicker, name);
		return picker;
	}
	
	private void registerPicker(ZTAPicker<?> picker, String name) {
		if (pickers.containsKey(name)) {
			new IllegalArgumentException("Nom de picker " + name + " déjà pris!").printStackTrace();
		}else {
			picker.setName(name);
			pickers.put(name, picker);
			
			Hierarchy<RandomizedPickerBase<?>> parent = pickersHierarchy;
			String nameTrimmed = name;
			int slashIndex;
			while ((slashIndex = nameTrimmed.indexOf('/')) != -1) {
				String key = nameTrimmed.substring(0, slashIndex);
				parent = parent.getOrCreateSubHierarchy(key);
				nameTrimmed = nameTrimmed.substring(slashIndex + 1);
			}
			parent.addObject(picker);
			OlympaZTA.getInstance().sendMessage("Enregistrement du picker %s", name);
			
			try {
				ResultSet resultSet = table.get(name);
				if (resultSet.next()) {
					picker.load(new String(resultSet.getBytes("data")));
				}else {
					// do saving only when editing
					table.insertAsync(null, null, name, new SerialBlob(picker.serialize().getBytes()));
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Map<String, RandomizedPickerBase<?>> getPickers() {
		return pickers;
	}
	
	public Hierarchy<RandomizedPickerBase<?>> getPickersHierarchy() {
		return pickersHierarchy;
	}
	
}
