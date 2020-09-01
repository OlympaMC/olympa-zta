package fr.olympa.zta;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.command.essentials.BackCommand;
import fr.olympa.api.command.essentials.FeedCommand;
import fr.olympa.api.command.essentials.HealCommand;
import fr.olympa.api.command.essentials.tp.TpaHandler;
import fr.olympa.api.economy.MoneyCommand;
import fr.olympa.api.economy.tax.TaxManager;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.lines.CyclingLine;
import fr.olympa.api.lines.DynamicLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.PlayerObservableLine;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.region.Region;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.region.tracking.flags.FishFlag;
import fr.olympa.api.region.tracking.flags.GameModeFlag;
import fr.olympa.api.region.tracking.flags.ItemDurabilityFlag;
import fr.olympa.api.region.tracking.flags.PhysicsFlag;
import fr.olympa.api.region.tracking.flags.PlayerBlockInteractFlag;
import fr.olympa.api.region.tracking.flags.PlayerBlocksFlag;
import fr.olympa.api.scoreboard.sign.Scoreboard;
import fr.olympa.api.scoreboard.sign.ScoreboardManager;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.bank.BankTrait;
import fr.olympa.zta.clans.ClansManagerZTA;
import fr.olympa.zta.clans.plots.ClanPlotsManager;
import fr.olympa.zta.enderchest.EnderChestCommand;
import fr.olympa.zta.hub.HubCommand;
import fr.olympa.zta.hub.HubManager;
import fr.olympa.zta.hub.SpreadManageCommand;
import fr.olympa.zta.lootchests.LootChestsManager;
import fr.olympa.zta.mobs.MobSpawning;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;
import fr.olympa.zta.mobs.MobsCommand;
import fr.olympa.zta.mobs.MobsListener;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.plots.PlayerPlotsManager;
import fr.olympa.zta.plots.TomHookTrait;
import fr.olympa.zta.plots.shops.CivilBlockShop;
import fr.olympa.zta.plots.shops.CorporationBlockShop;
import fr.olympa.zta.plots.shops.FraterniteBlockShop;
import fr.olympa.zta.registry.ItemStackableInstantiator;
import fr.olympa.zta.registry.ItemsListener;
import fr.olympa.zta.registry.RegistryCommand;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.registry.ZTARegistry.DeserializeDatas;
import fr.olympa.zta.utils.BeautyQuestsLink;
import fr.olympa.zta.utils.DynmapLink;
import fr.olympa.zta.utils.npcs.AuctionsTrait;
import fr.olympa.zta.weapons.WeaponsCommand;
import fr.olympa.zta.weapons.WeaponsListener;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.Gun;
import fr.olympa.zta.weapons.guns.accessories.CannonCaC;
import fr.olympa.zta.weapons.guns.accessories.CannonDamage;
import fr.olympa.zta.weapons.guns.accessories.CannonPower;
import fr.olympa.zta.weapons.guns.accessories.CannonSilent;
import fr.olympa.zta.weapons.guns.accessories.CannonStabilizer;
import fr.olympa.zta.weapons.guns.accessories.ScopeLight;
import fr.olympa.zta.weapons.guns.accessories.ScopeStrong;
import fr.olympa.zta.weapons.guns.accessories.StockLight;
import fr.olympa.zta.weapons.guns.accessories.StockStrong;
import fr.olympa.zta.weapons.guns.created.Gun870;
import fr.olympa.zta.weapons.guns.created.GunAK;
import fr.olympa.zta.weapons.guns.created.GunBarrett;
import fr.olympa.zta.weapons.guns.created.GunBenelli;
import fr.olympa.zta.weapons.guns.created.GunCobra;
import fr.olympa.zta.weapons.guns.created.GunDragunov;
import fr.olympa.zta.weapons.guns.created.GunG19;
import fr.olympa.zta.weapons.guns.created.GunKSG;
import fr.olympa.zta.weapons.guns.created.GunLupara;
import fr.olympa.zta.weapons.guns.created.GunM16;
import fr.olympa.zta.weapons.guns.created.GunM1897;
import fr.olympa.zta.weapons.guns.created.GunM1911;
import fr.olympa.zta.weapons.guns.created.GunP22;
import fr.olympa.zta.weapons.guns.created.GunSDMR;
import fr.olympa.zta.weapons.guns.created.GunSkorpion;
import fr.olympa.zta.weapons.guns.created.GunStoner;
import fr.olympa.zta.weapons.guns.created.GunUZI;
import fr.olympa.zta.weapons.knives.KnifeBatte;
import fr.olympa.zta.weapons.knives.KnifeBiche;
import fr.olympa.zta.weapons.knives.KnifeSurin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;

public class OlympaZTA extends OlympaAPIPlugin implements Listener {

