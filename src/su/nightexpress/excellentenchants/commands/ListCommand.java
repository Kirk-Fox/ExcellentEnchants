package su.nightexpress.excellentenchants.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.commands.api.ISubCommand;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;

public class ListCommand extends ISubCommand<ExcellentEnchants> {

    public ListCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"list"}, Perms.USER);
    }

    @Override
    public @NotNull String usage() {
        return "";
    }

    @Override
    public @NotNull String description() {
        return plugin.lang().Command_List_Desc.getMsg();
    }

    @Override
    public boolean playersOnly() {
        return true;
    }

    @Override
    protected void perform(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        plugin.getEnchantManager().getEnchantsListGUI().open((Player) sender, 1);
    }
}
