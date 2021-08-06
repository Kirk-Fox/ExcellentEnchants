package su.nightexpress.excellentenchants.manager.listeners;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.ObtainSettings;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.object.EnchantTier;
import su.nightexpress.excellentenchants.manager.type.ObtainType;

import java.util.Map;

public class EnchantGenericListener extends AbstractListener<ExcellentEnchants> {
	
	public EnchantGenericListener(@NotNull EnchantManager enchantManager) {
		super(enchantManager.plugin());
	}

	// ---------------------------------------------------------------
	// Handle Anvil
	// ---------------------------------------------------------------
	@EventHandler(priority = EventPriority.HIGH)
	public void onEnchantUpdateAnvil(PrepareAnvilEvent e) {
		AnvilInventory inventory = e.getInventory();
		
		ItemStack first = inventory.getItem(0);
		ItemStack second = inventory.getItem(1);
		ItemStack result = e.getResult();
		
		// Check if source item is an enchantable single item.
		if (first == null || !EnchantManager.isEnchantable(first) || first.getAmount() > 1) return;
		
		// For repair/rename, only re-add item enchants.
		if ((second == null || ItemUT.isAir(second) || !EnchantManager.isEnchantable(second)) && (result != null && result.getType() == first.getType())) {
			ItemStack result2 = new ItemStack(result);
			EnchantManager.getItemCustomEnchants(first).forEach((hasEnch, hasLevel) -> {
				EnchantManager.addEnchant(result2, hasEnch, hasLevel, true);
			});
			e.setResult(result2);
			return;
		}

		// Check if the second item is an enchantable single item.
		if (second == null || second.getAmount() > 1 || !EnchantManager.isEnchantable(second)) return;
		
		// Prevent operation if first item is book while the second one is another item.
		if (first.getType() == Material.ENCHANTED_BOOK && second.getType() != first.getType()) return;
		
		// Fine result item in case if it's nulled somehow.
		if (result == null || result.getType() == Material.AIR) {
			result = new ItemStack(first);
		}
		
		Map<ExcellentEnchant, Integer> enchAdd = EnchantManager.getItemCustomEnchants(first);
		int repairCost = inventory.getRepairCost();
		
		// If the second item is an enchanted book or the same item type, then
		// we can merge our enchantments.
		if (second.getType() == Material.ENCHANTED_BOOK || second.getType() == first.getType()) {
			for (Map.Entry<ExcellentEnchant, Integer> en : EnchantManager.getItemCustomEnchants(second).entrySet()) {
				enchAdd.merge(en.getKey(), en.getValue(), (oldLvl, newLvl) -> (oldLvl.equals(newLvl)) ? (oldLvl + 1) : (Math.max(oldLvl, newLvl)));
			}
		}
		
		// Recalculate operation cost depends on enchantments merge cost.
		for (Map.Entry<ExcellentEnchant, Integer> ent : enchAdd.entrySet()) {
			ExcellentEnchant enchant = ent.getKey();
			int level = Math.min(enchant.getMaxLevel(), ent.getValue());
			if (EnchantManager.addEnchant(result, enchant, level, false)) {
				repairCost += enchant.getAnvilMergeCost(level);
			}
		}
		
		if (!first.equals(result)) {
			EnchantManager.updateItemLoreEnchants(result);
			e.setResult(result);
			
			// NMS ContainerAnvil will set level cost to 0 right after calling the event
			// So we have to change it with a 1 tick delay.
			final int repairCost2 = repairCost;
			this.plugin.runTask((c) -> inventory.setRepairCost(repairCost2), false);
		}
	}
	
	// ---------------------------------------------------------------
	// Update enchantment lore after grindstone
	// ---------------------------------------------------------------
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEnchantUpdateGrindstone(InventoryClickEvent e) {
		Inventory inventory = e.getInventory();
		if (inventory.getType() != InventoryType.GRINDSTONE) return;
		
		this.plugin.getServer().getScheduler().runTask(plugin, () -> {
			ItemStack result = inventory.getItem(2);
			if (result == null || ItemUT.isAir(result)) return;
			
			EnchantManager.updateItemLoreEnchants(result);
		});
	}
	
