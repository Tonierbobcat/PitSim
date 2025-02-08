package net.pitsim.spigot.storage;

import dev.kyro.arcticapi.misc.AOutput;
import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.controllers.objects.PitPlayer;
import net.pitsim.spigot.controllers.objects.PluginMessage;
import net.pitsim.spigot.enums.RankInformation;
import net.pitsim.spigot.events.MessageEvent;
import net.pitsim.spigot.events.PitJoinEvent;
import net.pitsim.spigot.events.PitQuitEvent;
import net.pitsim.spigot.inventories.view.ViewGUI;
import net.pitsim.spigot.misc.Misc;
import net.pitsim.spigot.misc.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class StorageManager implements Listener {
	public static final int MAX_ENDERCHEST_PAGES = 18;
	public static final int ENDERCHEST_ITEM_SLOTS = 36;
	public static final int OUTFITS = 9;

	protected static final List<StorageProfile> profiles = new ArrayList<>();
	protected static final List<StorageProfile> viewProfiles = new ArrayList<>();
	protected static final List<EditSession> editSessions = new ArrayList<>();

	public static List<UUID> frozenPlayers = new ArrayList<>();

	public static StorageProfile getProfile(Player player) {
		if(player == null) return null;
		for(StorageProfile profile : profiles) {
			if(profile.getUniqueID().equals(player.getUniqueId())) return profile;
		}

		StorageProfile profile = new StorageProfile(player.getUniqueId());
		profiles.add(profile);
		return profile;
	}

	public static StorageProfile getProfile(Inventory inventory) {
		if(inventory == null) return null;
		for(StorageProfile profile : profiles) {
			if(!profile.isLoaded()) continue;
			for(EnderchestPage enderchestPage : profile.getEnderchestPages()) {
				if(enderchestPage.getInventory().equals(inventory)) return profile;
			}
		}
		for(StorageProfile profile : viewProfiles) {
			if(!profile.isLoaded()) continue;
			for(EnderchestPage enderchestPage : profile.getEnderchestPages()) {
				if(enderchestPage.getInventory().equals(inventory)) return profile;
			}
		}

		return null;
	}

	public static StorageProfile getProfile(UUID uuid) {
		for(StorageProfile profile : profiles) {
			if(profile.getUniqueID().equals(uuid)) return profile;
		}

		System.out.println("Creating new profile!");
		StorageProfile profile = new StorageProfile(uuid);
		profiles.add(profile);

		return profile;
	}

	public static StorageProfile getViewProfile(UUID uuid) {

		for(StorageProfile profile : viewProfiles) {
			if(profile.getUniqueID().equals(uuid)) return profile;
		}

		return null;
	}

//	@EventHandler(priority = EventPriority.LOWEST)
//	public void onJoin(AsyncPlayerPreLoginEvent event) {
//		if(PitSim.getStatus() == PitSim.ServerStatus.STANDALONE) return;
//
//		UUID uuid = event.getUniqueId();
//		StorageProfile profile = getProfile(uuid);
//
//		if(!profile.isLoaded()) {
//			event.setKickMessage(ChatColor.RED + "An error occurred when loading your data. Please report this issue.");
//			event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
//		}
//	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PitJoinEvent event) {
		if(PitSim.getStatus() == PitSim.ServerStatus.STANDALONE) return;

		Player player = event.getPlayer();
		StorageProfile profile = getProfile(player);

//		Disabled because the deletion/clear method was disabled
//		if(hasItem || !Misc.isAirOrNull(player.getInventory().getHelmet()) ||
//				!Misc.isAirOrNull(player.getInventory().getChestplate()) ||
//				!Misc.isAirOrNull(player.getInventory().getLeggings()) ||
//				!Misc.isAirOrNull(player.getInventory().getBoots())) {
//			Misc.alertDiscord("@everyone " + player.getName() + " logged in to server " + PitSim.serverName + " with items in their inventory");
//		}

		profile.initializePlayerInventory(player);
	}

	public static void quitInitiate(Player player) {
		StorageProfile profile = getProfile(player);

		profile.setInventory(player.getInventory().getContents());
		profile.setArmor(player.getInventory().getArmorContents());
	}

	public static void quitCleanup(Player player) {
		if(PitSim.getStatus() == PitSim.ServerStatus.STANDALONE) return;

		StorageProfile profile = getProfile(player);

		if(!isBeingEdited(player.getUniqueId())) profiles.remove(profile);
//		File file = new File("world/playerdata/" + player.getUniqueId().toString() + ".dat");
//		file.delete();

//		player.getInventory().clear();
//		player.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR),
//		new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
	}

	public static boolean isEditing(Player player) {
		for(EditSession editSession : editSessions) {
			if(editSession.getStaffMember() == player) return true;
		}
		return false;
	}

	public static boolean isEditing(UUID uuid) {
		for(EditSession editSession : editSessions) {
			if(editSession.getStaffMember().getUniqueId().equals(uuid)) return true;
		}
		return false;
	}

	public static boolean isBeingEdited(UUID uuid) {
		for(EditSession editSession : editSessions) {
			if(editSession.getPlayerUUID().equals(uuid)) return true;
		}
		return false;
	}

	public static EditSession getSession(Player staff) {
		for(EditSession editSession : editSessions) {
			if(editSession.getStaffMember() == staff) return editSession;
		}
		throw new RuntimeException();
	}

	public static EditSession getSession(UUID playerUUID) {
		for(EditSession editSession : editSessions) {
			if(editSession.getPlayerUUID().equals(playerUUID)) return editSession;
		}
		return null;
	}

	@EventHandler
	public void onPluginMessage(MessageEvent event) {
		PluginMessage message = event.getMessage();
		List<String> strings = message.getStrings();
		List<Boolean> booleans = message.getBooleans();

		if(strings.size() < 2) return;

		if(strings.get(0).equals("SAVE CONFIRMATION")) {
			UUID uuid = UUID.fromString(strings.get(1));

			StorageProfile profile = getProfile(uuid);

			profile.receiveSaveConfirmation(message);
		} else if(strings.get(0).equals("PLAYER DATA")) {
			strings.remove(0);
			UUID uuid = UUID.fromString(strings.remove(0));

			if(booleans.size() > 0 && booleans.remove(0)) {
				StorageProfile profile = new StorageProfile(uuid);
				profile.loadData(message, true);

				new BukkitRunnable() {
					@Override
					public void run() {
						for(Map.Entry<UUID, ViewGUI> entry : ViewGUI.viewGUIs.entrySet()) {
							if(entry.getValue().target.getUniqueID().equals(uuid)) {
								Player player = Bukkit.getPlayer(entry.getKey());
								if(player == null || !player.isOnline()) continue;
								player.closeInventory();
								ViewGUI gui = new ViewGUI(player, profile, profile.getUniqueID(), entry.getValue().name);
								gui.open();
							}
						}
					}
				}.runTask(PitSim.INSTANCE);
				viewProfiles.removeIf(p -> p.getUniqueID().equals(uuid));

				viewProfiles.add(profile);
				System.out.println("Added view profile");
				return;
			}

			StorageProfile profile = getProfile(uuid);
			profile.loadData(message, false);

			int index = PitSim.status.isOverworld() ? profile.getDefaultOverworldSet() : profile.getDefaultDarkzoneSet();
			if(index != -1) {
				Outfit outfit = profile.getOutfits()[index];
				outfit.equip(false);
			}
		} else if(strings.get(0).equals("PROMPT EDIT MENU")) {
			UUID staffUUID = UUID.fromString(strings.get(1));

			Player player = Bukkit.getPlayer(staffUUID);
			if(player == null) return;

			UUID playerUUID = UUID.fromString(strings.get(2));
			boolean isOnline = message.getBooleans().get(0);
			String serverName = strings.get(3);

			for(EditSession editSession : editSessions) {
				if(editSession.getPlayerUUID() != playerUUID) continue;

				editSession.delete();
				Misc.alertDiscord("@everyone EDIT SESSION CREATION ERROR! " + playerUUID + " already existed in edit session list");
			}

			new BukkitRunnable() {
				@Override
				public void run() {
					editSessions.add(new EditSession(player, playerUUID, serverName, isOnline));
				}
			}.runTask(PitSim.INSTANCE);
		}
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		StorageProfile profile = getProfile(player);
		if(profile.isLoaded() && profile.isSaving()) event.setCancelled(true);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		StorageProfile profile = getProfile(player);
		if(profile.isLoaded() && profile.isSaving()) event.setCancelled(true);
	}

	@EventHandler
	public void onClick(InventoryDragEvent event) {
		StorageProfile profile = getProfile(event.getInventory());
		if(profile == null) return;
		if(!profile.isLoaded()) return;
		if(profile.isSaving() || viewProfiles.contains(profile)) event.setCancelled(true);
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		int slot = event.getSlot();

		StorageProfile profile;
		if(isEditing(player)) {
			profile = Objects.requireNonNull(getSession(player)).getStorageProfile();
		} else profile = getProfile(event.getClickedInventory());

		StorageProfile topProfile = getProfile(event.getView().getTopInventory());
		if(profile == null && topProfile != null && viewProfiles.contains(topProfile)) event.setCancelled(true);

		if(profile == null || !profile.isLoaded()) {
			if(event.getView().getTopInventory() == event.getClickedInventory() && event.getInventory().getName().contains("Enderchest")) event.setCancelled(true);
			return;
		}

		if(profile.isLoaded() && profile.isSaving()) {
			event.setCancelled(true);
			return;
		}

		ViewGUI viewGUI = ViewGUI.viewGUIs.get(player.getUniqueId());

		for(EnderchestPage enderchestPage : profile.getEnderchestPages()) {
			Inventory inventory = enderchestPage.getInventory();
			if(!inventory.equals(event.getClickedInventory())) continue;

			if(slot == ENDERCHEST_ITEM_SLOTS + 9 && enderchestPage.getIndex() > 0) {
				if(isEditing(player)) getSession(player).playerClosed = false;
				if(viewGUI != null) viewGUI.playerClosed = false;
				player.openInventory(profile.getEnderchestPage(enderchestPage.getIndex() - 1).getInventory());
				if(isEditing(player)) getSession(player).playerClosed = true;
				if(viewGUI != null) viewGUI.playerClosed = true;
			} else if(slot == ENDERCHEST_ITEM_SLOTS + 17 && enderchestPage.getIndex() + 1 < MAX_ENDERCHEST_PAGES) {
				RankInformation rank = RankInformation.getRank(player);
				if(enderchestPage.getIndex() + 1 < rank.enderchestPages || isEditing(player)) {
					if(isEditing(player)) getSession(player).playerClosed = false;
					if(viewGUI != null) viewGUI.playerClosed = false;
					RankInformation rankInformation = RankInformation.getRank(profile.getUniqueID());
					if(rankInformation.enderchestPages > enderchestPage.getIndex() + 1 || isEditing(player))
						player.openInventory(profile.getEnderchestPage(enderchestPage.getIndex() + 1).getInventory());
					else Sounds.ERROR.play(player);
					if(isEditing(player)) getSession(player).playerClosed = true;
					if(viewGUI != null) viewGUI.playerClosed = true;
				}
			} else if(slot == ENDERCHEST_ITEM_SLOTS + 13) {
				if(StorageManager.viewProfiles.contains(profile)) {
					if(viewGUI != null) viewGUI.playerClosed = false;
					if(viewGUI != null) viewGUI.mainViewPanel.openPanel(new EnderchestPanel(viewGUI, viewGUI.target));
				} else {
					if(isEditing(player)) getSession(player).playerClosed = false;
					new EnderchestGUI(player, profile.getUniqueID()).open();
					if(isEditing(player)) getSession(player).playerClosed = true;
				}
			}

			if(slot > 8 && slot < 45) {
				if(StorageManager.viewProfiles.contains(profile)) event.setCancelled(true);
				return;
			}

			event.setCancelled(true);
			player.updateInventory();
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();

		ViewGUI viewGUI = ViewGUI.viewGUIs.get(player.getUniqueId());
		if(viewGUI != null) {
			if(viewGUI.playerClosed) {
				ViewGUI.viewGUIs.remove(player.getUniqueId());
				return;
			}
		}

		if(isEditing(player)) {
			EditSession session = getSession(player);
			StorageProfile profile = session.getStorageProfile();

			for(EnderchestPage enderchestPage : profile.getEnderchestPages()) {
				if(!enderchestPage.getInventory().equals(event.getInventory())) continue;
				if(session.playerClosed) session.end();
			}
		}
	}

	@EventHandler
	public void onQuit(PitQuitEvent event) {
		Player player = event.getPlayer();

		EditSession endSession = null;
		for(EditSession editSession : new ArrayList<>(editSessions)) {
			if(editSession.getPlayerUUID().equals(player.getUniqueId()) && editSession.getEditType() == EditType.ONLINE) {
				endSession = editSession;
				AOutput.error(editSession.getStaffMember(), "&cYour session ended because the player logged out!");
				editSession.getStaffMember().closeInventory();
			}
			if(editSession.getStaffMember() == player) endSession = editSession;
		}
		if(endSession != null) endSession.end();
	}
}
