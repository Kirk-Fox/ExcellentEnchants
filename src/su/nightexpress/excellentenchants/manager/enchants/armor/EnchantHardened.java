package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantPotionTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;

public class EnchantHardened extends IEnchantPotionTemplate implements CombatEnchant {

	public EnchantHardened(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
		super(plugin, cfg, PotionEffectType.DAMAGE_RESISTANCE);
	}

	@Override
	@NotNull
	public EnchantmentTarget getItemTarget() {
		return EnchantmentTarget.ARMOR_TORSO;
	}

	@Override
	public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager,
					   @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
		if (!this.checkTriggerChance(level)) return false;

		return this.addEffect(victim, level);
	}
}
