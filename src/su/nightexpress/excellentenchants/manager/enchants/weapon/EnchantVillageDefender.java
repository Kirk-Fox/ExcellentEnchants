package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pillager;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantVillageDefender extends IEnchantChanceTemplate implements CombatEnchant {

    private final boolean dmgAsModifier;
    private final Scaler  dmgAddict;
    private final String  effectParticle;

    public EnchantVillageDefender(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.dmgAddict = new EnchantScaler(this, "Settings.Damage.Formula");
        this.dmgAsModifier = cfg.getBoolean("Settings.Damage.As_Modifier");
        this.effectParticle = cfg.getString("Settings.Particle_Effect", Particle.VILLAGER_ANGRY.name());
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.effect-particle")) {
            String effect = cfg.getString("settings.effect-particle", "");
            boolean damageModifier = cfg.getBoolean("settings.damage-add.as-multiplier");
            String damageFormula = cfg.getString("settings.damage-add.formula", "").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Particle_Effect", effect);
            cfg.set("Settings.Damage.As_Modifier", damageModifier);
            cfg.set("Settings.Damage.Formula", damageFormula);
            cfg.set("settings.effect-particle", null);
            cfg.set("settings.damage-add.as-multiplier", null);
            cfg.set("settings.damage-add.formula", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%damage%", NumberUT.format(this.getDamageAddict(level))));
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {

        if (!(victim instanceof Pillager)) return false;
        if (!this.checkTriggerChance(level)) return false;

        double damageAdd = this.getDamageAddict(level);
        double damageHas = e.getDamage();

        e.setDamage(this.dmgAsModifier ? (damageHas * damageAdd) : (damageHas + damageAdd));
        EffectUT.playEffect(victim.getEyeLocation(), this.effectParticle, 0.15, 0.15, 0.15, 0.13f, 3);
        return true;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    public double getDamageAddict(int level) {
        return this.dmgAddict.getValue(level);
    }
}
