package su.nightexpress.excellentenchants;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.commands.api.IGeneralCommand;
import su.nexmedia.engine.core.Version;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.excellentenchants.commands.BookCommand;
import su.nightexpress.excellentenchants.commands.EnchantCommand;
import su.nightexpress.excellentenchants.commands.ListCommand;
import su.nightexpress.excellentenchants.commands.TierbookCommand;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.nms.EnchantNMS;

public class ExcellentEnchants extends NexPlugin<ExcellentEnchants> {
	
	private static ExcellentEnchants instance;
	
	private Config config;
	private Lang lang;
	
	private EnchantNMS nmsHandler;
	private EnchantManager enchantManager;
	
    public static ExcellentEnchants getInstance() {
    	return instance;
    }
    
	public ExcellentEnchants() {
	    instance = this;
	}
	
	@Override
	public void enable() {
		if (!this.setNMS()) {
			this.error("Could not setup internal NMS handler!");
			this.getPluginManager().disablePlugin(this);
			return;
		}
		
		this.enchantManager = new EnchantManager(this);
		this.enchantManager.setup();

		NexEngine.get().getPlayerManager().enableUserBlockListening();
	}

	@Override
	public void disable() {
		if (this.enchantManager != null) {
			this.enchantManager.shutdown();
			this.enchantManager = null;
		}
		this.nmsHandler = null;
	}
	
	private boolean setNMS() {
    	Version current = Version.CURRENT;
    	if (current == null) return false;
    	
    	String pack = EnchantNMS.class.getPackage().getName();
    	Class<?> clazz = Reflex.getClass(pack, current.name());
    	if (clazz == null) return false;
    	
    	try {
			this.nmsHandler = (EnchantNMS) clazz.getConstructor().newInstance();
		} 
    	catch (Exception e) {
			e.printStackTrace();
		}
		return this.nmsHandler != null;
	}

	@Override
	public void setConfig() {
		this.config = new Config(this);
		this.config.setup();
		
		this.lang = new Lang(this);
		this.lang.setup();
	}
	
	@Override
	public void registerCmds(@NotNull IGeneralCommand<ExcellentEnchants> mainCommand) {
		mainCommand.addSubCommand(new BookCommand(this));
		mainCommand.addSubCommand(new EnchantCommand(this));
		mainCommand.addSubCommand(new ListCommand(this));
		mainCommand.addSubCommand(new TierbookCommand(this));
	}

	@Override
	public void registerHooks() {
		
	}
	
	@Override
	public void registerEditor() {
		
	}

	@Override
	@NotNull
	public Config cfg() {
		return this.config;
	}

	@Override
	@NotNull
	public Lang lang() {
		return this.lang;
	}

	@NotNull
	public EnchantManager getEnchantManager() {
		return this.enchantManager;
	}
	
	@NotNull
	public EnchantNMS getNMSHandler() {
		return nmsHandler;
	}
}
