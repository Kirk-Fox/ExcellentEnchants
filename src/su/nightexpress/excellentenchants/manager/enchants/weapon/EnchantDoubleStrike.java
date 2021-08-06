package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;

public class EnchantDoubleStrike extends IEnchantChanceTemplate implements CombatEnchant {
	
	private final String enchantParticle;
	private final String enchantSound;
	
	public EnchantDoubleStrike(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
		super(plugin, cfg);

		this.enchantParticle = cfg.getString("Settings.Particle_Effect", Particle.EXPLOSION_NORMAL.name());
		this.enchantSound = cfg.getString("Settings.Sound", Sound.ENTITY_GENERIC_EXPLODE.name());
	}

	@Override
	protected void updateConfig() {
		super.updateConfig();

		if (cfg.contains("settings.enchant-particle-effect")) {
			String effect = cfg.getString("settings.enchant-particle-effect", "");
			String sound = cfg.getString("settings.enchant-sound-effect");

			cfg.set("Settings.Particle_Effect", effect);
			cfg.set("Settings.Sound", sound);
			cfg.set("settings.enchant-particle-effect", null);
			cfg.set("settings.enchant-sound-effect", null);
		}
	}

	@Override
	@NotNull
	public EnchantmentTarget getItemTarget() {
		return EnchantmentTarget.WEAPON;
	}

	@Override
	public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager,
					   @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
		
		if (!this.checkTriggerChance(level)) return false;
		
		e.setDamage(e.getDamage() * 2D);
		EffectUT.playEffect(victim.getEyeLocation(), this.enchantParticle, 0.2f, 0.15f, 0.2f, 0.15f, 20);
		MsgUT.sound(victim.getLocation(), this.enchantSound);
		return true;
	}
}
