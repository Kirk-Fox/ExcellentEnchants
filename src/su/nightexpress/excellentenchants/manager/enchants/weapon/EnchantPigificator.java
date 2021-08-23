package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

public class EnchantPigificator extends IEnchantChanceTemplate implements CombatEnchant {

    private final String sound;
    private final String effect;

    public EnchantPigificator(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.sound = cfg.getString("Settings.Sound", Sound.ENTITY_PIG_AMBIENT.name());
        this.effect = cfg.getString("Settings.Particle_Effect", Particle.HEART.name());
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.THUNDER);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.effect-particle")) {
            String effect = cfg.getString("settings.effect-particle", "");
            String sound = cfg.getString("settings.effect-sound");

            cfg.set("Settings.Particle_Effect", effect);
            cfg.set("Settings.Sound", sound);
            cfg.set("settings.effect-particle", null);
            cfg.set("settings.effect-sound", null);
        }
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {

        if (!(victim instanceof PigZombie)) return false;
        if (!this.checkTriggerChance(level)) return false;

        e.setCancelled(true);

        EffectUT.playEffect(victim.getLocation(), this.effect, 0.25, 0.25, 0.25, 0.1f, 30);
        MsgUT.sound(victim.getLocation(), this.sound);

        victim.getWorld().spawn(victim.getLocation(), Pig.class);
        victim.remove();
        return true;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }
}
