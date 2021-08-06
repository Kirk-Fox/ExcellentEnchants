package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantCombatPotionTemplate;

public class EnchantConfusion extends IEnchantCombatPotionTemplate {

    public EnchantConfusion(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, PotionEffectType.CONFUSION);
    }
}
