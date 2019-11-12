package dev.brighten.anticheat.utils;

import cc.funkemunky.api.tinyprotocol.packet.out.WrappedPacketPlayOutWorldParticle;
import cc.funkemunky.api.tinyprotocol.packet.types.WrappedEnumParticle;
import cc.funkemunky.api.utils.BoundingBox;
import cc.funkemunky.api.utils.Tuple;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RayCollision {
    public double originX;
    public double originY;
    public double originZ;
    public double directionX;
    public double directionY;
    public double directionZ;

    public RayCollision(double originX, double originY, double originZ, double directionX, double directionY, double directionZ) {
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.directionX = directionX;
        this.directionY = directionY;
        this.directionZ = directionZ;
    }

    public RayCollision(RayCollision ray) {
        this.originX = ray.originX;
        this.originY = ray.originY;
        this.originZ = ray.originZ;
        this.directionX = ray.directionX;
        this.directionY = ray.directionY;
        this.directionZ = ray.directionZ;
    }

    public RayCollision() {
        originX = 0;
        originY = 0;
        originZ = 0;
        directionX = 0;
        directionY = 0;
        directionZ = 0;
    }

    public RayCollision(LivingEntity e) {
        this(e.getEyeLocation());
    }

    public RayCollision(Location l) {
        this(l.toVector(),l.getDirection());
    }

    public RayCollision(Vector position, Vector direction) {
        this.originX = position.getX();
        this.originY = position.getY();
        this.originZ = position.getZ();
        this.directionX = direction.getX();
        this.directionY = direction.getY();
        this.directionZ = direction.getZ();
    }

    public void draw(WrappedEnumParticle particle, Player... players) {
        MiscUtils.drawRay(this,particle, Arrays.asList(players));
    }

    public KLocation getOrigin() {
        return new KLocation(originX, originY, originZ, 0, 0);
    }

    public boolean isCollided(BoundingBox other) {
        return intersect(this, other);
    }

    public void downCast(List<BoundingBox> list) {/*Do Nothing, Ray cannot be down-casted*/}

    public boolean isNull() {
        return true;
    }

    public static double distance(RayCollision ray, BoundingBox box ) {
        Tuple<Double,Double> Tuple = new Tuple<>(0D,0D);
        if (intersect(ray,box,Tuple))
            return Tuple.one;
        return -1;
    }

    public static boolean intersect(RayCollision ray, BoundingBox aab) {
        double invDirX = 1.0D / ray.directionX;
        double invDirY = 1.0D / ray.directionY;
        double invDirZ = 1.0D / ray.directionZ;
        double tFar;
        double tNear;
        if (invDirX >= 0.0D) {
            tNear = (aab.minX - ray.originX) * invDirX;
            tFar = (aab.maxX - ray.originX) * invDirX;
        } else {
            tNear = (aab.maxX - ray.originX) * invDirX;
            tFar = (aab.minX - ray.originX) * invDirX;
        }

        double tymin;
        double tymax;
        if (invDirY >= 0.0D) {
            tymin = (aab.minY - ray.originY) * invDirY;
            tymax = (aab.maxY - ray.originY) * invDirY;
        } else {
            tymin = (aab.maxY - ray.originY) * invDirY;
            tymax = (aab.minY - ray.originY) * invDirY;
        }

        if (tNear <= tymax && tymin <= tFar) {
            double tzmin;
            double tzmax;
            if (invDirZ >= 0.0D) {
                tzmin = (aab.minZ - ray.originZ) * invDirZ;
                tzmax = (aab.maxZ - ray.originZ) * invDirZ;
            } else {
                tzmin = (aab.maxZ - ray.originZ) * invDirZ;
                tzmax = (aab.minZ - ray.originZ) * invDirZ;
            }

            if (tNear <= tzmax && tzmin <= tFar) {
                tNear = tymin <= tNear && !Double.isNaN(tNear) ? tNear : tymin;
                tFar = tymax >= tFar && !Double.isNaN(tFar) ? tFar : tymax;
                tNear = tzmin > tNear ? tzmin : tNear;
                tFar = tzmax < tFar ? tzmax : tFar;
                if (tNear < tFar && tFar >= 0.0D) {
//                    result.x = tNear;
//                    result.y = tFar;
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // Result X = near
    // Result Y = far
    public static boolean intersect(RayCollision ray, BoundingBox aab, Tuple<Double,Double> result) {
        double invDirX = 1.0D / ray.directionX;
        double invDirY = 1.0D / ray.directionY;
        double invDirZ = 1.0D / ray.directionZ;
        double tFar;
        double tNear;
        if (invDirX >= 0.0D) {
            tNear = (aab.minX - ray.originX) * invDirX;
            tFar = (aab.maxX - ray.originX) * invDirX;
        } else {
            tNear = (aab.maxX - ray.originX) * invDirX;
            tFar = (aab.minX - ray.originX) * invDirX;
        }

        double tymin;
        double tymax;
        if (invDirY >= 0.0D) {
            tymin = (aab.minY - ray.originY) * invDirY;
            tymax = (aab.maxY - ray.originY) * invDirY;
        } else {
            tymin = (aab.maxY - ray.originY) * invDirY;
            tymax = (aab.minY - ray.originY) * invDirY;
        }

        if (tNear <= tymax && tymin <= tFar) {
            double tzmin;
            double tzmax;
            if (invDirZ >= 0.0D) {
                tzmin = (aab.minZ - ray.originZ) * invDirZ;
                tzmax = (aab.maxZ - ray.originZ) * invDirZ;
            } else {
                tzmin = (aab.maxZ - ray.originZ) * invDirZ;
                tzmax = (aab.minZ - ray.originZ) * invDirZ;
            }

            if (tNear <= tzmax && tzmin <= tFar) {
                tNear = tymin <= tNear && !Double.isNaN(tNear) ? tNear : tymin;
                tFar = tymax >= tFar && !Double.isNaN(tFar) ? tFar : tymax;
                tNear = tzmin > tNear ? tzmin : tNear;
                tFar = tzmax < tFar ? tzmax : tFar;
                if (tNear < tFar && tFar >= 0.0D) {
                    if (result != null) {
                        result.one = tNear;
                        result.two = tFar;
                    }
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public Vector collisionPoint(BoundingBox box) {
        Tuple<Double, Double> p = new Tuple<>(0D,0D);
        if (box==null||!intersect(this,box,p))
            return null;
        Vector vector = new Vector(directionX,directionY,directionZ);
        vector.normalize();
        vector.multiply(p.one);
        vector.add(new Vector(originX,originY,originZ));
        return vector;
    }

    public Vector collisionPoint(double dist) {
        Vector vector = new Vector(directionX,directionY,directionZ);
        vector.normalize();
        vector.multiply(dist);
        vector.add(new Vector(originX,originY,originZ));
        return vector;
    }
}
