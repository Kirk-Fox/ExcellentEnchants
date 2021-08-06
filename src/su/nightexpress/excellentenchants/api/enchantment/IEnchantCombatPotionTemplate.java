package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.EffectUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;

public abstract class IEnchantCombatPotionTemplate extends IEnchantPotionTemplate implements CombatEnchant {

    protected String particleEffect;

    public IEnchantCombatPotionTemplate(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg, @NotNull PotionEffectType effectType) {
        super(plugin, cfg, effectType);

        this.particleEffect = cfg.getString("Settings.Particle_Effect", "");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.particle-effect")) {
            String effect = cfg.getString("settings.particle-effect", "");

            cfg.set("Settings.Particle_Effect", effect);
            cfg.set("settings.particle-effect", null);
        }
    }

    @Override
    @NotNull
    public final EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {

        if (!this.checkTriggerChance(level)) return false;
        if (!this.addEffect(victim, level)) return false;

        EffectUT.playEffect(victim.getEyeLocation(), this.particleEffect, 0.2f, 0.15f, 0.2f, 0.1f, 40);
        return true;
    }
}
