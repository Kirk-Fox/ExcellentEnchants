package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.NumberUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantBlastMining extends IEnchantChanceTemplate implements BlockEnchant {

    private final Scaler explosionPower;

    public EnchantBlastMining(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.explosionPower = new EnchantScaler(this, "Settings.Explosion.Power");
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.DIVINE_TOUCH);
        this.addConflict(EnchantRegister.SMELTER);
        this.addConflict(Enchantment.SILK_TOUCH);
        this.addConflict(EnchantRegister.TUNNEL);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.explosion-power")) {
            String size = cfg.getString("settings.explosion-power", "").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Explosion.Power", size);
            cfg.set("settings.explosion-power", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str.replace("%power%", NumberUT.format(this.getExplosionPower(level))));
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.checkTriggerChance(level)) return false;

        float power = (float) this.getExplosionPower(level);

        Block block = e.getBlock();
        return block.getWorld().createExplosion(block.getLocation(), power, false, true, player);
    }

    public double getExplosionPower(int level) {
        return this.explosionPower.getValue(level);
    }

    @Override
    public boolean canEnchant(@NotNull ItemStack item) {
        return ItemUT.isPickaxe(item);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    // Do not damage around entities by en enchantment explosion.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastDamage(EntityDamageByEntityEvent e) {
        if (e.getCause() != DamageCause.ENTITY_EXPLOSION) return;

        Entity eDamager = e.getDamager();
        if (!(eDamager instanceof Player player)) return;

        ItemStack pick = player.getInventory().getItemInMainHand();
        if (EnchantManager.getEnchantLevel(pick, this) <= 0) return;

        e.setCancelled(true);
    }
}
