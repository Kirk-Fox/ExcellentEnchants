package su.nightexpress.excellentenchants.manager.enchants.bows;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

public class EnchantBomber extends IEnchantChanceTemplate implements BowEnchant {

    private final EnchantScaler fuseTicks;

    public EnchantBomber(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.fuseTicks = new EnchantScaler(this, "Settings.Fuse_Ticks");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
        this.cfg.addMissing("Settings.Fuse_Ticks", "100 - %enchantment_level% * 10");
    }

    public int getFuseTicks(int level) {
        return (int) this.fuseTicks.getValue(level);
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.ENDER_BOW);
        this.addConflict(EnchantRegister.GHAST);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public boolean use(@NotNull EntityShootBowEvent e, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {

        if (!this.checkTriggerChance(level)) return false;
        if (!(e.getProjectile() instanceof Projectile projectile)) return false;

        TNTPrimed primed = projectile.getWorld().spawn(projectile.getLocation(), TNTPrimed.class);
        primed.setVelocity(projectile.getVelocity().multiply(e.getForce() * 2.0));
        primed.setFuseTicks(this.getFuseTicks(level));
        e.setProjectile(primed);
        return true;
    }

    @Override
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
