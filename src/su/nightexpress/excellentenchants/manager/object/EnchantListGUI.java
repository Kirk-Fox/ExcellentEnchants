package su.nightexpress.excellentenchants.manager.object;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.utils.CollectionsUT;
import su.nexmedia.engine.utils.DataUT;
import su.nexmedia.engine.utils.ItemUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

import java.util.*;

public class EnchantListGUI extends AbstractMenu<ExcellentEnchants> {

    private final ItemStack enchantIcon;
    private final int[]     enchantSlots;

    private final NamespacedKey keyLevel;
    private final Map<String, Map<Integer, ItemStack>> iconCache;

    public EnchantListGUI(@NotNull ExcellentEnchants plugin) {
        super(plugin, JYML.loadOrExtract(plugin, "gui.enchants.yml"), "");
        this.keyLevel = new NamespacedKey(plugin, "display_level");
        this.iconCache = new HashMap<>();

        this.enchantIcon = cfg.getItem("Enchantments.Icon");
        this.enchantSlots = cfg.getIntArray("Enchantments.Slots");

        IMenuClick click = (p, type, e) -> {
            if (type instanceof MenuItemType type2) {
                switch (type2) {
                    case PAGE_NEXT -> this.open(p, this.getPage(p) + 1);
                    case PAGE_PREVIOUS -> this.open(p, this.getPage(p) - 1);
                    case CLOSE -> p.closeInventory();
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            IMenuItem menuItem = cfg.getMenuItem("Content." + sId);

            if (menuItem.getType() != null) {
                menuItem.setClick(click);
            }
            this.addItem(menuItem);
        }
    }

    private ItemStack getEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        return this.iconCache.computeIfAbsent(enchant.getId(), k -> new HashMap<>())
                .computeIfAbsent(level, k -> this.buildEnchantIcon(enchant, level));
    }

    @NotNull
    private ItemStack buildEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        ItemStack icon = new ItemStack(this.enchantIcon);
        ItemUT.replace(icon, enchant.formatString(level));
        return icon;
    }

    @Override
    public void onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        int page = this.getPage(player);
        int length = this.enchantSlots.length;
        List<ExcellentEnchant> list = new ArrayList<>(EnchantRegister.ENCHANT_LIST.stream()
            .sorted(Comparator.comparing(ExcellentEnchant::getName)).toList());
        List<List<ExcellentEnchant>> split = CollectionsUT.split(list, length);

        int pages = split.size();
        if (pages < 1 || pages < page) list = Collections.emptyList();
        else list = split.get(page - 1);

        int count = 0;
        for (ExcellentEnchant enchant : list) {
            ItemStack icon = this.getEnchantIcon(enchant, 1);
            DataUT.setData(icon, this.keyLevel, 1);

            IMenuClick click = (p, type, e) -> {
                if (!e.isLeftClick()) return;

                ItemStack itemClick = e.getCurrentItem();
                if (itemClick == null) return;

                int levelHas = DataUT.getIntData(itemClick, this.keyLevel);
                if (levelHas == 0) return;

                if (++levelHas > enchant.getMaxLevel()) levelHas = enchant.getStartLevel();
                itemClick = this.getEnchantIcon(enchant, levelHas);
                DataUT.setData(itemClick, this.keyLevel, levelHas);

                e.setCurrentItem(itemClick);
            };

            IMenuItem menuItem = new MenuItem(icon, this.enchantSlots[count++]);
            menuItem.setClick(click);
            this.addItem(player, menuItem);
        }
        this.setPage(player, page, pages);
    }

    @Override
    public void onReady(@NotNull Player player, @NotNull Inventory inventory) {

    }

    @Override
    public boolean cancelClick(@NotNull SlotType slotType, int slot) {
        return true;
    }
}
