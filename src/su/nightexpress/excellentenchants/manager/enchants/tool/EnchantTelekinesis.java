package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.ILangMsg;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.util.*;
import java.util.function.UnaryOperator;

public class EnchantTelekinesis extends IEnchantChanceTemplate implements BlockEnchant {

    public static final String META_BLOCK_DROP_HANDLER = "telekinesis_drop_handler";

    private final ILangMsg messageDropReceived;
    private final String messageItemName;
    private final String messageItemSeparator;

    public EnchantTelekinesis(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGHEST);

        this.messageDropReceived = new ILangMsg(plugin.lang(), cfg.getString("Settings.Message.Drop_Received", ""));
        this.messageItemName = StringUT.color(cfg.getString("Settings.Message.Item_Name", "&7x%item_amount% &f%item_name%"));
        this.messageItemSeparator = StringUT.color(cfg.getString("Settings.Message.Item_Separator", "&7, "));
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.set("Settings.Radius", null);
        cfg.set("Settings.Power", null);

        cfg.addMissing("Settings.Message.Drop_Received", "{message: ~type: ACTION_BAR; ~prefix: false;}%items%");
        cfg.addMissing("Settings.Message.Item_Name", "&7x%item_amount% &f%item_name%");
        cfg.addMissing("Settings.Message.Item_Separator", "&7, ");
    }

    public static boolean isDropHandled(@NotNull Block block) {
        return block.hasMetadata(META_BLOCK_DROP_HANDLER);
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return /*str -> */super.replacePlaceholders(level);//.apply(str.replace("%radius%", NumberUT.format(this.radHorizon.getValue(level))));
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        //if (!e.isDropItems()) return false;
        if (!this.checkTriggerChance(level)) return false;

        Block block = e.getBlock();
        List<ItemStack> drops = new ArrayList<>(plugin.getNMS().getBlockDrops(block, player, item));

        // Check inventory space.
        if (drops.stream().anyMatch(itemDrop -> PlayerUT.countItemSpace(player, itemDrop) == 0)) return false;

        // Tell other enchantments that block drops are handled by Telekinesis.
        block.setMetadata(META_BLOCK_DROP_HANDLER, new FixedMetadataValue(plugin, true));

        for (Map.Entry<CustomDropEnchant, Integer> entry : EnchantManager.getItemCustomEnchants(item, CustomDropEnchant.class).entrySet()) {
            CustomDropEnchant dropEnchant = entry.getKey();
            int dropLevel = entry.getValue();
            if (dropEnchant.isEventMustHaveDrops() && !e.isDropItems()) continue;

            if (dropEnchant instanceof IEnchantChanceTemplate chanceEnchant) {
                if (!chanceEnchant.checkTriggerChance(dropLevel)) continue;
            }
            if (dropEnchant instanceof EnchantSilkChest) {
                drops.removeIf(drop -> drop.getType() == block.getType());
            }
            if (dropEnchant instanceof EnchantSmelter smelter) {
                smelter.smelt(drops);
                smelter.playEffect(block);
                continue; // Do not add smelted items twice, only replace current ones.
            }

            drops.addAll(dropEnchant.getCustomDrops(player, item, block, dropLevel));
        }
        drops.removeIf(Objects::isNull);

        StringBuilder builder = new StringBuilder();
        drops.forEach(drop -> {
            ItemUT.addItem(player, drop);

            if (!builder.isEmpty()) builder.append(this.messageItemSeparator);
            builder.append(this.messageItemName
                    .replace("%item_name%", ItemUT.getItemName(drop))
                    .replace("%item_amount%", String.valueOf(drop.getAmount())));
        });
        this.messageDropReceived.replace("%items%", builder.toString()).send(player);

        e.setDropItems(false);
        block.removeMetadata(META_BLOCK_DROP_HANDLER, plugin);
        return true;
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack item) {
        return ItemUT.isTool(item);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }
}
