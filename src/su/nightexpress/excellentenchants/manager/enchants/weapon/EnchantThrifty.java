package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantThrifty extends IEnchantChanceTemplate implements DeathEnchant {

    private final Set<String> entityBlacklist;
    private final Set<String> spawnReasonBlacklist;

    private static final String META_SETTING_SPAWN_REASON = "GOLDEN_ENCHANTS_THRIFTY_SETTING_SPAWN_REASON";

    public EnchantThrifty(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.entityBlacklist = cfg.getStringSet("Settings.Ignored_Entity_Types").stream().map(String::toUpperCase).collect(Collectors.toSet());

        this.spawnReasonBlacklist = cfg.getStringSet("Settings.Ignored_Spawn_Reasons").stream().map(String::toUpperCase).collect(Collectors.toSet());
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.entity-blacklist")) {
            List<String> list1 = cfg.getStringList("settings.entity-blacklist");
            List<String> list2 = cfg.getStringList("settings.spawn-reason-blacklist");

            cfg.set("Settings.Ignored_Entity_Types", list1);
            cfg.set("Settings.Ignored_Spawn_Reasons", list2);
            cfg.set("settings.entity-blacklist", null);
            cfg.set("settings.spawn-reason-blacklist", null);
        }
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level) {
        if (this.entityBlacklist.contains(dead.getType().name())) return false;
        if (dead.hasMetadata(META_SETTING_SPAWN_REASON)) return false;
        if (!this.checkTriggerChance(level)) return false;

        Material material = Material.getMaterial(dead.getType().name() + "_SPAWN_EGG");
        if (material == null) {
            if (dead.getType() == EntityType.MUSHROOM_COW) {
                material = Material.MOOSHROOM_SPAWN_EGG;
            }
            else return false;
        }

        ItemStack egg = new ItemStack(material);
        e.getDrops().add(egg);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSettingCreatureSpawnReason(CreatureSpawnEvent e) {
        if (!this.spawnReasonBlacklist.contains(e.getSpawnReason().name())) return;

        e.getEntity().setMetadata(META_SETTING_SPAWN_REASON, new FixedMetadataValue(plugin, true));
    }
}
