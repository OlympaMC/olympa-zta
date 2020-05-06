package fr.olympa.zta.clans;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClansCommand;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.permission.OlympaPermission;

public class ZTAClansCommand<T extends Clan<ClanZTA>> extends ClansCommand<ClanZTA> {

	public ZTAClansCommand(ClansManager<ClanZTA> manager, String name, String description, OlympaPermission permission, String... aliases) {
		super(manager, name, description, permission, aliases);
	}

}
