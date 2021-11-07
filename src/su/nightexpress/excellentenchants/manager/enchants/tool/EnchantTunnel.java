package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.LocUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.hooks.HookNCP;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

public class EnchantTunnel extends IEnchantChanceTemplate implements BlockEnchant {

    private final boolean disableOnSneak;

    private static final String  LOOP_FIX             = "EVENT_STOP";
    // X and Z offsets for each block AoE mined
    private static final int[][] MINING_COORD_OFFSETS = new int[][]{{0, 0}, {0, -1}, {-1, 0}, {0, 1}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1},};

    public EnchantTunnel(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGH);
        this.disableOnSneak = cfg.getBoolean("Settings.Ignore_When_Sneaking");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.disable-on-sneak")) {
            boolean b1 = cfg.getBoolean("settings.disable-on-sneak");

            cfg.set("Settings.Ignore_When_Sneaking", b1);
            cfg.set("settings.disable-on-sneak", null);
        }
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.VEINMINER);
        this.addConflict(EnchantRegister.BLAST_MINING);

    }

    @Override
    public boolean canEnchant(@NotNull ItemStack item) {
        return ItemUT.isPickaxe(item) || ItemUT.isShovel(item);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (this.disableOnSneak && player.isSneaking()) return false;

        if (player.hasMetadata(LOOP_FIX)) {
            player.removeMetadata(LOOP_FIX, plugin);
            return false;
        }

        if (!this.checkTriggerChance(level)) return false;

        BlockFace dir = LocUT.getDirection(player);
        Block block = e.getBlock();

        // Redstone ore seems to be 'interactable'.
        if (block.getType().isInteractable() && block.getType() != Material.REDSTONE_ORE &&
                block.getType() != Material.getMaterial("DEEPSLATE_REDSTONE_ORE")) return false;
        if (block.getDrops(item).isEmpty()) return false;

        boolean isY = dir != null && block.getRelative(dir.getOppositeFace()).isEmpty();
        boolean isZ = dir == BlockFace.EAST || dir == BlockFace.WEST;

        // Mine + shape if Tunnel I, 3x3 if Tunnel II
        int blocksBroken = 1;
        if (level == 1) {
            blocksBroken = 2;
        }
        else if (level == 2) {
            blocksBroken = 5;
        }
        else if (level == 3) {
            blocksBroken = 9;
        }

        //int expDrop = e.getExpToDrop();
        HookNCP.exemptBlocks(player);
        for (int i = 0; i < blocksBroken; i++) {
            if (ItemUT.isAir(item)) break;

            int xAdd = MINING_COORD_OFFSETS[i][0];
            int zAdd = MINING_COORD_OFFSETS[i][1];

            Block blockAdd;
            if (isY) {
                blockAdd = block.getLocation().clone().add(isZ ? 0 : xAdd, zAdd, isZ ? xAdd : 0).getBlock();
            }
            else {
                blockAdd = block.getLocation().clone().add(xAdd, 0, zAdd).getBlock();
            }

            // Skip blocks that should not be mined
            if (blockAdd.getDrops(item).isEmpty()) continue;
            if (blockAdd.isLiquid()) continue;

            Material addType = blockAdd.getType();
            if (addType.isInteractable() && addType != Material.REDSTONE_ORE &&
                    addType != Material.getMaterial("DEEPSLATE_REDSTONE_ORE")) continue;
            if (addType == Material.BEDROCK || addType == Material.END_PORTAL || addType == Material.END_PORTAL_FRAME) continue;
            if (addType == Material.OBSIDIAN && addType != block.getType()) continue;

            // Play block break particles before the block broken.
            String particle = Particle.BLOCK_CRACK + ":" + blockAdd.getType().name();
            EffectUT.playEffect(LocUT.getCenter(blockAdd.getLocation()), particle, 0.2, 0.2, 0.2, 0.1, 20);

            // Add metadata to tool to prevent new block breaking event from triggering mining again
            player.setMetadata(LOOP_FIX, new FixedMetadataValue(plugin, true));
            plugin.getNMS().breakBlock(player, blockAdd);
        }
        HookNCP.unexemptBlocks(player);
        return true;
    }
}
