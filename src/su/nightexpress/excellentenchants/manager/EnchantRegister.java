package su.nightexpress.excellentenchants.manager;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.api.JYML;
import su.nexmedia.engine.manager.api.Cleanable;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.manager.enchants.armor.*;
import su.nightexpress.excellentenchants.manager.enchants.weapon.*;
import su.nightexpress.excellentenchants.manager.enchants.bows.*;
import su.nightexpress.excellentenchants.manager.enchants.tool.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EnchantRegister {

    private static final ExcellentEnchants     PLUGIN;
    public static final  Set<ExcellentEnchant> ENCHANT_LIST;

    public static final EnchantBlastMining BLAST_MINING;
    public static final EnchantDivineTouch DIVINE_TOUCH;
    public static final EnchantHaste       HASTE;
    public static final EnchantLuckyMiner  LUCKY_MINER;
    public static final EnchantReplanter   REPLANTER;
    public static final EnchantSilkChest   SILK_CHEST;
    public static final EnchantSmelter     SMELTER;
    public static final EnchantTelekinesis TELEKINESIS;
    public static final EnchantTreasures   TREASURES;
    public static final EnchantTunnel      TUNNEL;
    public static final EnchantVeinminer   VEINMINER;

    public static final EnchantBaneOfNetherspawn BANE_OF_NETHERSPAWN;
    public static final EnchantIceAspect         ICE_ASPECT;
    public static final EnchantInfernus          INFERNUS;
    public static final EnchantVenom             VENOM;
    public static final EnchantExhaust           EXHAUST;
    public static final EnchantWither            WITHER;
    public static final EnchantParalyze          PARALYZE;
    public static final EnchantExpHunter         EXP_HUNTER;
    public static final EnchantDecapitator       EXECUTIONER;
    public static final EnchantCutter            CUTTER;
    public static final EnchantConfusion         CONFUSION;
    public static final EnchantDoubleStrike      DOUBLE_STRIKE;
    public static final EnchantBlindness         BLINDNESS;
    public static final EnchantVampire           VAMPIRE;
    public static final EnchantPigificator       PIGIFICATOR;
    public static final EnchantRage              RAGE;
    public static final EnchantScavenger         SCAVENGER;
    public static final EnchantSurprise          SURPRISE;
    public static final EnchantThrifty           THRIFTY;
    public static final EnchantThunder           THUNDER;
    public static final EnchantVillageDefender   VILLAGE_DEFENDER;
    public static final EnchantRocket            ROCKET;

    public static final EnchantFlameWalker     FLAME_WALKER;
    public static final EnchantHardened        HARDENED;
    public static final EnchantColdSteel       COLD_STEEL;
    public static final EnchantSelfDestruction SELF_DESTRUCTION;
    public static final EnchantSaturation      SATURATION;
    public static final EnchantAquaman         AQUAMAN;
    public static final EnchantNightVision     NIGHT_VISION;
    public static final EnchantBunnyHop        BUNNY_HOP;
    public static final EnchantSonic           SONIC;

    public static final EnchantBomber          BOMBER;
    public static final EnchantEnderBow        ENDER_BOW;
    public static final EnchantGhast           GHAST;
    public static final EnchantPoisonedArrows  POISONED_ARROWS;
    public static final EnchantWitheredArrows  WITHERED_ARROWS;
    public static final EnchantExplosiveArrows EXPLOSIVE_ARROWS;

    static {
        PLUGIN = ExcellentEnchants.getInstance();
        PLUGIN.getConfigManager().extract("enchants");
        ENCHANT_LIST = new HashSet<>();

        // Tool enchants
        BLAST_MINING = init(EnchantBlastMining.class, "blast_mining");
        DIVINE_TOUCH = init(EnchantDivineTouch.class, "divine_touch");
        HASTE = init(EnchantHaste.class, "haste");
        LUCKY_MINER = init(EnchantLuckyMiner.class, "lucky_miner");
        REPLANTER = init(EnchantReplanter.class, "replanter");
        SILK_CHEST = init(EnchantSilkChest.class, "silk_chest");
        SMELTER = init(EnchantSmelter.class, "smelter");
        TELEKINESIS = init(EnchantTelekinesis.class, "telekinesis");
        TREASURES = init(EnchantTreasures.class, "treasures");
        TUNNEL = init(EnchantTunnel.class, "tunnel");
        VEINMINER = init(EnchantVeinminer.class, EnchantVeinminer.ID);

        // Weapon enchants
        BANE_OF_NETHERSPAWN = init(EnchantBaneOfNetherspawn.class, "bane_of_netherspawn");
        BLINDNESS = init(EnchantBlindness.class, "blindness");
        CONFUSION = init(EnchantConfusion.class, "confusion");
        CUTTER = init(EnchantCutter.class, "cutter");
        DOUBLE_STRIKE = init(EnchantDoubleStrike.class, "double_strike");
        EXECUTIONER = init(EnchantDecapitator.class, "decapitator");
        EXHAUST = init(EnchantExhaust.class, "exhaust");
        EXP_HUNTER = init(EnchantExpHunter.class, "exp_hunter");
        ICE_ASPECT = init(EnchantIceAspect.class, "ice_aspect");
        INFERNUS = init(EnchantInfernus.class, "infernus");
        PARALYZE = init(EnchantParalyze.class, "paralyze");
        PIGIFICATOR = init(EnchantPigificator.class, "pigificator");
        RAGE = init(EnchantRage.class, "rage");
        SCAVENGER = init(EnchantScavenger.class, "scavenger");
        SURPRISE = init(EnchantSurprise.class, "surprise");
        THRIFTY = init(EnchantThrifty.class, "thrifty");
        THUNDER = init(EnchantThunder.class, "thunder");
        ROCKET = init(EnchantRocket.class, "rocket");
        VAMPIRE = init(EnchantVampire.class, "vampire");
        VENOM = init(EnchantVenom.class, "venom");
        VILLAGE_DEFENDER = init(EnchantVillageDefender.class, "village_defender");
        WITHER = init(EnchantWither.class, "wither");

        // Armor enchants
        FLAME_WALKER = init(EnchantFlameWalker.class, "flame_walker");
        HARDENED = init(EnchantHardened.class, "hardened");
        COLD_STEEL = init(EnchantColdSteel.class, "cold_steel");
        SELF_DESTRUCTION = init(EnchantSelfDestruction.class, "self_destruction");
        SATURATION = init(EnchantSaturation.class, "saturation");
        AQUAMAN = init(EnchantAquaman.class, "aquaman");
        NIGHT_VISION = init(EnchantNightVision.class, "night_vision");
        BUNNY_HOP = init(EnchantBunnyHop.class, "bunny_hop");
        SONIC = init(EnchantSonic.class, "sonic");

        // Bow enchants
        BOMBER = init(EnchantBomber.class, "bomber");
        GHAST = init(EnchantGhast.class, "ghast");
        ENDER_BOW = init(EnchantEnderBow.class, "ender_bow");
        POISONED_ARROWS = init(EnchantPoisonedArrows.class, "poisoned_arrows");
        WITHERED_ARROWS = init(EnchantWitheredArrows.class, "withered_arrows");
        EXPLOSIVE_ARROWS = init(EnchantExplosiveArrows.class, "explosive_arrows");
    }

    @Nullable
    private static <T extends ExcellentEnchant> T init(@NotNull Class<T> clazz, @NotNull String id) {
        String enchantId = id.toLowerCase();
        if (Config.ENCHANTMENTS_DISABLED.contains(id)) return null;

        JYML enchantCfg = JYML.loadOrExtract(PLUGIN, "/enchants/" + enchantId + ".yml");
        try {
            return clazz.getConstructor(ExcellentEnchants.class, JYML.class).newInstance(PLUGIN, enchantCfg);
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void register(@Nullable ExcellentEnchant enchant) {
        if (enchant == null) return;

        Enchantment.registerEnchantment(enchant);
        ENCHANT_LIST.add(enchant);
        enchant.registerListeners();
        //IRegistry.a(IRegistry.ENCHANTMENT, enchant.getId(), CraftEnchantment.getRaw(enchant));
    }

    public static void setup() {
        ENCHANT_LIST.clear();
        Reflex.setFieldValue(Enchantment.class, "acceptingNew", true);

        for (Field field : EnchantRegister.class.getFields()) {
            if (!ExcellentEnchant.class.isAssignableFrom(field.getType())) continue;

            ExcellentEnchant enchant;
            try {
                enchant = (ExcellentEnchant) field.get(null);
                EnchantRegister.register(enchant);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Enchantment.stopAcceptingRegistrations();
        PLUGIN.info("Enchants Registered: " + ENCHANT_LIST.size());
    }

    @SuppressWarnings("unchecked")
    public static void shutdown() {
        Map<NamespacedKey, Enchantment> byKey = (Map<NamespacedKey, Enchantment>) Reflex.getFieldValue(Enchantment.class, "byKey");
        Map<String, Enchantment> byName = (Map<String, Enchantment>) Reflex.getFieldValue(Enchantment.class, "byName");

        if (byKey == null || byName == null) return;

        for (ExcellentEnchant enchant : ENCHANT_LIST) {
            if (enchant instanceof Cleanable) {
                ((Cleanable) enchant).clear();
            }

            byKey.remove(enchant.getKey());
            byName.remove(enchant.getName());
            enchant.unregisterListeners();
        }
        ENCHANT_LIST.clear();
        PLUGIN.info("All enchants are unregistered.");
    }
}
