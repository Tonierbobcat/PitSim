package net.pitsim.spigot.controllers;

import dev.kyro.arcticapi.misc.AOutput;
import net.pitsim.spigot.ChatUtils;
import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.darkzone.*;
import net.pitsim.spigot.controllers.objects.Shield;
import net.pitsim.spigot.darkzone.progression.ProgressionManager;
import net.pitsim.spigot.darkzone.progression.SkillBranch;
import net.pitsim.spigot.darkzone.progression.skillbranches.DefenceBranch;
import net.pitsim.spigot.controllers.objects.Non;
import net.pitsim.spigot.controllers.objects.PitEnchant;
import net.pitsim.spigot.controllers.objects.PitPlayer;
import net.pitsim.spigot.cosmetics.CosmeticManager;
import net.pitsim.spigot.cosmetics.CosmeticType;
import net.pitsim.spigot.cosmetics.PitCosmetic;
import net.pitsim.spigot.enchants.overworld.Regularity;
import net.pitsim.spigot.enchants.overworld.Singularity;
import net.pitsim.spigot.enchants.overworld.Telebow;
import net.pitsim.spigot.enchants.tainted.chestplate.Persephone;
import net.pitsim.spigot.enchants.tainted.uncommon.ShieldBuster;
import net.pitsim.spigot.enums.KillModifier;
import net.pitsim.spigot.enums.KillType;
import net.pitsim.spigot.enums.NonTrait;
import net.pitsim.spigot.events.AttackEvent;
import net.pitsim.spigot.events.KillEvent;
import net.pitsim.spigot.events.WrapperEntityDamageEvent;
import net.pitsim.spigot.misc.ArmorReduction;
import net.pitsim.spigot.misc.Misc;
import net.pitsim.spigot.misc.Sounds;
import net.pitsim.spigot.upgrades.KillSteal;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;

public class DamageManager implements Listener {
	public static List<LivingEntity> hitCooldownList = new ArrayList<>();
	public static List<LivingEntity> hopperCooldownList = new ArrayList<>();
	public static List<LivingEntity> nonHitCooldownList = new ArrayList<>();
	public static List<LivingEntity> bossHitCooldown = new ArrayList<>();

	public static Map<Projectile, Map<PitEnchant, Integer>> projectileMap = new HashMap<>();
	public static Map<Entity, LivingEntity> hitTransferMap = new HashMap<>();
	public static Map<LivingEntity, AttackInfo> attackInfoMap = new HashMap<>();

	static {
		new BukkitRunnable() {
			@Override
			public void run() {
				for(Map.Entry<Projectile, Map<PitEnchant, Integer>> entry : new ArrayList<>(projectileMap.entrySet())) {
					if(entry.getKey().isDead()) projectileMap.remove(entry.getKey());
				}
			}
		}.runTaskTimer(PitSim.INSTANCE, 0L, 1L);
	}

	public static void createIndirectAttack(LivingEntity fakeAttacker, LivingEntity defender, double damage) {
		createIndirectAttack(fakeAttacker, defender, damage, null, null);
	}

	public static void createIndirectAttack(LivingEntity fakeAttacker, LivingEntity defender, double damage,
											Map<PitEnchant, Integer> overrideAttackerEnchantMap,
											Map<PitEnchant, Integer> overrideDefenderEnchantMap) {
		createIndirectAttack(fakeAttacker, defender, damage, overrideAttackerEnchantMap, overrideDefenderEnchantMap, null);
	}

