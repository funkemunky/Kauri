//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package dev.brighten.anticheat.utils;

import cc.funkemunky.api.tinyprotocol.packet.types.MathHelper;
import org.bukkit.Location;

import javax.annotation.Nullable;

public class Vec3D {
    public static final Vec3D a = new Vec3D(0.0D, 0.0D, 0.0D);
    public final double x;
    public final double y;
    public final double z;

    public Vec3D(double var1, double var3, double var5) {
        if (var1 == -0.0D) {
            var1 = 0.0D;
        }

        if (var3 == -0.0D) {
            var3 = 0.0D;
        }

        if (var5 == -0.0D) {
            var5 = 0.0D;
        }

        this.x = var1;
        this.y = var3;
        this.z = var5;
    }

    public Vec3D(Location var1) {
        this((double)var1.getX(), (double)var1.getY(), (double)var1.getZ());
    }

    public Vec3D a(Vec3D var1) {
        return new Vec3D(var1.x - this.x, var1.y - this.y, var1.z - this.z);
    }

    public Vec3D a() {
        double var1 = (double) MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        return var1 < 1.0E-4D ? a : new Vec3D(this.x / var1, this.y / var1, this.z / var1);
    }

    public Vec3D clone() {
        return new Vec3D(x, y, z);
    }

    public double b(Vec3D var1) {
        return this.x * var1.x + this.y * var1.y + this.z * var1.z;
    }

    public Vec3D d(Vec3D var1) {
        return this.a(var1.x, var1.y, var1.z);
    }

    public Vec3D a(double var1, double var3, double var5) {
        return this.add(-var1, -var3, -var5);
    }

    public Vec3D e(Vec3D var1) {
        return this.add(var1.x, var1.y, var1.z);
    }

    public Vec3D add(double var1, double var3, double var5) {
        return new Vec3D(this.x + var1, this.y + var3, this.z + var5);
    }

    public double f(Vec3D var1) {
        double var2 = var1.x - this.x;
        double var4 = var1.y - this.y;
        double var6 = var1.z - this.z;
        return (double)MathHelper.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
    }

    public double distanceSquared(Vec3D var1) {
        double var2 = var1.x - this.x;
        double var4 = var1.y - this.y;
        double var6 = var1.z - this.z;
        return var2 * var2 + var4 * var4 + var6 * var6;
    }

    public double c(double var1, double var3, double var5) {
        double var7 = var1 - this.x;
        double var9 = var3 - this.y;
        double var11 = var5 - this.z;
        return var7 * var7 + var9 * var9 + var11 * var11;
    }

    public Vec3D a(double var1) {
        return new Vec3D(this.x * var1, this.y * var1, this.z * var1);
    }

    public double b() {
        return (double)MathHelper.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    @Nullable
    public Vec3D a(Vec3D var1, double var2) {
        double var4 = var1.x - this.x;
        double var6 = var1.y - this.y;
        double var8 = var1.z - this.z;
        if (var4 * var4 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double var10 = (var2 - this.x) / var4;
            return var10 >= 0.0D && var10 <= 1.0D ? new Vec3D(this.x + var4 * var10, this.y + var6 * var10, this.z + var8 * var10) : null;
        }
    }

    @Nullable
    public Vec3D b(Vec3D var1, double var2) {
        double var4 = var1.x - this.x;
        double var6 = var1.y - this.y;
        double var8 = var1.z - this.z;
        if (var6 * var6 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double var10 = (var2 - this.y) / var6;
            return var10 >= 0.0D && var10 <= 1.0D ? new Vec3D(this.x + var4 * var10, this.y + var6 * var10, this.z + var8 * var10) : null;
        }
    }

    @Nullable
    public Vec3D c(Vec3D var1, double var2) {
        double var4 = var1.x - this.x;
        double var6 = var1.y - this.y;
        double var8 = var1.z - this.z;
        if (var8 * var8 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double var10 = (var2 - this.z) / var8;
            return var10 >= 0.0D && var10 <= 1.0D ? new Vec3D(this.x + var4 * var10, this.y + var6 * var10, this.z + var8 * var10) : null;
        }
    }

    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (!(var1 instanceof Vec3D)) {
            return false;
        } else {
            Vec3D var2 = (Vec3D)var1;
            if (Double.compare(var2.x, this.x) != 0) {
                return false;
            } else if (Double.compare(var2.y, this.y) != 0) {
                return false;
            } else {
                return Double.compare(var2.z, this.z) == 0;
            }
        }
    }

    public int hashCode() {
        long var2 = Double.doubleToLongBits(this.x);
        int var1 = (int)(var2 ^ var2 >>> 32);
        var2 = Double.doubleToLongBits(this.y);
        var1 = 31 * var1 + (int)(var2 ^ var2 >>> 32);
        var2 = Double.doubleToLongBits(this.z);
        var1 = 31 * var1 + (int)(var2 ^ var2 >>> 32);
        return var1;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }

    public Vec3D a(float var1) {
        float var2 = MathHelper.cos(var1);
        float var3 = MathHelper.sin(var1);
        double var4 = this.x;
        double var6 = this.y * (double)var2 + this.z * (double)var3;
        double var8 = this.z * (double)var2 - this.y * (double)var3;
        return new Vec3D(var4, var6, var8);
    }

    public Vec3D b(float var1) {
        float var2 = MathHelper.cos(var1);
        float var3 = MathHelper.sin(var1);
        double var4 = this.x * (double)var2 + this.z * (double)var3;
        double var6 = this.y;
        double var8 = this.z * (double)var2 - this.x * (double)var3;
        return new Vec3D(var4, var6, var8);
    }
}
