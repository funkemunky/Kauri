package dev.brighten.anticheat.utils;

import cc.funkemunky.api.tinyprotocol.packet.types.enums.WrappedEnumDirection;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.world.types.RayCollision;
import cc.funkemunky.api.utils.world.types.SimpleCollisionBox;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class AxisAlignedBB {

    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AxisAlignedBB(double d0, double d1, double d2, double d3, double d4, double d5) {
        this.minX = Math.min(d0, d3);
        this.minY = Math.min(d1, d4);
        this.minZ = Math.min(d2, d5);
        this.maxX = Math.max(d0, d3);
        this.maxY = Math.max(d1, d4);
        this.maxZ = Math.max(d2, d5);
    }

    public AxisAlignedBB(BoundingBox box) {
        this.minX = box.minX;
        this.minY = box.minY;
        this.minZ = box.minZ;
        this.maxX = box.maxX;
        this.maxY = box.maxY;
        this.maxZ = box.maxZ;
    }

    public AxisAlignedBB(SimpleCollisionBox box) {
        this.minX = box.xMin;
        this.minY = box.yMin;
        this.minZ = box.zMin;
        this.maxX = box.xMax;
        this.maxY = box.yMax;
        this.maxZ = box.zMax;
    }

    public AxisAlignedBB(Location blockposition, Location blockposition1) {
        this.minX = (double) blockposition.getBlockX();
        this.minY = (double) blockposition.getBlockY();
        this.minZ = (double) blockposition.getBlockZ();
        this.maxX = (double) blockposition1.getBlockX();
        this.maxY = (double) blockposition1.getBlockY();
        this.maxZ = (double) blockposition1.getBlockZ();
    }

    public AxisAlignedBB a(double d0, double d1, double d2) {
        double d3 = this.minX;
        double d4 = this.minY;
        double d5 = this.minZ;
        double d6 = this.maxX;
        double d7 = this.maxY;
        double d8 = this.maxZ;

        if (d0 < 0.0D) {
            d3 += d0;
        } else if (d0 > 0.0D) {
            d6 += d0;
        }

        if (d1 < 0.0D) {
            d4 += d1;
        } else if (d1 > 0.0D) {
            d7 += d1;
        }

        if (d2 < 0.0D) {
            d5 += d2;
        } else if (d2 > 0.0D) {
            d8 += d2;
        }

        return new AxisAlignedBB(d3, d4, d5, d6, d7, d8);
    }

    public AxisAlignedBB grow(double d0, double d1, double d2) {
        double d3 = this.minX - d0;
        double d4 = this.minY - d1;
        double d5 = this.minZ - d2;
        double d6 = this.maxX + d0;
        double d7 = this.maxY + d1;
        double d8 = this.maxZ + d2;

        return new AxisAlignedBB(d3, d4, d5, d6, d7, d8);
    }

    public AxisAlignedBB a(AxisAlignedBB axisalignedbb) {
        double d0 = Math.min(this.minX, axisalignedbb.minX);
        double d1 = Math.min(this.minY, axisalignedbb.minY);
        double d2 = Math.min(this.minZ, axisalignedbb.minZ);
        double d3 = Math.max(this.maxX, axisalignedbb.maxX);
        double d4 = Math.max(this.maxY, axisalignedbb.maxY);
        double d5 = Math.max(this.maxZ, axisalignedbb.maxZ);

        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public AxisAlignedBB c(double d0, double d1, double d2) {
        return new AxisAlignedBB(this.minX + d0, this.minY + d1, this.minZ + d2, this.maxX + d0, this.maxY + d1, this.maxZ + d2);
    }

    public double a(AxisAlignedBB axisalignedbb, double d0) {
        if (axisalignedbb.maxY > this.minY && axisalignedbb.minY < this.maxY && axisalignedbb.maxZ > this.minZ && axisalignedbb.minZ < this.maxZ) {
            double d1;

            if (d0 > 0.0D && axisalignedbb.maxX <= this.minX) {
                d1 = this.minX - axisalignedbb.maxX;
                if (d1 < d0) {
                    d0 = d1;
                }
            } else if (d0 < 0.0D && axisalignedbb.minX >= this.maxX) {
                d1 = this.maxX - axisalignedbb.minX;
                if (d1 > d0) {
                    d0 = d1;
                }
            }

            return d0;
        } else {
            return d0;
        }
    }

    public double b(AxisAlignedBB axisalignedbb, double d0) {
        if (axisalignedbb.maxX > this.minX && axisalignedbb.minX < this.maxX && axisalignedbb.maxZ > this.minZ && axisalignedbb.minZ < this.maxZ) {
            double d1;

            if (d0 > 0.0D && axisalignedbb.maxY <= this.minY) {
                d1 = this.minY - axisalignedbb.maxY;
                if (d1 < d0) {
                    d0 = d1;
                }
            } else if (d0 < 0.0D && axisalignedbb.minY >= this.maxY) {
                d1 = this.maxY - axisalignedbb.minY;
                if (d1 > d0) {
                    d0 = d1;
                }
            }

            return d0;
        } else {
            return d0;
        }
    }

    public double c(AxisAlignedBB axisalignedbb, double d0) {
        if (axisalignedbb.maxX > this.minX && axisalignedbb.minX < this.maxX && axisalignedbb.maxY > this.minY && axisalignedbb.minY < this.maxY) {
            double d1;

            if (d0 > 0.0D && axisalignedbb.maxZ <= this.minZ) {
                d1 = this.minZ - axisalignedbb.maxZ;
                if (d1 < d0) {
                    d0 = d1;
                }
            } else if (d0 < 0.0D && axisalignedbb.minZ >= this.maxZ) {
                d1 = this.maxZ - axisalignedbb.minZ;
                if (d1 > d0) {
                    d0 = d1;
                }
            }

            return d0;
        } else {
            return d0;
        }
    }

    public boolean b(AxisAlignedBB axisalignedbb) {
        return axisalignedbb.maxX > this.minX && axisalignedbb.minX < this.maxX ? (axisalignedbb.maxY > this.minY && axisalignedbb.minY < this.maxY ? axisalignedbb.maxZ > this.minZ && axisalignedbb.minZ < this.maxZ : false) : false;
    }

    public boolean a(Vec3D vec3d) {
        return vec3d.a > this.minX && vec3d.a < this.maxX ? (vec3d.b > this.minY && vec3d.b < this.maxY ? vec3d.c > this.minZ && vec3d.c < this.maxZ : false) : false;
    }

    public double a() {
        double d0 = this.maxX - this.minX;
        double d1 = this.maxY - this.minY;
        double d2 = this.maxZ - this.minZ;

        return (d0 + d1 + d2) / 3.0D;
    }

    public AxisAlignedBB shrink(double d0, double d1, double d2) {
        double d3 = this.minX + d0;
        double d4 = this.minY + d1;
        double d5 = this.minZ + d2;
        double d6 = this.maxX - d0;
        double d7 = this.maxY - d1;
        double d8 = this.maxZ - d2;

        return new AxisAlignedBB(d3, d4, d5, d6, d7, d8);
    }

    public Vec3D rayTrace(RayCollision collision, double distance) {
        Vec3D origin = new Vec3D(collision.originX, collision.originY, collision.originZ);
        Vec3D dir = new Vec3D(collision.directionX * distance, collision.directionY * distance, collision.directionZ * distance);
        return rayTrace(origin, dir);
    }

    public Vec3D rayTrace(Vector vorigin, Vector vdirection, double distance) {
        Vec3D origin = new Vec3D(vorigin.getX(), vorigin.getY(), vorigin.getZ());
        Vector direction = vdirection.clone().multiply(distance);
        Vec3D dir = new Vec3D(direction.getX(), direction.getY(), direction.getZ());

        return rayTrace(origin, dir);
    }

    public Vec3D rayTrace(Vec3D vec3d, Vec3D vec3d1) {
        Vec3D vec3d2 = vec3d.a(vec3d1, this.minX);
        Vec3D vec3d3 = vec3d.a(vec3d1, this.maxX);
        Vec3D vec3d4 = vec3d.b(vec3d1, this.minY);
        Vec3D vec3d5 = vec3d.b(vec3d1, this.maxY);
        Vec3D vec3d6 = vec3d.c(vec3d1, this.minZ);
        Vec3D vec3d7 = vec3d.c(vec3d1, this.maxZ);

        if (!this.b(vec3d2)) {
            vec3d2 = null;
        }

        if (!this.b(vec3d3)) {
            vec3d3 = null;
        }

        if (!this.c(vec3d4)) {
            vec3d4 = null;
        }

        if (!this.c(vec3d5)) {
            vec3d5 = null;
        }

        if (!this.d(vec3d6)) {
            vec3d6 = null;
        }

        if (!this.d(vec3d7)) {
            vec3d7 = null;
        }

        Vec3D vec3d8 = null;

        if (vec3d2 != null) {
            vec3d8 = vec3d2;
        }

        if (vec3d3 != null && (vec3d8 == null || vec3d.distanceSquared(vec3d3) < vec3d.distanceSquared(vec3d8))) {
            vec3d8 = vec3d3;
        }

        if (vec3d4 != null && (vec3d8 == null || vec3d.distanceSquared(vec3d4) < vec3d.distanceSquared(vec3d8))) {
            vec3d8 = vec3d4;
        }

        if (vec3d5 != null && (vec3d8 == null || vec3d.distanceSquared(vec3d5) < vec3d.distanceSquared(vec3d8))) {
            vec3d8 = vec3d5;
        }

        if (vec3d6 != null && (vec3d8 == null || vec3d.distanceSquared(vec3d6) < vec3d.distanceSquared(vec3d8))) {
            vec3d8 = vec3d6;
        }

        if (vec3d7 != null && (vec3d8 == null || vec3d.distanceSquared(vec3d7) < vec3d.distanceSquared(vec3d8))) {
            vec3d8 = vec3d7;
        }

        if (vec3d8 == null) {
            return null;
        } else {
            WrappedEnumDirection enumdirection = null;

            if (vec3d8 == vec3d2) {
                enumdirection = WrappedEnumDirection.WEST;
            } else if (vec3d8 == vec3d3) {
                enumdirection = WrappedEnumDirection.EAST;
            } else if (vec3d8 == vec3d4) {
                enumdirection = WrappedEnumDirection.DOWN;
            } else if (vec3d8 == vec3d5) {
                enumdirection = WrappedEnumDirection.UP;
            } else if (vec3d8 == vec3d6) {
                enumdirection = WrappedEnumDirection.NORTH;
            } else {
                enumdirection = WrappedEnumDirection.SOUTH;
            }

            return vec3d8;
        }
    }

    private boolean b(Vec3D vec3d) {
        return vec3d == null ? false : vec3d.b >= this.minY && vec3d.b <= this.maxY && vec3d.c >= this.minZ && vec3d.c <= this.maxZ;
    }

    private boolean c(Vec3D vec3d) {
        return vec3d == null ? false : vec3d.a >= this.minX && vec3d.a <= this.maxX && vec3d.c >= this.minZ && vec3d.c <= this.maxZ;
    }

    private boolean d(Vec3D vec3d) {
        return vec3d == null ? false : vec3d.a >= this.minX && vec3d.a <= this.maxX && vec3d.b >= this.minY && vec3d.b <= this.maxY;
    }

    public String toString() {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }
}