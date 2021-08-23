package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.player.listener.PlayerBlockPlacedListener;
import su.nexmedia.engine.utils.EffectUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.MsgUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantTreasures extends IEnchantChanceTemplate implements BlockEnchant, CustomDropEnchant {

    private final String                               particleEffect;
    private final String                               sound;
    private final Map<Material, Map<Material, Double>> treasures;

    public EnchantTreasures(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.LOWEST);

        this.particleEffect = cfg.getString("Particle_Effect", Particle.VILLAGER_HAPPY.name());
        this.sound = cfg.getString("Settings.Sound", Sound.BLOCK_NOTE_BLOCK_BELL.name());
        this.treasures = new HashMap<>();
        for (String sFrom : cfg.getSection("Settings.Treasures")) {
            Material mFrom = Material.getMaterial(sFrom.toUpperCase());
            if (mFrom == null) {
                plugin.error("[Treasures] Invalid source material '" + sFrom + "' !");
                continue;
            }
            Map<Material, Double> treasuresList = new HashMap<>();

            for (String sTo : cfg.getSection("Settings.Treasures." + sFrom)) {
                Material mTo = Material.getMaterial(sTo.toUpperCase());
                if (mTo == null) {
                    plugin.error("[Treasures] Invalid result material '" + sTo + "' for '" + sFrom + "' !");
                    continue;
                }

                double tChance = cfg.getDouble("Settings.Treasures." + sFrom + "." + sTo);
                treasuresList.put(mTo, tChance);
            }
            this.treasures.put(mFrom, treasuresList);
        }
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
        cfg.addMissing("Settings.Particle_Effect", Particle.VILLAGER_HAPPY.name());
        cfg.addMissing("Settings.Sound", Sound.BLOCK_NOTE_BLOCK_BELL.name());
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
    public List<ItemStack> getCustomDrops(@NotNull Block block, int level) {
        ItemStack item = this.getTreasure(block);
        if (PlayerBlockPlacedListener.isUserPlaced(block) || item == null) return Collections.emptyList();
        return Collections.singletonList(item);
    }

    @Override
    public boolean isEventMustHaveDrops() {
        return false;
    }

    @Nullable
    public final ItemStack getTreasure(@NotNull Block block) {
        Map<Material, Double> treasures = this.treasures.get(block.getType());
        if (treasures == null) return null;

        Material mat = Rnd.get(treasures);
        return mat != null ? new ItemStack(mat) : null;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();

        if (EnchantTelekinesis.isDropHandled(block)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (this.isEventMustHaveDrops() && !e.isDropItems()) return false;
        if (PlayerBlockPlacedListener.isUserPlaced(block)) return false;

        Location loc = LocUT.getCenter(block.getLocation());

        this.getCustomDrops(block, level).forEach(itemDrop -> block.getWorld().dropItem(loc, itemDrop));
        MsgUT.sound(loc, this.sound);
        EffectUT.playEffect(loc, this.particleEffect, 0.2f, 0.2f, 0.2f, 0.12f, 20);
        return true;
    }
}
