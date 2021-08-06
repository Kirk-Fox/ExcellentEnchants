package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantInfernus extends IEnchantChanceTemplate {

    private final Scaler fireTicks;

    public EnchantInfernus(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.fireTicks = new EnchantScaler(this, "Settings.Fire_Ticks");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.fire-ticks")) {
            String damageModifier = cfg.getString("settings.fire-ticks", "").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Fire_Ticks", damageModifier);
            cfg.set("settings.fire-ticks", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%duration%", NumberUT.format((double) this.getFireTicks(level) / 20D)));
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack item) {
        return ItemUT.isTrident(item);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TRIDENT;
    }

    public int getFireTicks(int level) {
        return (int) this.fireTicks.getValue(level);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInfernusTridentLaunch(ProjectileLaunchEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Trident trident)) return;

        ItemStack item = trident.getItem();

        int level = item.getEnchantmentLevel(this);
        if (level <= 0) return;

        if (!this.checkTriggerChance(level)) return;
        trident.setFireTicks(Integer.MAX_VALUE);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInfernusDamageApply(EntityDamageByEntityEvent e) {
        Entity entity = e.getDamager();
        if (!(entity instanceof Trident trident)) return;

        ItemStack item = trident.getItem();

        int level = item.getEnchantmentLevel(this);
        if (level <= 0 || trident.getFireTicks() <= 0) return;

        int ticks = this.getFireTicks(level);
        e.getEntity().setFireTicks(ticks);
    }
}
