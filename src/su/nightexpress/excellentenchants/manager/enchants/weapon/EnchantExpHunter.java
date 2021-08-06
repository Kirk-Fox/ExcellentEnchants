package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantExpHunter extends IEnchantChanceTemplate implements DeathEnchant {

    private final Scaler expMod;

    public EnchantExpHunter(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);
        this.expMod = new EnchantScaler(this, "Settings.Exp_Modifier");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.exp-modifier")) {
            String damageModifier = cfg.getString("settings.exp-modifier", "").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Exp_Modifier", damageModifier);
            cfg.set("settings.exp-modifier", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%exp%", NumberUT.format(this.getExpModifier(level) * 100D - 100D)));
    }

    @Override
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level) {
        if (!this.checkTriggerChance(level)) return false;

        double mod = this.getExpModifier(level);
        double exp = e.getDroppedExp() * mod;

        e.setDroppedExp((int) Math.ceil(exp));
        return true;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    public final double getExpModifier(int level) {
        return this.expMod.getValue(level);
    }
}
