package fr.olympa.zta;

import java.sql.SQLException;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.region.ExpandedCuboid;
import fr.olympa.api.scoreboard.DynamicLine;
import fr.olympa.api.scoreboard.FixedLine;
import fr.olympa.api.scoreboard.ScoreboardManager;
import fr.olympa.zta.bank.BankTrait;
import fr.olympa.zta.clans.Clan;
import fr.olympa.zta.clans.ClansCommand;
import fr.olympa.zta.clans.ClansListener;
import fr.olympa.zta.clans.ClansManager;
import fr.olympa.zta.enderchest.EnderChestCommand;
import fr.olympa.zta.lootchests.LootChest;
import fr.olympa.zta.lootchests.LootChestCommand;
import fr.olympa.zta.lootchests.LootChestsListener;
import fr.olympa.zta.mobs.MobSpawning;
import fr.olympa.zta.mobs.Mobs;
import fr.olympa.zta.mobs.MobsCommand;
import fr.olympa.zta.mobs.MobsListener;
import fr.olympa.zta.registry.ItemsListener;
import fr.olympa.zta.registry.ZTARegistry;
import fr.olympa.zta.registry.ZTARegistry.DeserializeDatas;
import fr.olympa.zta.weapons.WeaponsCommand;
import fr.olympa.zta.weapons.WeaponsListener;
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
	private ClansListener clansListener = new ClansListener();
	private ItemsListener itemsListener = new ItemsListener();

	public MobSpawning mobSpawning;
	public ScoreboardManager scoreboards;

	public Location spawn;

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();

		OlympaPermission.registerPermissions(ZTAPermissions.class);
		AccountProvider.setPlayerProvider(OlympaPlayerZTA.class, OlympaPlayerZTA::new, "zta", OlympaPlayerZTA.COLUMNS);

		ClansManager.initialize();
		
		PluginManager pluginManager = this.getServer().getPluginManager();
		pluginManager.registerEvents(weaponListener, this);
		pluginManager.registerEvents(chestsListener, this);
		pluginManager.registerEvents(mobsListener, this);
		pluginManager.registerEvents(clansListener, this);
		pluginManager.registerEvents(itemsListener, this);

		new LootChestCommand().register();
		new WeaponsCommand().register();
		new MobsCommand().register();
		new ClansCommand().register();
		new EnderChestCommand().register();
		new MoneyCommand().register();
		new SpawnCommand().register();
		new ChunkFixCommand().register();

		Arrays.asList(
				GunM1911.class, GunCobra.class, Gun870.class, GunUZI.class, GunM16.class, GunM1897.class, GunG19.class, GunSkorpion.class, GunAK.class, GunBenelli.class, GunDragunov.class, GunLupara.class, GunP22.class, GunSDMR.class, GunStoner.class, GunBarrett.class, GunKSG.class)
				.forEach(x -> ZTARegistry.registerObjectType(x, Gun.TABLE_NAME, Gun.CREATE_TABLE_STATEMENT, Gun::deserializeGun));
		Arrays.asList(
				KnifeBatte.class, KnifeBiche.class, KnifeSurin.class,
				CannonDamage.class, CannonPower.class, CannonSilent.class, CannonStabilizer.class, ScopeLight.class, ScopeStrong.class, StockLight.class, StockStrong.class)
				.forEach(x -> ZTARegistry.registerObjectType(x, null, null, DeserializeDatas.easyClass()));
		ZTARegistry.registerObjectType(LootChest.class, LootChest.TABLE_NAME, LootChest.CREATE_TABLE_STATEMENT, LootChest::deserializeLootChest);
		ZTARegistry.registerObjectType(Clan.class, Clan.TABLE_NAME, Clan.CREATE_TABLE_STATEMENT, Clan::deserializeClan);

		new Mobs(); // initalise les mobs custom
		mobSpawning = new MobSpawning(getConfig().getSerializable("spawnRegion", ExpandedCuboid.class));
		mobSpawning.start();

		spawn = Location.deserialize(getConfig().getConfigurationSection("spawn").getValues(false));
		
		scoreboards = new ScoreboardManager(this, "§6Olympa §e§lZTA", Arrays.asList(
				FixedLine.EMPTY_LINE,
				new DynamicLine<OlympaPlayerZTA>(x -> "§eRang : §6" + x.getGroup().getName()),
				FixedLine.EMPTY_LINE,
				new DynamicLine<OlympaPlayerZTA>(x -> "§eNombre de mobs : §6" + mobSpawning.region.getWorld().getLivingEntities().size(), 1, 0),
				FixedLine.EMPTY_LINE,
				new DynamicLine<OlympaPlayerZTA>(x -> "§eMonnaie : §6" + x.getGameMoney().getFormatted(), 1, 0)));

		for (Player p : getServer().getOnlinePlayers()) {
			weaponListener.onJoin(new PlayerJoinEvent(p.getPlayer(), "random join message"));
		}

		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BankTrait.class).withName("bank"));

		try {
			sendMessage(ZTARegistry.loadFromDatabase() + " objets chargés dans le registre.");
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDisable(){
		HandlerList.unregisterAll(this);
		mobSpawning.end();
		scoreboards.unload();
		
		for (Player p : getServer().getOnlinePlayers()) {
			weaponListener.onQuit(new PlayerQuitEvent(p.getPlayer(), "random quit message"));
		}

		ZTARegistry.saveDatabase();
	}
	
}
