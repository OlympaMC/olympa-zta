package fr.olympa.zta;

import fr.olympa.api.objects.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public class ZTAPermissions{
	
	public static final OlympaPermission LOOT_CHEST_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission WEAPONS_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission MOBS_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	
}
