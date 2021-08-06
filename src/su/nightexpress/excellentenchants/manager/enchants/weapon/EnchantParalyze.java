package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantCombatPotionTemplate;
import su.nightexpress.excellentenchants.manager.EnchantManager;

public class EnchantParalyze extends IEnchantCombatPotionTemplate {
	
	public EnchantParalyze(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
		super(plugin, cfg, PotionEffectType.SLOW_DIGGING);
	}

	@Override
	public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager,
					   @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
		
		if (!super.use(e, damager, victim, weapon, level)) return false;
		
		int eDuration = this.getEffectDuration(level);
		int eLvl = this.getEffectLevel(level);
		PotionEffect slow = new PotionEffect(PotionEffectType.SLOW, eDuration, eLvl);
		EnchantManager.addPotionEffect(victim, slow, true);
		return true;
	}
}
