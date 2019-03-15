package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.anticheat.Kauri;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

@Getter
@Setter
public class CustomLocation {
    private double x, y, z;
    private float yaw, pitch;
    private int ticks;

    public CustomLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        ticks = Kauri.getInstance().getCurrentTicks();
    }

    public CustomLocation(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;

        ticks = Kauri.getInstance().getCurrentTicks();
    }

    public CustomLocation(double x, double y, double z, float yaw, float pitch, long timeStamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        ticks = Kauri.getInstance().getCurrentTicks();
    }

    public CustomLocation(Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();

        ticks = Kauri.getInstance().getCurrentTicks();
    }

    public CustomLocation clone() {
        return new CustomLocation(x, y, z, yaw, pitch, ticks);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Vector toVector() {
        return new Vector(x, y, z);
    }
}
