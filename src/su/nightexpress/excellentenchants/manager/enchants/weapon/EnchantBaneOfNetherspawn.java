package su.nightexpress.excellentenchants.manager.enchants.weapon;

import com.google.common.collect.Sets;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.core.Version;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.Set;
import java.util.function.UnaryOperator;

public class EnchantBaneOfNetherspawn extends IEnchantChanceTemplate implements CombatEnchant {

    private final String  effect;
    private final boolean damageModifier;
    private final Scaler  damageFormula;

    private final Set<EntityType> entityTypes;

    public EnchantBaneOfNetherspawn(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.effect = cfg.getString("Settings.Particle_Effect", "");
        this.damageModifier = cfg.getBoolean("Settings.Damage.As_Modifier");
        this.damageFormula = new EnchantScaler(this, "Settings.Damage.Formula");

        this.entityTypes = Sets.newHashSet(EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.WITHER_SKELETON, EntityType.GHAST, EntityType.WITHER);

        if (Version.CURRENT.isHigher(Version.V1_15_R1)) {
            this.entityTypes.add(EntityType.PIGLIN);
            this.entityTypes.add(EntityType.PIGLIN_BRUTE);
            this.entityTypes.add(EntityType.ZOGLIN);
            this.entityTypes.add(EntityType.HOGLIN);
            this.entityTypes.add(EntityType.STRIDER);
        }
        else {
            this.entityTypes.add(EntityType.valueOf("PIG_ZOMBIE"));
        }
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.particle-effect")) {
            String effect = cfg.getString("settings.particle-effect", "");
            boolean damageModifier = cfg.getBoolean("settings.damage.as-modifier");
            String damageFormula = cfg.getString("settings.damage.formula", "").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Particle_Effect", effect);
            cfg.set("Settings.Damage.As_Modifier", damageModifier);
            cfg.set("Settings.Damage.Formula", damageFormula);
            cfg.set("settings.particle-effect", null);
            cfg.set("settings.damage.as-modifier", null);
            cfg.set("settings.damage.formula", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%damage%", NumberUT.format(this.getDamageModifier(level))));
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {

        if (!this.entityTypes.contains(victim.getType())) return false;
        if (!this.checkTriggerChance(level)) return false;

        double damageHas = e.getDamage();
        double damageAdd = this.getDamageModifier(level);
        e.setDamage(this.damageModifier ? damageHas * damageAdd : damageHas + damageAdd);
        EffectUT.playEffect(victim.getEyeLocation(), this.effect, 0.25, 0.25, 0.25, 0.1f, 30);
        return true;
    }

    public double getDamageModifier(int level) {
        return this.damageFormula.getValue(level);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }
}
