package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantVampire extends IEnchantChanceTemplate implements CombatEnchant {

    private final String enchantParticle;
    private final Scaler healMod;

    public EnchantVampire(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.LOWEST);

        this.enchantParticle = cfg.getString("Settings.Particle_Effect", Particle.HEART.name());
        this.healMod = new EnchantScaler(this, "Settings.Heal_Of_Damage");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.enchant-particle-effect")) {
            String effect = cfg.getString("settings.enchant-particle-effect", "");
            String damageModifier = cfg.getString("settings.damage-heal-modifier", "").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Particle_Effect", effect);
            cfg.set("Settings.Heal_Of_Damage", damageModifier);
            cfg.set("settings.enchant-particle-effect", null);
            cfg.set("settings.damage-heal-modifier", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%modifier%", NumberUT.format(this.getHealthModifier(level) * 100D)));
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {

        if (!this.checkTriggerChance(level)) return false;

        AttributeInstance ai = damager.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (ai == null) return false;

        double healMod = e.getDamage() * this.getHealthModifier(level);
        double healMax = NumberUT.round(ai.getValue());

        damager.setHealth(Math.min(healMax, damager.getHealth() + healMod));

        EffectUT.playEffect(damager.getEyeLocation(), this.enchantParticle, 0.2f, 0.15f, 0.2f, 0.15f, 5);
        return true;
    }

    public double getHealthModifier(int level) {
        return this.healMod.getValue(level);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }
}
