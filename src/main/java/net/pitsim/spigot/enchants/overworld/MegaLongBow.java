package net.pitsim.spigot.enchants.overworld;

import dev.kyro.arcticapi.misc.AUtil;
import net.pitsim.spigot.PitSim;
import net.pitsim.spigot.controllers.Cooldown;
import net.pitsim.spigot.controllers.EnchantManager;
import net.pitsim.spigot.controllers.GrimManager;
import net.pitsim.spigot.controllers.PolarManager;
import net.pitsim.spigot.controllers.objects.AnticheatManager;
import net.pitsim.spigot.controllers.objects.PitEnchant;
import net.pitsim.spigot.enums.ApplyType;
import net.pitsim.spigot.events.AttackEvent;
import net.pitsim.spigot.misc.Misc;
import net.pitsim.spigot.misc.PitLoreBuilder;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MegaLongBow extends PitEnchant {
	public static MegaLongBow INSTANCE;
	public static List<UUID> mlbShots = new ArrayList<>();

	public MegaLongBow() {
		super("Mega Longbow", true, ApplyType.BOWS,
				"megalongbow", "mega-longbow", "mlb", "mega");
		INSTANCE = this;
	}

	@EventHandler
	public void onAttack(AttackEvent.Apply attackEvent) {
		if(!canApply(attackEvent)) return;
		if(attackEvent.getArrow() == null || !mlbShots.contains(attackEvent.getArrow().getUniqueId())) return;

		attackEvent.increaseCalcDecrease.add(Misc.getReductionMultiplier(getReduction()));
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBowShoot(EntityShootBowEvent event) {

		if(!(event.getEntity() instanceof Player) || !(event.getProjectile() instanceof Arrow)) return;
		Player player = ((Player) event.getEntity()).getPlayer();
		Arrow arrow = (Arrow) event.getProjectile();

		int enchantLvl = EnchantManager.getEnchantLevel(player, this);
		if(enchantLvl == 0) return;

//		if(event instanceof VolleyShootEvent) {
//
//			critArrow(player, arrow);
//			return;
//		}

		Cooldown cooldown = getCooldown(player, 20);
		if(cooldown.isOnCooldown()) return;
		else cooldown.restart();

		mlbShots.add(arrow.getUniqueId());
		new BukkitRunnable() {
			@Override
			public void run() {
				mlbShots.remove(arrow.getUniqueId());
			}
		}.runTaskLater(PitSim.INSTANCE, 200L);

		critArrow(player, arrow);
		Misc.applyPotionEffect(player, PotionEffectType.JUMP, 40, getJumpMultiplier(enchantLvl), true, false);

//		if(PitSim.anticheat instanceof PolarManager) { //todo add this back
//			PitSim.anticheat.exemptPlayer(player, 7);
//			new BukkitRunnable() {
//				@Override
//				public void run() {
//					PitSim.anticheat.exemptPlayer(player, 7);
//				}
//			}.runTaskLater(PitSim.INSTANCE, 35);
//		} else if(PitSim.anticheat instanceof GrimManager) {
//			PitSim.anticheat.exemptPlayer(player, 20 + getJumpMultiplier(enchantLvl) * 5L, AnticheatManager.FlagType.SIMULATION, AnticheatManager.FlagType.GROUND_SPOOF);
//		}
	}

	public static void critArrow(Player player, Arrow arrow) {

		arrow.setCritical(true);
		arrow.setVelocity(player.getLocation().getDirection().multiply(2.95));
	}

	@Override
	public List<String> getNormalDescription(int enchantLvl) {
		return new PitLoreBuilder(
				"&7One shot per second, this bow is automatically fully drawn and grants &aJump Boost " +
				AUtil.toRoman(getJumpMultiplier(enchantLvl) + 1) + " &7(2s). Arrows deal &c-" +
				getReduction() + "% &7damage"
		).getLore();
	}

	@Override
	public String getSummary() {
		return getDisplayName(false, true) + " &7is an enchant that allows you " +
				"to instantly shoot a fully charged arrow shot once per second. This enchant is what sets PvP apart " +
				"on this server from all others";
	}

	public static int getReduction() {
		return 50;
	}

	public int getJumpMultiplier(int enchantLvl) {

		return enchantLvl;
	}
}
