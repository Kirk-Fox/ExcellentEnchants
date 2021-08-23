package su.nightexpress.excellentenchants.manager.enchants.weapon;


import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantDecapitator extends IEnchantChanceTemplate implements DeathEnchant {

    private final String              particleEffect;
    private final String              headName;
    private final Set<String>         ignored;
    private final Map<String, String> headTextures;

    public EnchantDecapitator(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.particleEffect = cfg.getString("Settings.Particle_Effect", Particle.BLOCK_CRACK.name() + ":REDSTONE_BLOCK");
        this.ignored = cfg.getStringSet("Settings.Ignored_Entity_Types").stream().map(String::toUpperCase).collect(Collectors.toSet());
        this.headName = StringUT.color(cfg.getString("Settings.Head_Item.Name", "&c%entity%'s Head"));
        this.headTextures = new HashMap<>();
        for (String sType : cfg.getSection("Settings.Head_Item.Textures")) {
            String texture = cfg.getString("Settings.Head_Item.Textures." + sType);
            this.headTextures.put(sType.toUpperCase(), texture);
        }
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        this.cfg.addMissing("Settings.Ignored_Entity_Types", Arrays.asList("ENDER_DRAGON", "WITHER_SKELETON"));
        if (cfg.contains("settings.enchant-particle-effect")) {
            String effect = cfg.getString("settings.enchant-particle-effect", "");
            String name = cfg.getString("settings.head-name");

            cfg.set("Settings.Particle_Effect", effect);
            cfg.set("Settings.Head_Item.Name", name);
            cfg.set("settings.enchant-particle-effect", null);
            cfg.set("settings.head-name", null);
        }
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity victim, int level) {
        if (!this.checkTriggerChance(level)) return false;

        ItemStack item;
        if (victim instanceof WitherSkeleton) {
            item = new ItemStack(Material.WITHER_SKELETON_SKULL);
        }
        else if (victim instanceof Zombie || victim instanceof Giant) {
            item = new ItemStack(Material.ZOMBIE_HEAD);
        }
        else if (victim instanceof Skeleton) {
            item = new ItemStack(Material.SKELETON_SKULL);
        }
        else if (victim instanceof Creeper) {
            item = new ItemStack(Material.CREEPER_HEAD);
        }
        else if (victim instanceof EnderDragon) {
            item = new ItemStack(Material.DRAGON_HEAD);
        }
        else {
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta == null) return false;

            String entityName;
            if (victim instanceof Player player) {
                entityName = this.headName.replace("%entity%", victim.getName());
                meta.setOwningPlayer(player);
            }
            else {
                String texture = this.headTextures.get(victim.getType().name());
                if (texture == null) return false;

                entityName = this.headName.replace("%entity%", plugin.lang().getEnum(victim.getType()));
                ItemUT.addSkullTexture(item, texture);
                meta = (SkullMeta) item.getItemMeta();
            }

            meta.setDisplayName(entityName);
            item.setItemMeta(meta);
        }

        victim.getWorld().dropItemNaturally(victim.getLocation(), item);
        EffectUT.playEffect(victim.getEyeLocation(), this.particleEffect, 0.2f, 0.15f, 0.2f, 0.15f, 40);
        return true;
    }
}
