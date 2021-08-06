package su.nightexpress.excellentenchants.manager.object;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.StringUT;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.manager.type.ObtainType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EnchantTier {

    private final String                  id;
    private final String                  name;
    private final String                  color;
    private final Map<ObtainType, Double> chance;

    private final Set<ExcellentEnchant> enchants;

    public EnchantTier(@NotNull String id, @NotNull String name, @NotNull String color, @NotNull Map<ObtainType, Double> chance) {
        this.id = id.toLowerCase();
        this.name = StringUT.color(name);
        this.color = StringUT.color(color);
        this.chance = chance;
        this.enchants = new HashSet<>();
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public String getColor() {
        return this.color;
    }

    @NotNull
    public Map<ObtainType, Double> getChance() {
        return this.chance;
    }

    public double getChance(@NotNull ObtainType obtainType) {
        return this.getChance().getOrDefault(obtainType, 0D);
    }

    @NotNull
    public Set<ExcellentEnchant> getEnchants() {
        return this.enchants;
    }

    @NotNull
    public Set<ExcellentEnchant> getEnchants(@NotNull ObtainType obtainType) {
        return this.getEnchants(obtainType, null);
    }

    @NotNull
    public Set<ExcellentEnchant> getEnchants(@NotNull ObtainType obtainType, @Nullable ItemStack item) {
        Set<ExcellentEnchant> set = this.getEnchants().stream().filter(en -> en.getObtainChance(obtainType) > 0).filter(en -> item == null || en.canEnchantItem(item)).collect(Collectors.toSet());
        set.removeIf(en -> obtainType == ObtainType.ENCHANTING && en.isTreasure());
        return set;
    }

    @Nullable
    public ExcellentEnchant getEnchant(@NotNull ObtainType obtainType) {
        return getEnchant(obtainType, null);
    }

    @Nullable
    public ExcellentEnchant getEnchant(@NotNull ObtainType obtainType, @Nullable ItemStack item) {
        Map<ExcellentEnchant, Double> map = this.getEnchants(obtainType).stream().filter(en -> item == null || en.canEnchantItem(item)).collect(Collectors.toMap(k -> k, v -> v.getObtainChance(obtainType)));
        return map.isEmpty() ? null : Rnd.get(map);
    }
}
