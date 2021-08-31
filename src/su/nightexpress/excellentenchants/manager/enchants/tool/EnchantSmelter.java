package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

import java.util.*;

public class EnchantSmelter extends IEnchantChanceTemplate implements BlockEnchant, CustomDropEnchant {

    private final String                  sound;
    private final String                  particle;
    private final Map<Material, Material> smeltingTable;

    public EnchantSmelter(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.sound = cfg.getString("Settings.Sound", "");
        this.particle = cfg.getString("Settings.Particle_Effect", "");
        this.smeltingTable = new HashMap<>();
        for (String sFrom : cfg.getSection("Settings.Smelting_Table")) {
            Material mFrom = Material.getMaterial(sFrom.toUpperCase());
            if (mFrom == null) {
                plugin.error("[Smelter] Invalid source material '" + sFrom + "' !");
                continue;
            }
            String sTo = cfg.getString("Settings.Smelting_Table." + sFrom, "");
            Material mTo = Material.getMaterial(sTo.toUpperCase());
            if (mTo == null) {
                plugin.error("[Smelter] Invalid result material '" + sTo + "' !");
                continue;
            }
            this.smeltingTable.put(mFrom, mTo);
        }
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(Enchantment.SILK_TOUCH);
        this.addConflict(EnchantRegister.DIVINE_TOUCH);
        this.addConflict(EnchantRegister.BLAST_MINING);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.addMissing("Settings.Sound", Sound.BLOCK_LAVA_EXTINGUISH.name());
        cfg.addMissing("Settings.Particle_Effect", Particle.FLAME.name());
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack item) {
        return ItemUT.isTool(item) && !ItemUT.isHoe(item);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    @NotNull
    public List<ItemStack> getCustomDrops(@NotNull Player player, @NotNull ItemStack item, @NotNull Block block, int level) {
        if (block.getState() instanceof Container) return Collections.emptyList();

        List<ItemStack> drops = plugin.getNMS().getBlockDrops(block, player, item);
        return this.smelt(drops);
    }

    @NotNull
    public List<ItemStack> smelt(@NotNull List<ItemStack> drops) {
        return drops.stream().peek(drop -> {
            Material material = this.smeltingTable.get(drop.getType());
            if (material != null) drop.setType(material);
        }).toList();
    }

    public void playEffect(@NotNull Block block) {
        Location location = LocUT.getCenter(block.getLocation(), true);
        MsgUT.sound(location, this.sound);
        EffectUT.playEffect(location, this.particle, 0.2f, 0.2f, 0.2f, 0.05f, 30);
    }

    @Override
    public boolean isEventMustHaveDrops() {
        return true;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();

        if (EnchantTelekinesis.isDropHandled(block)) return false;
        if (this.isEventMustHaveDrops() && !e.isDropItems()) return false;
        if (!this.checkTriggerChance(level)) return false;

        List<ItemStack> defaults = plugin.getNMS().getBlockDrops(block, player, item);
        List<ItemStack> custom = this.getCustomDrops(player, item, block, level);
        if (custom.isEmpty() || custom.containsAll(defaults)) return false;

        e.setDropItems(false);

        World world = block.getWorld();
        Location location = LocUT.getCenter(block.getLocation(), true);

        custom.forEach(itemSmelt -> world.dropItem(location, itemSmelt));
        this.playEffect(block);
        return true;
    }
}
