package fr.olympa.zta.loot.pickers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.olympa.api.common.randomized.RandomizedFactory;
import fr.olympa.api.common.randomized.RandomizedPickerBase;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.loot.pickers.serial.SerializableManager;

public class PickersManager {
	
	private final SQLColumn<ZTAPicker<?>> COLUMN_NAME = new SQLColumn<ZTAPicker<?>>("name", "VARCHAR(45) NOT NULL", Types.VARCHAR).setPrimaryKey(ZTAPicker::getName);
	private final SQLColumn<ZTAPicker<?>> COLUMN_DATA = new SQLColumn<ZTAPicker<?>>("data", "BLOB NOT NULL", Types.BLOB);
	private final SQLTable<ZTAPicker<?>> table;
	
	private Map<String, RandomizedPickerBase<?>> pickers = new HashMap<>();
	
	public final SerializableManager serializable = new SerializableManager();
	
	public PickersManager() throws SQLException {
		table = new SQLTable<>(OlympaZTA.getInstance().getServerNameID() + "_pickers", Arrays.asList(COLUMN_NAME, COLUMN_DATA));
		table.createOrAlter();
		
		RandomizedFactory.setDefaultFactory(new PickersFactoryZTA());
	}
	
	public <T extends RandomizedPickerBase<T>> T registerPicker(T picker, String name) {
		if (!(picker instanceof ZTAPicker<?> ztaPicker)) {
			new IllegalArgumentException("Picker " + name + " invalide: class " + picker.getClass().getName()).printStackTrace();
			return picker;
		}
		if (pickers.containsKey(name)) {
			new IllegalArgumentException("Nom de picker " + name + " déjà pris!").printStackTrace();
		}else {
			ztaPicker.setName(name);
			pickers.put(name, picker);
			OlympaZTA.getInstance().sendMessage("Enregistrement du picker %s", name);
			
			try {
				ResultSet resultSet = table.get(name);
				if (resultSet.next()) {
					ztaPicker.load(resultSet.getString("data"));
				}else {
					// do saving only when editing
					table.insertAsync(null, null, name, ztaPicker.save());
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return picker;
	}
	
}
