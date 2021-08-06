package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

public class EnchantDivineTouch extends IEnchantChanceTemplate implements BlockEnchant {

    private final String particleEffect;
    private final String spawnerName;

    public EnchantDivineTouch(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.particleEffect = cfg.getString("Settings.Particle_Effect", Particle.VILLAGER_HAPPY.name());
        this.spawnerName = StringUT.color(cfg.getString("Settings.Spawner_Item.Name", "&aMob Spawner &7(%type%)"));
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.SMELTER);
        this.addConflict(EnchantRegister.BLAST_MINING);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.addMissing("Settings.Particle_Effect", Particle.VILLAGER_HAPPY.name());
        if (cfg.contains("settings.spawner-name")) {
            String name = cfg.getString("settings.spawner-name");

            cfg.set("Settings.Spawner_Item.Name", name);
            cfg.set("settings.spawner-name", null);
        }
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

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();
        if (block.getType() != Material.SPAWNER) return false;

        if (!this.checkTriggerChance(level)) return false;

        Location location = LocUT.getCenter(block.getLocation());
        World world = location.getWorld();
        if (world == null) return false;

        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

        ItemStack itemSpawner = new ItemStack(Material.SPAWNER);
        BlockStateMeta stateItem = (BlockStateMeta) itemSpawner.getItemMeta();
        if (stateItem == null) return false;

        CreatureSpawner spawnerItem = (CreatureSpawner) stateItem.getBlockState();
        spawnerItem.setSpawnedType(spawnerBlock.getSpawnedType());
        spawnerItem.update(true);
        stateItem.setBlockState(spawnerItem);
        stateItem.setDisplayName(this.spawnerName.replace("%type%", plugin.lang().getEnum(spawnerBlock.getSpawnedType())));
        itemSpawner.setItemMeta(stateItem);

        world.dropItemNaturally(location, itemSpawner);
        EffectUT.playEffect(location, this.particleEffect, 0.3f, 0.3f, 0.3f, 0.15f, 30);
        return true;
    }

    // ---------------------------------------------------------------
    // Spawner Type Fix
    // ---------------------------------------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        Player player = e.getPlayer();
        ItemStack spawner = player.getInventory().getItemInMainHand();
        if (ItemUT.isAir(spawner) || spawner.getType() != Material.SPAWNER) {
            spawner = player.getInventory().getItemInOffHand();
        }
        if (ItemUT.isAir(spawner) || spawner.getType() != Material.SPAWNER) {
            return;
        }

        BlockStateMeta meta = (BlockStateMeta) spawner.getItemMeta();
        if (meta == null) return;

        CreatureSpawner spawnerItem = (CreatureSpawner) meta.getBlockState();
        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

        spawnerBlock.setSpawnedType(spawnerItem.getSpawnedType());
        spawnerBlock.update();
    }
}
