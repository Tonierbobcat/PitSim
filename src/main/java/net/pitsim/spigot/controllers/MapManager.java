package net.pitsim.spigot.controllers;

import dev.kyro.arcticapi.misc.AOutput;
import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.controllers.objects.PitMap;
import net.pitsim.spigot.events.PlayerSpawnCommandEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class MapManager implements Listener {
	public static List<PitMap> mapList = new ArrayList<>();
	public static PitMap currentMap;

	public static Location darkzoneSpawn = new Location(getDarkzone(), 180.5, 91, -93.5, -90, 0);
	public static Location kyroDarkzoneSpawn = new Location(getDarkzone(), 310, 69, -136, -90, 0);
//	public static Location kyroDarkzoneSpawn = new Location(getDarkzone(), 353, 18, 10, 78, 6);

	public static PitMap registerMap(PitMap pitMap) {
		mapList.add(pitMap);
		return pitMap;
	}

	public static void setMap(PitMap pitMap) {
		currentMap = pitMap;
	}

	@EventHandler
	public void onSpawn(PlayerSpawnCommandEvent event) {
		Player player = event.getPlayer();
		if(player.getWorld() != MapManager.getDarkzone()) return;
		event.setCancelled(true);
		AOutput.error(event.getPlayer(), "&c&c&lERROR!&7 You cannot do that in the darkzone!");
	}

	public static PitMap getMap(String refName) {
		for(PitMap pitMap : mapList) {
			if(pitMap.world == null) return mapList.get(0);
			if(pitMap.world.getName().equalsIgnoreCase(refName)) return pitMap;
		}
		return null;
	}

	public static PitMap getNextMap(PitMap pitMap) {
		List<PitMap> applicableMaps = new ArrayList<>();
		for(PitMap map : mapList) if(map.rotationDays != -1) applicableMaps.add(map);
		int index = applicableMaps.indexOf(pitMap);
		return applicableMaps.get((index + 1) % applicableMaps.size());
	}

	public static World getTutorial() {
		return PitSim.TUTORIAL();
	}

	public static World getDarkzone() {
		return PitSim.DARKZONE();
	}

	public static Location getDarkzoneSpawn() {
		return darkzoneSpawn;
	}

	public static boolean inDarkzone(LivingEntity player) {
		if(player == null) return false;
		return inDarkzone(player.getLocation());
	}

	public static boolean inDarkzone(Location location) {
		if(location == null) return false;
		return location.getWorld() == getDarkzone();
	}

	public static Location getAuctionExitHolo() {
		return new Location(getDarkzone(), 178.512, 52.164, -1003.274);
	}

	public static Location getSkillsHolo() {
		return new Location(getDarkzone(), 188.498, 93.800, -84.700);
	}

	public static Location getMarketHolo() {
		return new Location(getDarkzone(), 203.993, 93.800, -84.700);
	}

	public static Location getShopHolo() {
		return new Location(getDarkzone(), 201.029, 93.800, -84.638);
	}

	public static Location getEnderChestHolo() {
		return new Location(getDarkzone(), 197.524, 94.060, -105.529);
	}

	public static Location getTaintedCrateHolo() {
		return new Location(getDarkzone(), 207.500, 94.800, -105.500);
	}


}
