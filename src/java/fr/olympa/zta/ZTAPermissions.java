package fr.olympa.zta;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaSpigotPermission;

public class ZTAPermissions{

	public static final OlympaSpigotPermission GROUP_SURVIVANT = new OlympaSpigotPermission(OlympaGroup.RESP,
			new OlympaGroup[] { OlympaGroup.ZTA_SURVIVANT, OlympaGroup.ZTA_RODEUR, OlympaGroup.ZTA_SAUVEUR, OlympaGroup.ZTA_HEROS, OlympaGroup.ZTA_LEGENDE });
	public static final OlympaSpigotPermission GROUP_RODEUR = new OlympaSpigotPermission(OlympaGroup.RESP, new OlympaGroup[] { OlympaGroup.ZTA_RODEUR, OlympaGroup.ZTA_SAUVEUR, OlympaGroup.ZTA_HEROS, OlympaGroup.ZTA_LEGENDE });
	public static final OlympaSpigotPermission GROUP_SAUVEUR = new OlympaSpigotPermission(OlympaGroup.RESP, new OlympaGroup[] { OlympaGroup.ZTA_SAUVEUR, OlympaGroup.ZTA_HEROS, OlympaGroup.ZTA_LEGENDE });
	public static final OlympaSpigotPermission GROUP_HEROS = new OlympaSpigotPermission(OlympaGroup.RESP, new OlympaGroup[] { OlympaGroup.ZTA_HEROS, OlympaGroup.ZTA_LEGENDE });
	public static final OlympaSpigotPermission GROUP_LEGENDE = new OlympaSpigotPermission(OlympaGroup.RESP, new OlympaGroup[] { OlympaGroup.ZTA_LEGENDE });

	public static final OlympaSpigotPermission TAX_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission LOOT_CHEST_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission WEAPONS_COMMAND = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission WEAPONS_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_BUILDER);
	public static final OlympaSpigotPermission MOBS_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission UTILS_COMMAND = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission PLAYER_SPREAD_COMMAND = new OlympaSpigotPermission(OlympaGroup.MOD);
	public static final OlympaSpigotPermission PLOTS_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.MOD);
	public static final OlympaSpigotPermission GLASS_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission PARACHUTE_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission CLAN_PLOTS_COMMAND = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission CLAN_PLOTS_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_BUILDER, new OlympaGroup[] { OlympaGroup.BUILDER });
	public static final OlympaSpigotPermission MINIGUNS_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission PROBLEM_MONITORING = new OlympaSpigotPermission(OlympaGroup.DEV);
	public static final OlympaSpigotPermission BYPASS_TELEPORT_WAIT_COMMAND = new OlympaSpigotPermission(OlympaGroup.MOD);
	public static final OlympaSpigotPermission MOD_COMMANDS = new OlympaSpigotPermission(OlympaGroup.MODP);
	public static final OlympaSpigotPermission CRATES_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_ANIMATION);
	public static final OlympaSpigotPermission PACKS_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission TPA_COMMANDS = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission ENDERCHEST_COMMAND_OTHER = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);

	public static final OlympaSpigotPermission CLANS_PLAYERS_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission CLANS_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.MOD);

	public static final OlympaSpigotPermission ECONOMIES_MANAGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission MONEY_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission MONEY_COMMAND_OTHER = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission MONEY_COMMAND_MANAGE = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission BACK_COMMAND_INFINITE = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission HUB_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);

}
