package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantLuckyMiner extends IEnchantChanceTemplate implements BlockEnchant {

    private final Scaler expModifier;

    public EnchantLuckyMiner(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);
        this.expModifier = new EnchantScaler(this, "Settings.Exp_Modifier");
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
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.checkTriggerChance(level)) return false;

        double expMod = this.getExpModifier(level);
        e.setExpToDrop((int) ((double) e.getExpToDrop() * expMod));
        return true;
    }

    public double getExpModifier(int level) {
        return this.expModifier.getValue(level);
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack item) {
        return ItemUT.isPickaxe(item);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }
}
