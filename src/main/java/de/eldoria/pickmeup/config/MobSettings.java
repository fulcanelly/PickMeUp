package de.eldoria.pickmeup.config;

import de.eldoria.eldoutilities.serialization.SerializationUtil;
import de.eldoria.eldoutilities.serialization.TypeResolvingMap;
import de.eldoria.pickmeup.util.Permissions;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SerializableAs("pickMeUpMobSettings")
public class MobSettings implements ConfigurationSerializable {
    private static final transient List<EntityType> DEFAULT_MOBS;
    private static final transient List<EntityType> DEFAULT_BLACKLISTED_MOBS 
        = List.of(
            EntityType.ENDER_DRAGON,
            EntityType.WITHER
        );

    static {
        DEFAULT_MOBS = new ArrayList<EntityType>() {{
            add(EntityType.PIG);
            add(EntityType.CHICKEN);
            add(EntityType.RABBIT);
            add(EntityType.WOLF);
            add(EntityType.BOAT);
            add(EntityType.SHEEP);
            add(EntityType.PARROT);
        }};
    }

    private boolean allowAllPeacefulMobs = true;
    private boolean allowAllHostileMobs;
    private boolean allowPlayers = true;

    private List<EntityType> allowedMobs = DEFAULT_MOBS;
    private List<EntityType> blackListedMobs = new ArrayList<>();

    private boolean requirePermission;

    public MobSettings(Map<String, Object> objectMap) {
        TypeResolvingMap map = SerializationUtil.mapOf(objectMap);
        allowedMobs = map.getValueOrDefault("allowedMobs", DEFAULT_MOBS, EntityType.class);
        blackListedMobs = map.getValueOrDefault("blackListedMobs", DEFAULT_BLACKLISTED_MOBS, EntityType.class);

        requirePermission = map.getValueOrDefault("requirePermission", requirePermission);
        
        allowAllPeacefulMobs = map.getValueOrDefault("allowAllPeacefulMobs", allowAllPeacefulMobs);
        allowAllHostileMobs = map.getValueOrDefault("allowAllHostileMobs", allowAllHostileMobs);
        allowPlayers = map.getValueOrDefault("allowPlayers", allowPlayers);
    }

    public MobSettings() {

    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return SerializationUtil.newBuilder()
                .addEnum("allowedMobs", allowedMobs)
                .addEnum("blackListedMobs", blackListedMobs)
                
                .add("requirePermission", requirePermission)
                
                .add("allowAllPeacefulMobs", allowAllPeacefulMobs)      
                .add("allowAllHostileMobs", allowAllHostileMobs)
                .add("allowPlayers", allowPlayers)
                .build();
    }


    public boolean canBePickedUp(Player player, Entity entity) {
        var type = entity.getType();

        if (requirePermission && !player.hasPermission(Permissions.getPickUpPermission(type))) {
            return false;
        }

        if (blackListedMobs.contains(type)) {
            return false;
        }

        if (allowedMobs.contains(type)) {
            return true;
        }
           
        return isAllowedByCategory(entity);
    }

    private boolean isAllowedByCategory(Entity targetEntity) {

        boolean isPlayer = targetEntity instanceof Player;

        if (isPlayer && allowPlayers) {
            return true;
        }

        boolean isMob = targetEntity instanceof Mob;
        boolean isMonster = targetEntity instanceof Monster;

        if (isMob && !isMonster && allowAllPeacefulMobs) {
            return true;
        }

        if (isMonster && allowAllHostileMobs) {
            return true;
        }

        return false; 
    }


}
