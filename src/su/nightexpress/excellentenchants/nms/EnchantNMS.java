package su.nightexpress.excellentenchants.nms;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface EnchantNMS {

	void handleFlameWalker(@NotNull LivingEntity entity, @NotNull Location loc, int level);
}
