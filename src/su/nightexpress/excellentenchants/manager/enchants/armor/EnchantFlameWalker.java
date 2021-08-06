package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.MoveEnchant;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.util.HashMap;
import java.util.Map;

public class EnchantFlameWalker extends IEnchantChanceTemplate implements MoveEnchant, Cleanable {

    private static final BlockFace[]      FACES             = {BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
    private static final Map<Block, Long> BLOCKS_TO_DESTROY = new HashMap<>();

    private BlockTickTask blockTickTask;

    public EnchantFlameWalker(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.blockTickTask = new BlockTickTask(plugin);
        this.blockTickTask.start();
    }

    public static void addBlock(@NotNull Block block, int seconds) {
        BLOCKS_TO_DESTROY.put(block, System.currentTimeMillis() + seconds * 1000L);
    }

    @Override
    public void clear() {
        if (this.blockTickTask != null) {
            this.blockTickTask.stop();
            this.blockTickTask = null;
        }
        BLOCKS_TO_DESTROY.keySet().forEach(b -> b.setType(Material.AIR));
        BLOCKS_TO_DESTROY.clear();
    }

    @Override
    public boolean use(@NotNull PlayerMoveEvent e, @NotNull LivingEntity entity, int level) {
        if (!this.checkTriggerChance(level)) return false;

        plugin.getNMSHandler().handleFlameWalker(entity, entity.getLocation(), level);
        return true;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_FEET;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantFlameWalker(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.isFlying()) return;

        Location from = e.getFrom();
        Location to = e.getTo();
        if (to == null) return;
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        Block bTo = to.getBlock().getRelative(BlockFace.DOWN);
        boolean hasLava = false;
        for (BlockFace face : FACES) {
            if (bTo.getRelative(face).getType() == Material.LAVA) {
                hasLava = true;
                break;
            }
        }
        if (!hasLava) return;

        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || ItemUT.isAir(boots)) return;

        int level = EnchantManager.getEnchantLevel(boots, this);
        if (level < 1) return;

        this.use(e, player, level);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFlameWalkerBlock(BlockBreakEvent e) {
        if (BLOCKS_TO_DESTROY.containsKey(e.getBlock())) {
            e.setCancelled(true);
            e.getBlock().setType(Material.LAVA);
        }
    }

    static class BlockTickTask extends ITask<ExcellentEnchants> {

        public BlockTickTask(@NotNull ExcellentEnchants plugin) {
            super(plugin, 1, false);
        }

        @Override
        public void action() {
            long now = System.currentTimeMillis();

            BLOCKS_TO_DESTROY.keySet().removeIf(block -> {
                if (block.isEmpty()) return true;

                long time = BLOCKS_TO_DESTROY.get(block);
                if (now >= time) {
                    block.setType(Material.LAVA);
                    EffectUT.playEffect(block.getLocation(), Particle.BLOCK_CRACK.name() + ":" + Material.COBBLESTONE.name(), 0.5, 0.7, 0.5, 0.03, 50);
                    return true;
                }
                return false;
            });
        }
    }
}
