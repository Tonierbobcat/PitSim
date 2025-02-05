package net.pitsim.spigot.controllers;

import dev.kyro.arcticapi.misc.AOutput;
import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.brewing.PotionManager;
import net.pitsim.spigot.controllers.objects.Hopper;
import net.pitsim.spigot.controllers.objects.PitPlayer;
import net.pitsim.spigot.misc.Misc;
import net.pitsim.spigot.misc.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PortalManager implements Listener {

	@EventHandler
	public void onPortal(EntityPortalEvent event) {
		if(event.getEntity() instanceof Player) return;
		event.setCancelled(true);
	}

	@EventHandler
	public void onPortal(PlayerPortalEvent event) {
		if(event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return;
		event.setCancelled(true);

		Player player = event.getPlayer();
		attemptServerSwitch(player);
	}

	public static void attemptServerSwitch(Player player) {
		PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);
		if(pitPlayer.prestige < 5 && !player.isOp()) {
			player.setVelocity(new Vector(3, 1, 0));
			AOutput.error(player, "&5&lDARKZONE &7You must be atleast prestige &eV &7to enter!");
			Sounds.NO.play(player);
			return;
		}

		if(pitPlayer.isOnMega() && !player.isOp()) {
			player.setVelocity(new Vector(3, 1, 0));
			AOutput.error(player, "&5&lDARKZONE &7You cannot be on a megastreak and enter the darkzone!");
			Sounds.NO.play(player);
			return;
		}

		if(CombatManager.isInCombat(player) && !player.isOp()) {
			player.setVelocity(new Vector(3, 1, 0));
			AOutput.error(player, "&5&lDARKZONE &7You cannot be in combat and enter the darkzone!");
			Sounds.NO.play(player);
			return;
		}

		if(HopperManager.isHopper(player)) return;

		boolean hasHopper = false;
		for(Hopper hopper : HopperManager.hopperList) {
			if(hopper.target != player) continue;
			hasHopper = true;
			break;
		}
		if(hasHopper && !player.isOp()) {
			player.setVelocity(new Vector(3, 1, 0));
			AOutput.error(player, "&c&lYOU WISH!&7 Kill that hopper first :P");
			Sounds.NO.play(player);
			return;
		}

		if(PitSim.getStatus() == PitSim.ServerStatus.STANDALONE) {
			Location playerLoc = player.getLocation();
			PotionManager.bossBars.remove(player);

			Location teleportLoc;
			var world = PitSim.DARKZONE();
			if(player.getWorld() != world) {
				teleportLoc = playerLoc.clone().add(235, 40, -97);
				teleportLoc.setWorld(world);
				teleportLoc.setX(173);
				teleportLoc.setY(92);
				teleportLoc.setZ(-94);
			} else {
				World destination = MapManager.currentMap.world;
				teleportLoc = MapManager.currentMap.getStandAlonePortalRespawn();
				teleportLoc.setWorld(destination);
				teleportLoc.setY(72);
			}

			if(teleportLoc.getYaw() > 0 || teleportLoc.getYaw() < -180) teleportLoc.setYaw(-teleportLoc.getYaw());
			teleportLoc.add(3, 0, 0);

			player.teleport(teleportLoc);
			player.setVelocity(new Vector(1.5, 1, 0).multiply(0.25));

			PitPlayer.getPitPlayer(player).updateMaxHealth();
			player.setHealth(player.getMaxHealth());

			if(player.getWorld() == PitSim.DARKZONE()) {
				Misc.sendTitle(player, "&d&k||&5&lDarkzone&d&k||", 40);
				Misc.sendSubTitle(player, "", 40);
				AOutput.send(player, "&7You have been sent to the &d&k||&5&lDarkzone&d&k||&7.");
			} else {
				Misc.sendTitle(player, "&a&lOverworld", 40);
				Misc.sendSubTitle(player, "", 40);
				AOutput.send(player, "&7You have been sent to the &a&lOverworld&7.");

				MusicManager.stopPlaying(player);
			}
			return;
		}

		LobbySwitchManager.setSwitchingPlayer(player);

		if(PitSim.getStatus().isDarkzone()) {
			ProxyMessaging.switchPlayer(player, 0);
		} else {
			ProxyMessaging.darkzoneSwitchPlayer(player, 0);
		}
	}

	@EventHandler
	public static void onTp(PlayerTeleportEvent event) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(!MapManager.inDarkzone(event.getPlayer())) MusicManager.stopPlaying(event.getPlayer());
			}
		}.runTaskLater(PitSim.INSTANCE, 10);
	}
}
