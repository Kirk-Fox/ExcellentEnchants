package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.manager.tasks.ArrowTrailsTask;

public abstract class IEnchantBowHitPotionTemplate extends IEnchantPotionTemplate implements BowEnchant {

    protected final String arrowTrail;
    protected final String arrowMeta;

    public IEnchantBowHitPotionTemplate(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg,
                                        @NotNull EnchantPriority priority,
                                        @NotNull PotionEffectType type) {
        super(plugin, cfg, priority, type);

        this.arrowTrail = cfg.getString("Settings.Arrow.Trail", "");
        this.arrowMeta = this.getId() + "_arrow";
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.arrow-trail")) {
            String trail = cfg.getString("settings.arrow-trail", "");

            cfg.set("Settings.Arrow.Trail", trail);
            cfg.set("settings.arrow-trail", null);
        }
    }

    public boolean isThisArrow(@NotNull Projectile projectile) {
        return projectile.hasMetadata(this.arrowMeta);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (!this.isThisArrow(projectile)) return false;
        if (!(e.getHitEntity() instanceof LivingEntity target)) return false;

        this.addEffect(target, level);
        return true;
    }

    @Override
    public boolean use(@NotNull EntityShootBowEvent e, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!(e.getProjectile() instanceof Projectile projectile)) return false;
        if (!this.checkTriggerChance(level)) return false;

        ArrowTrailsTask.add(projectile, this.arrowTrail);
        projectile.setMetadata(this.arrowMeta, new FixedMetadataValue(this.plugin, true));
        return true;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