	// ---------------------------------------------------------------
	// Handle Enchanting Table
	// ---------------------------------------------------------------
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEnchantPopulateEnchantingTable(final EnchantItemEvent e) {
		ObtainSettings settings = Config.getObtainSettings(ObtainType.ENCHANTING);
		if (settings == null || Rnd.get(true) > settings.getEnchantsCustomGenerationChance()) return;

		ItemStack target = e.getItem();
		boolean enchantAdded = false;

		int enchHas = e.getEnchantsToAdd().size();
		int enchMax = settings.getEnchantsTotalMax();
		int enchRoll = Rnd.get(settings.getEnchantsCustomMin(), settings.getEnchantsCustomMax());

		for (int count = 0; (count < enchRoll && count + enchHas < enchMax); count++) {
			EnchantTier tier = EnchantManager.getTierByChance(ObtainType.ENCHANTING);
			if (tier == null) continue;

			ExcellentEnchant enchant = tier.getEnchant(ObtainType.ENCHANTING, target);
			if (enchant == null) continue;
			if (e.getEnchantsToAdd().keySet().stream().anyMatch(add -> add.conflictsWith(enchant) || enchant.conflictsWith(add))) continue;

			int level = enchant.getLevelByEnchantCost(e.getExpLevelCost());
 			//if (!EnchantManager.canEnchant(target, enchant, level)) continue;

			e.getEnchantsToAdd().put(enchant, level);
			enchantAdded = true;
		}
		
		if (enchantAdded) {
			plugin.getServer().getScheduler().runTask(plugin, () -> {
				ItemStack result = e.getInventory().getItem(0);
				if (result == null) return;
				
				// Fix enchantments for Enchant Books.
				// Enchants are not added on book because they do not exists in NMS.
				// Server gets enchants from NMS to apply it on Book NBT tags.
				ItemMeta meta = result.getItemMeta();
				if (meta instanceof EnchantmentStorageMeta meta2) {
					e.getEnchantsToAdd().forEach((en, lvl) -> {
						if (!meta2.hasStoredEnchant(en)) {
							meta2.addStoredEnchant(en, lvl, true);
						}
					});
					result.setItemMeta(meta2);
				}
				
				EnchantManager.updateItemLoreEnchants(result);
				e.getInventory().setItem(0, result);
			});
		}
	}
	
	// ---------------------------------------------------------------
	// Adding Enchants to Villagers
	// ---------------------------------------------------------------
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEnchantPopulateVillagerAcquire(VillagerAcquireTradeEvent e) {
		MerchantRecipe recipe = e.getRecipe();
		ItemStack result = recipe.getResult();

		if (!EnchantManager.isEnchantable(result)) return;
		if (!EnchantManager.populateEnchantments(result, ObtainType.VILLAGER)) return;

		int uses = recipe.getUses();
		int maxUses = recipe.getMaxUses();
		boolean expReward = recipe.hasExperienceReward();
		int villagerExperience = recipe.getVillagerExperience();
		float priceMultiplier = recipe.getPriceMultiplier();
		
		MerchantRecipe recipe2 = new MerchantRecipe(result, uses, maxUses, expReward, villagerExperience, priceMultiplier);
		recipe2.setIngredients(recipe.getIngredients());
		e.setRecipe(recipe2);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEnchantPopulateLoot(LootGenerateEvent e) {
		if (Config.getObtainSettings(ObtainType.LOOT_GENERATION) == null) return;

		Entity entity = e.getEntity();
		InventoryHolder holder = e.getInventoryHolder();

		if (entity instanceof Minecart || holder instanceof Chest) {
			e.getLoot().forEach(item -> {
				if (item != null && EnchantManager.isEnchantable(item)) {
					EnchantManager.populateEnchantments(item, ObtainType.LOOT_GENERATION);
				}
			});
		}
	}
}
