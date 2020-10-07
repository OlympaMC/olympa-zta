package fr.olympa.zta.registry;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.zta.OlympaZTA;
import fr.olympa.zta.ZTAPermissions;

public class RegistryCommand extends ComplexCommand {

	public RegistryCommand() {
		super(OlympaZTA.getInstance(), "registry", "Gestion du registre", ZTAPermissions.REGISTRY_COMMAND);
	}

	@Cmd (syntax = "<id>", args = "INTEGER")
	public void info(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			ZTARegistry registry = ZTARegistry.get();
			sendInfo("Objets actuellement chargés dans le registre : §l" + registry.registry.size());
			sendInfo("Types d'objets disponible : §l" + registry.registrable.size());
		}else {
			Registrable obj = getObject(cmd.getArgument(0));
			if (obj == null) return;
			sendInfo("Identifiant de l'objet : §l" + obj.getID());
			sendInfo("Type d'objet : §l" + obj.getClass().getSimpleName());
			sendInfo("Assignable à un item : §l" + (obj instanceof ItemStackable ? "§aoui" : "§cnon"));
		}
	}

	@Cmd (player = true, min = 1, syntax = "<id>", args = "INTEGER")
	public void getItem(CommandContext cmd) {
		Registrable obj = getObject(cmd.getArgument(0));
		if (obj == null) return;
		if (obj instanceof ItemStackable) {
			ItemStackable stackable = (ItemStackable) obj;
			getPlayer().getInventory().addItem(stackable.createItemStack());
			sendSuccess("Vous venez de recevoir une copie de l'objet " + obj.getID() + ". §c§lAttention ! Il est probable que ce même objet soit employé ailleurs dans le jeu, ce qui peut mener à des comportements simultanés et imprévisibles.");
		}else {
			sendError("L'objet de type " + obj.getClass().getSimpleName() + " ne peut pas être transformé en item.");
		}
	}

	@Cmd (min = 1, syntax = "<id>", args = "INTEGER")
	public void remove(CommandContext cmd) {
		Registrable obj = getObject(cmd.getArgument(0));
		if (obj == null) return;
		ZTARegistry.get().removeObject(obj);
		sendSuccess("L'objet a été correctement supprimé du registre.");
	}

	private Registrable getObject(int id) {
		Registrable obj = ZTARegistry.get().registry.get(id);
		if (obj == null) sendError("§cL'objet avec l'ID " + id + " est introuvable dans le registre.");
		return obj;
	}

}
