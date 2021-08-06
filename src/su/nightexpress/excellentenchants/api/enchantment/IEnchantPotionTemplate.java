package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nexmedia.engine.utils.PlayerUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public abstract class IEnchantPotionTemplate extends IEnchantChanceTemplate {

    public static final String PLACEHOLDER_POTION_LEVEL    = "%enchantment_potion_level%";
    public static final String PLACEHOLDER_POTION_DURATION = "%enchantment_potion_duration%";
    public static final String PLACEHOLDER_POTION_TYPE     = "%enchantment_potion_type%";
    public static final String PLACEHOLDER_COST_ITEM       = "%enchantment_cost_item%";

    protected ItemStack payItem;
    protected boolean   payEnabled;

    protected       PotionEffectType potionType;
    protected       Scaler           potionDuration;
    protected       Scaler           potionLevel;
    protected final boolean          isParticles;

    public IEnchantPotionTemplate(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg, @NotNull PotionEffectType potionType) {
        super(plugin, cfg);
        this.payEnabled = cfg.getBoolean("Settings.Cost.Enabled");
        this.payItem = cfg.getItem("Settings.Cost.Item");
        this.potionType = potionType;
        this.potionDuration = new EnchantScaler(this, "Settings.Potion_Effect.Duration");
        this.potionLevel = new EnchantScaler(this, "Settings.Potion_Effect.Level");
        this.isParticles = !(this instanceof PassiveEnchant);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.effect-level")) {
            String effectDuration = cfg.getString("settings.effect-duration", "10").replace("%level%", PLACEHOLDER_LEVEL);
            String effectLevel = cfg.getString("settings.effect-level", "%level%").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Potion_Effect.Duration", effectDuration);
            cfg.set("Settings.Potion_Effect.Level", effectLevel);
            cfg.set("settings.effect-duration", null);
            cfg.set("settings.effect-level", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
                .replace(PLACEHOLDER_POTION_LEVEL, NumberUT.toRoman(this.getEffectLevel(level)))
                .replace(PLACEHOLDER_POTION_DURATION, NumberUT.format((double) this.getEffectDuration(level) / 20D))
                .replace(PLACEHOLDER_POTION_TYPE, plugin.lang().getPotionType(this.getPotionEffectType()))
                .replace(PLACEHOLDER_COST_ITEM, ItemUT.getItemName(this.payItem))
        );
    }

    public boolean hasPayItem() {
        return this.payEnabled && !ItemUT.isAir(this.payItem);
    }

    public boolean takePayItem(@NotNull Player player) {
        return PlayerUT.takeItem(player, this.payItem, 1);
    }

    @NotNull
    public final PotionEffectType getPotionEffectType() {
        return this.potionType;
    }

    public final int getEffectDuration(int level) {
        return (int) (this.potionDuration.getValue(level) * 20);
    }

    public final int getEffectLevel(int level) {
        return (int) this.potionLevel.getValue(level);
    }

    public final boolean addEffect(@NotNull LivingEntity target, int level) {
        if (this.hasPayItem() && target instanceof Player player) {
            if (!this.takePayItem(player)) return false;
        }

        int duration = this.getEffectDuration(level);
        int amplifier = Math.max(0, this.getEffectLevel(level) - 1);

        PotionEffect effect = new PotionEffect(this.potionType, duration, amplifier, false, this.isParticles);
        EnchantManager.addPotionEffect(target, effect, true);
        return true;
    }
}