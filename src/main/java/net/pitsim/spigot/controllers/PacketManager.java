package net.pitsim.spigot.controllers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.SortedPacketListenerList;
import com.comphenix.protocol.injector.packet.PacketInjector;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import dev.kyro.arcticapi.misc.AOutput;
import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.darkzone.abilities.CageAbility;
import net.pitsim.spigot.darkzone.altar.AltarManager;
import net.pitsim.spigot.darkzone.altar.AltarPedestal;
import net.pitsim.spigot.auction.AuctionDisplays;
import net.pitsim.spigot.controllers.objects.PitPlayer;
import net.pitsim.spigot.misc.effects.PacketBlock;
import net.pitsim.spigot.misc.effects.SelectiveDrop;
import net.pitsim.spigot.misc.packets.*;
import net.pitsim.spigot.tutorial.DarkzoneTutorial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;

public class PacketManager implements Listener {
	public static ProtocolManager protocolManager;

	public static Map<PacketBlock, List<Player>> suppressedLocations = new HashMap<>();

	public PacketManager() {
		protocolManager = ProtocolLibrary.getProtocolManager();

		// Debug log to confirm initialization
		AOutput.log("Initializing PacketManager...");

		try {
			Set<PacketListener> registeredListeners = (Set<PacketListener>) getFieldValue(protocolManager, "registeredListeners");
			AOutput.log("Found " + registeredListeners.size() + " registered listeners.");

			for (PacketListener listener : new ArrayList<>(registeredListeners)) {
				try {
					if (listener.getPlugin() == Bukkit.getPluginManager().getPlugin("PremiumVanish") &&
							listener.getSendingWhitelist().getTypes().contains(PacketType.Play.Server.PLAYER_INFO) &&
							listener.getClass().getName().contains("SilentOpenChestPacketAdapters")) {
						removePacketListener(listener);
						AOutput.log("Removed packet listener from " + listener.getPlugin().getName());
					}
				} catch (Exception e) {
					AOutput.log("Error processing listener: " + listener.getClass().getName());
					e.printStackTrace();
				}
			}

			// PacketListener for NAMED_SOUND_EFFECT
			protocolManager.addPacketListener(new PacketAdapter(PitSim.INSTANCE, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
				@Override
				public void onPacketSending(PacketEvent event) {
					try {
						Player player = event.getPlayer();
						String soundName = event.getPacket().getStrings().read(0);
						AOutput.log("Player " + player.getName() + " is sending sound: " + soundName);

						if (soundName.equals("mob.villager.idle") || soundName.equals("mob.rabbit.idle")) {
							event.setCancelled(true);
							AOutput.log("Cancelled sound: " + soundName);
						}

						Location auctions = AuctionDisplays.pedestalLocations[0];
						if (soundName.equals("mob.magmacube.big") && auctions.getWorld() == player.getWorld() && auctions.distance(player.getLocation()) < 50) {
							event.setCancelled(true);
							AOutput.log("Cancelled sound near auction location for player: " + player.getName());
						}

						if (soundName.equals("mob.endermen.stare")) {
							event.setCancelled(true);
							AOutput.log("Cancelled endermen stare sound.");
						}

					} catch (Exception e) {
						AOutput.log("Error handling NAMED_SOUND_EFFECT packet for player: " + event.getPlayer().getName());
						e.printStackTrace();
					}
				}
			});

			// Additional packet listeners for BLOCK_CHANGE and ENTITY_TELEPORT
			if (PitSim.status.isDarkzone()) {
				protocolManager.addPacketListener(new PacketAdapter(PitSim.INSTANCE, PacketType.Play.Server.BLOCK_CHANGE) {
					@Override
					public void onPacketSending(PacketEvent event) {
						try {
							WrapperPlayServerBlockChange wrapper = new WrapperPlayServerBlockChange(event.getPacket());
							Location location = wrapper.getLocation().toLocation(MapManager.getDarkzone());
							List<Player> viewers = null;

							for (Map.Entry<PacketBlock, List<Player>> entry : suppressedLocations.entrySet()) {
								Location stored = entry.getKey().getLocation();
								if (stored.getBlockX() == location.getBlockX() && stored.getBlockY() == location.getBlockY()
										&& stored.getBlockZ() == location.getBlockZ()) {
									viewers = entry.getValue();
								}
							}

							if (viewers == null) return;

							if (!viewers.contains(event.getPlayer())) return;

							event.setCancelled(true);

						} catch (Exception e) {
							AOutput.log("Error processing BLOCK_CHANGE packet");
							e.printStackTrace();
						}
					}
				});

				protocolManager.addPacketListener(new PacketAdapter(PitSim.INSTANCE, PacketType.Play.Server.ENTITY_TELEPORT) {
					@Override
					public void onPacketSending(PacketEvent event) {
						try {
							boolean disable = AltarManager.isInAnimation(event.getPlayer());
							if (!disable) return;

							WrapperPlayServerEntityTeleport wrapper = new WrapperPlayServerEntityTeleport(event.getPacket());
							Entity entity = wrapper.getEntity(event);
							if (!(entity instanceof ArmorStand)) return;

							for (AltarPedestal altarPedestal : AltarPedestal.altarPedestals) {
								if (altarPedestal.stand.getUniqueId().equals(entity.getUniqueId())) {
									event.setCancelled(true);
									AOutput.log("Cancelled ENTITY_TELEPORT for ArmorStand with ID: " + entity.getUniqueId());
									return;
								}
							}
						} catch (Exception e) {
							AOutput.log("Error processing ENTITY_TELEPORT packet");
							e.printStackTrace();
						}
					}
				});
			}

			// Add more packet listeners with debug and exception handling...
			protocolManager.addPacketListener(new PacketAdapter(PitSim.INSTANCE, PacketType.Play.Client.BLOCK_DIG) {
				@Override
				public void onPacketReceiving(PacketEvent event) {
					try {
						WrapperPlayClientBlockDig wrapper = new WrapperPlayClientBlockDig(event.getPacket());
						Location location = wrapper.getLocation().toLocation(MapManager.getDarkzone());
						List<Player> viewers = null;
						PacketBlock packetBlock = null;

						for (Map.Entry<PacketBlock, List<Player>> entry : suppressedLocations.entrySet()) {
							Location stored = entry.getKey().getLocation();
							if (stored.getBlockX() == location.getBlockX() && stored.getBlockY() == location.getBlockY()
									&& stored.getBlockZ() == location.getBlockZ()) {
								viewers = entry.getValue();
								packetBlock = entry.getKey();
							}
						}

						if (viewers == null || packetBlock == null) return;
						if (packetBlock.isRemoved()) return;

						for (List<PacketBlock> value : CageAbility.packetBlockMap.values()) {
							for (PacketBlock block : value) {
								if (block == packetBlock) return;
							}
						}

						if (!viewers.contains(event.getPlayer())) return;

						suppressedLocations.remove(packetBlock);
						PacketBlock finalPacketBlock = packetBlock;
						new BukkitRunnable() {
							@Override
							public void run() {
								finalPacketBlock.spawnBlock();
								AOutput.log("Spawned block at location: " + finalPacketBlock.getLocation());
							}
						}.runTask(PitSim.INSTANCE);

					} catch (Exception e) {
						AOutput.log("Error processing BLOCK_DIG packet for player: " + event.getPlayer().getName());
						e.printStackTrace();
					}
				}
			});

		} catch (Exception e) {
			AOutput.log("Error initializing PacketManager");
			e.printStackTrace();
		}
	}

