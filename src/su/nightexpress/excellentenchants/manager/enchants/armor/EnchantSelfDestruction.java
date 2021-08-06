package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantSelfDestruction extends IEnchantChanceTemplate implements DeathEnchant {
	
	private final Scaler explosionSize;
	
	public EnchantSelfDestruction(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
		this.explosionSize = new EnchantScaler(this, "Settings.Explosion.Size");
	}

	@Override
	protected void updateConfig() {
		super.updateConfig();

		if (cfg.contains("settings.explosion-size")) {
			String size = cfg.getString("settings.explosion-size", "")
					.replace("%level%", PLACEHOLDER_LEVEL);

			cfg.set("Settings.Explosion.Size", size);
			cfg.set("settings.explosion-size", null);
		}
	}

	@Override
	public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level) {
		if (!this.checkTriggerChance(level)) return false;
		
		double size = this.getExplosionSize(level);
		dead.getWorld().createExplosion(dead.getLocation(), (float) size, false, false);
		return true;
	}

	@Override
	public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
		return str -> super.replacePlaceholders(level).apply(str
			.replace("%power%", NumberUT.format(this.getExplosionSize(level)))
		);
	}

	@Override
	@NotNull
	public EnchantmentTarget getItemTarget() {
		return EnchantmentTarget.ARMOR_TORSO;
	}

	public final double getExplosionSize(int level) {
		return this.explosionSize.getValue(level);
	}
}
