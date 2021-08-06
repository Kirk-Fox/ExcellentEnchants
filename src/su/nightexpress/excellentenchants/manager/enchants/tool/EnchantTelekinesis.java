package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.LocUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantTelekinesis extends IEnchantChanceTemplate implements PassiveEnchant {

    private final Scaler radHorizon;
    private final Scaler radVert;
    private final Scaler power;

    public EnchantTelekinesis(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg);

        this.radHorizon = new EnchantScaler(this, "Settings.Radius.Horizontal");
        this.radVert = new EnchantScaler(this, "Settings.Radius.Vertical");
        this.power = new EnchantScaler(this, "Settings.Power");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.radius.horizontal")) {
            String i1 = cfg.getString("settings.radius.horizontal", "").replace("%level%", PLACEHOLDER_LEVEL);
            String i2 = cfg.getString("settings.radius.vertial", "").replace("%level%", PLACEHOLDER_LEVEL);
            String i3 = cfg.getString("settings.power", "").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Radius.Horizontal", i1);
            cfg.set("Settings.Radius.Vertical", i2);
            cfg.set("Settings.Power", i3);
            cfg.set("settings.radius.horizontal", null);
            cfg.set("settings.radius.vertial", null);
            cfg.set("settings.power", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%radius%", NumberUT.format(this.radHorizon.getValue(level))));
    }

    @Override
    public boolean use(@NotNull LivingEntity user, int level) {
        if (!(user instanceof Player)) return false;
        if (((Player) user).getInventory().firstEmpty() == -1) return false;
        if (!this.checkTriggerChance(level)) return false;

        double radH = this.radHorizon.getValue(level);
        double radV = this.radVert.getValue(level);
        double power = this.power.getValue(level);

        user.getNearbyEntities(radH, radV, radH).stream().filter(entity -> entity instanceof Item).map(entity -> (Item) entity).forEach(item -> {
            Vector dir = LocUT.getDirectionTo(item.getLocation(), user.getLocation());
            item.setVelocity(dir.multiply(power));
        });
        return true;
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack item) {
        return ItemUT.isTool(item) || ItemUT.isWeapon(item);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }
}
