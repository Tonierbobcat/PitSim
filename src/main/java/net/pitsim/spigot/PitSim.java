package net.pitsim.spigot;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.sk89q.worldedit.EditSession;
import com.xxmicloxx.NoteBlockAPI.songplayer.EntitySongPlayer;
import de.myzelyam.api.vanish.VanishAPI;
import de.tr7zw.nbtapi.NBTItem;
import dev.kyro.arcticapi.ArcticAPI;
import dev.kyro.arcticapi.commands.AMultiCommand;
import dev.kyro.arcticapi.data.AConfig;
import dev.kyro.arcticapi.hooks.AHook;
import dev.kyro.arcticapi.misc.AOutput;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import net.pitsim.spigot.battlepass.quests.*;
import net.pitsim.spigot.battlepass.quests.dzkillmobs.*;
import net.pitsim.spigot.boosters.*;
import net.pitsim.spigot.*;
import net.pitsim.spigot.commands.*;
import net.pitsim.spigot.commands.admin.*;
import net.pitsim.spigot.commands.beta.*;
import net.pitsim.spigot.commands.essentials.*;
import net.pitsim.spigot.controllers.*;
import net.pitsim.spigot.controllers.objects.*;
import net.pitsim.spigot.cosmetics.bounty.*;
import net.pitsim.spigot.cosmetics.capes.*;
import net.pitsim.spigot.cosmetics.killeffectsplayer.*;
import net.pitsim.spigot.cosmetics.trails.*;
import net.pitsim.spigot.darkzone.*;
import net.pitsim.spigot.darkzone.altar.BiomeChanger;
import net.pitsim.spigot.enchants.overworld.*;
import net.pitsim.spigot.enchants.tainted.chestplate.*;
import net.pitsim.spigot.enchants.tainted.common.*;
import net.pitsim.spigot.enchants.tainted.scythe.*;
import net.pitsim.spigot.enchants.tainted.uncommon.*;
import net.pitsim.spigot.enchants.tainted.uncommon.basic.*;
import net.pitsim.spigot.darkzone.abilities.CageAbility;
import net.pitsim.spigot.darkzone.altar.AltarManager;
import net.pitsim.spigot.darkzone.progression.ProgressionManager;
import net.pitsim.spigot.enchants.tainted.uncommon.basic.Shield;
import net.pitsim.spigot.enums.EquipmentType;
import net.pitsim.spigot.events.EquipmentChangeEvent;
import net.pitsim.spigot.helmetabilities.*;
import net.pitsim.spigot.help.HelpManager;
import net.pitsim.spigot.items.diamond.*;
import net.pitsim.spigot.items.misc.*;
import net.pitsim.spigot.items.mobdrops.*;
import net.pitsim.spigot.items.mystics.*;
import net.pitsim.spigot.killstreaks.*;
import net.pitsim.spigot.killstreaks.Leech;
import net.pitsim.spigot.leaderboards.*;
import net.pitsim.spigot.megastreaks.*;
import net.pitsim.spigot.misc.PitEquipment;
import net.pitsim.spigot.misc.ReloadManager;
import net.pitsim.spigot.misc.packets.SignPrompt;
import net.pitsim.spigot.npcs.*;
import net.pitsim.spigot.perks.*;
import net.pitsim.spigot.placeholders.*;
import net.pitsim.spigot.serverstatistics.StatisticsManager;
import net.pitsim.spigot.auction.AuctionDisplays;
import net.pitsim.spigot.auction.AuctionManager;
import net.pitsim.spigot.battlepass.PassManager;
import net.pitsim.spigot.battlepass.quests.daily.DailyBotKillQuest;
import net.pitsim.spigot.battlepass.quests.daily.DailyMegastreakQuest;
import net.pitsim.spigot.battlepass.quests.daily.DailyPlayerKillQuest;
import net.pitsim.spigot.battlepass.quests.daily.DailySWGamePlayedQuest;
import net.pitsim.spigot.brewing.BrewingManager;
import net.pitsim.spigot.brewing.PotionManager;
import net.pitsim.spigot.cosmetics.CosmeticManager;
import net.pitsim.spigot.cosmetics.PitCosmetic;
import net.pitsim.spigot.cosmetics.aura.*;
import net.pitsim.spigot.cosmetics.killeffectsbot.AlwaysExe;
import net.pitsim.spigot.cosmetics.killeffectsbot.IronKill;
import net.pitsim.spigot.cosmetics.killeffectsbot.OnlyExe;
import net.pitsim.spigot.cosmetics.killeffectsbot.Tetris;
import net.pitsim.spigot.cosmetics.misc.ElectricPresence;
import net.pitsim.spigot.cosmetics.misc.Halo;
import net.pitsim.spigot.cosmetics.misc.KyroCosmetic;
import net.pitsim.spigot.cosmetics.misc.MysticPresence;
import net.pitsim.spigot.enums.NBTTag;
import net.pitsim.spigot.enums.PitCalendarEvent;
import net.pitsim.spigot.holograms.HologramManager;
import net.pitsim.spigot.kits.EssentialKit;
import net.pitsim.spigot.kits.GoldKit;
import net.pitsim.spigot.kits.PvPKit;
import net.pitsim.spigot.kits.XPKit;
import net.pitsim.spigot.logging.LogManager;
import net.pitsim.spigot.market.MarketMessaging;
import net.pitsim.spigot.misc.ItemRename;
import net.pitsim.spigot.misc.effects.PacketBlock;
import net.pitsim.spigot.pitmaps.BiomesMap;
import net.pitsim.spigot.pitmaps.DimensionsMap;
import net.pitsim.spigot.pitmaps.SandMap;
import net.pitsim.spigot.pitmaps.XmasMap;
import net.pitsim.spigot.settings.scoreboard.*;
import net.pitsim.spigot.sql.TableManager;
import net.pitsim.spigot.storage.StorageManager;
import net.pitsim.spigot.tutorial.TutorialManager;
import net.pitsim.spigot.tutorial.checkpoints.*;
import net.pitsim.spigot.upgrades.*;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import septogeddon.pluginquery.PluginQuery;
import septogeddon.pluginquery.api.QueryMessenger;

import java.io.File;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;

public class PitSim extends JavaPlugin {
	public static final double VERSION = 3.0;

	public static final boolean MARKET_ENABLED = true;

	public static LuckPerms LUCKPERMS;
	public static PitSim INSTANCE;
	public static ProtocolManager PROTOCOL_MANAGER = null;
	public static BukkitAudiences adventure;

	public static String serverName;

//	public static PteroClient client;

	public static long currentTick = 0;
	public static final ZoneId TIME_ZONE = ZoneId.of("America/New_York");

	public static ServerStatus status;

//	public static AnticheatManager anticheat;