	private static OlympaZTA instance;

	public static OlympaZTA getInstance() {
		return (OlympaZTA) instance;
	}
	
	private WeaponsListener weaponListener = new WeaponsListener();
	private MobsListener mobsListener = new MobsListener();
	private ItemsListener itemsListener = new ItemsListener();

	public TeleportationManager teleportationManager;
	public PlayerPlotsManager plotsManager;
	public ClanPlotsManager clanPlotsManager;
	public LootChestsManager lootChestsManager;
	public MobSpawning mobSpawning;
	public ScoreboardManager<OlympaPlayerZTA> scoreboards;
	public HubManager hub;
	public ClansManagerZTA clansManager;
	public TaxManager taxManager;
	public AuctionsManager auctionsManager;
	
	public DynamicLine<Scoreboard<OlympaPlayerZTA>> lineRadar = new DynamicLine<>(x -> {
		Set<TrackedRegion> regions = OlympaCore.getInstance().getRegionManager().getCachedPlayerRegions(x.getOlympaPlayer().getPlayer());
		String title = "§c§kdddddddd";
		for (TrackedRegion region : regions) {
			SpawningFlag flag = region.getFlag(SpawningFlag.class);
			if (flag != null) {
				title = flag.type.title;
				break;
			}
		}
		return "§7Radar: " + title;
	});
	public DynamicLine<Scoreboard<OlympaPlayerZTA>> lineMoney = new DynamicLine<>(x -> "§7Monnaie: §6" + x.getOlympaPlayer().getGameMoney().getFormatted());
	public PlayerObservableLine<Scoreboard<OlympaPlayerZTA>> lineDeaths = new PlayerObservableLine<>(x -> "§7Morts: §6" + x.getOlympaPlayer().deaths.get(), (x) -> x.getOlympaPlayer().deaths);
	//public DynamicLine<Scoreboard<OlympaPlayerZTA>> lineGroup = new DynamicLine<>(x -> "§7Rang: §b" + x.getOlympaPlayer().getGroupNameColored());

	private Map<Integer, Class<? extends Trait>> traitsToAdd = new HashMap<>();

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();

		OlympaPermission.registerPermissions(ZTAPermissions.class);
		AccountProvider.setPlayerProvider(OlympaPlayerZTA.class, OlympaPlayerZTA::new, "zta", OlympaPlayerZTA.COLUMNS);

		if (getServer().getPluginManager().isPluginEnabled("dynmap")) DynmapLink.initialize();
		if (getServer().getPluginManager().isPluginEnabled("BeautyQuests")) BeautyQuestsLink.initialize();

		Arrays.asList(GunM1911.class, GunCobra.class, Gun870.class, GunUZI.class, GunM16.class, GunM1897.class, GunG19.class, GunSkorpion.class, GunAK.class, GunBenelli.class, GunDragunov.class, GunLupara.class, GunP22.class, GunSDMR.class, GunStoner.class, GunBarrett.class, GunKSG.class).forEach(x -> ZTARegistry.registerItemStackableType(new ItemStackableInstantiator<>(x), Gun.TABLE_NAME, Gun.CREATE_TABLE_STATEMENT, Gun::deserializeGun));
		Arrays.asList(KnifeBatte.class, KnifeBiche.class, KnifeSurin.class, CannonCaC.class, CannonDamage.class, CannonPower.class, CannonSilent.class, CannonStabilizer.class, ScopeLight.class, ScopeStrong.class, StockLight.class, StockStrong.class).forEach(x -> ZTARegistry.registerItemStackableType(new ItemStackableInstantiator<>(x), null, null, DeserializeDatas.easyClass()));

		Bukkit.clearRecipes();
		AmmoType.CARTRIDGE.getName();

		hub = new HubManager(getConfig().getSerializable("hub", Region.class), getConfig().getLocation("spawn"), getConfig().getList("spawnRegionTypes").stream().map(x -> SpawnType.valueOf((String) x)).collect(Collectors.toList()));
		teleportationManager = new TeleportationManager();
		
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(this, this);
		pluginManager.registerEvents(weaponListener, this);
		pluginManager.registerEvents(mobsListener, this);
		pluginManager.registerEvents(itemsListener, this);
		pluginManager.registerEvents(hub, this);
		pluginManager.registerEvents(teleportationManager, this);
		pluginManager.registerEvents(new TpaHandler(this, ZTAPermissions.TPA_COMMANDS), this);
		
