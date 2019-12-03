package dev.brighten.anticheat.utils;

public class MouseFilter {
    public float x;
    public float y;
    public float z;

    /**
     * Smooths mouse input
     */
    public float smooth(float p_76333_1_, float p_76333_2_) {
        this.x += p_76333_1_;
        p_76333_1_ = (this.x - this.y) * p_76333_2_;
        this.z += (p_76333_1_ - this.z) * 0.5F;

        if (p_76333_1_ > 0.0F && p_76333_1_ > this.z || p_76333_1_ < 0.0F && p_76333_1_ < this.z) {
            p_76333_1_ = this.z;
        }

        this.y += p_76333_1_;
        return p_76333_1_;
    }

    public void reset() {
        this.x = 0.0F;
        this.y = 0.0F;
        this.z = 0.0F;
    }
}