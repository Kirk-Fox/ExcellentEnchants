package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class EnchantVeinminer extends ExcellentEnchant implements BlockEnchant {

    public static final  String      ID             = "veinminer";
    private static final String      META_ORE_MINED = ID + "_mined";
    private static final BlockFace[] AREA           = {BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH};

    private final EnchantScaler blocksLimit;
    private final Set<Material> blocksAffected;

    public EnchantVeinminer(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.blocksLimit = new EnchantScaler(this, "Settings.Blocks.Max_At_Once");
        this.blocksAffected = cfg.getStringSet("Settings.Blocks.Affected").stream().map(type -> Material.getMaterial(type.toUpperCase())).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.TUNNEL);
        this.addConflict(EnchantRegister.BLAST_MINING);
    }

    @NotNull
    public Set<Material> getBlocksAffected() {
        return this.blocksAffected;
    }

    public int getBlocksLimit(int level) {
        return (int) this.blocksLimit.getValue(level);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%blocks_amount%", String.valueOf(this.getBlocksLimit(level))));
    }

    private void vein(@NotNull Player player, @NotNull Block source, int level) {
        Set<Block> ores = new HashSet<>();
        Set<Block> prepare = new HashSet<>(this.getNearby(source));

        int limit = this.getBlocksLimit(level);
        while (ores.addAll(prepare) && ores.size() < limit) {
            Set<Block> nearby = new HashSet<>();
            prepare.forEach(prepared -> nearby.addAll(this.getNearby(prepared)));
            prepare.clear();
            prepare.addAll(nearby);
        }
        ores.remove(source);
        ores.forEach(ore -> {
            ore.setMetadata(META_ORE_MINED, new FixedMetadataValue(plugin, true));
            plugin.getNMS().breakBlock(player, ore);
        });
    }

    @NotNull
    private Set<Block> getNearby(@NotNull Block block) {
        Set<Block> set = new HashSet<>();
        for (BlockFace face : AREA) {
            Block near = block.getRelative(face);
            if (near.getType() != block.getType()) continue;
            set.add(near);
        }
        return set;
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack item) {
        return ItemUT.isPickaxe(item);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack tool, int level) {
        Block block = e.getBlock();
        if (block.hasMetadata(META_ORE_MINED)) return false;

        if (!this.getBlocksAffected().contains(block.getType())) return false;
        this.vein(player, block, level);

        return true;
    }
}
