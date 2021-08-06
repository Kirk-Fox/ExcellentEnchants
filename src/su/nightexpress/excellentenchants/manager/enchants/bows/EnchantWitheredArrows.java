package su.nightexpress.excellentenchants.manager.enchants.bows;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantBowHitPotionTemplate;

public class EnchantWitheredArrows extends IEnchantBowHitPotionTemplate {

    public EnchantWitheredArrows(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, PotionEffectType.WITHER);
    }
}
