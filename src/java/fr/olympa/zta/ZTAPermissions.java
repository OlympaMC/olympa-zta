package fr.olympa.zta;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public class ZTAPermissions{
	
	public static final OlympaPermission LOOT_CHEST_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission WEAPONS_COMMAND = new OlympaPermission(OlympaGroup.BUILDER); // TODO change
	public static final OlympaPermission MOBS_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission UTILS_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission REGISTRY_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission PLAYER_SPREAD_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	public static final OlympaPermission PLOTS_MANAGE_COMMAND = new OlympaPermission(OlympaGroup.DEV);
	
	public static final OlympaPermission ENDERCHEST_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission CLANS_PLAYERS_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission MONEY_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission MONEY_COMMAND_OTHER = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission MONEY_COMMAND_MANAGE = new OlympaPermission(OlympaGroup.DEV);
	
	public static final OlympaPermission HUB_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);

	public static final OlympaPermission PROBLEM_MONITORING = new OlympaPermission(OlympaGroup.DEV);

}
