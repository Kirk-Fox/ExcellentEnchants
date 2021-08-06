package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;

public class EnchantThunder extends IEnchantChanceTemplate implements CombatEnchant {

	public EnchantThunder(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
		super(plugin, cfg);
	}

	@Override
	public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager,
					   @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
		if (!this.checkTriggerChance(level)) return false;
		
		victim.getWorld().strikeLightning(victim.getLocation());
		return true;
	}

	@Override
	@NotNull
	public EnchantmentTarget getItemTarget() {
		return EnchantmentTarget.WEAPON;
	}
}
