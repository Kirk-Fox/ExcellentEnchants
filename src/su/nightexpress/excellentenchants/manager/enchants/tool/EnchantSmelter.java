package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.*;
import org.bukkit.block.Block;
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
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

import java.util.HashMap;
import java.util.Map;

public class EnchantSmelter extends IEnchantChanceTemplate implements BlockEnchant {

	private final String sound;
	private final String particle;
	private final Map<Material, Material> smeltingTable;
	
	public EnchantSmelter(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
		super(plugin, cfg);

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
	public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
	    if (!e.isDropItems()) return false;
		if (!this.checkTriggerChance(level)) return false;
		
		Block block = e.getBlock();
		Material result = this.smeltingTable.get(block.getType());
    	if (result == null) return false;

		e.setDropItems(false);

		World world = block.getWorld();
	    Location location = LocUT.getCenter(block.getLocation(), true);
		ItemStack itemSmelt = new ItemStack(result);

	    world.dropItem(location, itemSmelt);
		MsgUT.sound(location, this.sound);
		EffectUT.playEffect(location, this.particle, 0.2f, 0.2f, 0.2f, 0.05f, 30);
		return true;
	}
}
