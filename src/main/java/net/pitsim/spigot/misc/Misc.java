package net.pitsim.spigot.misc;

import de.myzelyam.api.vanish.VanishAPI;
import de.tr7zw.nbtapi.NBTItem;
import dev.kyro.arcticapi.builders.ALoreBuilder;
import dev.kyro.arcticapi.gui.AGUIPanel;
import dev.kyro.arcticapi.libs.discord.DiscordWebhook;
import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.darkzone.BossManager;
import net.pitsim.spigot.darkzone.DarkzoneManager;
import net.pitsim.spigot.darkzone.PitBoss;
import net.pitsim.spigot.darkzone.PitMob;
import net.pitsim.spigot.items.PitItem;
import net.pitsim.spigot.items.TemporaryItem;
import net.pitsim.spigot.commands.LightningCommand;
import net.pitsim.spigot.controllers.*;
import net.pitsim.spigot.controllers.objects.HelmetManager;
import net.pitsim.spigot.controllers.objects.PitEnchant;
import net.pitsim.spigot.controllers.objects.PitPlayer;
import net.pitsim.spigot.controllers.objects.PluginMessage;
import net.pitsim.spigot.cosmetics.CosmeticManager;
import net.pitsim.spigot.cosmetics.CosmeticType;
import net.pitsim.spigot.cosmetics.PitCosmetic;
import net.pitsim.spigot.cosmetics.killeffectsbot.IronKill;
import net.pitsim.spigot.enums.DiscordLogChannel;
import net.pitsim.spigot.enums.NBTTag;
import net.pitsim.spigot.enums.PitEntityType;
import net.pitsim.spigot.events.HealEvent;
import net.pitsim.spigot.inventories.GenericConfirmationPanel;
import net.pitsim.spigot.megastreaks.Overdrive;
import net.pitsim.spigot.megastreaks.Uberstreak;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.model.group.Group;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Material;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Misc {
	public static List<Integer> createDistribution(int amount, double exponentialReduction) {
		int distributions = (int) Math.pow(amount, exponentialReduction);
		Map<Integer, Integer> distributionMap = new HashMap<>();
		amount -= distributions;
		for(int i = 0; i < distributions; i++) distributionMap.put(i, 1);
		for(int i = 0; i < amount; i++) {
			int randomStack = new Random().nextInt(distributions);
			distributionMap.put(randomStack, distributionMap.get(randomStack) + 1);
		}
		return new ArrayList<>(distributionMap.values());
	}

	public static void promptForConfirmation(AGUIPanel previousPanel, ChatColor chatColor, ALoreBuilder confirmLore,
											 ALoreBuilder cancelLore, Consumer<GenericConfirmationPanel> confirm,
											 Consumer<GenericConfirmationPanel> cancel) {
		GenericConfirmationPanel confirmationPanel =
				new GenericConfirmationPanel(previousPanel.gui, chatColor, confirmLore, cancelLore, confirm, cancel);
		previousPanel.openPanel(confirmationPanel);
	}

	public static void stunEntity(LivingEntity livingEntity, int ticks) {
		applyPotionEffect(livingEntity, PotionEffectType.SLOW, ticks, 7, true, false);
		applyPotionEffect(livingEntity, PotionEffectType.JUMP, ticks, 128, true, false);
		applyPotionEffect(livingEntity, PotionEffectType.SLOW_DIGGING, ticks, 99, true, false);

		if(livingEntity instanceof Player) {
			Player player = (Player) livingEntity;
			sendTitle(player, "&cSTUNNED", ticks);
			sendSubTitle(player, "&eYou cannot move!", ticks);
			Sounds.COMBO_STUN.play(player);
		}
	}

	private static int nextEntityID = Integer.MIN_VALUE;
	public static int getNextEntityID() {
		return nextEntityID++;
	}

//	Doesn't work on all blocks
	public static Sound getBlockBreakSound(Block block) {
		World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();
		net.minecraft.server.v1_8_R3.Block nmsBlock = nmsWorld.getType(
				new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
		try {
			return Sound.valueOf(nmsBlock.stepSound.getBreakSound().replaceAll("\\.", "_").toUpperCase());
		} catch(Exception ignored) {
			return null;
		}
	}

	public static List<Player> getNearbyRealPlayers(Location location, double range) {
		List<Player> realPlayers = new ArrayList<>();
		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if(onlinePlayer.getWorld() != location.getWorld()) continue;
			Location testLocation = onlinePlayer.getLocation();
			if(Math.abs(testLocation.getX() - location.getX()) > range || Math.abs(testLocation.getY() - location.getY()) > range ||
					Math.abs(testLocation.getZ() - location.getZ()) > range) continue;
			realPlayers.add(onlinePlayer);
		}
		return realPlayers;
	}

	public static boolean isEntity(Entity entity, PitEntityType... entityTypes) {
		PitEntityType pitEntityType = getEntity(entity);
		return Arrays.asList(entityTypes).contains(pitEntityType);
	}

	public static PitEntityType getEntity(Entity entity) {
		if(!(entity instanceof LivingEntity)) return PitEntityType.NON_LIVING;
		LivingEntity livingEntity = (LivingEntity) entity;
		if(PlayerManager.isRealPlayer(livingEntity)) return PitEntityType.REAL_PLAYER;
		if(NonManager.getNon(livingEntity) != null) return PitEntityType.NON;
		if(HopperManager.isHopper(livingEntity)) return PitEntityType.HOPPER;
		if(DarkzoneManager.isPitMob(livingEntity)) return PitEntityType.PIT_MOB;
		if(BossManager.isPitBoss(livingEntity)) return PitEntityType.PIT_BOSS;
		return PitEntityType.UNKNOWN;
	}

	public static boolean isValidMobPlayerTarget(Entity entity, Entity... excluded) {
		List<Entity> excludedList = Arrays.asList(excluded);

		if(!(entity instanceof LivingEntity) || SpawnManager.isInSpawn(entity.getLocation()) || excludedList.contains(entity)) return false;
		LivingEntity livingEntity = (LivingEntity) entity;

		PitMob pitMob = DarkzoneManager.getPitMob(livingEntity);
		PitBoss pitBoss = BossManager.getPitBoss(livingEntity);
		if(pitMob == null && pitBoss == null && !(entity instanceof Player)) return false;

		if(entity instanceof Player) {
			Player player = (Player) entity;
			if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || VanishAPI.isInvisible(player)) return false;
		}
		return true;
	}

	public static LivingEntity getMobPlayerClosest(Location location, double radius, Entity... excluded) {
		List<Entity> excludedList = Arrays.asList(excluded);

		LivingEntity closestEntity = null;
		double closestDistance = Double.MAX_VALUE;
		for(Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
			if(excludedList.contains(entity)) continue;
			if(!isValidMobPlayerTarget(entity, excluded)) continue;
			LivingEntity livingEntity = (LivingEntity) entity;

			double testDistance = livingEntity.getLocation().distance(location);
			if(testDistance > closestDistance) continue;
			closestEntity = livingEntity;
			closestDistance = testDistance;
		}
		return closestEntity;
	}

	public static void alertDiscord(String message) {
//		DiscordWebhook discordWebhook = new DiscordWebhook(FirestoreManager.CONFIG.alertsWebhook);
//		discordWebhook.setContent(message);
//		new BukkitRunnable() {
//			@Override
//			public void run() {
//				try {
//					discordWebhook.execute();
//				} catch(IOException exception) {
//					exception.printStackTrace();
//				}
//			}
//		}.runTaskAsynchronously(PitSim.INSTANCE);
	}

	public static void logToDiscord(DiscordLogChannel logChannel, String message) {
		new PluginMessage()
				.writeString("LOG_TO_DISCORD")
				.writeString(logChannel.name())
				.writeString(message)
				.send();
	}

	public static int getItemCount(Player player, boolean checkArmor, BiPredicate<PitItem, ItemStack> condition) {
		int count = 0;
		List<ItemStack> items = new ArrayList<>(Arrays.asList(player.getInventory().getContents()));
		if(checkArmor) items.addAll(Arrays.asList(player.getInventory().getArmorContents()));
		for(ItemStack itemStack : items) {
			PitItem pitItem = ItemFactory.getItem(itemStack);
			if(pitItem == null || !condition.test(pitItem, itemStack)) continue;
			count += itemStack.getAmount();
		}
		return count;
	}

	public static boolean removeItems(Player player, int amount, BiPredicate<PitItem, ItemStack> condition) {
		if(getItemCount(player, false, condition) < amount) return false;
		for(int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack itemStack = player.getInventory().getItem(i);
			PitItem pitItem = ItemFactory.getItem(itemStack);
			if(pitItem == null || !condition.test(pitItem, itemStack)) continue;

			if(amount >= itemStack.getAmount()) {
				player.getInventory().setItem(i, new ItemStack(Material.AIR));
				amount -= itemStack.getAmount();
				if(amount == 0) return true;
			} else {
				itemStack.setAmount(itemStack.getAmount() - amount);
				player.getInventory().setItem(i, itemStack);
				return true;
			}
		}
		throw new RuntimeException();
	}

	public static String distortMessage(String message, double chance) {
		message = ChatColor.translateAlternateColorCodes('&', message);
		char[] chars = message.toCharArray();
		String finalString = "";
		for(int i = 0; i < chars.length; i++) {
			char character = chars[i];
			if(character == ' ') {
				finalString += character;
				continue;
			}
			if(character == '\u00A7') {
				if(!finalString.contains("\u00A7k")) finalString += ChatColor.getLastColors(message.substring(0, i + 2));
				i++;
				continue;
			}

			if(Math.random() > chance) {
				finalString += chars[i];
			} else {
				String substring = message.substring(0, i);
				finalString += "\u00A7k" + chars[i] + ChatColor.getLastColors(substring);
			}
		}
		return finalString;
	}

	public static void broadcast(String message) {
		broadcast(message, true);
	}

	public static void broadcast(String message, boolean prefix) {
		new PluginMessage()
				.writeString("BROADCAST")
				.writeString(message)
				.writeBoolean(prefix)
				.send();
	}

	public static List<String> getTabComplete(String current, List<String> options) {
		return getTabComplete(current, options.toArray(new String[0]));
	}

	public static List<String> getTabComplete(String current, String... options) {
		if(current == null || current.isEmpty()) return Arrays.asList(options);
		List<String> tabComplete = new ArrayList<>();
		for(String option : options) if(option.toLowerCase().startsWith(current.toLowerCase())) tabComplete.add(option);
		return tabComplete;
	}

	public static <T> T weightedRandom(LinkedHashMap<T, Double> weightedMap) {
		return weightedRandom(weightedMap, null);
	}

	@Deprecated
	public static <T> T weightedRandom(LinkedHashMap<T, Double> weightedMap, long seed) {
		return weightedRandom(weightedMap, seed == -1 ? null : new Random(seed));
	}

	public static <T> T weightedRandom(LinkedHashMap<T, Double> weightedMap, Random random) {
		if(weightedMap.isEmpty()) throw new RuntimeException();
		// Normalize the weights
		double sum = 0.0;
		for(double weight : weightedMap.values()) sum += weight;
		LinkedHashMap<T, Double> normalizedWeights = new LinkedHashMap<>();
		for(Map.Entry<T, Double> entry : weightedMap.entrySet()) normalizedWeights.put(entry.getKey(), entry.getValue() / sum);

//		Create Random
		if(random == null) random = new Random();

		// Select a random number between 0 and 1
		double rand = random.nextDouble();

		// Find the element corresponding to the random number
		double total = 0.0;
		for(Map.Entry<T, Double> entry : normalizedWeights.entrySet()) {
			total += entry.getValue();
			if(total >= rand) return entry.getKey();
		}

		return normalizedWeights.entrySet().iterator().next().getKey();
	}

	public static int intBetween(int lowBound, int highBound) {
		return intBetween(lowBound, highBound, new Random());
	}

	public static int intBetween(int lowBound, int highBound, Random random) {
		return random.nextInt(highBound - lowBound + 1) + lowBound;
	}

	public static String getDisplayName(Player player) {
		String playerName = "%luckperms_prefix%%pitsim_nickname%";
		return PlaceholderAPI.setPlaceholders(player, playerName);
	}

	public static String stringifyItem(ItemStack itemStack) {
		String serializedItem = "";
		if(isAirOrNull(itemStack)) return addBraces(serializedItem);
		if(!itemStack.hasItemMeta()) return addBraces(itemStack.getType().toString());
		serializedItem += itemStack.getAmount() + "x";

		NBTItem nbtItem = new NBTItem(itemStack);
		ItemMeta itemMeta = itemStack.getItemMeta();
		serializedItem += " " + itemStack.getType();

		PitItem pitItem = ItemFactory.getItem(itemStack);

		if(pitItem != null && pitItem.isMystic) {
			TemporaryItem.TemporaryType temporaryType = pitItem.getTemporaryType(itemStack);
			TemporaryItem temporaryItem = temporaryType == null ? null : pitItem.getAsTemporaryItem();

			serializedItem += " " + nbtItem.getString(NBTTag.ITEM_UUID.getRef());

			if(temporaryType == TemporaryItem.TemporaryType.LOOSES_LIVES_ON_DEATH) {
				serializedItem += " " + temporaryItem.getLives(itemStack) + "/" + temporaryItem.getMaxLives(itemStack);
			}

			if(nbtItem.hasKey(NBTTag.IS_GEMMED.getRef())) serializedItem += " Gemmed";
			if(EnchantManager.isJewelComplete(itemStack)) serializedItem += " Jewel: " +
					EnchantManager.getEnchant(nbtItem.getString(NBTTag.ITEM_JEWEL_ENCHANT.getRef())).getDisplayName();
			serializedItem += " Enchants:";
			for(Map.Entry<PitEnchant, Integer> entry : EnchantManager.getEnchantsOnItem(itemStack).entrySet()) {
				serializedItem += " " + entry.getKey().getDisplayName() + " " + entry.getValue();
			}
		}
		if(!itemMeta.hasDisplayName()) return addBraces(serializedItem);
		serializedItem += " " + ChatColor.stripColor(itemMeta.getDisplayName());
		return addBraces(serializedItem);
	}

	public static String addBraces(String string) {
		return "{" + string + "}";
	}

	public static boolean hasSpaceForItem(Player player, ItemStack itemStack) {
		if(itemStack == null) return false;
		if(itemStack.getType() == Material.AIR) return true;

		int emptySlots = getEmptyInventorySlots(player);
		if(emptySlots != 0) return true;

		int itemsToFit = itemStack.getAmount();
		int maxStackSize = itemStack.getType().getMaxStackSize();
		for(ItemStack inventoryStack : player.getInventory()) {
			if(isAirOrNull(inventoryStack) || !inventoryStack.isSimilar(itemStack)) continue;
			int space = maxStackSize - inventoryStack.getAmount();
			if(space >= itemsToFit) return true;
			itemsToFit -= space;
		}
		return false;
	}

	public static int getEmptyInventorySlots(Player player) {
		int emptySlots = 0;
		for(int i = 0; i < 36; i++) if(isAirOrNull(player.getInventory().getItem(i))) emptySlots++;
		return emptySlots;
	}

	public static String getStateMessage(boolean state) {
		return state ? "&a&lENABLED" : "&c&lDISABLED";
	}

	public static double randomOffset(double variance) {
		return Math.random() * variance - variance / 2;
	}

	public static double randomOffsetPositive(double variance) {
		return Math.random() * variance / 2;
	}

	public static String getBountyClaimedMessage(PitPlayer pitKiller, PitPlayer pitDead, String bounty) {
		PitCosmetic pitCosmetic = null;
		for(PitCosmetic equippedCosmetic : CosmeticManager.getEquippedCosmetics(pitKiller)) {
			if(equippedCosmetic.cosmeticType != CosmeticType.BOUNTY_CLAIM_MESSAGE) continue;
			pitCosmetic = equippedCosmetic;
			break;
		}

		String killerName = PlaceholderAPI.setPlaceholders(pitKiller.player, "%luckperms_prefix%" + pitKiller.player.getDisplayName());
		String deadName = PlaceholderAPI.setPlaceholders(pitDead.player, "%luckperms_prefix%" + pitDead.player.getDisplayName());

		String message = "&6&lCLAIM!&7 ";
		if(pitCosmetic != null) {
			message += pitCosmetic.getBountyClaimMessage(killerName, deadName, bounty);
		} else {
			message += killerName + "&7 killed " + deadName + "&7 for " + bounty;
		}

		return message;
	}


	public static void addEnchantGlint(ItemStack itemStack) {
		itemStack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemStack.setItemMeta(itemMeta);
	}

	public static void removeEnchantGlint(ItemStack itemStack) {
		itemStack.removeEnchantment(Enchantment.WATER_WORKER);
	}

	public static String fetchUsernameFromMojang(String uuid) {
		try {
			// Make the HTTP request to the Mojang API
			URL url = new URL("https://api.mojang.com/user/profile/" + uuid);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			// Read the response from the Mojang API
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// Parse the JSON response to get the updated username=
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			return (String) jsonObject.get("name");
		} catch(Exception exception) {
			exception.printStackTrace();
			return "KyroKrypt";
		}
	}

