package net.pitsim.spigot.controllers;

import de.myzelyam.api.vanish.VanishAPI;
import de.tr7zw.nbtapi.NBTItem;
import dev.kyro.arcticapi.misc.AOutput;
import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.enums.EquipmentType;
import net.pitsim.spigot.misc.PitEquipment;
import net.pitsim.spigot.items.PitItem;
import net.pitsim.spigot.items.TemporaryItem;
import net.pitsim.spigot.items.misc.Arrow;
import net.pitsim.spigot.controllers.objects.*;
import net.pitsim.spigot.enums.NBTTag;
import net.pitsim.spigot.enums.NonTrait;
import net.pitsim.spigot.enums.PitEntityType;
import net.pitsim.spigot.events.*;
import net.pitsim.spigot.megastreaks.Highlander;
import net.pitsim.spigot.megastreaks.NoMegastreak;
import net.pitsim.spigot.misc.Misc;
import net.pitsim.spigot.misc.Sounds;
import net.pitsim.spigot.pitmaps.XmasMap;
import net.pitsim.spigot.upgrades.TheWay;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.node.types.PermissionNode;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class PlayerManager implements Listener {
	public static final Map<Player, PitEquipment> previousEquipmentMap = new HashMap<>();
	private static final List<UUID> realPlayers = new ArrayList<>();

	public static void addRealPlayer(UUID uuid) {
		realPlayers.add(uuid);
	}

	public static boolean isRealPlayer(LivingEntity testPlayer) {
		if(!(testPlayer instanceof Player)) return false;
		return realPlayers.contains(testPlayer.getUniqueId());
	}

	static {
		new BukkitRunnable() {
			@Override
			public void run() {
				for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					PitPlayer pitPlayer = PitPlayer.getPitPlayer(onlinePlayer);
					if(!MapManager.inDarkzone(pitPlayer.player)) continue;

					if(onlinePlayer.getLocation().getY() >= 85)
						onlinePlayer.spigot().playEffect(onlinePlayer.getLocation(), Effect.PORTAL, 0, 0, 10, 10, 10, 1, 128, 100);
				}
			}
		}.runTaskTimerAsynchronously(PitSim.INSTANCE, 0L, 1L);

		new BukkitRunnable() {
			@Override
			public void run() {
				for(Non non : NonManager.nons)
					if(non.non != null) ((CraftPlayer) non.non).getHandle().getDataWatcher().watch(9, (byte) 0);
				for(Player onlinePlayer : Bukkit.getOnlinePlayers())
					((CraftPlayer) onlinePlayer).getHandle().getDataWatcher().watch(9, (byte) 0);
			}
		}.runTaskTimer(PitSim.INSTANCE, 0L, 20L);

		if(PitSim.getStatus().isOverworld()) {
			new BukkitRunnable() {
				@Override
				public void run() {
						for(Player player : Bukkit.getOnlinePlayers()) {
							if(!player.hasPermission("group.eternal") || MapManager.currentMap.world != player.getWorld() || VanishAPI.isInvisible(player))
								continue;
							if(SpawnManager.isInSpawn(player)) continue;
							List<Player> nearbyNons = new ArrayList<>();
							for(Entity nearbyEntity : player.getNearbyEntities(4, 4, 4)) {
	//						if(nearbyEntity.getWorld() == Bukkit.getWorld("tutorial")) continue;
							if(!(nearbyEntity instanceof Player)) continue;
							Player nearby = (Player) nearbyEntity;
							if(NonManager.getNon(nearby) == null || SpawnManager.isInSpawn(nearby) ||
									nearby.getLocation().distance(player.getLocation()) > 4 || !player.canSee(nearby)) continue;
							nearbyNons.add(nearby);
						}
						if(!nearbyNons.isEmpty()) {
							Collections.shuffle(nearbyNons);
							Player target = nearbyNons.remove(0);

							double damage;
							if(Misc.isAirOrNull(player.getItemInHand())) {
								damage = 1;
							} else if(player.getItemInHand().getType() == Material.GOLD_SWORD) {
								damage = 7.5;
							} else {
								damage = 1;
							}
							if(Misc.isCritical(player)) damage *= 1.5;

							target.setNoDamageTicks(0);
							target.damage(damage, player);
						}
					}
				}
			}.runTaskTimer(PitSim.INSTANCE, 0L, 18L);
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				for(Player player : Bukkit.getOnlinePlayers()) {
					PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);
					pitPlayer.updateMaxHealth();
				}
			}
		}.runTaskTimer(PitSim.INSTANCE, 0L, 20L);

		new BukkitRunnable() {
			@Override
			public void run() {
				EnchantManager.readPlayerEnchants();
				for(Player player : Bukkit.getOnlinePlayers()) {
					PitEquipment currentEquipment = new PitEquipment(player);
					if(!previousEquipmentMap.containsKey(player)) {
						previousEquipmentMap.put(player, currentEquipment);
						continue;
					}

					PitEquipment previousEquipment = previousEquipmentMap.get(player);

					for(EquipmentType equipmentType : EquipmentType.values()) {
						ItemStack previousItem = previousEquipment.getItemStack(equipmentType);
						ItemStack currentItem = currentEquipment.getItemStack(equipmentType);
						if(previousItem.equals(currentItem)) continue;

						EquipmentChangeEvent event = new EquipmentChangeEvent(player, equipmentType, previousEquipment, currentEquipment, false);
						Bukkit.getPluginManager().callEvent(event);
					}

					previousEquipmentMap.put(player, currentEquipment);
				}
			}
		}.runTaskTimer(PitSim.INSTANCE, 0L, 1L);

		new BukkitRunnable() {
			@Override
			public void run() {
				for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					PitPlayer pitPlayer = PitPlayer.getPitPlayer(onlinePlayer);
					for(PitPlayer.MegastreakLimit cooldown : pitPlayer.getAllCooldowns()) cooldown.attemptReset(pitPlayer);
				}
			}
		}.runTaskTimer(PitSim.INSTANCE, Misc.getRunnableOffset(1), 20 * 60);
	}

	public static boolean isStaff(UUID uuid) {
		User user;
		try {
			user = PitSim.LUCKPERMS.getUserManager().loadUser(uuid).get();
		} catch(InterruptedException | ExecutionException exception) {
			exception.printStackTrace();
			return false;
		}
		Group group = PitSim.LUCKPERMS.getGroupManager().getGroup(user.getPrimaryGroup());
		return group.data().contains(PermissionNode.builder("pitsim.staff").build(), NodeEqualityPredicate.EXACT).asBoolean();
	}

	@EventHandler
	public void onEquipmentChange(EquipmentChangeEvent event) {
		PitPlayer pitPlayer = event.getPitPlayer();
		pitPlayer.updateMaxHealth();
		pitPlayer.updateWalkingSpeed();
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onItemPickup(PlayerPickupItemEvent event) {
		ItemStack itemStack = event.getItem().getItemStack();
		if(Misc.isAirOrNull(itemStack) || itemStack.getType() != Material.ARROW) return;
		event.setCancelled(true);
	}

	@EventHandler
	public void giveMoonCap(KillEvent killEvent) {
		if(!PlayerManager.isRealPlayer(killEvent.getKillerPlayer())) return;
		PitPlayer pitPlayer = PitPlayer.getPitPlayer(killEvent.getKillerPlayer());
		killEvent.xpCap += pitPlayer.moonBonus;
	}

	public static void sendItemLossMessage(Player player, ItemStack itemStack) {
		PitItem pitItem = ItemFactory.getItem(itemStack);
		assert pitItem != null;
		TemporaryItem temporaryItem = pitItem.getAsTemporaryItem();
		temporaryItem.setLives(itemStack, 0);

		TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&c&lRIP!&7 You lost " +
				(itemStack.getAmount() == 1 ? "" : itemStack.getAmount() + "x ")));
		message.addExtra(Misc.createItemHover(itemStack));
