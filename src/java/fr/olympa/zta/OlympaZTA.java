package fr.olympa.zta;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import fr.olympa.api.CombatManager;
import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.command.essentials.BackCommand;
import fr.olympa.api.command.essentials.FeedCommand;
import fr.olympa.api.command.essentials.HealCommand;
import fr.olympa.api.command.essentials.KitCommand;
import fr.olympa.api.command.essentials.KitCommand.Kit;
import fr.olympa.api.command.essentials.tp.TpaHandler;
import fr.olympa.api.economy.MoneyCommand;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.economy.tax.TaxManager;
import fr.olympa.api.groups.OlympaGroup;
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
import fr.olympa.zta.enderchest.EnderChestManager;
import fr.olympa.zta.hub.HubCommand;
import fr.olympa.zta.hub.HubManager;
import fr.olympa.zta.hub.SpreadManageCommand;
import fr.olympa.zta.lootchests.LootChestsManager;
import fr.olympa.zta.lootchests.creators.FoodCreator.Food;
import fr.olympa.zta.mobs.MobSpawning;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;
import fr.olympa.zta.mobs.MobsCommand;
import fr.olympa.zta.mobs.MobsListener;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.plots.PlayerPlotsManager;
import fr.olympa.zta.plots.TomHookTrait;
import fr.olympa.zta.shops.CivilBlockShop;
import fr.olympa.zta.shops.CorporationBlockShop;
import fr.olympa.zta.shops.FoodBuyingShop;
import fr.olympa.zta.shops.FraterniteBlockShop;
import fr.olympa.zta.shops.QuestItemShop;
import fr.olympa.zta.utils.DynmapLink;
import fr.olympa.zta.utils.npcs.AuctionsTrait;
import fr.olympa.zta.utils.quests.BeautyQuestsLink;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.TrainingManager;
import fr.olympa.zta.weapons.WeaponsCommand;
import fr.olympa.zta.weapons.WeaponsGiveGUI;
import fr.olympa.zta.weapons.WeaponsListener;
import fr.olympa.zta.weapons.guns.Accessory;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunFlag;
import fr.olympa.zta.weapons.guns.GunRegistry;
import fr.olympa.zta.weapons.guns.GunType;
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
	
	public BeautyQuestsLink beautyQuestsLink;

	public MobsListener mobsListener;
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
	public GunRegistry gunRegistry;
	public EnderChestManager ecManager;
	public TrainingManager training;
	public CombatManager combat;
	
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

	private Map<Integer, Class<? extends Trait>> traitsToAdd = new HashMap<>();

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();
		
		Bukkit.clearRecipes();
		sendMessage("Recettes par défaut supprimées.");

		OlympaPermission.registerPermissions(ZTAPermissions.class);
		AccountProvider.setPlayerProvider(OlympaPlayerZTA.class, OlympaPlayerZTA::new, "zta", OlympaPlayerZTA.COLUMNS);

		try {
			if (getServer().getPluginManager().isPluginEnabled("dynmap")) DynmapLink.initialize();
			if (getServer().getPluginManager().isPluginEnabled("BeautyQuests")) beautyQuestsLink = new BeautyQuestsLink();
		}catch (Exception ex) {
			sendMessage("Une erreur est survenue durant le chargement d'un plugin externe.");
			ex.printStackTrace();
		}

		try {
			gunRegistry = new GunRegistry();
		}catch (Exception ex) {
			throw new RuntimeException("Registry failed to load", ex);
		}
		
		for (GunType gun : GunType.values()) WeaponsGiveGUI.stackables.add(gun);
		for (Knife knife : Knife.values()) WeaponsGiveGUI.stackables.add(knife);
		for (Accessory accessory : Accessory.values()) WeaponsGiveGUI.stackables.add(accessory);
		
		AmmoType.CARTRIDGE.getName();

		hub = new HubManager(getConfig().getSerializable("hub", Region.class), getConfig().getLocation("spawn"), getConfig().getList("spawnRegionTypes").stream().map(x -> SpawnType.valueOf((String) x)).collect(Collectors.toList()));
		teleportationManager = new TeleportationManager();
		
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(this, this);
		pluginManager.registerEvents(new WeaponsListener(), this);
		pluginManager.registerEvents(mobsListener = new MobsListener(config.getLocation("waitingRoom")), this);
		pluginManager.registerEvents(hub, this);
		pluginManager.registerEvents(teleportationManager, this);
		pluginManager.registerEvents(new TpaHandler(this, ZTAPermissions.TPA_COMMANDS), this);
		pluginManager.registerEvents(training = new TrainingManager(getConfig().getConfigurationSection("training")), this);
		pluginManager.registerEvents(combat = new CombatManager(this, 10), this);
		if (beautyQuestsLink != null) pluginManager.registerEvents(beautyQuestsLink, this);
		
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
			taxManager = new TaxManager(this, ZTAPermissions.TAX_MANAGE_COMMAND, "zta_tax", 0);
			auctionsManager = new AuctionsManager(this, "zta_auctions", taxManager) {
				public int getMaxAuctions(MoneyPlayerInterface player) {
					return player.getGroup().getPower() >= OlympaGroup.VIP.getPower() ? 20 : 10;
				};
			};
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
		
		try {
			ecManager = new EnderChestManager();
		}catch (SQLException ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors du chargement des coffres de l'end.");
		}

		new WeaponsCommand().register();
		new MobsCommand().register();
		new HubCommand().register();
		new SpreadManageCommand().register();
		new MoneyCommand<OlympaPlayerZTA>(this, "money", "Gérer son porte-monnaie.", ZTAPermissions.MONEY_COMMAND, ZTAPermissions.MONEY_COMMAND_OTHER, ZTAPermissions.MONEY_COMMAND_MANAGE, "monnaie").register();
		new HealCommand(this, ZTAPermissions.MOD_COMMANDS).register();
		new FeedCommand(this, ZTAPermissions.MOD_COMMANDS).register();
		new BackCommand(this, ZTAPermissions.MOD_COMMANDS).register();
		new KitCommand<OlympaPlayerZTA>(this,
				new Kit<>("VIP", ZTAPermissions.KIT_VIP_PERMISSION, TimeUnit.DAYS.toMillis(1), x -> x.kitVIPtime.get(), (x, time) -> x.kitVIPtime.set(time), (op, p) -> new ItemStack[] {
								GunType.M16.createItem(),
								Food.COOKED_RABBIT.get(15),
								ArmorType.ANTIRIOT.get(ArmorSlot.BOOTS),
								ArmorType.ANTIRIOT.get(ArmorSlot.LEGGINGS),
								ArmorType.ANTIRIOT.get(ArmorSlot.CHESTPLATE),
								ArmorType.ANTIRIOT.get(ArmorSlot.HELMET),
								AmmoType.HANDWORKED.getAmmo(10, true),
								AmmoType.LIGHT.getAmmo(10, true),
								AmmoType.HEAVY.getAmmo(10, true) })).register();
		new StatsCommand(this).register();
		
		new Mobs(); // initalise les mobs custom
		mobSpawning = new MobSpawning(getConfig().getInt("seaLevel"), getConfig().getConfigurationSection("mobRegions"), getConfig().getConfigurationSection("safeRegions"));
		mobSpawning.start();
		
		OlympaCore.getInstance().getRegionManager().awaitWorldTracking("world", e -> e.getRegion().registerFlags(
				new GunFlag(false, false),
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
		checkForTrait(QuestItemShop.class, "questitemshop", getConfig().getIntegerList("questItemShop"));
		checkForTrait(FoodBuyingShop.class, "foodshop", getConfig().getIntegerList("foodBuyingShop"));
	}

	public void checkForTrait(Class<? extends Trait> trait, String name, Iterable<Integer> npcs) {
		if (CitizensAPI.getTraitFactory().getTraitClass(name) == null) CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(trait).withName(name));
		if (npcs != null) npcs.forEach(x -> traitsToAdd.put(x, trait));
	}

	@EventHandler
	public void onCitizensEnable(CitizensEnableEvent e) {
		traitsToAdd.forEach((npcID, trait) -> {
			NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
			if (npc == null) {
				getLogger().warning("Le NPC " + npcID + " n'existe pas. (trait " + trait.getSimpleName() + ")");
			}else if (!npc.hasTrait(trait)) npc.addTrait(trait);
		});
		training.hashCode();
	}
	
	@EventHandler
	public void onBlockDrop(BlockDropItemEvent e) {
		for (Item item : e.getItems()) {
			ItemStack originalItem = item.getItemStack();
			Material type = originalItem.getType();
			Food food = null;
			if (type == Material.WHEAT) {
				food = Food.BREAD;
			}else if (type == Material.POTATO || type == Material.POISONOUS_POTATO) {
				BlockData data = e.getBlock().getBlockData();
				System.out.println(data.getClass().getName());
				if (data instanceof Ageable) {
					Ageable ageable = (Ageable) data;
					System.out.println(ageable.getAge() + " " + ageable.getMaximumAge());
					if ((ageable.getAge() == ageable.getMaximumAge()) && ThreadLocalRandom.current().nextBoolean()) {
						food = Food.BAKED_POTATO;
					}
				}
			}else if (type == Material.CARROT) {
				food = Food.CARROT;
			}
			if (food != null) item.setItemStack(food.get(originalItem.getAmount()));
		}
	}

	@Override
	public void onDisable(){
		super.onDisable();
		training.unload();

		HandlerList.unregisterAll((Plugin) this);
		mobSpawning.end();
		scoreboards.unload();
		combat.unload();
		
		gunRegistry.unload();
	}
	
}
