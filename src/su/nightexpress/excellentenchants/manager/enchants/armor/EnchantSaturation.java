package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantSaturation extends IEnchantChanceTemplate implements PassiveEnchant {

    private final Scaler saturationAmount;

    public EnchantSaturation(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.saturationAmount = new EnchantScaler(this, "Settings.Saturation.Amount");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.saturation-amount")) {
            String amount = cfg.getString("settings.saturation-amount", "").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Saturation.Amount", amount);
            cfg.set("settings.saturation-amount", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%amount%", NumberUT.format(this.getSaturationAmount(level))));
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    public final double getSaturationAmount(int level) {
        return this.saturationAmount.getValue(level);
    }

    @Override
    public boolean use(@NotNull LivingEntity entity, int level) {
        if (!(entity instanceof Player player)) return false;
        if (!this.checkTriggerChance(level)) return false;

        int amount = (int) this.getSaturationAmount(level);
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + amount));
        return true;
    }
}
