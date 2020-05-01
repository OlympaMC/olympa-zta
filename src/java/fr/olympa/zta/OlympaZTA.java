package fr.olympa.zta;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;

import fr.olympa.api.hook.ProtocolAction;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.region.Region;
import fr.olympa.api.scoreboard.DynamicLine;
import fr.olympa.api.scoreboard.FixedLine;
import fr.olympa.api.scoreboard.ScoreboardManager;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.bank.BankTrait;
import fr.olympa.zta.clans.ClansManagerZTA;
import fr.olympa.zta.enderchest.EnderChestCommand;
import fr.olympa.zta.hub.HubCommand;
import fr.olympa.zta.hub.HubManager;
import fr.olympa.zta.hub.SpreadManageCommand;
import fr.olympa.zta.lootchests.LootChest;
import fr.olympa.zta.lootchests.LootChestCommand;
import fr.olympa.zta.lootchests.LootChestsListener;
import fr.olympa.zta.mobs.MobSpawning;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.mobs.Mobs;
import fr.olympa.zta.mobs.MobsCommand;
import fr.olympa.zta.mobs.MobsListener;
import fr.olympa.zta.plots.players.PlayerPlotsManager;
import fr.olympa.zta.plots.players.TomHookTrait;
import fr.olympa.zta.registry.ItemStackableInstantiator;
import fr.olympa.zta.registry.ItemsListener;
import fr.olympa.zta.registry.RegistryCommand;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.registry.ZTARegistry.DeserializeDatas;
import fr.olympa.zta.utils.DynmapLink;
import fr.olympa.zta.weapons.WeaponsCommand;
import fr.olympa.zta.weapons.WeaponsListener;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.Gun870;
import fr.olympa.zta.weapons.guns.GunAK;
import fr.olympa.zta.weapons.guns.GunBarrett;
import fr.olympa.zta.weapons.guns.GunBenelli;
import fr.olympa.zta.weapons.guns.GunCobra;
import fr.olympa.zta.weapons.guns.GunDragunov;
import fr.olympa.zta.weapons.guns.GunG19;
import fr.olympa.zta.weapons.guns.GunKSG;
import fr.olympa.zta.weapons.guns.GunLupara;
import fr.olympa.zta.weapons.guns.GunM16;
import fr.olympa.zta.weapons.guns.GunM1897;
import fr.olympa.zta.weapons.guns.GunM1911;
import fr.olympa.zta.weapons.guns.GunP22;
import fr.olympa.zta.weapons.guns.GunSDMR;
import fr.olympa.zta.weapons.guns.GunSkorpion;
import fr.olympa.zta.weapons.guns.GunStoner;
import fr.olympa.zta.weapons.guns.GunUZI;
import fr.olympa.zta.weapons.guns.accessories.CannonDamage;
import fr.olympa.zta.weapons.guns.accessories.CannonPower;
import fr.olympa.zta.weapons.guns.accessories.CannonSilent;
import fr.olympa.zta.weapons.guns.accessories.CannonStabilizer;
import fr.olympa.zta.weapons.guns.accessories.ScopeLight;
import fr.olympa.zta.weapons.guns.accessories.ScopeStrong;
import fr.olympa.zta.weapons.guns.accessories.StockLight;
import fr.olympa.zta.weapons.guns.accessories.StockStrong;
import fr.olympa.zta.weapons.knives.KnifeBatte;
import fr.olympa.zta.weapons.knives.KnifeBiche;
import fr.olympa.zta.weapons.knives.KnifeSurin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class OlympaZTA extends OlympaAPIPlugin {

	private static OlympaZTA instance;

	public static OlympaZTA getInstance() {
		return (OlympaZTA) instance;
	}
	
	private WeaponsListener weaponListener = new WeaponsListener();
	private LootChestsListener chestsListener = new LootChestsListener();
	private MobsListener mobsListener = new MobsListener();
	private ItemsListener itemsListener = new ItemsListener();

	public ClansManagerZTA clansManager;
	public PlayerPlotsManager plotsManager;
	public MobSpawning mobSpawning;
	public ScoreboardManager scoreboards;
	public HubManager hub;
	
	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();

		OlympaPermission.registerPermissions(ZTAPermissions.class);
		AccountProvider.setPlayerProvider(OlympaPlayerZTA.class, OlympaPlayerZTA::new, "zta", OlympaPlayerZTA.COLUMNS);

		DynmapLink.initialize();

		hub = new HubManager(getConfig().getSerializable("hub", Region.class), getConfig().getLocation("spawn"), getConfig().getList("spawnRegionTypes").stream().map(x -> SpawnType.valueOf((String) x)).collect(Collectors.toList()));
		
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(weaponListener, this);
		pluginManager.registerEvents(chestsListener, this);
		pluginManager.registerEvents(mobsListener, this);
		pluginManager.registerEvents(itemsListener, this);
		pluginManager.registerEvents(hub, this);

		try {
			pluginManager.registerEvents(clansManager = new ClansManagerZTA(), this);
		}catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors de l'initialisation du système de clans.");
		}

		try {
			pluginManager.registerEvents(plotsManager = new PlayerPlotsManager(), this);
			CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TomHookTrait.class).withName("plots"));
		}catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors de l'initialisation du système de plots.");
		}

		new LootChestCommand().register();
		new WeaponsCommand().register();
		new MobsCommand().register();
		new EnderChestCommand().register();
		new MoneyCommand().register();
		new HubCommand().register();
		new RegistryCommand().register();
		new SpreadManageCommand().register();

		Arrays.asList(
				GunM1911.class, GunCobra.class, Gun870.class, GunUZI.class, GunM16.class, GunM1897.class, GunG19.class, GunSkorpion.class, GunAK.class, GunBenelli.class, GunDragunov.class, GunLupara.class, GunP22.class, GunSDMR.class, GunStoner.class, GunBarrett.class, GunKSG.class)
				.forEach(x -> ZTARegistry.registerItemStackableType(new ItemStackableInstantiator<>(x), Gun.TABLE_NAME, Gun.CREATE_TABLE_STATEMENT, Gun::deserializeGun));
		Arrays.asList(
				KnifeBatte.class, KnifeBiche.class, KnifeSurin.class,
				CannonDamage.class, CannonPower.class, CannonSilent.class, CannonStabilizer.class, ScopeLight.class, ScopeStrong.class, StockLight.class, StockStrong.class)
				.forEach(x -> ZTARegistry.registerItemStackableType(new ItemStackableInstantiator<>(x), null, null, DeserializeDatas.easyClass()));
		ZTARegistry.registerObjectType(LootChest.class, LootChest.TABLE_NAME, LootChest.CREATE_TABLE_STATEMENT, LootChest::deserializeLootChest);

		Bukkit.clearRecipes();
		AmmoType.CARTRIDGE.getName();

		new Mobs(); // initalise les mobs custom
		mobSpawning = new MobSpawning(getConfig().getConfigurationSection("mobRegions"));
		mobSpawning.start();
		
		scoreboards = new ScoreboardManager(this, "§6Olympa §e§lZTA", Arrays.asList(
				FixedLine.EMPTY_LINE,
				new DynamicLine<OlympaPlayerZTA>(x -> "§eRang : §6" + x.getGroupNameColored()),
				FixedLine.EMPTY_LINE,
				new DynamicLine<OlympaPlayerZTA>(x -> "§eNombre de mobs : §6" + mobSpawning.world.getLivingEntities().size(), 1, 0),
				FixedLine.EMPTY_LINE,
				new DynamicLine<OlympaPlayerZTA>(x -> "§eMonnaie : §6" + x.getGameMoney().getFormatted(), 1, 0)));

		for (Player p : getServer().getOnlinePlayers()) {
			mobsListener.onJoin(new PlayerJoinEvent(p.getPlayer(), "random join message"));
		}

		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BankTrait.class).withName("bank"));

		try {
			sendMessage(ZTARegistry.loadFromDatabase() + " objets chargés dans le registre.");
		}catch (SQLException e) {
			e.printStackTrace();
		}
		ProtocolAction protocolSupport = OlympaCore.getInstance().getProtocolSupport();
		if (protocolSupport != null) {
			protocolSupport.disable1_8();
		}
	}

	@Override
	public void onDisable(){
		HandlerList.unregisterAll(this);
		mobSpawning.end();
		scoreboards.unload();

		ZTARegistry.saveDatabase();
	}
	
}