//		message.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "")));

		player.spigot().sendMessage(message);
	}

	public static void sendLivesLostMessage(Player player, int livesLost) {
		if(livesLost == 0) return;
		AOutput.error(player, "&c&lRIP!&7 You lost lives on &f" + livesLost + " &7item" + (livesLost == 1 ? "" : "s"));
	}

	public Map<UUID, Long> viewShiftCooldown = new HashMap<>();

	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent event) {
		Player player = event.getPlayer();
		if(!(event.getRightClicked() instanceof Player)) return;
		Player target = (Player) event.getRightClicked();
		if(!player.isSneaking() || !SpawnManager.isInSpawn(player) || !SpawnManager.isInSpawn(target))
			return;
		if(!PlayerManager.isRealPlayer(target)) return;
		if(viewShiftCooldown.getOrDefault(player.getUniqueId(), 0L) + 2000 > System.currentTimeMillis()) return;
		viewShiftCooldown.put(player.getUniqueId(), System.currentTimeMillis());

		new PluginMessage().writeString("VIEW REQUEST").writeString(player.getUniqueId().toString())
				.writeString(target.getUniqueId().toString()).send();
	}

	@EventHandler
	public void onAnvil(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Material material = event.getClickedBlock().getType();
		switch(material) {
			case ANVIL:
			case WORKBENCH:
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		ItemStack itemStack = event.getItem().getItemStack();
		if(Misc.isAirOrNull(itemStack)) return;
		NBTItem nbtItem = new NBTItem(itemStack);
		if(!nbtItem.hasKey(NBTTag.CANNOT_PICKUP.getRef())) return;
		event.setCancelled(true);
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);
		pitPlayer.updateXPBar();
	}

	@EventHandler
	public void onItemCraft(CraftItemEvent event) {
		Player player = (Player) event.getWhoClicked();
		event.setCancelled(true);
		player.updateInventory();
		AOutput.error(player, "You are not allowed to craft items");
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);
		pitPlayer.lastCommand = System.currentTimeMillis();

		if(player.isOp()) return;

		if(ChatColor.stripColor(event.getMessage()).toLowerCase().startsWith("/trade")) {
			int levelRequired = 100 - TheWay.INSTANCE.getLevelReduction(pitPlayer.player);
			if(pitPlayer.level < levelRequired) {
				event.setCancelled(true);
				AOutput.error(player, "&c&lERROR!&7 You cannot trade until you are level " + levelRequired);
				return;
			}
		}

		if(ChatColor.stripColor(event.getMessage()).toLowerCase().startsWith("/invsee")) {
			event.setCancelled(true);
			AOutput.send(player, "&c&lOUTDATED!&7 Please use /view <player> instead");
			return;
		}
	}

	@EventHandler
	public void onCommand2(PlayerCommandPreprocessEvent event) {
		if(event.getPlayer().isOp()) return;
	}

	@EventHandler
	public void onKillForRank(KillEvent killEvent) {
		if(killEvent.isDeadPlayer()) {
			XmasMap.removeFromRadio(killEvent.getDeadPlayer());
			new BukkitRunnable() {
				@Override
				public void run() {
					XmasMap.addToRadio(killEvent.getDeadPlayer());
				}
			}.runTaskLater(PitSim.INSTANCE, 20);
		}

		if(killEvent.isKillerPlayer()) {
			double multiplier = 1;
			if(killEvent.getKiller().hasPermission("group.nitro")) {
				multiplier += 0.1;
			}

			if(killEvent.getKiller().hasPermission("group.eternal")) {
				multiplier += 0.30;
			} else if(killEvent.getKiller().hasPermission("group.unthinkable")) {
				multiplier += 0.25;
			} else if(killEvent.getKiller().hasPermission("group.miraculous")) {
				multiplier += 0.20;
			} else if(killEvent.getKiller().hasPermission("group.extraordinary")) {
				multiplier += 0.15;
			} else if(killEvent.getKiller().hasPermission("group.overpowered")) {
				multiplier += 0.1;
			} else if(killEvent.getKiller().hasPermission("group.legendary")) {
				multiplier += 0.05;
			}
			killEvent.xpMultipliers.add(multiplier);
			killEvent.goldMultipliers.add(multiplier);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onKill(KillEvent killEvent) {
		if(PitSim.status.isDarkzone()) return;
		if(!killEvent.isDeadPlayer() || !killEvent.isKillerPlayer()) return;

		PitPlayer pitKiller = killEvent.getKillerPitPlayer();
		PitPlayer pitDead = killEvent.getDeadPitPlayer();
		Non killingNon = NonManager.getNon(killEvent.getKiller());

		if(pitDead.bounty != 0 && killingNon == null && pitKiller != pitDead) {
			DecimalFormat formatter = new DecimalFormat("#,###.#");
			String bountyMessage = Misc.getBountyClaimedMessage(pitKiller, pitDead, "&6&l" + formatter.format(pitDead.bounty) + "g");
			for(Player player : Bukkit.getOnlinePlayers()) {
				PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);
				if(pitPlayer.bountiesDisabled) continue;
				AOutput.send(player, bountyMessage);
			}
			LevelManager.addGold(killEvent.getKillerPlayer(), pitDead.bounty);
			if(!(pitDead.getMegastreak() instanceof Highlander)) pitDead.bounty = 0;

			pitKiller.stats.bountiesClaimed++;
		}

		int maxBounty = 20_000;
		if(Math.random() < 0.1 && killingNon == null && pitKiller.bounty < maxBounty) {

			int bountyBump = (new Random().nextInt(5) + 1) * 200;
			pitKiller.bounty = Math.min(pitKiller.bounty + bountyBump, maxBounty);
			ChatTriggerManager.sendBountyInfo(pitKiller);
			String message = "&6&lBOUNTY!&7 bump &6&l" + bountyBump + "g&7 on %luckperms_prefix%" + killEvent.getKillerPlayer().getDisplayName() +
					"&7 for high streak";
			if(!pitKiller.bountiesDisabled)
				AOutput.send(killEvent.getKiller(), PlaceholderAPI.setPlaceholders(killEvent.getKillerPlayer(), message));
			Sounds.BOUNTY.play(killEvent.getKiller());
		}
	}

	@EventHandler
	public void onIncrement(IncrementKillsEvent event) {
		Player player = event.getPlayer();
		PitPlayer pitPlayer = event.getPitPlayer();
		int kills = event.getKills();
		Megastreak megastreak = pitPlayer.getMegastreak();
		if(kills == megastreak.requiredKills && !(megastreak instanceof NoMegastreak)) {
			megastreak.proc(player);

			for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				PitPlayer onlinePitPlayer = PitPlayer.getPitPlayer(onlinePlayer);
				if(onlinePitPlayer.streaksDisabled) continue;
				String streakMessage = ChatColor.translateAlternateColorCodes('&',
						"&c&lMEGASTREAK! %luckperms_prefix%" + pitPlayer.player.getDisplayName() + " &7activated " + megastreak.getCapsDisplayName() + "&7!");
				AOutput.send(onlinePlayer, PlaceholderAPI.setPlaceholders(pitPlayer.player, streakMessage));
			}
		}
	}

	public static List<UUID> pantsSwapCooldown = new ArrayList<>();
	public static List<UUID> helmetSwapCooldown = new ArrayList<>();
	public static List<UUID> chestplateSwapCooldown = new ArrayList<>();

	@EventHandler(priority = EventPriority.HIGH)
	public static void onClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if(Misc.isAirOrNull(player.getItemInHand())) return;

		int firstArrow = -1;
		boolean multipleStacks = false;
		boolean hasSpace = false;
		if(player.getItemInHand().getType() == Material.BOW) {

			for(int i = 0; i < 36; i++) {
				ItemStack itemStack = player.getInventory().getItem(i);
				if(Misc.isAirOrNull(itemStack)) {
					hasSpace = true;
					continue;
				}
				if(itemStack.getType() != Material.ARROW) continue;
				if(firstArrow == -1) firstArrow = i;
				else {
					multipleStacks = true;
					break;
				}
			}
			if(!multipleStacks) {
				if(firstArrow == -1) {
					if(hasSpace) {
						player.getInventory().addItem(ItemFactory.getItem(Arrow.class).getItem(32));
					} else {
						AOutput.error(player, "Please make room in your inventory for arrows");
					}
				} else {
					player.getInventory().setItem(firstArrow, ItemFactory.getItem(Arrow.class).getItem(32));
				}
			}
		}

		if(player.getItemInHand().getType().toString().contains("LEGGINGS")) {
			if(Misc.isAirOrNull(player.getInventory().getLeggings())) return;

			if(pantsSwapCooldown.contains(player.getUniqueId())) {

				Sounds.NO.play(player);
				return;
			}

			ItemStack held = player.getItemInHand();
			player.setItemInHand(player.getInventory().getLeggings());
			player.getInventory().setLeggings(held);

			pantsSwapCooldown.add(player.getUniqueId());
			new BukkitRunnable() {
				@Override
				public void run() {
					pantsSwapCooldown.remove(player.getUniqueId());
				}
			}.runTaskLater(PitSim.INSTANCE, 40L);
			Sounds.ARMOR_SWAP.play(player);
		}

		if(player.getItemInHand().getType().toString().contains("HELMET")) {
			if(player.isSneaking()) return;
			if(Misc.isAirOrNull(player.getInventory().getHelmet())) return;

			if(HelmetManager.abilities.get(event.getPlayer()) != null) {
				HelmetManager.deactivate(event.getPlayer());
			}
			HelmetManager.toggledPlayers.remove(event.getPlayer());
			HelmetManager.abilities.remove(event.getPlayer());

			if(helmetSwapCooldown.contains(player.getUniqueId())) {

				Sounds.NO.play(player);
				return;
			}

			ItemStack held = player.getItemInHand();
			player.setItemInHand(player.getInventory().getHelmet());
			player.getInventory().setHelmet(held);

			helmetSwapCooldown.add(player.getUniqueId());
			new BukkitRunnable() {
				@Override
				public void run() {
					helmetSwapCooldown.remove(player.getUniqueId());
				}
			}.runTaskLater(PitSim.INSTANCE, 40L);
			Sounds.ARMOR_SWAP.play(player);
		}

		if(player.getItemInHand().getType().toString().contains("CHESTPLATE")) {
			if(Misc.isAirOrNull(player.getInventory().getChestplate())) return;

			Block block = event.getClickedBlock();
			if(block != null && block.getType() == Material.ENCHANTMENT_TABLE) return;

			if(chestplateSwapCooldown.contains(player.getUniqueId())) {

				Sounds.NO.play(player);
				return;
			}

			ItemStack held = player.getItemInHand();
			player.setItemInHand(player.getInventory().getChestplate());
			player.getInventory().setChestplate(held);

			chestplateSwapCooldown.add(player.getUniqueId());
			new BukkitRunnable() {
				@Override
				public void run() {
					chestplateSwapCooldown.remove(player.getUniqueId());
				}
			}.runTaskLater(PitSim.INSTANCE, 40L);
			Sounds.ARMOR_SWAP.play(player);
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				event.getPlayer().teleport(MapManager.currentMap.getSpawn());
			}
		}.runTaskLater(PitSim.INSTANCE, 10L);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if(event.getPlayer().getLocation().getY() < 10 && event.getPlayer().getWorld() == PitSim.TUTORIAL())
			DamageManager.killPlayer(event.getPlayer());
		else if(event.getPlayer().getLocation().getY() < 10 && (MapManager.getDarkzone() == event.getPlayer().getWorld() || MapManager.currentMap.world == event.getPlayer().getWorld())) {
			DamageManager.killPlayer(event.getPlayer());
		} else if(event.getPlayer().getLocation().getY() < 10) DamageManager.killPlayer(event.getPlayer());
	}

	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onAttack(AttackEvent.Apply attackEvent) {

		Non defendingNon = NonManager.getNon(attackEvent.getDefender());
		if(PlayerManager.isRealPlayer(attackEvent.getDefenderPlayer()) && PitSim.status.isOverworld()) {
//			Arch chest archangel chestplate
			attackEvent.multipliers.add(0.8);
		} else if(defendingNon != null) {
//			Non defence
			if(defendingNon.traits.contains(NonTrait.IRON_STREAKER)) attackEvent.multipliers.add(0.8);
		}

//		ItemStack itemStack = attackEvent.attacker.getItemInHand();
//		if(itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasEnchant(Enchantment.DAMAGE_ALL)
//				&& itemStack.getItemMeta().getEnchantLevel(Enchantment.DAMAGE_ALL) == 1) {
//			itemStack.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
//			attackEvent.attacker.setItemInHand(itemStack);
//		}
	}

	@EventHandler
	public void onPlayerJoin(PitJoinEvent event) {
		Player player = event.getPlayer();
		PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);

		event.getEvent().setJoinMessage(null);

		PitEquipment currentEquipment = new PitEquipment(player);
		for(EquipmentType equipmentType : EquipmentType.values()) {
			EquipmentChangeEvent equipmentChangeEvent = new EquipmentChangeEvent(player, equipmentType,
					new PitEquipment(), currentEquipment, true);
			Bukkit.getPluginManager().callEvent(equipmentChangeEvent);
		}

