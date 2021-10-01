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
import java.util.function.BiFunction;
import java.util.function.Consumer;
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

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.spigot.CombatManager;
import fr.olympa.api.spigot.auctions.AuctionsManager;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.command.essentials.BackCommand;
import fr.olympa.api.spigot.command.essentials.FeedCommand;
import fr.olympa.api.spigot.command.essentials.HealCommand;
import fr.olympa.api.spigot.command.essentials.tp.TpaHandler;
import fr.olympa.api.spigot.economy.MoneyCommand;
import fr.olympa.api.spigot.economy.MoneyPlayerInterface;
import fr.olympa.api.spigot.economy.fluctuating.FluctuatingEconomiesManager;
import fr.olympa.api.spigot.economy.tax.TaxManager;
import fr.olympa.api.spigot.holograms.HologramCycler;
import fr.olympa.api.spigot.lines.CyclingLine;
import fr.olympa.api.spigot.lines.DynamicLine;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.spigot.lines.PlayerObservableLine;
import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.region.tracking.TrackedRegion;
import fr.olympa.api.spigot.region.tracking.flags.FishFlag;
import fr.olympa.api.spigot.region.tracking.flags.FoodFlag;
import fr.olympa.api.spigot.region.tracking.flags.GameModeFlag;
import fr.olympa.api.spigot.region.tracking.flags.ItemDurabilityFlag;
import fr.olympa.api.spigot.region.tracking.flags.PhysicsFlag;
import fr.olympa.api.spigot.region.tracking.flags.PlayerBlockInteractFlag;
import fr.olympa.api.spigot.region.tracking.flags.PlayerBlocksFlag;
import fr.olympa.api.spigot.scoreboard.sign.Scoreboard;
import fr.olympa.api.spigot.scoreboard.sign.ScoreboardManager;
import fr.olympa.api.spigot.scoreboard.tab.TabManager;
import fr.olympa.api.spigot.trades.TradesManager;
import fr.olympa.api.spigot.utils.CustomDayDuration;
import fr.olympa.api.spigot.utils.KillManager;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.zta.bank.BankTrait;
import fr.olympa.zta.clans.ClansManagerZTA;
import fr.olympa.zta.clans.plots.ClanPlotsManager;
import fr.olympa.zta.enderchest.EnderChestManager;
import fr.olympa.zta.glass.GlassSmashFlag;
import fr.olympa.zta.glass.GlassSmashManager;
import fr.olympa.zta.hub.HubCommand;
import fr.olympa.zta.hub.HubManager;
import fr.olympa.zta.hub.SpreadManageCommand;
import fr.olympa.zta.itemstackable.Artifacts;
import fr.olympa.zta.itemstackable.Bandage;
import fr.olympa.zta.itemstackable.Brouilleur;
import fr.olympa.zta.itemstackable.ItemStackableManager;
import fr.olympa.zta.itemstackable.ParachuteModule;
import fr.olympa.zta.itemstackable.QuestItem;
import fr.olympa.zta.loot.chests.LootChestsManager;
import fr.olympa.zta.loot.crates.CratesManager;
import fr.olympa.zta.loot.creators.FoodCreator.Food;
import fr.olympa.zta.loot.packs.PackBlock;
import fr.olympa.zta.loot.packs.PackCommand;
import fr.olympa.zta.loot.pickers.PickersManager;
import fr.olympa.zta.mobs.MobSpawning;
import fr.olympa.zta.mobs.MobSpawning.SpawnType;
import fr.olympa.zta.mobs.MobSpawning.SpawnType.SpawningFlag;
import fr.olympa.zta.mobs.MobsCommand;
import fr.olympa.zta.mobs.MobsListener;
import fr.olympa.zta.mobs.PlayersListener;
import fr.olympa.zta.mobs.custom.Mobs;
import fr.olympa.zta.plots.PlayerPlotsManager;
import fr.olympa.zta.plots.TomHookTrait;
import fr.olympa.zta.primes.BountyTrait;
import fr.olympa.zta.primes.PrimesManager;
import fr.olympa.zta.ranks.ClanMoneyRanking;
import fr.olympa.zta.ranks.KillPlayerRanking;
import fr.olympa.zta.ranks.KillZombieRanking;
import fr.olympa.zta.ranks.LootChestRanking;
import fr.olympa.zta.ranks.MoneyRanking;
import fr.olympa.zta.settings.PlayerSettingsCommand;
import fr.olympa.zta.shops.CivilBlockShop;
import fr.olympa.zta.shops.CorporationBlockShop;
import fr.olympa.zta.shops.FoodBuyingShop;
import fr.olympa.zta.shops.FraterniteBlockShop;
import fr.olympa.zta.shops.GunShop;
import fr.olympa.zta.shops.QuestItemShop;
import fr.olympa.zta.tyrolienne.Tyrolienne;
import fr.olympa.zta.utils.AuctionsManagerZTA;
import fr.olympa.zta.utils.ResourcePackCommand;
import fr.olympa.zta.utils.TeleportationManagerZTA;
import fr.olympa.zta.utils.map.DynmapLink;
import fr.olympa.zta.utils.npcs.AuctionsTrait;
import fr.olympa.zta.utils.npcs.SentinelZTA;
import fr.olympa.zta.utils.quests.BeautyQuestsLink;
import fr.olympa.zta.weapons.Grenade;
import fr.olympa.zta.weapons.Knife;
import fr.olympa.zta.weapons.TrainingManager;
import fr.olympa.zta.weapons.WeaponsCommand;
import fr.olympa.zta.weapons.WeaponsListener;
import fr.olympa.zta.weapons.guns.Accessory;
import fr.olympa.zta.weapons.guns.AmmoType;
import fr.olympa.zta.weapons.guns.GunFlag;
import fr.olympa.zta.weapons.guns.GunRegistry;
import fr.olympa.zta.weapons.guns.GunType;
import fr.olympa.zta.weapons.guns.ambiance.SoundAmbiance;
import fr.olympa.zta.weapons.guns.minigun.MinigunsManager;
import fr.olympa.zta.weapons.skins.SkinsTrait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class OlympaZTA extends OlympaAPIPlugin implements Listener {

	private static OlympaZTA instance;

	public static OlympaZTA getInstance() {
		return instance;
	}

	private int serverID = -1;

	public BeautyQuestsLink beautyQuestsLink;

	public TeleportationManagerZTA teleportationManager;
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
	public TabManager tab;
	public KillManager kills;
	public PrimesManager primes;
	public GlassSmashManager glass;
	public CustomDayDuration customDay;
	public FluctuatingEconomiesManager economies;
	public PickersManager pickers;

	public ResourcePackCommand resourcePackCommand;

	public KillPlayerRanking rankingKillPlayer;
	public KillZombieRanking rankingKillZombie;
	public LootChestRanking rankingLootChest;
	public MoneyRanking rankingMoney;
	public ClanMoneyRanking rankingMoneyClan;

	public DynamicLine<Scoreboard<OlympaPlayerZTA>> lineRadar = new DynamicLine<>(x -> {
		Set<TrackedRegion> regions = OlympaCore.getInstance().getRegionManager().getCachedPlayerRegions((Player) x.getOlympaPlayer().getPlayer());
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

	private Tyrolienne tyrolienne;

	@Override
	public void onEnable() {
		try {
			instance = this;
			super.onEnable();
			OlympaCore.getInstance().setOlympaServer(OlympaServer.ZTA);

			serverID = getConfig().getInt("serverIndex", -1);
			if (serverID == -1) throw new IllegalArgumentException("Server ID not found");
			sendMessage("Server ID : §6%s", getServerNameID());

			Bukkit.clearRecipes();
			sendMessage("Recettes par défaut supprimées.");

			OlympaPermission.registerPermissions(ZTAPermissions.class);
			AccountProviderAPI.getter().setPlayerProvider(OlympaPlayerZTA.class, OlympaPlayerZTA::new, getServerNameID(), OlympaPlayerZTA.COLUMNS);
			if (AccountProviderAPI.getter().getPluginPlayerTable() == null) throw new RuntimeException();

			loadIntegration("dynmap", DynmapLink::initialize);
			loadIntegration("BeautyQuests", () -> beautyQuestsLink = new BeautyQuestsLink());
			loadIntegration("Sentinel", () -> {
				SentinelZTA sentinelZTA = new SentinelZTA();
				JavaPlugin.getPlugin(SentinelPlugin.class).registerIntegration(sentinelZTA);
				getServer().getPluginManager().registerEvents(sentinelZTA, this);
			});

			try {
				gunRegistry = new GunRegistry();
			}catch (Exception ex) {
				throw new RuntimeException("Registry failed to load", ex);
			}

			GunType.values();
			Knife.values();
			Accessory.values();
			Grenade.values();
			QuestItem.values();
			Artifacts.values();
			ItemStackableManager.register(Bandage.BANDAGE);
			ItemStackableManager.register(Brouilleur.BROUILLEUR);
			sendMessage("§6%d §etypes d'items enregistrés !", ItemStackableManager.stackables.size());

			AmmoType.values();

			OlympaGroup.PLAYER.setRuntimePermission("dynmap.*", false);
			OlympaGroup.ASSISTANT.setRuntimePermission("citizens.*");
			OlympaGroup.GAMEMASTER.setRuntimePermission("beautyquests.*");

			hub = new HubManager(getConfig().getSerializable("hub", Region.class), getConfig().getLocation("spawn"), getConfig().getList("spawnRegionTypes").stream().map(x -> SpawnType.valueOf((String) x)).collect(Collectors.toList()));
			teleportationManager = new TeleportationManagerZTA(this, ZTAPermissions.BYPASS_TELEPORT_WAIT_COMMAND);

			PluginManager pluginManager = getServer().getPluginManager();
			pluginManager.registerEvents(this, this);
			pluginManager.registerEvents(new WeaponsListener(), this);
			pluginManager.registerEvents(new MobsListener(), this);
			pluginManager.registerEvents(new PlayersListener(config.getLocation("waitingRoom")), this);
			pluginManager.registerEvents(hub, this);
			pluginManager.registerEvents(teleportationManager, this);
			pluginManager.registerEvents(new TpaHandler(this, ZTAPermissions.TPA_COMMANDS, teleportationManager), this);
			BiFunction<CommandSender, OlympaPlayer, Boolean> canExecuteTpaCmds = (sender, olympaPlayer) -> {
				return !combat.isInCombat((Player) sender);
			};
			Consumer<CommandSender> cantMsg = sender -> {
				Prefix.DEFAULT_BAD.sendMessage(sender, "Tu ne peux pas utiliser la téléportation.");
			};
			OlympaCommand tpCmd = OlympaCommand.getCmd("tpahere");
			tpCmd.addCanExecute(canExecuteTpaCmds, cantMsg);
			tpCmd = OlympaCommand.getCmd("tpa");
			tpCmd.addCanExecute(canExecuteTpaCmds, cantMsg);
			tpCmd = OlympaCommand.getCmd("tpayes");
			tpCmd.addCanExecute(canExecuteTpaCmds, cantMsg);
			pluginManager.registerEvents(training = new TrainingManager(getConfig().getConfigurationSection("training")), this);
			pluginManager.registerEvents(combat = new CombatManager(this, 15) {
				@Override
				public boolean canEnterCombat(Player damager, Player damaged) {
					return !CitizensAPI.getNPCRegistry().isNPC(damaged) && !CitizensAPI.getNPCRegistry().isNPC(damager);
				}
			}, this);
			pluginManager.registerEvents(crates = new CratesManager(), this);
			/*pluginManager.registerEvents(new SitManager(this) {
				@Override
				public boolean canSit(Player p) {
					return super.canSit(p) && !combat.isInCombat(p);
				}
			}, this);*/
			if (beautyQuestsLink != null)
				pluginManager.registerEvents(beautyQuestsLink, this);

			try {
				pluginManager.registerEvents(clansManager = new ClansManagerZTA(), this);
				pluginManager.registerEvents(clanPlotsManager = new ClanPlotsManager(clansManager, getConfig().getLocation("clanPlotsBook")), this);
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
				pluginManager.registerEvents(primes = new PrimesManager(), this);
			}catch (Exception ex) {
				sendMessage("§cLes primes n'ont pas chargé.");
				ex.printStackTrace();
			}

			try {
				pluginManager.registerEvents(glass = new GlassSmashManager(), this);
			}catch (Exception ex) {
				sendMessage("§cLe système de cassage des vitres n'a pas chargé.");
				ex.printStackTrace();
			}

			try {
				pluginManager.registerEvents(tyrolienne = new Tyrolienne(getConfig().getLocation("tyrolienne.from"), getConfig().getLocation("tyrolienne.to")), this);
			}catch (Exception ex) {
				sendMessage("§cLa tyrolienne pas chargé.");
				ex.printStackTrace();
			}

			try {
				economies = new FluctuatingEconomiesManager(this, getServerNameID(), ZTAPermissions.ECONOMIES_MANAGE_COMMAND);
			}catch (Exception ex) {
				sendMessage("§cLes économies n'ont pas chargé.");
				ex.printStackTrace();
			}

			try {
				String schemName = getConfig().getString("firstBuildSchem");
				File file = new File(getDataFolder(), schemName);
				if (!file.exists())
					Files.copy(getResource(schemName), file.toPath());
				plotsManager = new PlayerPlotsManager(file);
				checkForTrait(TomHookTrait.class, "plots", getConfig().getIntegerList("tomHookNPC"));
			}catch (Exception ex) {
				ex.printStackTrace();
				getLogger().severe("Une erreur est survenue lors de l'initialisation du système de plots joueurs.");
			}

			/*try {
			int i = 0;
			tab = new TabManager(this)
					.addText(3, "§6§l    Olympa").addText(5, "§e  Serveur multi-jeux").addText(i = 11, "§7 ➤ Aide").addText(++i, "§7  Utilise /help").addText(++i, "§7  pour une liste").addText(++i, "§7  des commandes.")
					.addText(i = 21, "  §7⬛⬛⬛⬛⬛⬛⬛⬛").addText(++i, "  §7⬛⬛⬛§e⬛⬛§7⬛⬛⬛").addText(++i, "  §7⬛⬛§e⬛§7⬛⬛§e⬛§7⬛⬛").addText(++i, "  §7⬛§e⬛§7⬛⬛⬛⬛§e⬛§7⬛").addText(++i, "  §7⬛§e⬛§7⬛⬛⬛⬛§e⬛§7⬛").addText(++i, "  §7⬛⬛§e⬛§7⬛⬛§e⬛§7⬛⬛")
					.addText(++i, "  §7⬛§e⬛⬛§7⬛⬛§e⬛⬛§7⬛").addText(++i, "  §7⬛⬛⬛⬛⬛⬛⬛⬛")
					.addText(35, "§7    Bon jeu").addText(36, "§7  sur §lOlympa§7 !")
					.build();
			} catch (Exception ex) {
			ex.printStackTrace();
			}*/

			try {
				taxManager = new TaxManager(this, ZTAPermissions.TAX_MANAGE_COMMAND, OlympaZTA.getInstance().getServerNameID() + "_tax", 0);
				auctionsManager = new AuctionsManagerZTA(this, OlympaZTA.getInstance().getServerNameID() + "_auctions", taxManager) {
					@Override
					public int getMaxAuctions(MoneyPlayerInterface player) {
						return player.getGroup().getPower() >= OlympaGroup.VIP.getPower() ? 20 : 10;
					}
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

			try {
				new ParachuteModule(this);
			}catch (Exception ex) {
				ex.printStackTrace();
				getLogger().severe("Une erreur est survenue lors du chargement des parachutes.");
			}

			try {
				new KillManager(this);
			}catch (Exception ex) {
				ex.printStackTrace();
				getLogger().severe("Une erreur est survenue lors du chargement du module de tracking des kills.");
			}
			
			try {
				pickers = new PickersManager();
			}catch (Exception ex) {
				ex.printStackTrace();
				getLogger().severe("Une erreur est survenue lors du chargement du module de pickers random.");
			}

			soundAmbiance = new SoundAmbiance();
			soundAmbiance.start();

			resourcePackCommand = new ResourcePackCommand(this, getConfig().getConfigurationSection("resourcePack"));
			resourcePackCommand.register();

			new WeaponsCommand().register();
			new MobsCommand().register();
			new HubCommand().register();
			new PlayerSettingsCommand().register();
			new SpreadManageCommand().register();
			new MoneyCommand<OlympaPlayerZTA>(this, "money", "Gérer son porte-monnaie.", ZTAPermissions.MONEY_COMMAND, ZTAPermissions.MONEY_COMMAND_OTHER, ZTAPermissions.MONEY_COMMAND_MANAGE, "monnaie").register();
			new HealCommand(this, ZTAPermissions.MOD_COMMANDS).register();
			new FeedCommand(this, ZTAPermissions.MOD_COMMANDS).register();
			new BackCommand(this, ZTAPermissions.GROUP_HEROS) {
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
						long timeToWait = olympaPlayer.backVIPTime.get() + timeBetween - System.currentTimeMillis();
						if (timeToWait > 0) {
							sendError("Tu dois encore attendre %s avant de pouvoir refaire un /back !", Utils.durationToString(numberFormat, timeToWait));
							return false;
						}
					}
					return super.onCommand(sender, command, label, args);
				}
			}.register();
			new PackCommand(this).register();
			new KitZTACommand(this).register();
			new StatsCommand(this).register();

			Mobs.Zombies.COMMON.getName(); // initalise les mobs custom
			mobSpawning = new MobSpawning(getConfig().getInt("seaLevel"), getConfig().getConfigurationSection("mobRegions"), getConfig().getConfigurationSection("safeRegions"));
			mobSpawning.start();

			Map<Location, PackBlock> packBlocks = getConfig().getStringList("packBlocks").stream().map(SpigotUtils::convertStringToLocation).collect(Collectors.toMap(x -> x, PackBlock::new));
			sendMessage("§6%d §eblocs de packs d'équipement chargés.", packBlocks.size());

			OlympaCore.getInstance().getRegionManager().awaitWorldTracking("world", e -> e.getRegion().registerFlags(new GunFlag(false, false), new ItemDurabilityFlag(true), new PhysicsFlag(true), new PlayerBlocksFlag(true), new FishFlag(true), new FoodFlag(false), new GameModeFlag(GameMode.ADVENTURE), new GlassSmashFlag(true), new PlayerBlockInteractFlag(false, true, true) {
				@Override
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

			scoreboards = new ScoreboardManager<OlympaPlayerZTA>(this, "§6Olympa §e§lZTA").addLines(FixedLine.EMPTY_LINE, lineMoney, lineDeaths, FixedLine.EMPTY_LINE, lineRadar).addFooters(FixedLine.EMPTY_LINE, CyclingLine.olympaAnimation());

			customDay = new CustomDayDuration(this, Bukkit.getWorld("world"), 15600, 12000, 3).setNightRunnable(() -> {
				Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§7La nuit tombe... §oprenez garde !")));
			});

			try {
				Location first = getConfig().getLocation("scoreHolograms.first");
				Location second = getConfig().getLocation("scoreHolograms.second");

				rankingKillPlayer = new KillPlayerRanking(first);
				rankingKillZombie = new KillZombieRanking(first);
				new HologramCycler(this, Arrays.asList(rankingKillPlayer.getHologram(), rankingKillZombie.getHologram()), 200).start();

				rankingLootChest = new LootChestRanking(second);
				rankingMoney = new MoneyRanking(second);
				rankingMoneyClan = new ClanMoneyRanking(second);
				new HologramCycler(this, Arrays.asList(rankingLootChest.getHologram(), rankingMoney.getHologram(), rankingMoneyClan.getHologram()), 150).start();
			}catch (Exception e) {
				e.printStackTrace();
				sendMessage("§cUne erreur est survenue lors du chargment des tableaux de scores.");
			}

			checkForTrait(BankTrait.class, "bank", getConfig().getIntegerList("bank"));
			checkForTrait(AuctionsTrait.class, "auctions", getConfig().getIntegerList("auctions"));
			checkForTrait(CivilBlockShop.class, "blockshopcivil", getConfig().getIntegerList("blockShopCivil"));
			checkForTrait(FraterniteBlockShop.class, "blockshopfraternite", getConfig().getIntegerList("blockShopFraternite"));
			checkForTrait(CorporationBlockShop.class, "blockshopcorporation", getConfig().getIntegerList("blockShopCorporation"));
			checkForTrait(QuestItemShop.class, "questitemshop", getConfig().getIntegerList("questItemShop"));
			checkForTrait(FoodBuyingShop.class, "foodshop", getConfig().getIntegerList("foodBuyingShop"));
			checkForTrait(GunShop.class, "gunShop", getConfig().getIntegerList("gunShop"));
			checkForTrait(SkinsTrait.class, "skins", getConfig().getIntegerList("skinsNPC"));
			if (primes != null)
				checkForTrait(BountyTrait.class, "bountyMan", getConfig().getIntegerList("bountyMan"));

			OlympaAPIPermissionsSpigot.GAMEMODE_COMMAND.setMinGroup(OlympaGroup.MOD);
			OlympaAPIPermissionsSpigot.TP_COMMAND.setMinGroup(OlympaGroup.MOD);
			OlympaAPIPermissionsSpigot.TP_COMMAND_NOT_VANISH.setMinGroup(OlympaGroup.RESP_GAMES);
			OlympaAPIPermissionsSpigot.FLY_COMMAND.setMinGroup(OlympaGroup.MODP);
			OlympaAPIPermissionsSpigot.GAMEMODE_COMMAND_OTHER.setMinGroup(OlympaGroup.MOD);
			OlympaAPIPermissionsSpigot.INVSEE_COMMAND_INTERACT.setMinGroup(OlympaGroup.MOD);
			OlympaAPIPermissionsSpigot.ECSEE_COMMAND_INTERACT.setMinGroup(OlympaGroup.MOD);
			OlympaCommand enderchest = OlympaCommand.getCmd("enderchest");
			if (enderchest != null)
				enderchest.unregister();
			OlympaCore.getInstance().getVanishApi().registerHandler("zta_dynmap", (p, olympaPlayer, isVanish) -> {
				DynmapLink.ifEnabled(link -> link.setPlayerVisiblity(p, isVanish));
			});
		}catch (Throwable ex) {
			ex.printStackTrace();
			sendMessage("§4Une erreur est survenue lors du chargement du plugin. Le serveur ne peut être lancé sans risque.");
			Bukkit.shutdown();
		}
	}

	public void checkForTrait(Class<? extends Trait> trait, String name, Iterable<Integer> npcs) {
		if (CitizensAPI.getTraitFactory().getTraitClass(name) == null)
			CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(trait).withName(name));
		if (npcs != null)
			npcs.forEach(x -> traitsToAdd.put(x, trait));
	}

	@EventHandler
	public void onCitizensEnable(CitizensEnableEvent e) {
		traitsToAdd.forEach((npcID, trait) -> {
			NPC npc = CitizensAPI.getNPCRegistry().getById(npcID);
			if (npc == null)
				getLogger().warning("Le NPC " + npcID + " n'existe pas. (trait " + trait.getSimpleName() + ")");
			else if (!npc.hasTrait(trait))
				npc.addTrait(trait);
		});
		if (training != null)
			training.hashCode();
	}

	@EventHandler
	public void onBlockDrop(BlockDropItemEvent e) {
		for (Item item : e.getItems()) {
			ItemStack originalItem = item.getItemStack();
			Material type = originalItem.getType();
			Food food = null;
			if (type == Material.WHEAT)
				food = Food.BREAD;
			else if (type == Material.POTATO || type == Material.POISONOUS_POTATO) {
				BlockData data = e.getBlockState().getBlockData();
				if (data instanceof Ageable) {
					Ageable ageable = (Ageable) data;
					if (ageable.getAge() == ageable.getMaximumAge() && ThreadLocalRandom.current().nextBoolean())
						food = Food.BAKED_POTATO;
				}
			} else if (type == Material.CARROT)
				food = Food.CARROT;
			if (food != null)
				item.setItemStack(food.get(originalItem.getAmount()));
		}
	}

	@Override
	public void onBeforeStop() {
		super.onBeforeStop();
		if (combat != null)
			combat.unload();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		if (gunRegistry != null)
			gunRegistry.unload();

		if (training != null)
			training.unload();
		if (tyrolienne != null)
			tyrolienne.unload();
		if (customDay != null)
			customDay.unload();
		HandlerList.unregisterAll((Plugin) this);

		if (mobSpawning != null)
			mobSpawning.end();
		if (scoreboards != null)
			scoreboards.unload();
		if (combat != null)
			combat.unload();
		if (crates != null)
			crates.unload();
		if (soundAmbiance != null)
			soundAmbiance.stop();
	}

	private void loadIntegration(String pluginName, Runnable runnable) {
		try {
			if (getServer().getPluginManager().isPluginEnabled(pluginName)) {
				runnable.run();
				sendMessage("§aIntégration §2%s§a chargée", pluginName);
			} else
				sendMessage("§cLe plugin §4%s§c n'a pas été trouvé, l'intégration n'a pas chargé", pluginName);
		} catch (Exception ex) {
			sendMessage("§cUne erreur est survenue lors du chargement de l'intégration du plugin §4%s", pluginName);
			ex.printStackTrace();
		}
	}

	public int getServerID() {
		if (serverID == -1) throw new IllegalArgumentException();
		return serverID;
	}

	public String getServerNameID() {
		return "zta" + getServerID();
	}

}
