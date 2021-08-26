package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.StringUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EnchantSilkChest extends IEnchantChanceTemplate implements BlockEnchant, CustomDropEnchant {

    private final Map<Integer, NamespacedKey> keyItems;
    private final String                      chestName;

    public EnchantSilkChest(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
        this.keyItems = new TreeMap<>();
        this.chestName = StringUT.color(cfg.getString("Settings.Chest_Item.Name", "%name% &7(%items% items)"));

        for (int pos = 0; pos < 27; pos++) {
            this.getItemKey(pos);
        }
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.chest-name")) {
            String name = cfg.getString("settings.chest-name");

            cfg.set("Settings.Chest_Item.Name", name);
            cfg.set("settings.chest-name", null);
        }
    }

    private NamespacedKey getItemKey(int pos) {
        return this.keyItems.computeIfAbsent(pos, key -> new NamespacedKey(plugin, "silkchest_item_" + pos));
    }

    public boolean isSilkChest(@NotNull ItemStack item) {
        return DataUT.getStringData(item, this.getItemKey(0)) != null;
    }

    @NotNull
    public ItemStack getSilkChest(@NotNull Chest chest) {
        Block block = chest.getBlock();
        ItemStack chestItem = new ItemStack(block.getType());

        // Store and count chest items.
        int amount = 0;
        int count = 0;
        for (ItemStack itemInv : chest.getBlockInventory().getContents()) {
            if (itemInv == null) itemInv = new ItemStack(Material.AIR);
            else amount++;

            String base64 = ItemUT.toBase64(itemInv);
            if (base64 == null) continue;
            if (base64.length() >= Short.MAX_VALUE) {
                block.getWorld().dropItemNaturally(block.getLocation(), itemInv);
                continue;
            }
            DataUT.setData(chestItem, this.getItemKey(count++), base64);
        }

        // Apply item meta name and items data string.
        ItemMeta meta = chestItem.getItemMeta();
        if (meta != null) {
            String nameOrig = ItemUT.getItemName(chestItem);
            String nameChest = this.chestName.replace("%name%", nameOrig).replace("%items%", String.valueOf(amount));
            meta.setDisplayName(nameChest);
            chestItem.setItemMeta(meta);
        }

        return chestItem;
    }

    @Override
    @NotNull
    public List<ItemStack> getCustomDrops(@NotNull Player player, @NotNull ItemStack item, @NotNull Block block, int level) {
        if (block.getType() == Material.ENDER_CHEST) return Collections.emptyList();
        if (!(block.getState() instanceof Chest chest)) return Collections.emptyList();

        return Collections.singletonList(this.getSilkChest(chest));
    }

    @Override
    public boolean isEventMustHaveDrops() {
        return true;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();
        if (EnchantTelekinesis.isDropHandled(block)) return false;
        if (block.getType() == Material.ENDER_CHEST) return false;
        if (this.isEventMustHaveDrops() && !e.isDropItems()) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!(block.getState() instanceof Chest chest)) return false;

        // Drop custom chest and do not drop the original one.
        this.getCustomDrops(player, item, block, level).forEach(chestItem -> block.getWorld().dropItemNaturally(block.getLocation(), chestItem));

        // Do not drop chest items.
        chest.getBlockInventory().clear();
        e.setDropItems(false);

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSilkChestPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (ItemUT.isAir(item)) return;

        Block block = e.getBlockPlaced();
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) return;

        Inventory inventory = chest.getBlockInventory();
        for (int pos = 0; pos < inventory.getSize(); pos++) {
            String data = DataUT.getStringData(item, this.getItemKey(pos));
            if (data == null) continue;

            ItemStack itemInv = ItemUT.fromBase64(data);
            inventory.setItem(pos, itemInv);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSilkChestStore(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getType() == InventoryType.CRAFTING || inv.getType() == InventoryType.CREATIVE) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || ItemUT.isAir(item)) return;

        if (this.isSilkChest(item)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSilkChestHopper(InventoryPickupItemEvent e) {
        e.setCancelled(this.isSilkChest(e.getItem().getItemStack()));
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack item) {
        return ItemUT.isAxe(item);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }
}