//		if(Misc.isKyro(player.getUniqueId()) && PitSim.anticheat instanceof GrimManager) { //todo add this back
//			Bukkit.getServer().dispatchCommand(player, "grim alerts");
//		}

//		FeatherBoardAPI.resetDefaultScoreboard(player); //todo add this back
		ScoreboardManager.updateScoreboard(player);

		for(PitPlayer.MegastreakLimit cooldown : pitPlayer.getAllCooldowns()) cooldown.attemptReset(pitPlayer);

		new BukkitRunnable() {
			@Override
			public void run() {
				if(!player.isOnline()) return;
				player.setGameMode(GameMode.SURVIVAL);

				pitPlayer.updateMaxHealth();
				player.setHealth(player.getMaxHealth());
			}
		}.runTaskLater(PitSim.INSTANCE, 1L);
	}

	@EventHandler
	public void onJoin(PlayerSpawnLocationEvent event) {
		Player player = event.getPlayer();
		PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);
		Location spawnLoc = PitSim.getStatus() == PitSim.ServerStatus.DARKZONE ? MapManager.getDarkzoneSpawn() : MapManager.currentMap.getSpawn();
		if(LobbySwitchManager.joinedFromDarkzone.contains(player.getUniqueId())) spawnLoc = MapManager.currentMap.getFromDarkzoneSpawn();
		if(ProxyMessaging.joinTeleportMap.containsKey(player.getUniqueId())) {
			Player tpPlayer = Bukkit.getPlayer(ProxyMessaging.joinTeleportMap.get(player.getUniqueId()));
			if(tpPlayer.isOnline()) spawnLoc = tpPlayer.getLocation();

			new BukkitRunnable() {
				@Override
				public void run() {
					if(!tpPlayer.isOnline()) AOutput.error(player, "&cThe player you were trying to teleport to is no longer online.");
					else AOutput.send(player, "&aTeleporting to " + tpPlayer.getName() + "...");
				}
			}.runTaskLater(PitSim.INSTANCE, 10);
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				if(!player.isOnline()) return;
				if(!pitPlayer.musicDisabled && XmasMap.radio != null) XmasMap.addToRadio(player);
			}
		}.runTaskLater(PitSim.INSTANCE, 20);

