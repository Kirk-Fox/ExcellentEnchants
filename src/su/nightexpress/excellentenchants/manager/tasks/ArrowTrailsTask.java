package su.nightexpress.excellentenchants.manager.tasks;

import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.api.task.ITask;
import su.nexmedia.engine.utils.EffectUT;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;

import java.util.*;

public class ArrowTrailsTask extends ITask<ExcellentEnchants> {
	
	private static final Map<Projectile, Set<String>> TRAILS_MAP = Collections.synchronizedMap(new HashMap<>());
	
	public ArrowTrailsTask(@NotNull ExcellentEnchants plugin) {
		super(plugin, Config.TASKS_ARROW_TRAIL_TICKS_INTERVAL, true);
		TRAILS_MAP.clear();
	}
	
    @Override
	public void action() {
    	TRAILS_MAP.keySet().removeIf(projectile -> !projectile.isValid() || projectile.isDead());
    	
    	TRAILS_MAP.forEach((arrow, effects) -> {
    		effects.forEach(effect -> {
    			EffectUT.playEffect(arrow.getLocation(), effect, 0f, 0f, 0f, 0f, 10);
    		});
    	});
    }
    
    public static void add(@NotNull Projectile projectile, @NotNull String effect) {
		synchronized (TRAILS_MAP) {
			TRAILS_MAP.computeIfAbsent(projectile, list -> new HashSet<>()).add(effect);
		}
    }
}