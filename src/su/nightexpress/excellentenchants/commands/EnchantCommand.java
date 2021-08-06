package su.nightexpress.excellentenchants.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.commands.api.ISubCommand;
import su.nexmedia.engine.utils.ItemUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.util.Arrays;
import java.util.List;

public class EnchantCommand extends ISubCommand<ExcellentEnchants> {

	public EnchantCommand(@NotNull ExcellentEnchants plugin) {
		super(plugin, new String[] {"enchant"}, Perms.ADMIN);
	}
	
	@Override
	@NotNull
	public String description() {
		return plugin.lang().Command_Enchant_Desc.getMsg();
	}

	@Override
	@NotNull
	public String usage() {
		return plugin.lang().Command_Enchant_Usage.getMsg();
	}
	
	@Override
	public boolean playersOnly() {
		return true;
	}

	@Override
	@NotNull
	public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
		if (i == 1) {
			return Arrays.stream(Enchantment.values()).map(e -> e.getKey().getKey()).toList();
		}
		if (i == 2) {
			return Arrays.asList("-1", "1", "5", "10");
		}
		return super.getTab(player, i, args);
	}
	
	@Override
	public void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if (args.length != 3) {
			this.printUsage(sender);
			return;
		}
		
		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		if (ItemUT.isAir(item)) {
			this.errItem(sender);
			return;
		}

		Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[1].toLowerCase()));
		if (enchantment == null) {
			plugin.lang().Error_NoEnchant.send(sender);
			return;
		}
		
		int level = this.getNumI(sender, args[2], -1, true);
		if (level < 0) {
			level = Rnd.get(enchantment.getStartLevel(), enchantment.getMaxLevel());
		}
		
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;
		
		if (meta instanceof EnchantmentStorageMeta) {
			if (level == 0) {
				((EnchantmentStorageMeta)meta).removeStoredEnchant(enchantment);
			}
			else {
				((EnchantmentStorageMeta)meta).addStoredEnchant(enchantment, level, true);
			}
		}
		else {
			if (level == 0) {
				meta.removeEnchant(enchantment);
			}
			else {
				meta.addEnchant(enchantment, level, true);
			}
		}
		item.setItemMeta(meta);
		EnchantManager.updateItemLoreEnchants(item);
		
		plugin.lang().Command_Enchant_Done.send(sender);
	}
}