//	public static Date convertToEST(Date date) {
//		DateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
//		formatter.setTimeZone(TimeZone.getTimeZone("EST"));
//		try {
//			return formatter.parse((formatter.format(date)));
//		} catch(ParseException exception) {
//			throw new RuntimeException();
//		}
//	}

	public static String ordinalWords(int num) {

		switch(num) {
			case 1:
				return "";
			case 2:
				return "second";
			case 3:
				return "third";
			case 4:
				return "fourth";
			case 5:
				return "fifth";
			case 6:
				return "sixth";
			case 7:
				return "seventh";
			case 8:
				return "eighth";
			case 9:
				return "ninth";
			case 10:
				return "tenth";
		}
		return "";
	}

	public static void applyPotionEffect(LivingEntity entity, PotionEffectType type, int duration, int amplifier, boolean ambient, boolean particles) {
		if(entity == null || amplifier < 0 || duration == 0) return;

		if(NonManager.getNon(entity) == null && entity instanceof Player) {
			Player player = (Player) entity;
			PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);
			if(pitPlayer.getMegastreak() instanceof Uberstreak) {
				List<Uberstreak.UberEffect> uberEffects = Uberstreak.getUberEffects(player);
				if(uberEffects.contains(Uberstreak.UberEffect.NO_SPEED) && type == PotionEffectType.SPEED)
					return;
			} else if(!(pitPlayer.getMegastreak() instanceof Overdrive) && pitPlayer.isOnMega() && type == PotionEffectType.SLOW)
				return;
		}

		for(PotionEffect potionEffect : entity.getActivePotionEffects()) {
			if(!potionEffect.getType().equals(type) || potionEffect.getAmplifier() > amplifier) continue;
			if(potionEffect.getAmplifier() == amplifier && potionEffect.getDuration() >= duration) continue;
			entity.removePotionEffect(type);
			break;
		}
		entity.addPotionEffect(new PotionEffect(type, duration, amplifier, ambient, particles));
		if(type == PotionEffectType.POISON) {
			if(HelmetManager.abilities.get(entity) != null) {
				HelmetManager.abilities.get(entity).onDeactivate();
			}
			HelmetManager.toggledPlayers.remove(entity);
		}
	}

	public static void playKillSound(PitPlayer pitPlayer) {
		playKillSound(pitPlayer, null);
	}

	public static void playKillSound(PitPlayer pitPlayer, PitCosmetic killEffect) {
		PitCosmetic pitCosmetic = null;
		for(PitCosmetic equippedCosmetic : CosmeticManager.getEquippedCosmetics(pitPlayer)) {
			if(equippedCosmetic.cosmeticType != CosmeticType.BOT_KILL_EFFECT) continue;
			pitCosmetic = equippedCosmetic;
			break;
		}

		if(pitCosmetic != null && pitCosmetic.preventKillSound) return;
		new BukkitRunnable() {
			int count = 0;

			@Override
			public void run() {

				switch(count) {
					case 0:
						if(killEffect instanceof IronKill) Sounds.IRON_1.play(pitPlayer.player);
						else Sounds.MULTI_1.play(pitPlayer.player);
						break;
					case 1:
						if(killEffect instanceof IronKill) Sounds.IRON_2.play(pitPlayer.player);
						else Sounds.MULTI_2.play(pitPlayer.player);
						break;
					case 2:
						if(killEffect instanceof IronKill) Sounds.IRON_3.play(pitPlayer.player);
						else Sounds.MULTI_3.play(pitPlayer.player);
						break;
					case 3:
						if(killEffect instanceof IronKill) Sounds.IRON_4.play(pitPlayer.player);
						else Sounds.MULTI_4.play(pitPlayer.player);
						break;
					case 4:
						if(killEffect instanceof IronKill) Sounds.IRON_5.play(pitPlayer.player);
						else Sounds.MULTI_5.play(pitPlayer.player);
						break;
				}

				if(++count > 5) cancel();
			}
		}.runTaskTimer(PitSim.INSTANCE, 0L, 2L);
	}

	/**
	 * Rounds damage and then converts to hearts.
	 * Should only be used for displaying, not calculation.
	 */
	public static String getHearts(double damage) {

		return roundString(damage / 2) + "\u2764";
	}

	/**
	 * Rounds a number to 2 decimal places and trims extra zeros.
	 * Should only be used for displaying, not calculation.
	 */
	public static String roundString(double number) {
		return new DecimalFormat("#,##0.##").format(number);
	}

	/**
	 * Converts to multiplier
	 */
	public static double getReductionMultiplier(double reduction) {
		return Math.min(Math.max(1 - (reduction / 100.0), 0), 1);
	}

	public static int linearEnchant(int level, double step, double base) {
		return (int) (level * step + base);
	}

	public static void sendTitle(Player player, String message, int length) {
		IChatBaseComponent chatTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" +
				ChatColor.translateAlternateColorCodes('&', message) + "\"}");

		PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle titleLength = new PacketPlayOutTitle(5, length, 5);

		((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
	}

	public static void sendSubTitle(Player player, String message, int length) {
		IChatBaseComponent chatSubTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" +
				ChatColor.translateAlternateColorCodes('&', message) + "\"}");

		PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, chatSubTitle);
		PacketPlayOutTitle subTitleLength = new PacketPlayOutTitle(5, length, 5);

		((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitle);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(subTitleLength);
	}

	public static void sendActionBar(Player player, String message) {
		PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" +
				ChatColor.translateAlternateColorCodes('&', message) + "\"}"), (byte) 2);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public static boolean isCritical(LivingEntity entity) {
		if(entity == null) return false;
		return entity.getFallDistance() > 0.0F &&
				!entity.isOnGround() &&
				!entity.isInsideVehicle() &&
				!entity.hasPotionEffect(PotionEffectType.BLINDNESS) &&
				entity.getLocation().getBlock().getType() != Material.LADDER &&
				entity.getLocation().getBlock().getType() != Material.VINE;
	}

	public static boolean isAirOrNull(ItemStack itemStack) {
		return itemStack == null || itemStack.getType() == Material.AIR;
	}

	public static void strikeLightningForPlayers(Location location, double radius) {
		List<Player> nearbyPlayers = new ArrayList<>();
		for(Entity nearbyEntity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
			if(!(nearbyEntity instanceof Player) || NonManager.getNon((Player) nearbyEntity) != null) continue;
			nearbyPlayers.add((Player) nearbyEntity);
		}
		for(Player lightningPlayer : LightningCommand.lightningPlayers) nearbyPlayers.remove(lightningPlayer);

		Player[] lightningPlayers = new Player[nearbyPlayers.size()];
		lightningPlayers = nearbyPlayers.toArray(lightningPlayers);
		strikeLightningForPlayers(location, lightningPlayers);
	}

	public static void strikeLightningForPlayers(Location location, Player... players) {
		World world = ((CraftWorld) location.getWorld()).getHandle();

		for(Player player : players) {
			EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
			entityPlayer.playerConnection.sendPacket(new PacketPlayOutSpawnEntityWeather(
					new EntityLightning(world, location.getX(), location.getY(), location.getZ(), false, false)));

			player.playSound(location, Sound.AMBIENCE_THUNDER, 10, 1);
			player.playSound(location, Sound.EXPLODE, 10, (float) (Math.random() * 0.2 + 0.6));
		}
	}

	public static long getRunnableOffset(int minutes) {
		return (long) (Math.random() * 20 * 60 * minutes);
	}

	public static Duration parseDuration(String durationString) throws Exception {
		durationString = durationString.toLowerCase().replaceAll(" ", "");
		Matcher matcher = Pattern.compile("(\\d+)([mhdw])").matcher(durationString);
		Duration duration = Duration.ZERO;
		while(matcher.find()) {
			int number = Integer.parseInt(matcher.group(1));
			String timeUnit = matcher.group(2);
			switch(timeUnit) {
				case "m":
					duration = duration.plus(Duration.ofMinutes(number));
					break;
				case "h":
					duration = duration.plus(Duration.ofHours(number));
					break;
				case "d":
					duration = duration.plus(Duration.ofDays(number));
					break;
				case "w":
					duration = duration.plus(Duration.ofDays(number).multipliedBy(7));
			}
		}
		if(duration.equals(Duration.ZERO)) throw new Exception();
		return duration;
	}

	public static HealEvent heal(LivingEntity entity, double amount, HealEvent.HealType healType, int max, PitEnchant pitEnchant) {
		if(entity == null) return null;
		if(HopperManager.isHopper(entity) && healType == HealEvent.HealType.ABSORPTION) return null;
		if(max == -1) max = Integer.MAX_VALUE;

		HealEvent healEvent = new HealEvent(entity, amount, healType, max);
		healEvent.pitEnchant = pitEnchant;
		Bukkit.getServer().getPluginManager().callEvent(healEvent);

		if(healType == HealEvent.HealType.HEALTH) {
			entity.setHealth(Math.min(entity.getHealth() + healEvent.getFinalHeal(), entity.getMaxHealth()));
		} else {
			EntityPlayer nmsPlayer = ((CraftPlayer) entity).getHandle();
			if(nmsPlayer.getAbsorptionHearts() < healEvent.max)
				nmsPlayer.setAbsorptionHearts(Math.min((float) (nmsPlayer.getAbsorptionHearts() + healEvent.getFinalHeal()), max));
		}
		return healEvent;
	}

	public static String ticksToTime(int ticks) {
		int seconds = (int) Math.floor(ticks / 20);
		int minutes = (int) Math.floor(seconds / 60);
		if(minutes != 0 && seconds != 0) return minutes + "m " + (seconds - (minutes * 60)) + "s";
		else if(seconds != 0) return seconds + "s";
		else return minutes + "s";
	}

	public static boolean isKyro(UUID uuid) {
		List<UUID> kyroAccounts = new ArrayList<>();
//		KyroKrypt
		kyroAccounts.add(UUID.fromString("01acbb49-6357-4502-81ca-e79f4b31a44e"));
//		BHunter
		kyroAccounts.add(UUID.fromString("a7d2e208-e475-40bf-94d7-f96c6e1238a8"));
//		UUIDSpoof
		kyroAccounts.add(UUID.fromString("1088c509-e8e0-4b7b-8faa-b9f3f72b06f6"));
//		PayForTruce
		kyroAccounts.add(UUID.fromString("777566ed-d4ad-4cf1-a4d9-0e37769357df"));
//		Fishduper
		kyroAccounts.add(UUID.fromString("1db946e6-edfe-42ac-9fd6-bf135aa5130e"));
		return kyroAccounts.contains(uuid);
	}

	public static boolean isWiji(UUID uuid) {
		List<UUID> wijiAccounts = new ArrayList<>();
//		wiji1
		wijiAccounts.add(UUID.fromString("5c06626c-66bf-48a9-84cb-f2683e6aca87"));
		return wijiAccounts.contains(uuid);
	}

	public static TextComponent createItemHover(ItemStack itemStack) {
		return createItemHover(itemStack, null);
	}

	public static TextComponent createItemHover(ItemStack itemStack, String prefix) {
		if(isAirOrNull(itemStack)) return null;

		ItemMeta itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
		String displayName = itemMeta.getDisplayName();
		if(displayName == null) {
			net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
			displayName = nmsStack.getName();
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(displayName);

		List<String> lore = itemMeta.getLore();
		if(itemMeta.hasLore()) {
			for(int i = 0; i < lore.size(); i++) {
				stringBuilder.append("\n");
				stringBuilder.append(lore.get(i));
			}
		} else {
			stringBuilder.append("\n").append(ChatColor.GRAY).append("Just a plain ").append(displayName);
		}

		BaseComponent[] hoverEventComponents = new BaseComponent[]{
				new TextComponent(String.valueOf(stringBuilder))
		};

		TextComponent hoverComponent = new TextComponent(ChatColor.translateAlternateColorCodes('&', (prefix != null ? prefix : "") + displayName));
		hoverComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverEventComponents));
		return hoverComponent;
	}

	public static Map<UUID, String> rankColorMap = new HashMap<>();

	public static String getRankColor(UUID uuid) {
		if(rankColorMap.containsKey(uuid)) return rankColorMap.get(uuid);
		try {
			String rankColor = PitSim.LUCKPERMS.getUserManager().loadUser(uuid).get().getCachedData().getMetaData().getPrefix();
//			TODO: Check-in with players to make sure this fixed it and it wasn't returning "null" as a string
			if(rankColor == null) throw new Exception();
			rankColorMap.put(uuid, rankColor);
			return rankColor;
		} catch(Exception ignored) {
			return "&7";
		}
	}

	public static String getRankString(UUID uuid) {
		try {
			String rank = PitSim.LUCKPERMS.getUserManager().loadUser(uuid).get().getCachedData().getMetaData().getPrimaryGroup();
			if(rank == null) throw new Exception();
			Group group = PitSim.LUCKPERMS.getGroupManager().getGroup(rank);
			if(group == null) throw new Exception();

			return "&8[" + getRankColor(uuid) + group.getDisplayName() + "&8]" + getRankColor(uuid);
		} catch(Exception ignored) {
			return "&8[&Member&8]&7";
		}
	}

	public static void createMeta(ItemStack itemStack) {
		if(itemStack.hasItemMeta()) return;
		ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
		itemStack.setItemMeta(itemMeta);
	}

	public static int getFallTime(int totalHeight) {
		final double gravity = -0.03999999910593033;
		final double drag = 0.9800000190734863;

		double locY = 0;
		double motY = 0;

		int ticks = 0;

		while(locY <= totalHeight) {
			locY += motY;
			motY *= drag;
			motY -= gravity;

			ticks++;
		}

		return ticks;
	}

	public static String s(double value) {
		return value == 1 ? "" : "s";
	}

	public static long hashLong(long value) {
		long hashedValue = value;
		hashedValue = (hashedValue ^ (hashedValue >>> 30)) * 0xbf58476d1ce4e5b9L;
		hashedValue = (hashedValue ^ (hashedValue >>> 27)) * 0x94d049bb133111ebL;
		hashedValue = hashedValue ^ (hashedValue >>> 31);
		return hashedValue;
	}

	public static int fromFixedPoint(double fixedPoint) {
		return (int) (fixedPoint * 32.0);
	}
}