//		new BukkitRunnable() {
//			@Override
//			public void run() {
//				if(!player.isOnline()) return;
//				DarkzoneLeveling.updateAltarXP(pitPlayer);
//			}
//		}.runTaskLater(PitSim.INSTANCE, 20);

		player.teleport(spawnLoc);
		Location finalSpawnLoc = spawnLoc;
		new BukkitRunnable() {
			@Override
			public void run() {
				if(player.isOnline()) player.teleport(finalSpawnLoc);
			}
		}.runTaskLater(PitSim.INSTANCE, 1L);

		new BukkitRunnable() {
			@Override
			public void run() {
				if(!player.isOnline()) return;
				player.teleport(finalSpawnLoc);

				if(PitSim.getStatus() == PitSim.ServerStatus.DARKZONE) {
//					player.setVelocity(new Vector(1.5, 1, 0).multiply(0.3));
					Misc.sendTitle(player, "&d&k||&5&lDarkzone&d&k||", 40);
					Misc.sendSubTitle(player, "", 40);
					AOutput.send(player, "&7You have been sent to the &d&k||&5&lDarkzone&d&k||&7.");

					if(!pitPlayer.darkzoneCutscene) {
//						CutsceneManager.play(player);
						return;
					}

				} else if(PitSim.getStatus() == PitSim.ServerStatus.OVERWORLD && LobbySwitchManager.joinedFromDarkzone.contains(player.getUniqueId()) &&
						!ProxyMessaging.joinTeleportMap.containsKey(player.getUniqueId())) {
					player.setVelocity(new Vector(1.5, 1, 0));
					Misc.sendTitle(player, "&a&lOverworld", 40);
					Misc.sendSubTitle(player, "", 40);
					AOutput.send(player, "&7You have been sent to the &a&lOverworld&7.");
				}

				ProxyMessaging.joinTeleportMap.remove(player.getUniqueId());
			}
		}.runTaskLater(PitSim.INSTANCE, 5L);
	}

	@EventHandler
	public void onCraft(InventoryClickEvent event) {
		if(event.getSlot() == 80 || event.getSlot() == 81 || event.getSlot() == 82 || event.getSlot() == 83)
			event.setCancelled(true);
	}

	@EventHandler
	public void onJoin(AsyncPlayerPreLoginEvent event) {
		UUID playerUUID = event.getUniqueId();
		if(!realPlayers.contains(playerUUID)) addRealPlayer(playerUUID);
		boolean success = PitPlayer.loadPitPlayer(playerUUID);
		if(FirestoreManager.FIRESTORE == null) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
					ChatColor.RED + "Server still starting up");
		} else if(!success) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
					ChatColor.RED + "Playerdata failed to load. Please open a support ticket: discord.pitsim.net");
		}

		Player player = Bukkit.getServer().getPlayerExact(event.getName());
		if(player == null) return;
		if(player.isOnline()) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "You are already online! \nIf you believe this is an error, try re-logging in a few seconds.");
		}
	}

	public static List<Player> toggledPlayers = new ArrayList<>();

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if(!event.getPlayer().isOp()) return;
		if(toggledPlayers.contains(event.getPlayer())) return;
		event.setCancelled(true);
		AOutput.error(event.getPlayer(), "&CBlock breaking disabled, run /pitsim bypass to toggle");
	}

	@EventHandler
	public void onBreak(BlockPlaceEvent event) {
		if(!event.getPlayer().isOp()) return;
		if(toggledPlayers.contains(event.getPlayer())) return;
		event.setCancelled(true);
		AOutput.error(event.getPlayer(), "&CBlock placing disabled, run /pitsim bypass to toggle");
	}

	@EventHandler
	public void onItemFrameBreak(EntityDamageByEntityEvent event) {
		if(!Misc.isEntity(event.getDamager(), PitEntityType.REAL_PLAYER)) {
			if(event.getEntity() instanceof ItemFrame) event.setCancelled(true);
			return;
		}

		Player player = (Player) event.getDamager();
		if(!(event.getEntity() instanceof ItemFrame)) return;
		if(!player.isOp()) {
			event.setCancelled(true);
			return;
		}
		if(toggledPlayers.contains(player)) return;

		event.setCancelled(true);
		AOutput.error(player, "&CBlock interactions disabled, run /pitsim bypass to toggle");
	}

	@EventHandler
	public void onWitherSkullExplode(ExplosionPrimeEvent event) {
		if(!(event.getEntity() instanceof WitherSkull)) return;
		for(Entity nearbyEntity : event.getEntity().getNearbyEntities(5, 5, 5)) {
			if(nearbyEntity instanceof ItemFrame) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if(!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
		if(event.getClickedBlock().getType() != Material.TRAP_DOOR) return;

		if(!event.getPlayer().isOp()) return;
		if(toggledPlayers.contains(event.getPlayer())) return;
		event.setCancelled(true);
		AOutput.error(event.getPlayer(), "&CBlock interactions disabled, run /pitsim bypass to toggle");
	}

	@EventHandler
	public void onQuit(PitQuitEvent event) {
		Player player = event.getPlayer();
		previousEquipmentMap.remove(player);
		event.getEvent().setQuitMessage(null);
		XmasMap.removeFromRadio(player);
		PitPlayer pitPlayer = event.getPitPlayer();
		pitPlayer.endKillstreak();
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if(event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
				event.getCause() == EntityDamageEvent.DamageCause.LAVA || event.getCause() == EntityDamageEvent.DamageCause.DROWNING ||
				event.getCause() == EntityDamageEvent.DamageCause.FALL) event.setCancelled(true);

//		TODO: Only do this if grim is running
		if(event.getCause() == EntityDamageEvent.DamageCause.WITHER) event.setCancelled(true);
	}
}