	public static void removePacketListener(PacketListener listener) {
		SortedPacketListenerList inboundListeners = (SortedPacketListenerList) getFieldValue(protocolManager, "inboundListeners");
		SortedPacketListenerList outboundListeners = (SortedPacketListenerList) getFieldValue(protocolManager, "outboundListeners");
		Set<PacketListener> registeredListeners = (Set<PacketListener>) getFieldValue(protocolManager, "registeredListeners");

		if(registeredListeners.remove(listener)) {
			ListeningWhitelist outbound = listener.getSendingWhitelist();
			ListeningWhitelist inbound = listener.getReceivingWhitelist();
			List removed;
			if(outbound != null && outbound.isEnabled()) {
				removed = outboundListeners.removeListener(listener, outbound);
				if(!removed.isEmpty()) {
					unregisterPacketListenerInInjectors(removed);
				}
			}

			if(inbound != null && inbound.isEnabled()) {
				removed = inboundListeners.removeListener(listener, inbound);
				if(!removed.isEmpty()) {
					unregisterPacketListenerInInjectors(removed);
				}
			}
		}
	}

	private static void unregisterPacketListenerInInjectors(Collection<PacketType> packetTypes) {
		PlayerInjectionHandler playerInjectionHandler = (PlayerInjectionHandler) getFieldValue(protocolManager, "playerInjectionHandler");
		PacketInjector packetInjector = (PacketInjector) getFieldValue(protocolManager, "packetInjector");

		for(PacketType packetType : packetTypes) {
			if(packetType.getSender() == PacketType.Sender.SERVER) {
				playerInjectionHandler.removePacketHandler(packetType);
			} else if(packetType.getSender() == PacketType.Sender.CLIENT) {
				packetInjector.removePacketHandler(packetType);
			}
		}
	}

	public static Object getFieldValue(Object object, String fieldName) {
		try {
			Field field;
			field = object.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(object);
		} catch(Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