	public static World DARKZONE() {
		return Bukkit.getWorld(PitSim.INSTANCE.getConfig().getString("worlds.darkzone"));
	}
	public static World TUTORIAL() {
		return Bukkit.getWorld(PitSim.INSTANCE.getConfig().getString("worlds.tutorial"));
	}
	@Override
	public void onEnable() {
		INSTANCE = this;

		loadConfig();
		ArcticAPI.configInit(this, "prefix", "error-prefix");
		serverName = AConfig.getString("server");
		if(AConfig.getBoolean("standalone-server")) status = ServerStatus.STANDALONE;
		else status = serverName.contains("darkzone") ? ServerStatus.DARKZONE : ServerStatus.OVERWORLD;


		FirestoreManager.init();

		new Config().load();

		if (FirestoreManager.CONFIG == null) {
			getLogger().log(Level.SEVERE, "Could not load config!");
			Bukkit.getPluginManager().disablePlugin(this);
		}
		else {
			getLogger().log(Level.INFO, "Config successfully loaded!");
		}

		Arrays.asList(
				"url: " + FirestoreManager.CONFIG.SQLHostname,
				"user: " + "****",
				"pass: " + "****"
		).forEach(string -> {
			getLogger().log(Level.INFO, "SQL " + string);
		});

		try {
			Class.forName("litebans.api.Database");
			LitebansManager.init();
			getLogger().log(Level.INFO, "Hooked into LiteBans!");
		} catch (Exception ignore) {
		}

		if (!TableManager.registerTables()) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		else {
			getLogger().log(Level.INFO, "Successfully registered tables to database!");
		}

		if(status.isDarkzone()) {
			DarkzoneManager.loadChunks();
			DarkzoneManager.clearEntities();
		}

		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			PlayerManager.addRealPlayer(onlinePlayer.getUniqueId());

			PitEquipment currentEquipment = new PitEquipment(onlinePlayer);
			for(EquipmentType equipmentType : EquipmentType.values()) {
				new BukkitRunnable() {
					@Override
					public void run() {
						if(!onlinePlayer.isOnline()) return;
						EquipmentChangeEvent event = new EquipmentChangeEvent(onlinePlayer, equipmentType,
								new PitEquipment(), currentEquipment, true);
						Bukkit.getPluginManager().callEvent(event);
					}
				}.runTaskLater(PitSim.INSTANCE, 1L);
			}

//			onlinePlayer.teleport(new Location(MapManager.getDarkzone(), 312.5, 68, -139.5, -104, 7));
		}

		BossBarManager.init();
		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			boolean success = PitPlayer.loadPitPlayer(onlinePlayer.getUniqueId());
			if(success) continue;
			onlinePlayer.kickPlayer(ChatColor.RED + "Playerdata failed to load. Please open a support ticket: discord.pitsim.net");
		}

//		if(Bukkit.getPluginManager().getPlugin("GrimAC") != null) hookIntoAnticheat(new GrimManager());
//		if(Bukkit.getPluginManager().getPlugin("PolarLoader") != null) hookIntoAnticheat(new PolarManager());

