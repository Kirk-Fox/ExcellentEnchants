package su.nightexpress.excellentenchants.manager.enchants.bows;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantBowHitTemplate;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantExplosiveArrows extends IEnchantBowHitTemplate {

    private final Scaler explosionSize;

    public EnchantExplosiveArrows(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);
        this.explosionSize = new EnchantScaler(this, "Settings.Explosion.Size");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.explosion-size")) {
            String size = cfg.getString("settings.explosion-size", "").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Explosion.Size", size);
            cfg.set("settings.explosion-size", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%power%", NumberUT.format(this.getExplosionSize(level))));
    }

    public final double getExplosionSize(int level) {
        return this.explosionSize.getValue(level);
    }

    @Override
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (!super.use(e, projectile, bow, level)) return false;

        Entity shooter = null;
        if (projectile.getShooter() instanceof Entity entity) {
            shooter = entity;
        }

        World world = projectile.getWorld();
        return world.createExplosion(projectile.getLocation(), (float) this.getExplosionSize(level), true, false, shooter);
    }
}