	public static void createIndirectAttack(LivingEntity fakeAttacker, LivingEntity defender, double damage,
											Map<PitEnchant, Integer> overrideAttackerEnchantMap,
											Map<PitEnchant, Integer> overrideDefenderEnchantMap,
											Consumer<AttackEvent.Apply> callback) {
		assert defender != null;
		if(!Misc.isValidMobPlayerTarget(defender)) return;

		attackInfoMap.put(defender, new AttackInfo(AttackInfo.AttackType.FAKE_INDIRECT, fakeAttacker,
				overrideAttackerEnchantMap, overrideDefenderEnchantMap, callback));
		EntityDamageEvent event = new EntityDamageEvent(defender, EntityDamageEvent.DamageCause.CUSTOM, damage);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()) defender.damage(event.getDamage());
	}

	public static void createDirectAttack(LivingEntity attacker, LivingEntity defender, double damage) {
		createDirectAttack(attacker, defender, damage, null, null, null);
	}

	public static void createDirectAttack(LivingEntity attacker, LivingEntity defender, double damage,
										  Map<PitEnchant, Integer> overrideAttackerEnchantMap,
										  Map<PitEnchant, Integer> overrideDefenderEnchantMap) {
		createDirectAttack(attacker, defender, damage, overrideAttackerEnchantMap, overrideDefenderEnchantMap, null);
	}

	public static void createDirectAttack(LivingEntity attacker, LivingEntity defender, double damage,
										  Map<PitEnchant, Integer> overrideAttackerEnchantMap,
										  Map<PitEnchant, Integer> overrideDefenderEnchantMap,
										  Consumer<AttackEvent.Apply> callback) {
		assert attacker != null && defender != null;
		if(!Misc.isValidMobPlayerTarget(defender)) return;

		attackInfoMap.put(defender, new AttackInfo(AttackInfo.AttackType.FAKE_DIRECT, null,
				overrideAttackerEnchantMap, overrideDefenderEnchantMap, callback));

		defender.damage(damage, attacker);
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		if(event.getCause() != EntityDamageEvent.DamageCause.WITHER) return;
		Player player = (Player) event.getEntity();
		if(event.getFinalDamage() >= player.getHealth()) {
			event.setCancelled(true);
			killPlayer(player);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onHeal(EntityRegainHealthEvent event) {
		if(!(event.getEntity() instanceof Player) || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.CUSTOM)
			return;
		Player player = (Player) event.getEntity();
		event.setCancelled(true);

		PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);
		pitPlayer.heal(event.getAmount());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onBowShoot(ProjectileLaunchEvent event) {
		if(!(event.getEntity().getShooter() instanceof Player)) return;
		Projectile projectile = event.getEntity();
		Player shooter = (Player) projectile.getShooter();
		projectileMap.put(projectile, EnchantManager.getEnchantsOnPlayer(shooter));
	}

	public void transferHit(LivingEntity attacker, Entity realDamager, LivingEntity defender, double damage) {
		if(attacker != realDamager) hitTransferMap.put(realDamager, attacker);
		defender.damage(damage, attacker);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAttack(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof LivingEntity)) return;
		WrapperEntityDamageEvent wrapperEvent = new WrapperEntityDamageEvent(event);
		onAttack(wrapperEvent);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAttack(EntityDamageEvent event) {
		if(event instanceof EntityDamageByEntityEvent) return;
		if(!(event.getEntity() instanceof LivingEntity)) return;
		WrapperEntityDamageEvent wrapperEvent = new WrapperEntityDamageEvent(event);
		onAttack(wrapperEvent);
	}

	public void onAttack(WrapperEntityDamageEvent event) {
		if(event.getEntity() == null) return;
		Entity realDamager = event.getDamager();
		LivingEntity attacker = getAttacker(event.getDamager());
		LivingEntity defender = event.getEntity();

		if(defender.isDead()) return;

		for(Map.Entry<Entity, LivingEntity> entry : DamageManager.hitTransferMap.entrySet()) {
			if(entry.getValue() != realDamager) continue;
			realDamager = entry.getKey();
			DamageManager.hitTransferMap.remove(entry.getKey());
			break;
		}

		if(PitSim.status.isDarkzone()) {
			PitMob attackerMob = DarkzoneManager.getPitMob(attacker);
			PitMob defenderMob = DarkzoneManager.getPitMob(defender);

			if(attackerMob != null && defenderMob != null) {
				event.setCancelled(true);
				return;
			}

			if(defenderMob != null) {
				try {
					event.getSpigotEvent().setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
				} catch(Exception ignored) {}
			}

			for(SubLevel subLevel : DarkzoneManager.subLevels) {
				for(PitMob pitMob : subLevel.mobs) {
					for(LivingEntity entity : pitMob.getNameTag().getEntities()) {
						if(entity == defender) {
							event.setCancelled(true);
							transferHit(attacker, realDamager, pitMob.getMob(), event.getDamage());
							return;
						}
						if(entity == attacker) {
							event.setCancelled(true);
							return;
						}
					}
				}
			}
		}

		if(defender instanceof ArmorStand) return;
		if(defender instanceof Slime && !(defender instanceof MagmaCube)) return;

		boolean fakeHit = false;

		Non attackingNon = NonManager.getNon(attacker);
		Non defendingNon = NonManager.getNon(defender);
//		Hit on non or by non
		if((attackingNon != null && nonHitCooldownList.contains(defender)) ||
				(attackingNon == null && defendingNon != null && hitCooldownList.contains(defender)) && !Regularity.toReg.contains(defender.getUniqueId()) &&
						!(realDamager instanceof Arrow)) {
			event.setCancelled(true);
			DamageManager.hitTransferMap.remove(realDamager);
			return;
		}
//		Regular player to player hit
		if(attackingNon == null && !Regularity.toReg.contains(defender.getUniqueId())) {
			fakeHit = hitCooldownList.contains(defender);
			if(hopperCooldownList.contains(defender) && HopperManager.isHopper(defender)) {
				event.setCancelled(true);
				return;
			}
		}

		if(bossHitCooldown.contains(defender)) {
			event.setCancelled(true);
			return;
		}

		if(Regularity.regCooldown.contains(defender.getUniqueId()) && !Regularity.toReg.contains(defender.getUniqueId())) {
			event.setCancelled(true);
			return;
		}

		if(!fakeHit) {
//			if(attackingNon == null) attacker.setHealth(Math.min(attacker.getHealth() + 1, attacker.getMaxHealth()));
			hitCooldownList.add(defender);
			hopperCooldownList.add(defender);
			nonHitCooldownList.add(defender);
			if(BossManager.isPitBoss(defender)) bossHitCooldown.add(defender);

			new BukkitRunnable() {
				int count = 0;

				@Override
				public void run() {
					if(++count == 15) cancel();

					if(count == 5) DamageManager.hitCooldownList.remove(defender);
					if(count == 10) DamageManager.hopperCooldownList.remove(defender);
					if(count == 15) DamageManager.nonHitCooldownList.remove(defender);
					if(count == 10) DamageManager.bossHitCooldown.remove(defender);
				}
			}.runTaskTimer(PitSim.INSTANCE, 0L, 1L);
		}

//		Reduce cpu load by not handling non v non
		if(attackingNon != null && defendingNon != null) {
			if(defender.getHealth() <= event.getSpigotEvent().getFinalDamage()) {
				defender.setHealth(defender.getMaxHealth());
			} else {
				defender.setHealth(defender.getHealth() - event.getSpigotEvent().getFinalDamage());
			}
			event.getSpigotEvent().setDamage(0);
			return;
		}

		if(attackingNon != null) {
//			Non damage
			double damage = attackingNon.traits.contains(NonTrait.IRON_STREAKER) ? 9.6 : 7;
			if(Misc.isCritical(attacker)) damage *= 1.5;
			event.getSpigotEvent().setDamage(damage);
		}

		AttackEvent.Pre preEvent;

		Map<PitEnchant, Integer> attackerEnchantMap = new HashMap<>();
		Map<PitEnchant, Integer> defenderEnchantMap = EnchantManager.getEnchantsOnPlayer(defender);
		if(realDamager instanceof Projectile) {
			for(Map.Entry<Projectile, Map<PitEnchant, Integer>> entry : projectileMap.entrySet()) {
				if(!entry.getKey().equals(realDamager)) continue;
				attackerEnchantMap = projectileMap.get(entry.getKey());
				break;
			}
		} if(realDamager instanceof LivingEntity) {
			attackerEnchantMap = EnchantManager.getEnchantsOnPlayer(attacker);
		}

		if(event.hasAttackInfo()) {
			AttackInfo attackInfo = event.getAttackInfo();
			if(attackInfo.hasOverrideAttackerEnchantMap()) attackerEnchantMap = attackInfo.getOverrideAttackerEnchantMap();
			if(attackInfo.hasOverrideDefenderEnchantMap()) defenderEnchantMap = attackInfo.getOverrideDefenderEnchantMap();
		}

//		Remove disabled enchants
		for(Map.Entry<PitEnchant, Integer> entry : new ArrayList<>(attackerEnchantMap.entrySet()))
			if(!entry.getKey().isEnabled()) attackerEnchantMap.remove(entry.getKey());
		for(Map.Entry<PitEnchant, Integer> entry : new ArrayList<>(defenderEnchantMap.entrySet()))
			if(!entry.getKey().isEnabled()) defenderEnchantMap.remove(entry.getKey());

		preEvent = new AttackEvent.Pre(event, realDamager, attackerEnchantMap, defenderEnchantMap, fakeHit);
		Bukkit.getServer().getPluginManager().callEvent(preEvent);
		if(preEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		AttackEvent.Apply applyEvent = new AttackEvent.Apply(preEvent);
		Bukkit.getServer().getPluginManager().callEvent(applyEvent);

		double finalDamage = handleAttack(applyEvent);

		AttackEvent.Post postEvent = new AttackEvent.Post(applyEvent, finalDamage);
		Bukkit.getServer().getPluginManager().callEvent(postEvent);
	}

	public static double handleAttack(AttackEvent.Apply attackEvent) {
//		AOutput.send(attackEvent.attacker, "Initial Damage: " + attackEvent.event.getDamage());

		if(PitSim.status.isDarkzone()) {
			PitMob attackerMob = DarkzoneManager.getPitMob(attackEvent.getAttacker());
			PitMob defenderMob = DarkzoneManager.getPitMob(attackEvent.getDefender());
			PitBoss attackerBoss = BossManager.getPitBoss(attackEvent.getAttacker());
			PitBoss defenderBoss = BossManager.getPitBoss(attackEvent.getDefender());

			if(attackerMob != null || attackerBoss != null) {
				attackEvent.selfTrueDamage /= DarkzoneBalancing.SPOOFED_HEALTH_INCREASE;
				attackEvent.selfVeryTrueDamage /= DarkzoneBalancing.SPOOFED_HEALTH_INCREASE;
			} else if(defenderMob != null || defenderBoss != null) {
				attackEvent.multipliers.add(1 / DarkzoneBalancing.SPOOFED_HEALTH_INCREASE);
				attackEvent.trueDamage /= DarkzoneBalancing.SPOOFED_HEALTH_INCREASE;
				attackEvent.veryTrueDamage /= DarkzoneBalancing.SPOOFED_HEALTH_INCREASE;
			}
		}

//		As strong as iron
		attackEvent.multipliers.add(ArmorReduction.getMissingReductionMultiplier(attackEvent.getDefender()));

//		New player defence
		if(PitSim.status.isOverworld() && attackEvent.isDefenderRealPlayer() && attackEvent.isAttackerRealPlayer() &&
				attackEvent.getDefender().getWorld() != MapManager.getDarkzone() &&
				attackEvent.getDefender().getLocation().distance(MapManager.currentMap.getMid()) < 12) {
			if(attackEvent.getDefenderPitPlayer().prestige < 10) {
				int minutesPlayed = attackEvent.getDefenderPitPlayer().stats.minutesPlayed;
				double reduction = Math.max(50 - (minutesPlayed / 8.0), 0);
				attackEvent.multipliers.add(Misc.getReductionMultiplier(reduction));
				attackEvent.trueDamage *= Misc.getReductionMultiplier(reduction);
			}
		}

		if(attackEvent.getWrapperEvent().hasAttackInfo()) {
			AttackInfo attackInfo = attackEvent.getWrapperEvent().getAttackInfo();
			if(attackInfo.getCallback() != null) attackInfo.getCallback().accept(attackEvent);
		}

		double damage = attackEvent.getFinalPitDamage();
		if(attackEvent.isDefenderRealPlayer()) {
			Shield defenderShield = attackEvent.getDefenderPitPlayer().shield;
			double multiplier = 2;
			multiplier *= ShieldBuster.getMultiplier(attackEvent.getAttackerPlayer());
			if(ProgressionManager.isUnlocked(attackEvent.getDefenderPitPlayer(), DefenceBranch.INSTANCE, SkillBranch.MajorUnlockPosition.LAST))
				multiplier *= Misc.getReductionMultiplier(DefenceBranch.getShieldDamageReduction());
			if(attackEvent.isAttackerRealPlayer() && ProgressionManager.isUnlocked(attackEvent.getDefenderPitPlayer(),
					DefenceBranch.INSTANCE, SkillBranch.MajorUnlockPosition.FIRST_PATH))
				multiplier *= Misc.getReductionMultiplier(DefenceBranch.getShieldDamageFromPlayersReduction());
			if(defenderShield.isActive()) damage = defenderShield.damageShield(damage, multiplier);
		}
//		Armor for fake indirect attacks
		if(attackEvent.getWrapperEvent().hasAttackInfo() &&
				attackEvent.getWrapperEvent().getAttackInfo().getAttackType() == AttackInfo.AttackType.FAKE_INDIRECT) {
			damage *= 1 - (ArmorReduction.getArmorPoints(attackEvent.getDefender()) * 0.04);
		}
		attackEvent.getWrapperEvent().getSpigotEvent().setDamage(damage);

		EntityLiving nmsDefender = ((CraftLivingEntity) attackEvent.getDefender()).getHandle();
		float absorption = nmsDefender.getAbsorptionHearts();
		if(absorption != 0) nmsDefender.setAbsorptionHearts(0);

		double finalDamage = Math.max(Singularity.getAdjustedFinalDamage(attackEvent), 0);
		attackEvent.getWrapperEvent().getSpigotEvent().setDamage(0);

		DamageIndicator.onAttack(attackEvent, finalDamage);
		BossManager.onAttack(attackEvent, finalDamage);

		if(absorption != 0) {
			if(absorption > finalDamage) {
				finalDamage = 0;
				absorption -= finalDamage;
			} else {
				finalDamage -= absorption;
				absorption = 0;
			}
			nmsDefender.setAbsorptionHearts(absorption);
		}

		if(attackEvent.trueDamage != 0 || attackEvent.veryTrueDamage != 0) {
			double finalHealth = attackEvent.getDefender().getHealth() - attackEvent.trueDamage - attackEvent.veryTrueDamage;
			if(Persephone.shouldPreventDeath(attackEvent.getDefenderPlayer())) finalHealth = Math.max(finalHealth, 1);
			if(finalHealth <= 0) {
				attackEvent.setCancelled(true);
				handleKill(attackEvent, attackEvent.getAttacker(), attackEvent.getDefender(), KillType.KILL);
				return 0;
			} else {
				attackEvent.getDefender().setHealth(Math.max(finalHealth, 0));
			}
		}

		if(attackEvent.selfTrueDamage != 0 || attackEvent.selfVeryTrueDamage != 0) {
			double finalHealth = attackEvent.getAttacker().getHealth() - attackEvent.selfTrueDamage - attackEvent.selfVeryTrueDamage;
			if(Persephone.shouldPreventDeath(attackEvent.getAttackerPlayer())) finalHealth = Math.max(finalHealth, 1);
			if(finalHealth <= 0) {
				attackEvent.setCancelled(true);
				handleKill(attackEvent, attackEvent.getDefender(), attackEvent.getAttacker(), KillType.KILL);
				return 0;
			} else {
				attackEvent.getAttacker().setHealth(Math.max(finalHealth, 0));
//				attackEvent.attacker.damage(0);
			}
		}

		if(attackEvent.isDefenderPlayer()) {
			PitPlayer pitPlayer = PitPlayer.getPitPlayer(attackEvent.getDefenderPlayer());
			pitPlayer.addDamage(attackEvent.getAttacker(), finalDamage + attackEvent.trueDamage);
		}

//		AOutput.send(attackEvent.attacker, "Final Damage: " + attackEvent.event.getDamage());
//		AOutput.send(attackEvent.attacker, "Final Damage: " + attackEvent.event.getFinalDamage());

		if(finalDamage + attackEvent.executeUnder >= attackEvent.getDefender().getHealth()) {
			if(Persephone.shouldPreventDeath(attackEvent.getDefenderPlayer())) {
				attackEvent.getWrapperEvent().getSpigotEvent().setDamage(0);
				attackEvent.getDefender().setHealth(1);
			} else {
				attackEvent.setCancelled(true);
				boolean exeDeath = finalDamage < attackEvent.getDefender().getHealth();
				if(exeDeath) {
					handleKill(attackEvent, attackEvent.getAttacker(), attackEvent.getDefender(), KillType.KILL, KillModifier.EXECUTION);
				} else {
					handleKill(attackEvent, attackEvent.getAttacker(), attackEvent.getDefender(), KillType.KILL);
				}
			}
		} else {
			attackEvent.getDefender().setHealth(Math.min(attackEvent.getDefender().getHealth() - finalDamage, attackEvent.getDefender().getMaxHealth()));
		}
		return finalDamage;
	}

	public static LivingEntity getAttacker(Entity damager) {
		if(damager instanceof Projectile) return (LivingEntity) ((Projectile) damager).getShooter();
		if(damager instanceof Slime && !(damager instanceof MagmaCube)) return BlobManager.getOwner((Slime) damager);
		if(damager instanceof LivingEntity) return (LivingEntity) damager;
		return null;
	}

	public static void handleKill(AttackEvent attackEvent, LivingEntity killer, LivingEntity dead, KillType killType, KillModifier... killModifiers) {
		boolean killerIsPlayer = killer instanceof Player;
		boolean deadIsPlayer = dead instanceof Player;
		Player killerPlayer = killerIsPlayer ? (Player) killer : null;
		Player deadPlayer = deadIsPlayer ? (Player) dead : null;
		boolean killerIsRealPlayer = PlayerManager.isRealPlayer(killerPlayer);
		boolean deadIsRealPlayer = PlayerManager.isRealPlayer(deadPlayer);

		PitPlayer pitKiller = PitPlayer.getPitPlayer(killerPlayer);
		PitPlayer pitDead = PitPlayer.getPitPlayer(deadPlayer);
		Non killerNon = NonManager.getNon(killer);
		Non deadNon = NonManager.getNon(dead);
		PitMob killerMob = DarkzoneManager.getPitMob(killer);
		PitMob deadMob = DarkzoneManager.getPitMob(dead);

		KillEvent killEvent;

		killEvent = new KillEvent(attackEvent, killer, dead, killType, killModifiers);
		Bukkit.getServer().getPluginManager().callEvent(killEvent);
		killEvent.updateItems();

		if(killerIsRealPlayer && deadIsPlayer) EnchantManager.incrementKillsOnJewels(killerPlayer);

		if(deadIsPlayer && killType != KillType.FAKE_KILL) {
			EntityPlayer nmsPlayer = ((CraftPlayer) dead).getHandle();
			nmsPlayer.setAbsorptionHearts(0);

//			if(!LifeInsurance.isApplicable(deadPlayer) && !hasKillModifier(KillModifier.SELF_CHECKOUT, killModifiers))
//				loseLives(dead, killer);

			if(deadIsRealPlayer) pitDead.endKillstreak();
			Telebow.teleShots.removeIf(teleShot -> teleShot.getShooter().equals(dead));
		}

		if(killType != KillType.FAKE_KILL) {
			dead.setHealth(dead.getMaxHealth());
			new BukkitRunnable() {
				@Override
				public void run() {
					dead.setHealth(dead.getMaxHealth());
				}
			}.runTaskLater(PitSim.INSTANCE, 1L);
			dead.playEffect(EntityEffect.HURT);
			Sounds.DEATH_FALL.play(dead);
			Sounds.DEATH_FALL.play(dead);
			Regularity.toReg.remove(dead.getUniqueId());
		}

		if(killerIsRealPlayer) {
			if(deadNon != null || deadIsRealPlayer) pitKiller.incrementKills();
			if(deadMob != null) {
				Sounds.MOB_KILL.play(killerPlayer);
			} else {
				PitCosmetic botKill = CosmeticManager.getEquippedCosmetic(PitPlayer.getPitPlayer(killerPlayer), CosmeticType.BOT_KILL_EFFECT);
				Misc.playKillSound(pitKiller, botKill);
			}
		}

		if(PitSim.status.isOverworld()) {
			if(killerIsRealPlayer) {
				LevelManager.addXP(pitKiller.player, killEvent.getFinalXp());
				LevelManager.addGold(killEvent.getKillerPlayer(), (int) killEvent.getFinalGold());
			}
		} else {
			if(deadIsRealPlayer && killEvent.shouldLoseItems()) {
				int finalSouls = killEvent.getFinalSouls();
				if(finalSouls != 0) {
					pitDead.taintedSouls -= finalSouls;
					DarkzoneManager.createSoulExplosion(null, dead.getLocation(), finalSouls, finalSouls >= 50);
				}
			}
		}

		if(deadIsPlayer) {
			if(deadNon == null && dead.getWorld() != MapManager.getTutorial()) {
				Location spawnLoc = PitSim.getStatus() == PitSim.ServerStatus.DARKZONE ? MapManager.getDarkzoneSpawn() : MapManager.currentMap.getSpawn();

				if(killType != KillType.FAKE_KILL) dead.teleport(spawnLoc);
			} else if(deadNon != null) {
				deadNon.respawn(killType == KillType.FAKE_KILL);
			}
		} else {
			dead.remove();
		}

		if(killType != KillType.FAKE_KILL) {
			if(deadIsPlayer) {
				pitDead.bounty = 0;
				ChatTriggerManager.sendBountyInfo(pitDead);
			}
			for(PotionEffect potionEffect : dead.getActivePotionEffects()) {
				dead.removePotionEffect(potionEffect.getType());
			}
		}

		if(killerNon != null) {
			killerNon.rewardKill();
		}

		DecimalFormat df = new DecimalFormat("#,##0.##");
		String kill = null;
		if(deadMob != null) {
			kill = ChatUtils.parse("&a&lKILL!&7 on " + deadMob.getDisplayName());
		} else if(killType != KillType.DEATH && PitSim.status.isOverworld() && pitKiller != null) {
			double altarMultiplier = DarkzoneLeveling.getReductionMultiplier(pitKiller);
			String altarPercent = DarkzoneLeveling.getReductionPercent(pitKiller);

			PrestigeValues.PrestigeInfo info = PrestigeValues.getPrestigeInfo(pitKiller.prestige);
			int altarLevel = DarkzoneLeveling.getLevel(pitKiller.darkzoneData.altarXP);
			int difference = info.getDarkzoneLevel() - altarLevel;
			String color = difference > 0 ? "&c" : "&a";

			TextComponent hover = new TextComponent(ChatUtils.parse("&6" +
					df.format(killEvent.getFinalGold()) + "g &8(" + color + altarPercent + "%&8)"));

			String hoverText = ChatUtils.parse(difference > 0 ? "&7Go to the &5Darkzone &7to remove this debuff" : "&7The &5Darkzone &7rewards you for your sacrifices");
			hover.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));

			kill = ChatUtils.parse("&a&lKILL!&7 on %luckperms_prefix%" +
					(deadNon == null ? "%player_name%" : deadNon.displayName) + " &b+" + killEvent.getFinalXp() + "XP" +
					" &6+" + (altarMultiplier == 1 ? df.format(killEvent.getFinalGold()) + "g" : ""), killEvent.getDeadPlayer());

			TextComponent killComponent = new TextComponent(kill);
			if(altarMultiplier != 1) killComponent.addExtra(hover);
			if(killerPlayer != null && !pitKiller.killFeedDisabled) killerPlayer.spigot().sendMessage(killComponent);
			kill = null;
		}

		String death;
		String soulsLostString = "";
		if(PitSim.status.isDarkzone() && deadIsRealPlayer){
			int finalSouls = killEvent.getFinalSouls();
			if(finalSouls != 0) {
				soulsLostString = ChatUtils.parse(" &f-" + finalSouls + " soul" + Misc.s(finalSouls));
			}
		}
		if(killType == KillType.KILL && killerIsPlayer) {
			death = PlaceholderAPI.setPlaceholders(killEvent.getKillerPlayer(), "&c&lDEATH!&7 by %luckperms_prefix%" +
					(killerNon == null ? "%player_name%" : killerNon.displayName)) + soulsLostString;
		} else {
			death = "&c&lDEATH!" + soulsLostString;
		}

		String killActionBar = null;
		if(killType != KillType.DEATH && killerIsPlayer && deadMob == null) {
			killActionBar = "&7%luckperms_prefix%" + (deadNon == null ? "%player_name%" : deadNon.displayName) + " &a&lKILL!";
		}

		if(killerIsPlayer && !CitizensAPI.getNPCRegistry().isNPC(killer) && !pitKiller.killFeedDisabled && killType != KillType.DEATH) {
			if(kill != null) AOutput.send(killEvent.getKiller(), PlaceholderAPI.setPlaceholders(killEvent.getDeadPlayer(), kill));
			pitKiller.stats.mobsKilled++; // TODO: this is definitely the wrong spot
		}
		if(deadIsPlayer && !pitDead.killFeedDisabled && killType != KillType.FAKE_KILL && killEvent != null)
			AOutput.send(killEvent.getDead(), death);
		String actionBarPlaceholder;
		if(killActionBar != null) {
			assert killEvent != null;
			actionBarPlaceholder = PlaceholderAPI.setPlaceholders(killEvent.getDeadPlayer(), killActionBar);
			KillEvent finalKillEvent = killEvent;
			new BukkitRunnable() {
				@Override
				public void run() {
					ActionBarManager.sendActionBar(finalKillEvent.getKillerPlayer(), actionBarPlaceholder);
				}
			}.runTaskLater(PitSim.INSTANCE, 1L);
		}

		if(killType == KillType.KILL && deadIsPlayer) {
			double finalDamage = 0;
			for(Map.Entry<UUID, Double> entry : pitDead.recentDamageMap.entrySet()) finalDamage += entry.getValue();
			for(Map.Entry<UUID, Double> entry : new ArrayList<>(pitDead.recentDamageMap.entrySet())) {
				if(entry.getKey().equals(killEvent.getKiller().getUniqueId())) continue;

				Player assistPlayer = Bukkit.getPlayer(entry.getKey());
				if(assistPlayer == null) continue;
				double assistPercent = Math.max(Math.min(entry.getValue() / finalDamage, 1), 0);

				if(UpgradeManager.hasUpgrade(assistPlayer, KillSteal.INSTANCE) && !deadIsRealPlayer) {
					int tier = UpgradeManager.getTier(assistPlayer, KillSteal.INSTANCE);
					assistPercent += (tier * 10) / 100D;
					if(assistPercent >= 1) {
						fakeKill(assistPlayer, dead);
						continue;
					}
				}

				int assistXP = (int) Math.ceil(20 * assistPercent);
				double assistGold = 20 * assistPercent;

				PitPlayer assistPitPlayer = PitPlayer.getPitPlayer(assistPlayer);
				LevelManager.addXP(assistPitPlayer.player, assistXP);
				LevelManager.addGold(assistPlayer, (int) assistGold);

				Sounds.ASSIST.play(assistPlayer);
				String assist = "&a&lASSIST!&7 " + Math.round(assistPercent * 100) + "% on %luckperms_prefix%" +
						(deadNon == null ? "%player_name%" : deadNon.displayName) + " &b+" + assistXP + "XP" + " &6+" + df.format(assistGold) + "g";

				if(!assistPitPlayer.killFeedDisabled)
					AOutput.send(assistPlayer, PlaceholderAPI.setPlaceholders(killEvent.getDeadPlayer(), assist));
			}
		}

		if(deadIsPlayer) {
			pitDead.assistRemove.forEach(BukkitTask::cancel);
			pitDead.assistRemove.clear();
			pitDead.recentDamageMap.clear();
		}
	}

	public static boolean hasKillModifier(KillModifier killModifier, KillModifier... killModifiers) {
		return Arrays.asList(killModifiers).contains(killModifier);
	}

	public static void killPlayer(Player player, KillModifier... killModifiers) {
		PitPlayer pitPlayer = PitPlayer.getPitPlayer(player);
		UUID attackerUUID = pitPlayer.lastHitUUID;
		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			if(onlinePlayer.getUniqueId().equals(attackerUUID)) {

				Map<PitEnchant, Integer> attackerEnchant = new HashMap<>();
				Map<PitEnchant, Integer> defenderEnchant = new HashMap<>();
				EntityDamageByEntityEvent newEvent = new EntityDamageByEntityEvent(onlinePlayer, player, EntityDamageEvent.DamageCause.CUSTOM, 0);
				AttackEvent attackEvent = new AttackEvent(new WrapperEntityDamageEvent(newEvent), attackerEnchant, defenderEnchant, false);

				DamageManager.handleKill(attackEvent, onlinePlayer, player, KillType.KILL);
				return;
			}
		}
		handleKill(null, null, player, KillType.DEATH, killModifiers);
	}

	public static void fakeKill(LivingEntity killer, LivingEntity dead, KillModifier... killModifiers) {
		Map<PitEnchant, Integer> attackerEnchant = EnchantManager.getEnchantsOnPlayer(killer);
		Map<PitEnchant, Integer> defenderEnchant = new HashMap<>();
		EntityDamageByEntityEvent newEvent = new EntityDamageByEntityEvent(killer, dead, EntityDamageEvent.DamageCause.CUSTOM, 0);
		AttackEvent attackEvent = new AttackEvent(new WrapperEntityDamageEvent(newEvent), attackerEnchant, defenderEnchant, false);
		handleKill(attackEvent, killer, dead, KillType.FAKE_KILL, killModifiers);
	}
}
