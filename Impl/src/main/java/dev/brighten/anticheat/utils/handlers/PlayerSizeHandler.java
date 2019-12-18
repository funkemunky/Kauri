package dev.brighten.anticheat.utils.handlers;

import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public interface PlayerSizeHandler {

    PlayerSizeHandler instance = getInstance();

    double height(Player player);
    double width(Player player);

    boolean isGliding(Player player);

    default SimpleCollisionBox bounds(Player player) {
        Location l = player.getLocation();
        return bounds(player,l.getX(),l.getY(),l.getZ());
    }

    default SimpleCollisionBox bounds(Player player,double x, double y, double z) {
        double width = width(player);
        return new SimpleCollisionBox().offset(x,y,z).expand(width,0,width).expandMax(0,height(player),0);
    }

    static PlayerSizeHandler getInstance() {
        if (instance!=null)
            return instance;
        try {
            Method method = Entity.class.getMethod("getWidth");
            return new PlayerSizeHandlerModern();
        } catch (NoSuchMethodException e) {
            return new PlayerSizeHandlerLegacy();
        }
    }

}