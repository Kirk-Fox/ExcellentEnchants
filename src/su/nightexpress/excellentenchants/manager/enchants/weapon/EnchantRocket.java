package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.MsgUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

public class EnchantRocket extends IEnchantChanceTemplate implements CombatEnchant {
	
	private final Scaler fireworkPower;
	
	public EnchantRocket(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
		this.fireworkPower = new EnchantScaler(this, "Settings.Firework_Power");
	}

	@Override
	protected void updateConfig() {
		super.updateConfig();

		if (cfg.contains("settings.firework-power")) {
			String damageModifier = cfg.getString("settings.firework-power", "")
					.replace("%level%", PLACEHOLDER_LEVEL);

			cfg.set("Settings.Firework_Power", damageModifier);
			cfg.set("settings.firework-power", null);
		}
	}

	@Override
	@NotNull
	public EnchantmentTarget getItemTarget() {
		return EnchantmentTarget.WEAPON;
	}
	
	public final double getFireworkPower(int level) {
		return this.fireworkPower.getValue(level);
	}

	@Override
	public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager,
					   @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
		
		if (!this.checkTriggerChance(level)) return false;
		
		if (victim.isInsideVehicle()) {
			victim.leaveVehicle();
		}
		
		Firework firework = Rnd.spawnRandomFirework(victim.getLocation());
		FireworkMeta meta = firework.getFireworkMeta();
		meta.setPower((int) this.getFireworkPower(level));
		firework.setFireworkMeta(meta);
		firework.addPassenger(victim);
		
		Sound sound = Sound.ENTITY_FIREWORK_ROCKET_LAUNCH;
		MsgUT.sound(victim.getLocation(), sound.name());
		return true;
	}
}
