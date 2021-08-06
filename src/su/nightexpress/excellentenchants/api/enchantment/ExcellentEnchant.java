package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.IListener;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;
import su.nightexpress.excellentenchants.manager.object.EnchantTier;
import su.nightexpress.excellentenchants.manager.type.ObtainType;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public abstract class ExcellentEnchant extends Enchantment implements IListener {

    public static final String PLACEHOLDER_NAME                          = "%enchantment_name%";
    public static final String PLACEHLDER_NAME_FORMATTED                 = "%enchantment_name_formatted%";
    public static final String PLACEHOLDER_DESCRIPTION                   = "%enchantment_description%";
    public static final String PLACEHOLDER_LEVEL                         = "%enchantment_level%";
    public static final String PLACEHOLDER_LEVEL_MIN                     = "%enchantment_level_min%";
    public static final String PLACEHOLDER_LEVEL_MAX                     = "%enchantment_level_max%";
    public static final String PLACEHOLDER_CONFLICTS                     = "%enchantment_conflicts%";
    public static final String PLACEHOLDER_TARGET                        = "%enchantment_target%";
    public static final String PLACEHOLDER_TIER                          = "%enchantment_tier%";
    public static final String PLACEHOLDER_OBTAIN_CHANCE_ENCHANTING      = "%enchantment_obtain_chance_enchanting%";
    public static final String PLACEHOLDER_OBTAIN_CHANCE_VILLAGER        = "%enchantment_obtain_chance_villager%";
    public static final String PLACEHOLDER_OBTAIN_CHANCE_LOOT_GENERATION = "%enchantment_obtain_chance_loot_generation%";

    protected final ExcellentEnchants plugin;
    protected final JYML              cfg;
    protected final String            id;

    protected String       displayName;
    protected EnchantTier  tier;
    protected List<String> description;

    private final Set<Enchantment>        conflicts;
    protected     boolean                 isTreasure;
    protected     int                     levelMin;
    protected     int                     levelMax;
    protected     Scaler                  levelByEnchantCost;
    protected     Scaler                  anvilMergeCost;
    protected     Map<ObtainType, Double> obtainChance;

    public ExcellentEnchant(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(NamespacedKey.minecraft(cfg.getFile().getName().replace(".yml", "").toLowerCase()));
        this.plugin = plugin;
        this.id = this.getKey().getKey();
        this.cfg = cfg;
        this.updateConfig();
        this.cfg.saveChanges();

        this.displayName = StringUT.color(cfg.getString("Name", this.getId()));
        this.tier = EnchantManager.getTierById(cfg.getString("Tier", Constants.DEFAULT));
        if (this.tier == null) {
            throw new IllegalStateException("Invalid tier provided for the '" + id + "' enchantment!");
        }
        this.tier.getEnchants().add(this);
        this.description = StringUT.color(cfg.getStringList("Description"));

        this.conflicts = new HashSet<>();
        this.isTreasure = cfg.getBoolean("Is_Treasure");
        this.levelMin = cfg.getInt("Level.Min");
        this.levelMax = cfg.getInt("Level.Max");
        this.levelByEnchantCost = new EnchantScaler(this, "Enchantment_Table.Level_By_Exp_Cost");
        this.anvilMergeCost = new EnchantScaler(this, "Anvil.Merge_Cost");

        double tableChance = cfg.getDouble("Enchantment_Table.Chance");
        double villagerChance = this.cfg.getDouble("Villagers.Chance");
        double lootGenChance = this.cfg.getDouble("Loot_Generation.Chance");
        this.obtainChance = new HashMap<>();
        this.obtainChance.put(ObtainType.ENCHANTING, tableChance);
        this.obtainChance.put(ObtainType.VILLAGER, villagerChance);
        this.obtainChance.put(ObtainType.LOOT_GENERATION, lootGenChance);
    }

    protected void updateConfig() {
        cfg.addMissing("Is_Treasure", false);

        if (cfg.contains("Name")) return;

        String displayName = cfg.getString("name", this.getId());
        String tier = cfg.getString("tier", Constants.DEFAULT);
        String description = cfg.getString("description", "").replace("%chance%", IEnchantChanceTemplate.PLACEHOLDER_CHANCE).replace("%potion-duration%", IEnchantPotionTemplate.PLACEHOLDER_POTION_DURATION).replace("%potion-level%", IEnchantPotionTemplate.PLACEHOLDER_POTION_LEVEL).replace("%potion-effect%", IEnchantPotionTemplate.PLACEHOLDER_POTION_TYPE);

        int levelMin = cfg.getInt("level.min");
        int levelMax = cfg.getInt("level.max");
        String tableLevelMin = cfg.getString("enchantment-table.min-player-level", "1").replace("%level%", PLACEHOLDER_LEVEL);
        String anvilMergeCost = cfg.getString("anvil.merge-cost", PLACEHOLDER_LEVEL).replace("%level%", PLACEHOLDER_LEVEL);

        double tableChance = cfg.getDouble("enchantment-table.chance");
        double villagerChance = this.cfg.getDouble("villagers.chance");
        double lootGenChance = this.cfg.getDouble("loot-generation.chance");
        cfg.set("Name", displayName);
        cfg.set("name", null);
        cfg.set("Tier", tier);
        cfg.set("tier", null);
        cfg.set("Description", description);
        cfg.set("description", null);
        cfg.set("Level.Min", levelMin);
        cfg.set("Level.Max", levelMax);
        cfg.set("level", null);
        cfg.set("Anvil.Merge_Cost", anvilMergeCost);
        cfg.set("anvil", null);
        cfg.set("Enchantment_Table.Level_By_Exp_Cost", tableLevelMin);
        cfg.set("Enchantment_Table.Chance", tableChance);
        cfg.set("enchantment-table", null);
        cfg.set("Villagers.Chance", villagerChance);
        cfg.set("villagers", null);
        cfg.set("Loot_Generation.Chance", lootGenChance);
        cfg.set("loot-generation", null);
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        String conflicts = this.getConflicts().isEmpty() ? plugin.lang().Other_None.getMsg() : this.getConflicts().stream().filter(Objects::nonNull).map(en -> plugin.lang().getEnchantment(en)).collect(Collectors.joining(", "));

        return str -> str
                .replace(PLACEHOLDER_NAME, this.getDisplayName())
                .replace(PLACEHLDER_NAME_FORMATTED, this.getNameFormatted(level))
                .replace(PLACEHOLDER_LEVEL, NumberUT.toRoman(level))
                .replace(PLACEHOLDER_LEVEL_MIN, String.valueOf(this.getStartLevel()))
                .replace(PLACEHOLDER_LEVEL_MAX, String.valueOf(this.getMaxLevel()))
                .replace(PLACEHOLDER_TARGET, plugin.lang().getEnum(this.getItemTarget()))
                .replace(PLACEHOLDER_TIER, this.getTier().getName())
                .replace(PLACEHOLDER_CONFLICTS, conflicts)
                .replace(PLACEHOLDER_OBTAIN_CHANCE_ENCHANTING, NumberUT.format(this.getObtainChance(ObtainType.ENCHANTING)))
                .replace(PLACEHOLDER_OBTAIN_CHANCE_VILLAGER, NumberUT.format(this.getObtainChance(ObtainType.VILLAGER)))
                .replace(PLACEHOLDER_OBTAIN_CHANCE_LOOT_GENERATION, NumberUT.format(this.getObtainChance(ObtainType.LOOT_GENERATION)));
    }

    @NotNull
    public UnaryOperator<String> formatString(int level) {
        return str -> this.replacePlaceholders(level).apply(str
                .replace(PLACEHOLDER_DESCRIPTION, String.join("\n", Config.formatDescription(this.getDescription())))
        );
    }

    protected void addConflicts() {

    }

    protected void addConflict(@NotNull Enchantment enchantment) {
        this.conflicts.add(enchantment);
    }

    @Override
    public void registerListeners() {
        this.addConflicts();
        this.plugin.getPluginManager().registerEvents(this, plugin);
    }

    public @NotNull JYML getConfig() {
        return this.cfg;
    }

    public @NotNull String getId() {
        return this.id;
    }

    @NotNull
    @Override
    public String getName() {
        return getId().toUpperCase();
    }

    public @NotNull String getDisplayName() {
        return this.displayName;
    }

    @NotNull
    public String getNameFormatted(int level) {
        return this.getTier().getColor() + this.getDisplayName() + " " + NumberUT.toRoman(level);
    }

    public @NotNull List<String> getDescription() {
        return this.description;
    }

    public @NotNull List<String> getDescription(int level) {
        List<String> description = new ArrayList<>(this.description);
        description.replaceAll(this.replacePlaceholders(level));
        return description;
    }

    @NotNull
    public Set<Enchantment> getConflicts() {
        return conflicts;
    }

    public @NotNull EnchantTier getTier() {
        return this.tier;
    }

    @Override
    public int getMaxLevel() {
        return this.levelMax;
    }

    @Override
    public int getStartLevel() {
        return this.levelMin;
    }

    public int getLevelByEnchantCost(int expLevel) {
        Optional<Map.Entry<Integer, Double>> opt = this.levelByEnchantCost.getValues().entrySet().stream().filter(en -> expLevel >= en.getValue().intValue()).max(Comparator.comparingInt(Map.Entry::getKey));
        return opt.isPresent() ? opt.get().getKey() : Rnd.get(this.getStartLevel(), this.getMaxLevel());
    }

    public double getObtainChance(@NotNull ObtainType obtainType) {
        return this.obtainChance.getOrDefault(obtainType, 0D);
    }

    public int getAnvilMergeCost(int level) {
        return (int) this.anvilMergeCost.getValue(level);
    }

    @Override
    public final boolean conflictsWith(@NotNull Enchantment enchantment) {
        return this.conflicts.contains(enchantment);
    }

    @Override
    public final boolean canEnchantItem(@Nullable ItemStack item) {
        if (item == null) return false;
        if (item.getEnchantments().keySet().stream().anyMatch(e -> e.conflictsWith(this))) return false;
        if (!item.containsEnchantment(this) && EnchantManager.getItemCustomEnchantsAmount(item) >= Config.ENCHANTMENTS_ITEM_CUSTOM_MAX)
            return false;
        if (item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK) {
            return true;
        }
        return this.canEnchant(item);
    }

    protected boolean canEnchant(@NotNull ItemStack item) {
        EnchantmentTarget target = this.getItemTarget();
        return switch (target) {
            case ARMOR -> ItemUT.isArmor(item);
            case ARMOR_FEET -> ItemUT.isBoots(item);
            case ARMOR_LEGS -> ItemUT.isLeggings(item);
            case ARMOR_TORSO -> ItemUT.isChestplate(item) || (Config.ENCHANTMENTS_ITEM_ELYTRA_AS_CHESTPLATE && item.getType() == Material.ELYTRA);
            case ARMOR_HEAD -> ItemUT.isHelmet(item);
            case WEAPON -> ItemUT.isSword(item) || (Config.ENCHANTMENTS_ITEM_SWORDS_AS_AXES && ItemUT.isAxe(item));
            case TOOL -> ItemUT.isTool(item);
            case BOW -> item.getType() == Material.BOW || (Config.ENCHANTMENTS_ITEM_CROSSBOWS_AS_BOWS && ItemUT.isBow(item));
            case FISHING_ROD -> item.getType() == Material.FISHING_ROD;
            case BREAKABLE -> true;
            case WEARABLE -> EnchantManager.isEnchantable(item);
            case TRIDENT -> ItemUT.isTrident(item);
            case CROSSBOW -> item.getType() == Material.CROSSBOW;
            default -> false;
        };
    }

    @Override
    public final boolean isCursed() {
        return false;
    }

    @Override
    public final boolean isTreasure() {
        return this.isTreasure;
    }
}
