package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CustomDropEnchant {

    @NotNull List<ItemStack> getCustomDrops(@NotNull Block block, int level);

    boolean isEventMustHaveDrops();
}