//		if(!isDev()) {
//			if(anticheat == null) {
//				Bukkit.getLogger().severe("No anticheat found! Shutting down...");
//				Bukkit.getPluginManager().disablePlugin(this);
//				return;
//			} else getServer().getPluginManager().registerEvents(anticheat, this);
//		}

		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		adventure = BukkitAudiences.create(this);
		if(getStatus().isDarkzone()) TaintedWell.onStart();
		if(getStatus().isDarkzone()) AltarManager.init();
		if(getStatus().isDarkzone()) SpawnBlocker.init();
		if(getStatus().isDarkzone()) BrewingManager.onStart();
		ScoreboardManager.init();

		RegisteredServiceProvider<LuckPerms> luckpermsProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if(luckpermsProvider != null) LUCKPERMS = luckpermsProvider.getProvider();

		PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();
		new BiomeChanger(this);

		new BukkitRunnable() {
			@Override
			public void run() {
				List<NPC> toRemove = new ArrayList<>();
				for(NPC npc : CitizensAPI.getNPCRegistry()) {
					toRemove.add(npc);
				}
				while(!toRemove.isEmpty()) {
					toRemove.get(0).destroy();
					toRemove.remove(0);
				}
			}
		}.runTaskLater(PitSim.INSTANCE, 10);

		if(status.isOverworld()) registerMaps();

		if(getStatus().isOverworld()) NonManager.init();
		SignPrompt.registerSignUpdateListener();
		ReloadManager.init();

		if(!Bukkit.getServer().getPluginManager().getPlugin("NoteBlockAPI").getDescription().getVersion().toLowerCase().contains("kyro")) {
			AOutput.log("Wrong version of NoteBlockAPI found");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

//		Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
//		EntityDamageEvent.getHandlerList().unregister(essentials);

		Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
		BlockIgniteEvent.getHandlerList().unregister(worldGuard);
		PotionSplashEvent.getHandlerList().unregister(worldGuard);

		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
		} else {
			AOutput.log(String.format("Could not find PlaceholderAPI! This plugin is required."));
			Bukkit.getPluginManager().disablePlugin(this);
		}

		if(!Bukkit.getPluginManager().isPluginEnabled("NoteBlockAPI")) {
			getLogger().severe("*** NoteBlockAPI is not installed or not enabled. ***");
			return;
		}

		QueryMessenger messenger = PluginQuery.getMessenger();
		messenger.getEventBus().registerListener(new PluginMessageManager());

		registerBoosters();
		registerUpgrades();
		registerPerks();
		registerKillstreaks();
		registerMegastreaks();
		registerPassQuests();
		if(getStatus().isOverworld()) registerLeaderboards();
		if(getStatus().isOverworld()) LeaderboardManager.init();

		ArcticAPI.setupPlaceholderAPI("pitsim");
		AHook.registerPlaceholder(new PrefixPlaceholder());
		AHook.registerPlaceholder(new SuffixPlaceholder());
		AHook.registerPlaceholder(new StrengthChainingPlaceholder());
		AHook.registerPlaceholder(new GladiatorPlaceholder());
		AHook.registerPlaceholder(new CombatTimerPlaceholder());
		AHook.registerPlaceholder(new StreakPlaceholder());
		AHook.registerPlaceholder(new ExperiencePlaceholder());
		AHook.registerPlaceholder(new LevelPlaceholder());
		AHook.registerPlaceholder(new PrestigeLevelPlaceholder());
		AHook.registerPlaceholder(new PrestigePlaceholder());
		AHook.registerPlaceholder(new GoldReqPlaceholder());
		AHook.registerPlaceholder(new SoulsPlaceholder());
		AHook.registerPlaceholder(new PlayerCountPlaceholder());
		AHook.registerPlaceholder(new GoldPlaceholder());
		AHook.registerPlaceholder(new NicknamePlaceholder());
		AHook.registerPlaceholder(new ServerIPPlaceholder());
		AHook.registerPlaceholder(new CustomScoreboardPlaceholder());

		CooldownManager.init();

		registerEnchants();
		registerItems();
		registerCommands();
		registerListeners();
		registerHelmetAbilities();
		registerKits();
		registerCosmetics();
		registerScoreboardOptions();
		registerNPCCheckpoints();

		PassManager.registerPasses();
		HelpManager.registerIntentsAndPages();
		if(getStatus().isDarkzone()) AuctionManager.onStart();
		if(getStatus().isDarkzone()) AuctionDisplays.onStart();

		new BukkitRunnable() {
			@Override
			public void run() {
				ProxyMessaging.sendStartup();
			}
		}.runTaskLater(this, 20 * 10);

		new BukkitRunnable() {
			@Override
			public void run() {
				registerNPCs();
			}
		}.runTaskLater(PitSim.INSTANCE, 20);
	}

	@Override
	public void onDisable() {
		StatisticsManager.getDataChunk().send();
		FirestoreManager.CONFIG.save();

		if(status.isDarkzone()) {
			TaintedWell.onStop();
			AuctionDisplays.onDisable();
		}

		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			PitPlayer pitPlayer = PitPlayer.getPitPlayer(onlinePlayer);

			pitPlayer.overworldTutorial.endTutorial();
			pitPlayer.darkzoneTutorial.endTutorial();


//			disable cosmetics
			if(!VanishAPI.isInvisible(onlinePlayer)) {
				List<PitCosmetic> activeCosmetics = CosmeticManager.getEquippedCosmetics(pitPlayer);
				for(PitCosmetic activeCosmetic : activeCosmetics) activeCosmetic.disable(pitPlayer);
			}

			pitPlayer.save(true, true);
		}

		for(World world : Bukkit.getWorlds()) {
			for(Entity entity : new ArrayList<>(world.getEntities())) {
				if(!(entity instanceof Item)) continue;
				ItemStack itemStack = ((Item) entity).getItemStack();
				NBTItem nbtItem = new NBTItem(itemStack);
				if(nbtItem.hasKey(NBTTag.CANNOT_PICKUP.getRef())) entity.remove();
			}
		}

		if(MapManager.getDarkzone() != null) {
			for(Entity entity : MapManager.getDarkzone().getEntities()) {
				if(entity instanceof Item) {
					entity.remove();
				}
			}
		}

		if(getStatus().isDarkzone()) {
			for(NPC clickable : AuctionDisplays.clickables) {
				clickable.destroy();
				NPCRegistry registry = CitizensAPI.getNPCRegistry();
				registry.deregister(clickable);
			}
		}

		for(EditSession session : FreezeSpell.sessionMap.keySet()) {
			session.undo(session);
		}

		try {
			for(List<PacketBlock> blocks : CageAbility.packetBlockMap.values()) {
				for(PacketBlock block : blocks) {
					block.removeBlock();
				}
			}
		} catch(NoClassDefFoundError ignored) { }

		for(Map.Entry<Location, Material> entry : FreezeSpell.blockMap.entrySet()) {
			entry.getKey().getBlock().setType(entry.getValue());
		}

		if(status.isDarkzone()) {
			for(SubLevel subLevel : DarkzoneManager.subLevels) subLevel.removeMobs();
			for(PitBoss pitBoss : new ArrayList<>(BossManager.pitBosses)) pitBoss.remove();
		}

		if(adventure != null) {
			adventure.close();
			adventure = null;
		}

		NPCManager.onDisable();
		List<Non> copyList = new ArrayList<>(NonManager.nons);
		for(Non non : copyList) {
			non.remove();
		}

		for(PitEnchant pitEnchant : EnchantManager.pitEnchants) pitEnchant.onDisable();

		Iterator<Map.Entry<Player, EntitySongPlayer>> it = StereoManager.playerMusic.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry<Player, EntitySongPlayer> pair = it.next();
			EntitySongPlayer esp = pair.getValue();
			esp.destroy();
			it.remove();
		}

		File file = new File("plugins/Citizens/saves.yml");
		if(file.exists()) file.deleteOnExit();
	}

	private void registerMaps() {
		PitMap pitMap = null;
		long time;

		PitMap biomes = MapManager.registerMap(new BiomesMap("biomes", 7));
		PitMap sand = MapManager.registerMap(new SandMap("sand", 3));
		PitMap dimensions = MapManager.registerMap(new DimensionsMap("dimensions", 7));
		PitMap xmas = MapManager.registerMap(new XmasMap("xmas", -1));

		String configString = FirestoreManager.CONFIG.mapData;
		String mapName = null;
		if(configString == null || configString.isEmpty()) {
			pitMap = biomes;
			time = System.currentTimeMillis();
		} else {
			String[] split = configString.split(":");
			mapName = split[0];
			time = Long.parseLong(split[1]);
			PitMap currentMap = MapManager.getMap(mapName);
			if(currentMap == null) currentMap = biomes;
			pitMap = currentMap;

			if(((System.currentTimeMillis() - time) / 1000.0 / 60.0 / 60.0 / 24.0) >= currentMap.rotationDays) {
				pitMap = MapManager.getNextMap(currentMap);
				time = System.currentTimeMillis();
			}
		}

		if(TimeManager.isEventActive(PitCalendarEvent.CHRISTMAS_SEASON) && status.isOverworld()) {
			pitMap = xmas;
			time = System.currentTimeMillis();
			MapManager.currentMap.world.setStorm(true);
			MapManager.currentMap.world.setWeatherDuration(Integer.MAX_VALUE);
		}

		if(mapName == null || !Objects.equals(pitMap.world.getName(), mapName)) {
			FirestoreManager.CONFIG.mapData = pitMap.world.getName() + ":" + time;
			FirestoreManager.CONFIG.save();
		}
		MapManager.setMap(pitMap);
	}

	private void registerPerks() {
		PerkManager.registerUpgrade(new NoPerk());

		PerkManager.registerUpgrade(new Vampire());
		PerkManager.registerUpgrade(new Dirty());
		PerkManager.registerUpgrade(new StrengthChaining());
		PerkManager.registerUpgrade(new Gladiator());
		PerkManager.registerUpgrade(new Thick());
		PerkManager.registerUpgrade(new JewelHunter());
		PerkManager.registerUpgrade(new Dispersion());
		PerkManager.registerUpgrade(new Streaker());
		PerkManager.registerUpgrade(new FirstStrike());
		PerkManager.registerUpgrade(new CounterJanitor());
	}

	private void registerKillstreaks() {
		PerkManager.registerKillstreak(new NoKillstreak());

		PerkManager.registerKillstreak(new Limiter());
		PerkManager.registerKillstreak(new Explicious());
		PerkManager.registerKillstreak(new AssuredStrike());
		PerkManager.registerKillstreak(new Leech());

		PerkManager.registerKillstreak(new RAndR());
		PerkManager.registerKillstreak(new FightOrFlight());
		PerkManager.registerKillstreak(new HerosHaste());
		PerkManager.registerKillstreak(new CounterStrike());

		PerkManager.registerKillstreak(new Survivor());
		PerkManager.registerKillstreak(new AuraOfProtection());
		PerkManager.registerKillstreak(new GoldNanoFactory());
		PerkManager.registerKillstreak(new Baker());

		PerkManager.registerKillstreak(new Monster());
		PerkManager.registerKillstreak(new Spongesteve());
		PerkManager.registerKillstreak(new GoldStack());
		PerkManager.registerKillstreak(new Shockwave());
	}

	private void registerMegastreaks() {
		PerkManager.registerMegastreak(new NoMegastreak());
		PerkManager.registerMegastreak(new Overdrive());
//		PerkManager.registerMegastreak(new StashStreaker());
		PerkManager.registerMegastreak(new Rampage());
		PerkManager.registerMegastreak(new HighStakes());
		PerkManager.registerMegastreak(new Beastmode());
		PerkManager.registerMegastreak(new Highlander());
		PerkManager.registerMegastreak(new Uberstreak());
		PerkManager.registerMegastreak(new ToTheMoon());
		PerkManager.registerMegastreak(new Prosperity());
		PerkManager.registerMegastreak(new Apostle());
		PerkManager.registerMegastreak(new RNGesus());
	}

	private void registerLeaderboards() {
		LeaderboardManager.registerLeaderboard(new XPLeaderboard());
		LeaderboardManager.registerLeaderboard(new GoldLeaderboard());
		LeaderboardManager.registerLeaderboard(new GoldGrindedLeaderboard());
//		LeaderboardManager.registerLeaderboard(new PlayerKillsLeaderboard());
		LeaderboardManager.registerLeaderboard(new BotKillsLeaderboard());
		LeaderboardManager.registerLeaderboard(new PlaytimeLeaderboard());
		LeaderboardManager.registerLeaderboard(new UbersCompletedLeaderboard());
		LeaderboardManager.registerLeaderboard(new JewelsCompletedLeaderboard());
		LeaderboardManager.registerLeaderboard(new FeathersLostLeaderboard());
		LeaderboardManager.registerLeaderboard(new BossesKilledLeaderboard());
		LeaderboardManager.registerLeaderboard(new LifetimeSoulsLeaderboard());
		LeaderboardManager.registerLeaderboard(new AuctionsWonLeaderboard());
		LeaderboardManager.registerLeaderboard(new HighestBidLeaderboard());
	}

	private void registerScoreboardOptions() {
		ScoreboardManager.registerScoreboard(new StrengthScoreboard());
		ScoreboardManager.registerScoreboard(new GladiatorScoreboard());
		ScoreboardManager.registerScoreboard(new TelebowScoreboard());
//		ScoreboardManager.registerScoreboard(new BulletTimeScoreboard());
		ScoreboardManager.registerScoreboard(new ReallyToxicScoreboard());
		ScoreboardManager.registerScoreboard(new JudgementScoreboard());
		ScoreboardManager.registerScoreboard(new AuctionScoreboard());
	}

	private void registerNPCs() {
		if(status.isDarkzone()) {
			NPCManager.registerNPC(new TaintedShopNPC(Collections.singletonList(MapManager.getDarkzone())));
			NPCManager.registerNPC(new PotionMasterNPC(Collections.singletonList(MapManager.getDarkzone())));
			NPCManager.registerNPC(new AuctioneerNPC(Collections.singletonList(MapManager.getDarkzone())));
			NPCManager.registerNPC(new MainProgressionNPC(Collections.singletonList(MapManager.getDarkzone())));
			NPCManager.registerNPC(new FastTravelNPC(Collections.singletonList(MapManager.getDarkzone())));
			NPCManager.registerNPC(new PlayerMarketNPC(Collections.singletonList(MapManager.getDarkzone())));
		}

		if(status.isOverworld()) {
			NPCManager.registerNPC(new PerkNPC(Collections.singletonList(MapManager.currentMap.world)));
			NPCManager.registerNPC(new PassNPC(Collections.singletonList(MapManager.currentMap.world)));
			NPCManager.registerNPC(new PrestigeNPC(Collections.singletonList(MapManager.currentMap.world)));
			NPCManager.registerNPC(new KeeperNPC(Collections.singletonList(MapManager.currentMap.world)));
			NPCManager.registerNPC(new KitNPC(Collections.singletonList(MapManager.currentMap.world)));
			NPCManager.registerNPC(new StatsNPC(Collections.singletonList(MapManager.currentMap.world)));

			NPCManager.registerNPC(new KyroNPC(Collections.singletonList(MapManager.currentMap.world)));
			NPCManager.registerNPC(new WijiNPC(Collections.singletonList(MapManager.currentMap.world)));
			NPCManager.registerNPC(new SammymonNPC(Collections.singletonList(MapManager.currentMap.world)));
			NPCManager.registerNPC(new ReyerticNPC(Collections.singletonList(MapManager.currentMap.world)));
		}
	}

	private void registerCommands() {
		AMultiCommand adminCommand = new BaseAdminCommand("pitsim");
		getCommand("ps").setExecutor(adminCommand);
		AMultiCommand giveCommand = new BaseSetCommand(adminCommand, "give");
		AMultiCommand setCommand = new BaseSetCommand(adminCommand, "set");

		HopperCommand hopperCommand = new HopperCommand(adminCommand, "hopper");
		new UUIDCommand(adminCommand, "uuid");
		new NBTCommand(adminCommand, "nbt");
		new RandomizeCommand(adminCommand, "randomize");
		new ReloadCommand(adminCommand, "reload");
		new BypassCommand(adminCommand, "bypass");
		new ExtendCommand(adminCommand, "extend");
		new UnlockCosmeticCommand(adminCommand, "unlockcosmetic");
		new GodCommand(adminCommand, "god");
		new KyroCommand(adminCommand, "kyro");

		new JewelCommand(giveCommand, "jewel");
		new StreakCommand(giveCommand, "streak");
		new BountyCommand(giveCommand, "bounty");
		new RNGCommand(giveCommand, "rng");

		AMultiCommand betaCommand = new BaseBetaCommand("beta");
		new SoulsCommand(betaCommand, "souls");
		new RenownCommand(betaCommand, "renown");
		new ResetCommand(betaCommand, "reset");
		new GodCommand(betaCommand, "god");
		new SkillsCommand(betaCommand, "skills");
		new AltarCommand(betaCommand, "altar");
		new PrestigeCommand(betaCommand, "prestige");
		new OverflowCommand(betaCommand, "overflow");
		new LevelCommand(betaCommand, "level");
		new ApostleCommand(betaCommand, "apostle");
		new MassEnchantCommand(betaCommand, "me");
		new FastTravelCommand(betaCommand, "ft");
		new FreezeCommand(betaCommand, "freeze");

		getCommand("atest").setExecutor(new ATestCommand());
		getCommand("ktest").setExecutor(new KTestCommand());

		getCommand("fps").setExecutor(new FPSCommand());
		getCommand("oof").setExecutor(new OofCommand());
		getCommand("perks").setExecutor(new PerksCommand());
		getCommand("non").setExecutor(new NonCommand());
		getCommand("enchant").setExecutor(new EnchantCommand());
		getCommand("fresh").setExecutor(new FreshCommand());
		getCommand("show").setExecutor(new ShowCommand());
		getCommand("enchants").setExecutor(new EnchantListCommand());
		getCommand("donator").setExecutor(new DonatorCommand());
		getCommand("renown").setExecutor(new RenownShopCommand());
		getCommand("spawn").setExecutor(new SpawnCommand());
		getCommand("reward").setExecutor(new RewardCommand());
		getCommand("store").setExecutor(new StoreCommand());
		getCommand("shop").setExecutor(new StoreCommand());
		getCommand("discord").setExecutor(new DiscordCommand());
		getCommand("disc").setExecutor(new DiscordCommand());
		getCommand("booster").setExecutor(new BoosterCommand());
		getCommand("boostergive").setExecutor(new BoosterGiveCommand());
		getCommand("resource").setExecutor(new ResourceCommand());
		getCommand("lightning").setExecutor(new LightningCommand());
		getCommand("stat").setExecutor(new StatCommand());
//		getCommand("captcha").setExecutor(new CaptchaCommand());
		getCommand("pay").setExecutor(new PayCommand());
		getCommand("kit").setExecutor(new KitCommand());
		getCommand("music").setExecutor(new MusicCommand());
//		getCommand("migrate").setExecutor(new MigrateCommand());
		getCommand("pass").setExecutor(new PassCommand());
		getCommand("quests").setExecutor(new QuestsCommand());
		SettingsCommand settingsCommand = new SettingsCommand();
		getCommand("settings").setExecutor(settingsCommand);
		getCommand("setting").setExecutor(settingsCommand);
		getCommand("set").setExecutor(settingsCommand);
		getCommand("potions").setExecutor(new PotionsCommand());
		getCommand("balance").setExecutor(new BalanceCommand());
		getCommand("eco").setExecutor(new EcoCommand());
		getCommand("ignore").setExecutor(new IgnoreCommand());
		getCommand("ignore").setTabCompleter(new IgnoreCommand());
		getCommand("cookie").setExecutor(new StaffCookieCommand());
		getCommand("loadskin").setExecutor(new LoadSkinCommand());
		getCommand("ineeddata").setExecutor(new ChatTriggerSubscribeCommand());
		getCommand("givemedata").setExecutor(new ChatTriggerDataCommand());
		getCommand("claim").setExecutor(new ClaimCommand());
		getCommand("enderchest").setExecutor(new EnderchestCommand());
		getCommand("wardrobe").setExecutor(new WardrobeCommand());
		//TODO: Remove this
//		getCommand("massmigrate").setExecutor(new MassMigrateCommand());

		getCommand("gamemode").setExecutor(new GamemodeCommand());
		getCommand("nickname").setExecutor(new NicknameCommand());
		getCommand("fly").setExecutor(new FlyCommand());
		getCommand("fly").setTabCompleter(new FlyCommand());
		getCommand("teleport").setExecutor(new TeleportCommand());
		getCommand("teleporthere").setExecutor(new TeleportHereCommand());
		getCommand("broadcast").setExecutor(new BroadcastCommand());
		getCommand("trash").setExecutor(new TrashCommand());
		getCommand("rename").setExecutor(new RenameCommand());
		getCommand("hopper").setExecutor(hopperCommand);
	}

	private void registerListeners() {

		getServer().getPluginManager().registerEvents(new DamageManager(), this);
		getServer().getPluginManager().registerEvents(new PlayerManager(), this);
		getServer().getPluginManager().registerEvents(new EnchantManager(), this);
		getServer().getPluginManager().registerEvents(new PlayerDataManager(), this);
		getServer().getPluginManager().registerEvents(new ChatManager(), this);
		getServer().getPluginManager().registerEvents(new DamageIndicator(), this);
		getServer().getPluginManager().registerEvents(new ItemManager(), this);
		getServer().getPluginManager().registerEvents(new CombatManager(), this);
		getServer().getPluginManager().registerEvents(new SpawnManager(), this);
		getServer().getPluginManager().registerEvents(new ItemRename(), this);
		getServer().getPluginManager().registerEvents(new AFKManager(), this);
		getServer().getPluginManager().registerEvents(new TotallyLegitGem(), this);
		getServer().getPluginManager().registerEvents(new BlobManager(), this);
		getServer().getPluginManager().registerEvents(new BoosterManager(), this);
		getServer().getPluginManager().registerEvents(new HopperManager(), this);
		getServer().getPluginManager().registerEvents(new ResourcePackManager(), this);
		getServer().getPluginManager().registerEvents(new StatManager(), this);
		getServer().getPluginManager().registerEvents(new HelmetManager(), this);
		getServer().getPluginManager().registerEvents(new MapManager(), this);
		getServer().getPluginManager().registerEvents(new GuildIntegrationManager(), this);
		getServer().getPluginManager().registerEvents(new UpgradeManager(), this);
		getServer().getPluginManager().registerEvents(new KitManager(), this);
		getServer().getPluginManager().registerEvents(new PortalManager(), this);
		getServer().getPluginManager().registerEvents(new PotionManager(), this);
		getServer().getPluginManager().registerEvents(new TaintedManager(), this);
		getServer().getPluginManager().registerEvents(new StereoManager(), this);
		getServer().getPluginManager().registerEvents(new ScoreboardManager(), this);
		getServer().getPluginManager().registerEvents(new ProxyMessaging(), this);
		getServer().getPluginManager().registerEvents(new LobbySwitchManager(), this);
		getServer().getPluginManager().registerEvents(new PassManager(), this);
		getServer().getPluginManager().registerEvents(new SkinManager(), this);
		getServer().getPluginManager().registerEvents(new TimeManager(), this);
		getServer().getPluginManager().registerEvents(new NPCManager(), this);
		getServer().getPluginManager().registerEvents(new CosmeticManager(), this);
		getServer().getPluginManager().registerEvents(new LogManager(), this);
		getServer().getPluginManager().registerEvents(new StorageManager(), this);
		getServer().getPluginManager().registerEvents(new CrossServerMessageManager(), this);
		getServer().getPluginManager().registerEvents(new PacketManager(), this);
		getServer().getPluginManager().registerEvents(new GrimManager(), this);
		getServer().getPluginManager().registerEvents(new MiscManager(), this);
		getServer().getPluginManager().registerEvents(new FirstJoinManager(), this);
		getServer().getPluginManager().registerEvents(new ChatTriggerManager(), this);
		getServer().getPluginManager().registerEvents(new AuthenticationManager(), this);
		getServer().getPluginManager().registerEvents(new DiscordManager(), this);
//		getServer().getPluginManager().registerEvents(new AIManager(), this);
		getServer().getPluginManager().registerEvents(new MarketMessaging(), this);
		getServer().getPluginManager().registerEvents(new MigrationManager(), this);
		getServer().getPluginManager().registerEvents(new ActionBarManager(), this);
		getServer().getPluginManager().registerEvents(new HelpManager(), this);
		getServer().getPluginManager().registerEvents(new VoucherManager(), this);
		getServer().getPluginManager().registerEvents(new TutorialManager(), this);
		getServer().getPluginManager().registerEvents(new CustomEventManager(), this);
		getServer().getPluginManager().registerEvents(new HologramManager(), this);
		getServer().getPluginManager().registerEvents(new AuctionManager(), this);
		getServer().getPluginManager().registerEvents(new OutpostManager(), this);
		getServer().getPluginManager().registerEvents(new ProgressionManager(), this);
		if(!PitSim.isDev()) getServer().getPluginManager().registerEvents(new StatisticsManager(), this);

		if(getStatus().isDarkzone()) {
			getServer().getPluginManager().registerEvents(new TaintedWell(), this);
			getServer().getPluginManager().registerEvents(new BrewingManager(), this);
			getServer().getPluginManager().registerEvents(new MusicManager(), this);
			getServer().getPluginManager().registerEvents(new AuctionDisplays(), this);

			getServer().getPluginManager().registerEvents(new DarkzoneManager(), this);
			getServer().getPluginManager().registerEvents(new BossManager(), this);
			getServer().getPluginManager().registerEvents(new ShieldManager(), this);
			getServer().getPluginManager().registerEvents(new AltarManager(), this);
		}
	}

	public void registerBoosters() {
		BoosterManager.registerBooster(new XPBooster());
		BoosterManager.registerBooster(new GoldBooster());
		BoosterManager.registerBooster(new PvPBooster());
		BoosterManager.registerBooster(new ChaosBooster());
		BoosterManager.registerBooster(new SoulBooster());
	}

	public void registerUpgrades() {
		UpgradeManager.registerUpgrade(new Tenacity());
		UpgradeManager.registerUpgrade(new RenownGoldBoost());
		UpgradeManager.registerUpgrade(new RenownXPBoost());
		UpgradeManager.registerUpgrade(new LuckyKill());
		UpgradeManager.registerUpgrade(new Impatient());
		UpgradeManager.registerUpgrade(new UnlockStreaker());
		UpgradeManager.registerUpgrade(new DoubleDeath());
		UpgradeManager.registerUpgrade(new UnlockFirstStrike());
		UpgradeManager.registerUpgrade(new UnlockCounterJanitor());
		UpgradeManager.registerUpgrade(new UberInsurance());
		UpgradeManager.registerUpgrade(new Helmetry());
		UpgradeManager.registerUpgrade(new DivineIntervention());
		UpgradeManager.registerUpgrade(new Withercraft());
		UpgradeManager.registerUpgrade(new TaxEvasion());
		UpgradeManager.registerUpgrade(new XPComplex());
		UpgradeManager.registerUpgrade(new VentureCapitalist());
		UpgradeManager.registerUpgrade(new KillSteal());
		UpgradeManager.registerUpgrade(new ShardHunter());
		UpgradeManager.registerUpgrade(new TheWay());
		UpgradeManager.registerUpgrade(new FastPass());
		UpgradeManager.registerUpgrade(new Celebrity());
		UpgradeManager.registerUpgrade(new BreadDealer());
		UpgradeManager.registerUpgrade(new HandOfGreed());
	}

	private void registerHelmetAbilities() {
		HelmetAbility.registerHelmetAbility(new LeapAbility(null));
		HelmetAbility.registerHelmetAbility(new BlobAbility(null));
		HelmetAbility.registerHelmetAbility(new GoldRushAbility(null));
		HelmetAbility.registerHelmetAbility(new HermitAbility(null));
		HelmetAbility.registerHelmetAbility(new JudgementAbility(null));
		HelmetAbility.registerHelmetAbility(new PhoenixAbility(null));
	}

	private void registerKits() {
		KitManager.registerKit(new EssentialKit());
		KitManager.registerKit(new XPKit());
		KitManager.registerKit(new GoldKit());
		KitManager.registerKit(new PvPKit());
	}

	private void registerPassQuests() {
//		Daily quests
		PassManager.registerQuest(new DailyBotKillQuest());
		PassManager.registerQuest(new DailyPlayerKillQuest());
		PassManager.registerQuest(new DailySWGamePlayedQuest());
		PassManager.registerQuest(new DailyMegastreakQuest());

//		Weekly quests
		PassManager.registerQuest(new KillPlayersQuest());
		PassManager.registerQuest(new CompleteUbersQuest());
		PassManager.registerQuest(new DoTrueDamageVSBotsQuest());
		PassManager.registerQuest(new DoTrueDamageVSPlayersQuest());
		PassManager.registerQuest(new ReachKillstreakQuest());
		PassManager.registerQuest(new GrindXPQuest());
		PassManager.registerQuest(new GrindGoldQuest());
		PassManager.registerQuest(new HoursPlayedQuest());
		PassManager.registerQuest(new AttackBotsWithHealerQuest());
		PassManager.registerQuest(new LandMLBShotsQuest());
		PassManager.registerQuest(new UseHelmetGoldQuest());
		PassManager.registerQuest(new WinAuctionsQuest());
		PassManager.registerQuest(new EarnGuildReputationQuest());
		PassManager.registerQuest(new EarnRenownQuest());
		PassManager.registerQuest(new PunchUniquePlayers());
		PassManager.registerQuest(new GainAbsorptionQuest());
		PassManager.registerQuest(new SneakingBotKillQuest());
		PassManager.registerQuest(new WalkDistanceQuest());
		PassManager.registerQuest(new CongratulatePrestigeQuest());
		PassManager.registerQuest(new HaveSpeedQuest());
		PassManager.registerQuest(new JudgementHopperQuest());
		PassManager.registerQuest(new BrewPotionsQuest());

		PassManager.registerQuest(new KillZombiesQuest());
		PassManager.registerQuest(new KillSkeletonsQuest());
		PassManager.registerQuest(new KillSpidersQuest());
		PassManager.registerQuest(new KillCreepersQuest());
		PassManager.registerQuest(new KillWolvesQuest());
		PassManager.registerQuest(new KillBlazesQuest());
		PassManager.registerQuest(new KillZombiePigmenQuest());
		PassManager.registerQuest(new KillWitherSkeletonsQuest());
		PassManager.registerQuest(new KillIronGolemsQuest());
		PassManager.registerQuest(new KillEndermenQuest());
	}

	private void registerCosmetics() {
		CosmeticManager.registerCosmetic(new AlwaysExe());
		CosmeticManager.registerCosmetic(new OnlyExe());
		CosmeticManager.registerCosmetic(new Tetris());
		CosmeticManager.registerCosmetic(new IronKill());

		CosmeticManager.registerCosmetic(new DeathHowl());
		CosmeticManager.registerCosmetic(new DeathScream());
		CosmeticManager.registerCosmetic(new SuperMario());
		CosmeticManager.registerCosmetic(new DeathExplosion());
		CosmeticManager.registerCosmetic(new DeathFirework());

		CosmeticManager.registerCosmetic(new BountyBlueShell());
		CosmeticManager.registerCosmetic(new BountyBully());
		CosmeticManager.registerCosmetic(new BountyCope());
		CosmeticManager.registerCosmetic(new BountyQuickDropped());
		CosmeticManager.registerCosmetic(new BountyEmbarrassed());
		CosmeticManager.registerCosmetic(new BountyForgotToPay());
		CosmeticManager.registerCosmetic(new BountyHunted());
		CosmeticManager.registerCosmetic(new BountyPacking());
		CosmeticManager.registerCosmetic(new BountyRailed());
		CosmeticManager.registerCosmetic(new BountyRatted());
		CosmeticManager.registerCosmetic(new BountyReaper());
		CosmeticManager.registerCosmetic(new BountyRobbery());
		CosmeticManager.registerCosmetic(new BountySuffocated());
		CosmeticManager.registerCosmetic(new BountySystemMalfunction());
		CosmeticManager.registerCosmetic(new BountyTakeTheL());

		CosmeticManager.registerCosmetic(new SolidCape());
		CosmeticManager.registerCosmetic(new FireCape());
		CosmeticManager.registerCosmetic(new MagicCape());
		CosmeticManager.registerCosmetic(new CritCape());
		CosmeticManager.registerCosmetic(new CritMagicCape());

		CosmeticManager.registerCosmetic(new SmokeTrail());
		CosmeticManager.registerCosmetic(new FootstepTrail());
		CosmeticManager.registerCosmetic(new IceTrail());
		CosmeticManager.registerCosmetic(new RainbowTrail());
		CosmeticManager.registerCosmetic(new CoalTrail());
		CosmeticManager.registerCosmetic(new IronTrail());
		CosmeticManager.registerCosmetic(new RedstoneTrail());
		CosmeticManager.registerCosmetic(new LapisTrail());
		CosmeticManager.registerCosmetic(new DiamondTrail());
		CosmeticManager.registerCosmetic(new EmeraldTrail());
		CosmeticManager.registerCosmetic(new SlimeTrail());
		CosmeticManager.registerCosmetic(new LavaTrail());

		CosmeticManager.registerCosmetic(new KyroAura());
		CosmeticManager.registerCosmetic(new KyroAura2());
		CosmeticManager.registerCosmetic(new LivelyAura());
		CosmeticManager.registerCosmetic(new PotionAura());
		CosmeticManager.registerCosmetic(new WaterAura());
		CosmeticManager.registerCosmetic(new FireAura());
		CosmeticManager.registerCosmetic(new MysticAura());
		CosmeticManager.registerCosmetic(new FireworkAura());

		CosmeticManager.registerCosmetic(new KyroCosmetic());
		CosmeticManager.registerCosmetic(new MysticPresence());
		CosmeticManager.registerCosmetic(new ElectricPresence());
		CosmeticManager.registerCosmetic(new Halo());

		CosmeticManager.loadForOnlinePlayers();
	}

	private void registerItems() {
		ItemFactory.registerItem(new MysticSword());
		ItemFactory.registerItem(new MysticBow());
		ItemFactory.registerItem(new MysticPants());
		ItemFactory.registerItem(new TaintedScythe());
		ItemFactory.registerItem(new TaintedChestplate());

		ItemFactory.registerItem(new FunkyFeather());
		ItemFactory.registerItem(new CorruptedFeather());
		ItemFactory.registerItem(new ChunkOfVile());
		ItemFactory.registerItem(new TotallyLegitGem());
		ItemFactory.registerItem(new AncientGemShard());
		ItemFactory.registerItem(new YummyBread());
		ItemFactory.registerItem(new VeryYummyBread());
		ItemFactory.registerItem(new Arrow());

		ItemFactory.registerItem(new SoulPickup());
		ItemFactory.registerItem(new GoldPickup());
		ItemFactory.registerItem(new GoldenHelmet());
		ItemFactory.registerItem(new StaffCookie());
		ItemFactory.registerItem(new TokenOfAppreciation());
		ItemFactory.registerItem(new TheCakeIsALie());


		ItemFactory.registerItem(new DiamondHelmet());
		ItemFactory.registerItem(new DiamondChestplate());
		ItemFactory.registerItem(new DiamondLeggings());
		ItemFactory.registerItem(new DiamondBoots());
		ItemFactory.registerItem(new ProtHelmet());
		ItemFactory.registerItem(new ProtChestplate());
		ItemFactory.registerItem(new ProtLeggings());
		ItemFactory.registerItem(new ProtBoots());

		ItemFactory.registerItem(new RottenFlesh());
		ItemFactory.registerItem(new Bone());
		ItemFactory.registerItem(new SpiderEye());
		ItemFactory.registerItem(new Gunpowder());
		ItemFactory.registerItem(new BlazeRod());
		ItemFactory.registerItem(new Leather());
		ItemFactory.registerItem(new RawPork());
		ItemFactory.registerItem(new Charcoal());
		ItemFactory.registerItem(new IronIngot());
		ItemFactory.registerItem(new EnderPearl());

		ItemFactory.registerItem(new Potion());
	}

	public static void registerNPCCheckpoints() {
		new TaintedWellCheckpoint();
		new ProgressionCheckpoint();
		new AltarCheckpoint();
		new BrewingCheckpoint();
		new MarketShopCheckpoint();
		new CaveCheckpoint();
	}

	private void loadConfig() {
		saveDefaultConfig();
	}

	@Override
	public void onLoad() {
		File file = new File("plugins/Citizens/save.yml");
		if(file.exists()) file.delete();
	}

	private void registerEnchants() {
		EnchantManager.registerEnchant(new ComboVenom());
//		EnchantManager.registerEnchant(new aCPLEnchant());
		EnchantManager.registerEnchant(new SelfCheckout());

		EnchantManager.registerEnchant(new Billionaire());
		EnchantManager.registerEnchant(new ComboPerun());
		EnchantManager.registerEnchant(new Executioner());
		EnchantManager.registerEnchant(new Gamble());
		EnchantManager.registerEnchant(new ComboStun());
		EnchantManager.registerEnchant(new SpeedyHit());
		EnchantManager.registerEnchant(new Healer());
		EnchantManager.registerEnchant(new Lifesteal());
		EnchantManager.registerEnchant(new ComboHeal());

		EnchantManager.registerEnchant(new Shark());
		EnchantManager.registerEnchant(new PainFocus());
		EnchantManager.registerEnchant(new DiamondStomp());
		EnchantManager.registerEnchant(new ComboDamage());
		EnchantManager.registerEnchant(new Berserker());
		EnchantManager.registerEnchant(new KingBuster());
		EnchantManager.registerEnchant(new Sharp());
		EnchantManager.registerEnchant(new Punisher());
		EnchantManager.registerEnchant(new BeatTheSpammers());
		EnchantManager.registerEnchant(new GoldAndBoosted());

		EnchantManager.registerEnchant(new ComboSwift());
		EnchantManager.registerEnchant(new BulletTime());
		EnchantManager.registerEnchant(new Guts());
		EnchantManager.registerEnchant(new Crush());

		EnchantManager.registerEnchant(new MegaLongBow());
		EnchantManager.registerEnchant(new Robinhood());
		EnchantManager.registerEnchant(new Volley());
		EnchantManager.registerEnchant(new Telebow());
		EnchantManager.registerEnchant(new Pullbow());
		EnchantManager.registerEnchant(new Explosive());
		EnchantManager.registerEnchant(new TrueShot());
		EnchantManager.registerEnchant(new LuckyShot());

		EnchantManager.registerEnchant(new SprintDrain());
		EnchantManager.registerEnchant(new Wasp());
		EnchantManager.registerEnchant(new PinDown());
		EnchantManager.registerEnchant(new FasterThanTheirShadow());
		EnchantManager.registerEnchant(new PushComesToShove());
		EnchantManager.registerEnchant(new Parasite());
		EnchantManager.registerEnchant(new Chipping());
		EnchantManager.registerEnchant(new Fletching());
		EnchantManager.registerEnchant(new Sniper());
		EnchantManager.registerEnchant(new SpammerAndProud());
		EnchantManager.registerEnchant(new Jumpspammer());

		EnchantManager.registerEnchant(new RetroGravityMicrocosm());
		EnchantManager.registerEnchant(new Regularity());
		EnchantManager.registerEnchant(new Solitude());
		EnchantManager.registerEnchant(new Singularity());

		EnchantManager.registerEnchant(new Mirror());
		EnchantManager.registerEnchant(new Sufferance());
		EnchantManager.registerEnchant(new CriticallyFunky());
		EnchantManager.registerEnchant(new FractionalReserve());
		EnchantManager.registerEnchant(new NotGladiator());
		EnchantManager.registerEnchant(new Protection());
		EnchantManager.registerEnchant(new RingArmor());

		EnchantManager.registerEnchant(new Peroxide());
		EnchantManager.registerEnchant(new Booboo());
		EnchantManager.registerEnchant(new ReallyToxic());
		EnchantManager.registerEnchant(new NewDeal());
		EnchantManager.registerEnchant(new HeighHo());

		EnchantManager.registerEnchant(new GoldenHeart());
		EnchantManager.registerEnchant(new Hearts());
		EnchantManager.registerEnchant(new Prick());
		EnchantManager.registerEnchant(new Electrolytes());
		EnchantManager.registerEnchant(new GottaGoFast());
		EnchantManager.registerEnchant(new CounterOffensive());
		EnchantManager.registerEnchant(new LastStand());
		EnchantManager.registerEnchant(new Stereo());
//		EnchantManager.registerEnchant(new DiamondAllergy());
//		EnchantManager.registerEnchant(new PitBlob());

//		Resource Enchants
		EnchantManager.registerEnchant(new Moctezuma());
		EnchantManager.registerEnchant(new GoldBump());
		EnchantManager.registerEnchant(new GoldBoost());

		EnchantManager.registerEnchant(new Sweaty());
//		EnchantManager.registerEnchant(new XpBump());

//		Darkzone enchants

//		Spells
		EnchantManager.registerEnchant(new FreezeSpell());
		EnchantManager.registerEnchant(new MeteorSpell());
		EnchantManager.registerEnchant(new CleaveSpell());
		EnchantManager.registerEnchant(new WarpSpell());
		EnchantManager.registerEnchant(new Necrotic());

//		Effects
		EnchantManager.registerEnchant(new Sonic());

//		Uncommon Curses
		EnchantManager.registerEnchant(new Weak());
		EnchantManager.registerEnchant(new Frail());

//		Rare
		EnchantManager.registerEnchant(new Defraction());
		EnchantManager.registerEnchant(new Devour());
		EnchantManager.registerEnchant(new Bipolar());
		EnchantManager.registerEnchant(new ElectricShock());
		EnchantManager.registerEnchant(new Hemorrhage());
		EnchantManager.registerEnchant(new Inferno());
		EnchantManager.registerEnchant(new net.pitsim.spigot.enchants.tainted.scythe.Leech());
		EnchantManager.registerEnchant(new Medic());
		EnchantManager.registerEnchant(new Persephone());
		EnchantManager.registerEnchant(new RollingThunder());
		EnchantManager.registerEnchant(new Swarm());
		EnchantManager.registerEnchant(new Terror());

//		Uncommon
		EnchantManager.registerEnchant(new Hunter());
		EnchantManager.registerEnchant(new EliteHunter());
		EnchantManager.registerEnchant(new TitanHunter());
		EnchantManager.registerEnchant(new Guard());
		EnchantManager.registerEnchant(new Shield());
		EnchantManager.registerEnchant(new Barricade());
		EnchantManager.registerEnchant(new Durable());

		EnchantManager.registerEnchant(new Adrenaline());
		EnchantManager.registerEnchant(new Barbaric());
		EnchantManager.registerEnchant(new ComboDefence());
		EnchantManager.registerEnchant(new ComboMana());
		EnchantManager.registerEnchant(new ComboSlow());
		EnchantManager.registerEnchant(new Desperate());
		EnchantManager.registerEnchant(new Emboldened());
		EnchantManager.registerEnchant(new Ethereal());
		EnchantManager.registerEnchant(new Fearmonger());
		EnchantManager.registerEnchant(new Fortify());
		EnchantManager.registerEnchant(new Greed());
		EnchantManager.registerEnchant(new Hoarder());
		EnchantManager.registerEnchant(new LeaveMeAlone());
		EnchantManager.registerEnchant(new Mechanic());
		EnchantManager.registerEnchant(new Mending());
		EnchantManager.registerEnchant(new Permed());
		EnchantManager.registerEnchant(new PitPocket());
		EnchantManager.registerEnchant(new Reaper());
		EnchantManager.registerEnchant(new Resilient());
		EnchantManager.registerEnchant(new ShieldBuster());
		EnchantManager.registerEnchant(new StartingHand());
		EnchantManager.registerEnchant(new Tanky());

//		Common
		EnchantManager.registerEnchant(new Aloft());
		EnchantManager.registerEnchant(new AnkleBiter());
		EnchantManager.registerEnchant(new AnomalyDetected());
		EnchantManager.registerEnchant(new Antagonist());
		EnchantManager.registerEnchant(new Attentive());
		EnchantManager.registerEnchant(new Belittle());
		EnchantManager.registerEnchant(new BOOM());
		EnchantManager.registerEnchant(new Embalm());
		EnchantManager.registerEnchant(new Evasive());
		EnchantManager.registerEnchant(new Extinguish());
		EnchantManager.registerEnchant(new GeneticReconstruction());
		EnchantManager.registerEnchant(new Huggable());
		EnchantManager.registerEnchant(new Intimidating());
		EnchantManager.registerEnchant(new Nimble());
		EnchantManager.registerEnchant(new NocturnalPredator());
		EnchantManager.registerEnchant(new Piercing());
		EnchantManager.registerEnchant(new PinCushion());
		EnchantManager.registerEnchant(new Pyrotechnic());
		EnchantManager.registerEnchant(new Sentinel());
		EnchantManager.registerEnchant(new ShadowCloak());
		EnchantManager.registerEnchant(new Territorial());
		EnchantManager.registerEnchant(new Undertaker());
		EnchantManager.registerEnchant(new WhoNeedsBows());
	}

	public void hookIntoAnticheat(AnticheatManager anticheat) {
//		if(PitSim.anticheat != null) {
//			Bukkit.getLogger().severe("Multiple anticheats found! Shutting down...");
//			Bukkit.getPluginManager().disablePlugin(this);
//			return;
//		}
//		PitSim.anticheat = anticheat;
	}

	public static boolean isDev() {
		return serverName.contains("dev");
	}

	public enum ServerStatus {
		DARKZONE, OVERWORLD, STANDALONE;

		public boolean isDarkzone() {
			return this == DARKZONE || this == STANDALONE;
		}

		public boolean isOverworld() {
			return this == OVERWORLD || this == STANDALONE;
		}

		public boolean isAll() {
			return true;
		}
	}

	public static ServerStatus getStatus() {
		return status;
	}
}
