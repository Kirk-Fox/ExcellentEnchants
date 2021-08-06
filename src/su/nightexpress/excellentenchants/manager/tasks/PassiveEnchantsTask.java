package su.nightexpress.excellentenchants.manager.tasks;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.EntityUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.config.Config;

import java.util.*;

public class PassiveEnchantsTask extends ITask<ExcellentEnchants> {

    public PassiveEnchantsTask(@NotNull ExcellentEnchants plugin) {
        super(plugin, Config.TASKS_PASSIVE_ENCHANTS_TICKS_INTERVAL, false);
    }

    @Override
    public void action() {
        for (LivingEntity entity : this.getEntities()) {

            List<ItemStack> list = new ArrayList<>(Arrays.asList(EntityUT.getArmor(entity)));
            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null && !ItemUT.isArmor(equipment.getItemInMainHand())) {
                list.add(equipment.getItemInMainHand());
            }

            for (ItemStack armor : list) {
                if (armor == null || armor.getType() == Material.ENCHANTED_BOOK) continue;

                ItemMeta meta = armor.getItemMeta();
                if (meta == null) continue;

                meta.getEnchants().forEach((en, lvl) -> {
                    if (lvl < 1) return;
                    if (!(en instanceof PassiveEnchant passiveEnchant)) return;

                    passiveEnchant.use(entity, lvl);
                });
            }
        }
    }

    @NotNull
    private Collection<@NotNull ? extends LivingEntity> getEntities() {
        Set<LivingEntity> list = new HashSet<>(plugin.getServer().getOnlinePlayers());

        if (Config.ENCHANTMENTS_ENTITY_PASSIVE_FOR_MOBS) {
            plugin.getServer().getWorlds().forEach(world -> {
                list.addAll(world.getEntitiesByClass(LivingEntity.class));
            });
        }
        return list;
    }
}