		try {
			pluginManager.registerEvents(clansManager = new ClansManagerZTA(), this);
			pluginManager.registerEvents(clanPlotsManager = new ClanPlotsManager(clansManager), this);
		}catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors de l'initialisation du système de clans et parcelles de clans.");
		}

		try {
			String schemName = getConfig().getString("firstBuildSchem");
			File file = new File(getDataFolder(), schemName);
			if (!file.exists()) {
				Files.copy(getResource(schemName), file.toPath());
			}
			plotsManager = new PlayerPlotsManager(file);
			checkForTrait(TomHookTrait.class, "plots", getConfig().getIntegerList("tomHookNPC"));
		}catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors de l'initialisation du système de plots joueurs.");
		}
		
		try {
			taxManager = new TaxManager(this, null, "zta_tax", 0);
			auctionsManager = new AuctionsManager(this, "zta_auctions", taxManager);
		}catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors du chargement de la taxe et des ventes.");
		}

		try {
			pluginManager.registerEvents(lootChestsManager = new LootChestsManager(), this);
		}catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors du chargement des coffres de loot.");
		}

		new WeaponsCommand().register();
		new MobsCommand().register();
		new EnderChestCommand().register();
		new HubCommand().register();
		new RegistryCommand().register();
		new SpreadManageCommand().register();
		new MoneyCommand<OlympaPlayerZTA>(this, "money", "Gérer son porte-monnaie.", ZTAPermissions.MONEY_COMMAND, ZTAPermissions.MONEY_COMMAND_OTHER, ZTAPermissions.MONEY_COMMAND_MANAGE, "monnaie").register();
		new HealCommand(this, ZTAPermissions.MOD_COMMANDS).register();
		new FeedCommand(this, ZTAPermissions.MOD_COMMANDS).register();
		new BackCommand(this, ZTAPermissions.MOD_COMMANDS).register();
		new StatsCommand(this).registerPreProcess();
		
		new Mobs(); // initalise les mobs custom
		mobSpawning = new MobSpawning(getConfig().getInt("seaLevel"), getConfig().getConfigurationSection("mobRegions"), getConfig().getConfigurationSection("safeRegions"));
		mobSpawning.start();
		
		OlympaCore.getInstance().getRegionManager().awaitWorldTracking("world", e -> e.getRegion().registerFlags(
				new ItemDurabilityFlag(true),
				new PhysicsFlag(true),
				new PlayerBlocksFlag(true),
				new FishFlag(true),
				new GameModeFlag(GameMode.ADVENTURE),
				new PlayerBlockInteractFlag(false, true, true)));
		
		scoreboards = new ScoreboardManager<OlympaPlayerZTA>(this, "§6Olympa §e§lZTA").addLines(
				FixedLine.EMPTY_LINE,
				lineMoney,
				lineDeaths,
				FixedLine.EMPTY_LINE,
				lineRadar)
				.addFooters(
				FixedLine.EMPTY_LINE,
				CyclingLine.olympaAnimation());

		checkForTrait(BankTrait.class, "bank", getConfig().getIntegerList("bank"));
		checkForTrait(AuctionsTrait.class, "auctions", getConfig().getIntegerList("auctions"));
		checkForTrait(CivilBlockShop.class, "blockshopcivil", getConfig().getIntegerList("blockShopCivil"));
		checkForTrait(FraterniteBlockShop.class, "blockshopfraternite", getConfig().getIntegerList("blockShopFraternite"));
		checkForTrait(CorporationBlockShop.class, "blockshopcorporation", getConfig().getIntegerList("blockShopCorporation"));

		try {
			sendMessage(ZTARegistry.loadFromDatabase() + " objets chargés dans le registre.");
		}catch (SQLException e) {
			e.printStackTrace();
		}

		IProtocolSupport protocolSupport = OlympaCore.getInstance().getProtocolSupport();
		if (protocolSupport != null) {
			protocolSupport.disable1_8();
		}
	}

	public void checkForTrait(Class<? extends Trait> trait, String name, Iterable<Integer> npcs) {
		if (CitizensAPI.getTraitFactory().getTraitClass(name) == null) CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(trait).withName(name));
		if (npcs != null) npcs.forEach(x -> traitsToAdd.put(x, trait));
	}

	@EventHandler
	public void onCitizensEnable(CitizensEnableEvent e) {
		traitsToAdd.forEach((npcID, trait) -> {
			NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
			if (!npc.hasTrait(trait)) npc.addTrait(trait);
		});
	}

	/*@EventHandler
	public void onPlayerGroupChange(AsyncOlympaPlayerChangeGroupEvent e) {
		lineGroup.updateHolder(scoreboards.getPlayerScoreboard((OlympaPlayerZTA) e.getOlympaPlayer()));
	}*/

	@Override
	public void onDisable(){
		super.onDisable();

		HandlerList.unregisterAll((Plugin) this);
		mobSpawning.end();
		scoreboards.unload();

		ZTARegistry.saveDatabase();
	}
	
}
