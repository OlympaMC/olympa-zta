package fr.olympa.zta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcmonkey.sentinel.SentinelPlugin;

import fr.olympa.api.CombatManager;
import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.command.essentials.BackCommand;
import fr.olympa.api.command.essentials.FeedCommand;
import fr.olympa.api.command.essentials.HealCommand;
import fr.olympa.api.command.essentials.KitCommand;
import fr.olympa.api.command.essentials.KitCommand.SimpleKit;
import fr.olympa.api.command.essentials.tp.TpaHandler;
import fr.olympa.api.economy.MoneyCommand;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.economy.tax.TaxManager;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.holograms.HologramCycler;
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
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.trades.TradesManager;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.api.utils.spigot.TeleportationManager;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.bank.BankTrait;
import fr.olympa.zta.clans.ClansManagerZTA;
import fr.olympa.zta.clans.plots.ClanPlotsManager;
import fr.olympa.zta.enderchest.EnderChestManager;
import fr.olympa.zta.hub.HubCommand;
import fr.olympa.zta.hub.HubManager;
import fr.olympa.zta.hub.SpreadManageCommand;
import fr.olympa.zta.loot.chests.LootChestsManager;
import fr.olympa.zta.loot.crates.CratesManager;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.creators.QuestItemCreator.QuestItem;
import fr.olympa.zta.loot.packs.PackBlock;
import fr.olympa.zta.mobs.MobSpawning;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;
import fr.olympa.zta.mobs.MobsCommand;
import fr.olympa.zta.mobs.MobsListener;
import fr.olympa.zta.mobs.PlayersListener;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.plots.PlayerPlotsManager;
import fr.olympa.zta.plots.TomHookTrait;
import fr.olympa.zta.ranks.ClanMoneyRanking;
import fr.olympa.zta.ranks.KillPlayerRanking;
import fr.olympa.zta.ranks.KillZombieRanking;
import fr.olympa.zta.ranks.LootChestRanking;
import fr.olympa.zta.ranks.MoneyRanking;
import fr.olympa.zta.shops.CivilBlockShop;
import fr.olympa.zta.shops.CorporationBlockShop;
import fr.olympa.zta.shops.FoodBuyingShop;
import fr.olympa.zta.shops.FraterniteBlockShop;
import fr.olympa.zta.shops.QuestItemShop;
import fr.olympa.zta.utils.DynmapLink;
import fr.olympa.zta.utils.npcs.AuctionsTrait;
import fr.olympa.zta.utils.npcs.SentinelZTA;
import fr.olympa.zta.utils.quests.BeautyQuestsLink;
import fr.olympa.zta.weapons.ArmorType;
import fr.olympa.zta.weapons.ArmorType.ArmorSlot;
import fr.olympa.zta.weapons.Grenade;
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
import fr.olympa.zta.weapons.guns.ambiance.SoundAmbiance;
import fr.olympa.zta.weapons.guns.minigun.MinigunsManager;
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
	public CratesManager crates;
	public SoundAmbiance soundAmbiance;
	public MinigunsManager miniguns;
	
	public KillPlayerRanking rankingKillPlayer;
	public KillZombieRanking rankingKillZombie;
	public LootChestRanking rankingLootChest;
	public MoneyRanking rankingMoney;
	public ClanMoneyRanking rankingMoneyClan;
	
	public DynamicLine<Scoreboard<OlympaPlayerZTA>> lineRadar = new DynamicLine<>(x -> {
		Set<TrackedRegion> regions = OlympaCore.getInstance().getRegionManager().getCachedPlayerRegions(x.getOlympaPlayer().getPlayer());
		String title = "§c§kdddddddd";
		for (TrackedRegion region : regions) {
			SpawningFlag flag = region.getFlag(SpawningFlag.class);
			if (flag != null && flag.type != null) {
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
		OlympaCore.getInstance().setOlympaServer(OlympaServer.ZTA);
		
		Bukkit.clearRecipes();
		sendMessage("Recettes par défaut supprimées.");

		OlympaPermission.registerPermissions(ZTAPermissions.class);
		AccountProvider.setPlayerProvider(OlympaPlayerZTA.class, OlympaPlayerZTA::new, "zta", OlympaPlayerZTA.COLUMNS);

		loadIntegration("dynmap", DynmapLink::initialize);
		loadIntegration("BeautyQuests", () -> beautyQuestsLink = new BeautyQuestsLink());
		loadIntegration("Sentinel", () -> JavaPlugin.getPlugin(SentinelPlugin.class).registerIntegration(new SentinelZTA()));

		try {
			gunRegistry = new GunRegistry();
		}catch (Exception ex) {
			throw new RuntimeException("Registry failed to load", ex);
		}
		
		try {
			Location first = getConfig().getLocation("scoreHolograms.first");
			Location second = getConfig().getLocation("scoreHolograms.second");
			
			rankingKillPlayer = new KillPlayerRanking(first);
			rankingKillZombie = new KillZombieRanking(first);
			new HologramCycler(this, Arrays.asList(rankingKillPlayer.getHologram(), rankingKillZombie.getHologram()), 200).start();
			
			rankingLootChest = new LootChestRanking(second);
			rankingMoney = new MoneyRanking(second);
			rankingMoneyClan = new ClanMoneyRanking(second);
			new HologramCycler(this, Arrays.asList(rankingLootChest.getHologram(), rankingMoney.getHologram(), rankingMoneyClan.getHologram()), 200).start();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		for (GunType gun : GunType.values()) WeaponsGiveGUI.stackables.add(gun);
		for (Knife knife : Knife.values()) WeaponsGiveGUI.stackables.add(knife);
		for (Accessory accessory : Accessory.values()) WeaponsGiveGUI.stackables.add(accessory);
		for (Grenade grenade : Grenade.values()) WeaponsGiveGUI.stackables.add(grenade);
		for (QuestItem item : QuestItem.values()) WeaponsGiveGUI.stackables.add(item);
		
		AmmoType.CARTRIDGE.getName();

		OlympaGroup.ASSISTANT.setRuntimePermission("citizens.*");
		
		hub = new HubManager(getConfig().getSerializable("hub", Region.class), getConfig().getLocation("spawn"), getConfig().getList("spawnRegionTypes").stream().map(x -> SpawnType.valueOf((String) x)).collect(Collectors.toList()));
		teleportationManager = new TeleportationManager(this, ZTAPermissions.BYPASS_TELEPORT_WAIT_COMMAND);
		
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(this, this);
		pluginManager.registerEvents(new WeaponsListener(), this);
		pluginManager.registerEvents(new MobsListener(), this);
		pluginManager.registerEvents(new PlayersListener(config.getLocation("waitingRoom")), this);
		pluginManager.registerEvents(hub, this);
		pluginManager.registerEvents(teleportationManager, this);
		pluginManager.registerEvents(new TpaHandler(this, ZTAPermissions.TPA_COMMANDS), this);
		pluginManager.registerEvents(training = new TrainingManager(getConfig().getConfigurationSection("training")), this);
		pluginManager.registerEvents(combat = new CombatManager(this, 10) {
			public boolean canEnterCombat(Player damager, Player damaged) {
				return !CitizensAPI.getNPCRegistry().isNPC(damaged) && !CitizensAPI.getNPCRegistry().isNPC(damager);
			};
		}, this);
		pluginManager.registerEvents(crates = new CratesManager(), this);
		if (beautyQuestsLink != null) pluginManager.registerEvents(beautyQuestsLink, this);
		
		try {
			pluginManager.registerEvents(clansManager = new ClansManagerZTA(), this);
			pluginManager.registerEvents(clanPlotsManager = new ClanPlotsManager(clansManager), this);
		}catch (Exception ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors de l'initialisation du système de clans et parcelles de clans.");
		}
		
		new TradesManager<>(this, 10);
		
		try {
			pluginManager.registerEvents(miniguns = new MinigunsManager(new File(getDataFolder(), "miniguns.yml")), this);
		}catch (IOException ex) {
			ex.printStackTrace();
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
			pluginManager.registerEvents(ecManager = new EnderChestManager(), this);
		}catch (SQLException ex) {
			ex.printStackTrace();
			getLogger().severe("Une erreur est survenue lors du chargement des coffres de l'end.");
		}
		
		soundAmbiance = new SoundAmbiance();
		soundAmbiance.start();

		new WeaponsCommand().register();
		new MobsCommand().register();
		new HubCommand().register();
		new SpreadManageCommand().register();
		new MoneyCommand<OlympaPlayerZTA>(this, "money", "Gérer son porte-monnaie.", ZTAPermissions.MONEY_COMMAND, ZTAPermissions.MONEY_COMMAND_OTHER, ZTAPermissions.MONEY_COMMAND_MANAGE, "monnaie").register();
		new HealCommand(this, ZTAPermissions.MOD_COMMANDS).register();
		new FeedCommand(this, ZTAPermissions.MOD_COMMANDS).register();
		new BackCommand(this, ZTAPermissions.BACK_COMMAND) {
			final long timeBetween = TimeUnit.DAYS.toMillis(1);
			final NumberFormat numberFormat = new DecimalFormat("00");
			
			@Override
			protected void teleport(Player p, Location location) {
				super.teleport(p, location);
				super.<OlympaPlayerZTA>getOlympaPlayer().backVIPTime.set(System.currentTimeMillis());
			}
			
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				OlympaPlayerZTA olympaPlayer = getOlympaPlayer();
				if (!ZTAPermissions.BACK_COMMAND_INFINITE.hasPermission(olympaPlayer)) {
					long timeToWait = (olympaPlayer.backVIPTime.get() + timeBetween) - System.currentTimeMillis();
					if (timeToWait > 0) {
						sendError("Tu dois encore attendre %s avant de pouvoir refaire un /back !", Utils.durationToString(numberFormat, timeToWait));
						return false;
					}
				}
				return super.onCommand(sender, command, label, args);
			}
		}.register();
		new KitCommand<OlympaPlayerZTA>(this,
				new SimpleKit<>("VIP", ZTAPermissions.KIT_VIP_PERMISSION, TimeUnit.DAYS.toMillis(1), x -> x.kitVIPTime.get(), (x, time) -> x.kitVIPTime.set(time), (op, p) -> new ItemStack[] {
								GunType.M16.createItem(),
								Food.COOKED_BEEF.get(15),
								ArmorType.ANTIRIOT.get(ArmorSlot.BOOTS),
								ArmorType.ANTIRIOT.get(ArmorSlot.LEGGINGS),
								ArmorType.ANTIRIOT.get(ArmorSlot.CHESTPLATE),
								ArmorType.ANTIRIOT.get(ArmorSlot.HELMET),
								AmmoType.HANDWORKED.getAmmo(10, true),
								AmmoType.LIGHT.getAmmo(10, true),
								AmmoType.HEAVY.getAmmo(10, true) })).register();
		new StatsCommand(this).register();
		
		Mobs.Zombies.COMMON.getName(); // initalise les mobs custom
		mobSpawning = new MobSpawning(getConfig().getInt("seaLevel"), getConfig().getConfigurationSection("mobRegions"), getConfig().getConfigurationSection("safeRegions"));
		mobSpawning.start();
		
		Map<Location, PackBlock> packBlocks = getConfig().getStringList("packBlocks").stream().map(SpigotUtils::convertStringToLocation).collect(Collectors.toMap(x -> x, PackBlock::new));
		
		OlympaCore.getInstance().getRegionManager().awaitWorldTracking("world", e -> e.getRegion().registerFlags(
				new GunFlag(false, false),
				new ItemDurabilityFlag(true),
				new PhysicsFlag(true),
				new PlayerBlocksFlag(true),
				new FishFlag(true),
				new GameModeFlag(GameMode.ADVENTURE),
				new PlayerBlockInteractFlag(false, true, true) {
					public void interactEvent(PlayerInteractEvent event) {
						PackBlock packBlock = packBlocks.get(event.getClickedBlock().getLocation());
						if (packBlock != null) {
							packBlock.click(event.getPlayer());
							event.setCancelled(true);
							return;
						}
						super.interactEvent(event);
					}
				}));
		
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
		crates.unload();
		soundAmbiance.stop();
		
		gunRegistry.unload();
	}
	
	private void loadIntegration(String pluginName, Runnable runnable) {
		try {
			if (getServer().getPluginManager().isPluginEnabled(pluginName)) {
				runnable.run();
				sendMessage("§aIntégration §2%s§a chargée", pluginName);
			}else sendMessage("§cLe plugin §4%s§c n'a pas été trouvé, l'intégration n'a pas chargé", pluginName);
		}catch (Exception ex) {
			sendMessage("§cUne erreur est survenue lors du chargement de l'intégration du plugin §4%s", pluginName);
			ex.printStackTrace();
		}
	}
	
}
