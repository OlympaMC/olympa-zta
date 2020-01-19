package fr.olympa.zta.registry;

import java.sql.SQLException;

/**
 * Représente un objet qui peut être inséré dans le registre
 */
public interface Registrable{
	
	public abstract int getID();

	public default void createDatas() throws SQLException {}

	public default void updateDatas() throws SQLException {}

}
