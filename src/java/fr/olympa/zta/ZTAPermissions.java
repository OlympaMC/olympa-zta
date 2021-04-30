package fr.olympa.zta;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaSpigotPermission;

public class ZTAPermissions{
	
	public static final OlympaSpigotPermission TAX_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission LOOT_CHEST_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission WEAPONS_COMMAND = new OlympaSpigotPermission(OlympaGroup.BUILDER); // TODO change
	public static final OlympaSpigotPermission MOBS_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission UTILS_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission PLAYER_SPREAD_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission PLOTS_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission CLAN_PLOTS_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission MINIGUNS_COMMAND = new OlympaSpigotPermission(OlympaGroup.BUILDER);
	public static final OlympaSpigotPermission PROBLEM_MONITORING = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission BYPASS_TELEPORT_WAIT_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission MOD_COMMANDS = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission CRATES_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_ANIMATION);
	
	public static final OlympaSpigotPermission TPA_COMMANDS = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission ENDERCHEST_COMMAND = new OlympaSpigotPermission(OlympaGroup.VIP);
	public static final OlympaSpigotPermission ENDERCHEST_COMMAND_OTHER = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission ENDERCHEST_MORE_SPACE = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.VIP });
	
	public static final OlympaSpigotPermission CLANS_PLAYERS_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	
	public static final OlympaSpigotPermission MONEY_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission MONEY_COMMAND_OTHER = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission MONEY_COMMAND_MANAGE = new OlympaSpigotPermission(OlympaGroup.RESP);
	
	public static final OlympaSpigotPermission BACK_COMMAND = new OlympaSpigotPermission(OlympaGroup.VIP);
	public static final OlympaSpigotPermission BACK_COMMAND_INFINITE = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	
	public static final OlympaSpigotPermission HUB_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	
	public static final OlympaSpigotPermission KIT_VIP_PERMISSION = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.VIP });
	
	public static final OlympaSpigotPermission CLAN_MORE_SPACE_PERMISSION = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.VIP, OlympaGroup.YOUTUBER });

}
