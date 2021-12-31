package dev.brighten.anticheat.utils;

import cc.funkemunky.api.utils.MathUtils;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class AimbotUtil {
    public static final Map<Integer, Float> SENSITIVITY_MAP = Maps.newHashMap();

    static {
        SENSITIVITY_MAP.put(1, 0.0070422534F);
        SENSITIVITY_MAP.put(2, 0.014084507F);
        SENSITIVITY_MAP.put(4, 0.02112676F);
        SENSITIVITY_MAP.put(5, 0.028169014F);
        SENSITIVITY_MAP.put(6, 0.0281690166F);
        SENSITIVITY_MAP.put(7, 0.03521127F);
        SENSITIVITY_MAP.put(8, 0.04225352F);
        SENSITIVITY_MAP.put(9, 0.049295776F);
        SENSITIVITY_MAP.put(10, 0.0492957736F);
        SENSITIVITY_MAP.put(11, 0.056338027F);
        SENSITIVITY_MAP.put(12, 0.06338028F);
        SENSITIVITY_MAP.put(14, 0.07042254F);
        SENSITIVITY_MAP.put(15, 0.07746479F);
        SENSITIVITY_MAP.put(16, 0.08450704F);
        SENSITIVITY_MAP.put(18, 0.09154929F);
        SENSITIVITY_MAP.put(19, 0.09859155F);
        SENSITIVITY_MAP.put(21, 0.1056338F);
        SENSITIVITY_MAP.put(22, 0.112676054F);
        SENSITIVITY_MAP.put(23, 0.11971831F);
        SENSITIVITY_MAP.put(25, 0.12676056F);
        SENSITIVITY_MAP.put(26, 0.13380282F);
        SENSITIVITY_MAP.put(28, 0.14084508F);
        SENSITIVITY_MAP.put(29, 0.14788732F);
        SENSITIVITY_MAP.put(30, 0.15492958F);
        SENSITIVITY_MAP.put(32, 0.16197184F);
        SENSITIVITY_MAP.put(33, 0.16901408F);
        SENSITIVITY_MAP.put(35, 0.17605634F);
        SENSITIVITY_MAP.put(36, 0.18309858F);
        SENSITIVITY_MAP.put(38, 0.19014084F);
        SENSITIVITY_MAP.put(39, 0.1971831F);
        SENSITIVITY_MAP.put(40, 0.20422535F);
        SENSITIVITY_MAP.put(42, 0.2112676F);
        SENSITIVITY_MAP.put(43, 0.21830986F);
        SENSITIVITY_MAP.put(45, 0.22535211F);
        SENSITIVITY_MAP.put(46, 0.23239437F);
        SENSITIVITY_MAP.put(47, 0.23943663F);
        SENSITIVITY_MAP.put(49, 0.24647887F);
        SENSITIVITY_MAP.put(50, 0.2535211F);
        SENSITIVITY_MAP.put(52, 0.26056337F);
        SENSITIVITY_MAP.put(53, 0.26760563F);
        SENSITIVITY_MAP.put(54, 0.2746479F);
        SENSITIVITY_MAP.put(56, 0.28169015F);
        SENSITIVITY_MAP.put(57, 0.28873238F);
        SENSITIVITY_MAP.put(59, 0.29577464F);
        SENSITIVITY_MAP.put(60, 0.3028169F);
        SENSITIVITY_MAP.put(61, 0.30985916F);
        SENSITIVITY_MAP.put(63, 0.31690142F);
        SENSITIVITY_MAP.put(64, 0.32394367F);
        SENSITIVITY_MAP.put(66, 0.3309859F);
        SENSITIVITY_MAP.put(67, 0.33802816F);
        SENSITIVITY_MAP.put(68, 0.34507042F);
        SENSITIVITY_MAP.put(70, 0.35211268F);
        SENSITIVITY_MAP.put(71, 0.35915494F);
        SENSITIVITY_MAP.put(73, 0.36619717F);
        SENSITIVITY_MAP.put(74, 0.37323943F);
        SENSITIVITY_MAP.put(76, 0.3802817F);
        SENSITIVITY_MAP.put(77, 0.38732395F);
        SENSITIVITY_MAP.put(78, 0.3943662F);
        SENSITIVITY_MAP.put(80, 0.40140846F);
        SENSITIVITY_MAP.put(81, 0.4084507F);
        SENSITIVITY_MAP.put(83, 0.41549295F);
        SENSITIVITY_MAP.put(84, 0.4225352F);
        SENSITIVITY_MAP.put(85, 0.42957747F);
        SENSITIVITY_MAP.put(87, 0.43661973F);
        SENSITIVITY_MAP.put(88, 0.44366196F);
        SENSITIVITY_MAP.put(90, 0.45070422F);
        SENSITIVITY_MAP.put(91, 0.45774648F);
        SENSITIVITY_MAP.put(92, 0.46478873F);
        SENSITIVITY_MAP.put(94, 0.471831F);
        SENSITIVITY_MAP.put(95, 0.47887325F);
        SENSITIVITY_MAP.put(97, 0.48591548F);
        SENSITIVITY_MAP.put(98, 0.49295774F);
        SENSITIVITY_MAP.put(100, 0.5F);
        SENSITIVITY_MAP.put(101, 0.5070422F);
        SENSITIVITY_MAP.put(102, 0.5140845F);
        SENSITIVITY_MAP.put(104, 0.52112675F);
        SENSITIVITY_MAP.put(105, 0.52816904F);
        SENSITIVITY_MAP.put(107, 0.53521127F);
        SENSITIVITY_MAP.put(108, 0.5422535F);
        SENSITIVITY_MAP.put(109, 0.5492958F);
        SENSITIVITY_MAP.put(111, 0.556338F);
        SENSITIVITY_MAP.put(112, 0.5633803F);
        SENSITIVITY_MAP.put(114, 0.57042253F);
        SENSITIVITY_MAP.put(115, 0.57746476F);
        SENSITIVITY_MAP.put(116, 0.58450705F);
        SENSITIVITY_MAP.put(118, 0.5915493F);
        SENSITIVITY_MAP.put(119, 0.59859157F);
        SENSITIVITY_MAP.put(121, 0.6056338F);
        SENSITIVITY_MAP.put(122, 0.6126761F);
        SENSITIVITY_MAP.put(123, 0.6197183F);
        SENSITIVITY_MAP.put(125, 0.62676054F);
        SENSITIVITY_MAP.put(126, 0.63380283F);
        SENSITIVITY_MAP.put(128, 0.64084506F);
        SENSITIVITY_MAP.put(129, 0.647887350F);
        SENSITIVITY_MAP.put(130, 0.6549296F);
        SENSITIVITY_MAP.put(132, 0.6619718F);
        SENSITIVITY_MAP.put(133, 0.6690141F);
        SENSITIVITY_MAP.put(135, 0.6760563F);
        SENSITIVITY_MAP.put(136, 0.6830986F);
        SENSITIVITY_MAP.put(138, 0.69014084F);
        SENSITIVITY_MAP.put(139, 0.6971831F);
        SENSITIVITY_MAP.put(140, 0.70422536F);
        SENSITIVITY_MAP.put(142, 0.7112676F);
        SENSITIVITY_MAP.put(143, 0.7183099F);
        SENSITIVITY_MAP.put(145, 0.7253521F);
        SENSITIVITY_MAP.put(146, 0.73239434F);
        SENSITIVITY_MAP.put(147, 0.7394366F);
        SENSITIVITY_MAP.put(149, 0.74647886F);
        SENSITIVITY_MAP.put(150, 0.75352114F);
        SENSITIVITY_MAP.put(152, 0.7605634F);
        SENSITIVITY_MAP.put(153, 0.76760566F);
        SENSITIVITY_MAP.put(154, 0.7746479F);
        SENSITIVITY_MAP.put(156, 0.7816901F);
        SENSITIVITY_MAP.put(157, 0.7887324F);
        SENSITIVITY_MAP.put(159, 0.79577464F);
        SENSITIVITY_MAP.put(160, 0.8028169F);
        SENSITIVITY_MAP.put(161, 0.80985916F);
        SENSITIVITY_MAP.put(163, 0.8169014F);
        SENSITIVITY_MAP.put(164, 0.8239437F);
        SENSITIVITY_MAP.put(166, 0.8309859F);
        SENSITIVITY_MAP.put(167, 0.8380282F);
        SENSITIVITY_MAP.put(169, 0.8450704F);
        SENSITIVITY_MAP.put(170, 0.85211265F);
        SENSITIVITY_MAP.put(171, 0.85915494F);
        SENSITIVITY_MAP.put(173, 0.86619717F);
        SENSITIVITY_MAP.put(174, 0.87323946F);
        SENSITIVITY_MAP.put(176, 0.8802817F);
        SENSITIVITY_MAP.put(177, 0.8873239F);
        SENSITIVITY_MAP.put(178, 0.8943662F);
        SENSITIVITY_MAP.put(180, 0.90140843F);
        SENSITIVITY_MAP.put(181, 0.9084507F);
        SENSITIVITY_MAP.put(183, 0.91549295F);
        SENSITIVITY_MAP.put(184, 0.92253524F);
        SENSITIVITY_MAP.put(185, 0.92957747F);
        SENSITIVITY_MAP.put(187, 0.9366197F);
        SENSITIVITY_MAP.put(188, 0.943662F);
        SENSITIVITY_MAP.put(190, 0.9507042F);
        SENSITIVITY_MAP.put(191, 0.9577465F);
        SENSITIVITY_MAP.put(192, 0.96478873F);
        SENSITIVITY_MAP.put(194, 0.97183096F);
        SENSITIVITY_MAP.put(195, 0.97887325F);
        SENSITIVITY_MAP.put(197, 0.9859155F);
        SENSITIVITY_MAP.put(198, 0.9929578F);
        SENSITIVITY_MAP.put(200, 1.0F);
    }

    /*
     * Essentially what we're doing here is enclosing the possible rotations the player
     * would've made in a normal framed scenario. The problem with the method we're using
     * to verify the player's rotation (prediction) has one problem. And that problem is
     * frame updates or as the client calls them "partial ticks". With this method, we can
     * directly check if the player's rotation was in the range of possible values, and we can
     * get that range with a simple limit check by complying the prediction vs the rotation
     *
     * Essentially it accounts for rotations that couldn't have been accurately predicted because
     * they got updated many times outside of the tick.
     */
    public static boolean enclosed(final float a, final float b, final float x) {
        final float distance = MathUtils.getAngleDelta(a, b);

        return MathUtils.getAngleDelta(a, x) < distance && MathUtils.getAngleDelta(b, x) < distance;
    }
    
    /*
     * This gets the players sensitivity through reversing the GCD of the player which
     * is the constant of the past 40 rotations, directly using minecraft math to achieve that.
     *
     *  // Update the mouse position per frame
     *  this.mouseHelper.updateXY();
     *
     *  // Grab the new deltaXY mouse values made
     *  final int deltaX = mouseHelper.getX();
     *  final int deltaY = mouseHelper.getY();
     *
     *  // Run the sensitivity formula to construct the new yaw/pitch values
     *  final float sensitivityFormat = (float) sensitivity * 0.6F + 0.2F;
     *  final float sensitivityProduct = var132 * var132 * var132 * 8.0F;
     *
     *  // Run the construction formula for yaw and pitch
     *  final float constructedYaw = (float) deltaX * sensitivityFormat;
     *  final float constructedPitch = (float) deltaY * sensitivityFormat
     *
     *  // Account for rotation slowdown using the 0.15 as a constant number
     *  final float productYaw = Math.abs(rotationYaw) + constructedYaw * 0.15
     *  final float productPitch = Math.abs(rotationPitch) + constructedPitch * 0.15
     *
     *  // Account for inversion
     *  this.rotationYaw = productYaw * inverseYaw;
     *  this.rotationPitch = productPitch * inversePitch;
     */
    public static float getSensitivityFromYawGCD(float gcd) {
        return ((float)Math.cbrt(yawToF2(gcd) / 8f) - .2f) / .6f;
    }

    public static float getSensitivityFromPitchGCD(float gcd) {
        return ((float)Math.cbrt(pitchToF3(gcd) / 8f) - .2f) / .6f;
    }

    private static float yawToF2(float yawDelta) {
        return yawDelta / .15f;
    }

    private static float pitchToF3(float pitchDelta) {
        int b0 = pitchDelta >= 0 ? 1 : -1; //Checking for inverted mouse.
        return (pitchDelta / b0) / .15f;
    }

    /*
     * This is going to be used for our inverse method for rotations since the method we are currently
     * using for inverse is a little inaccurate in some places due to the implementation of the game.
     * This should have a non existent margin of error since it will always be accurate to the degree.
     */
    private static float toRegularCircle(float angles) {
        angles %= 360.F;
        return angles < 0 ? angles + 360.F : angles;
    }

    public static int getInverseValue(final float current, final float previous, final InverseType type) {
        int result;
        /*
        * We're not using a switch statement since there is only really two possibilities so a switch
        * statement for my taste would simply be a little messy.
         */
        if (type == InverseType.YAW) {
            final float distance = MathUtils.getAngleDelta(previous, current);

            /*
            * We're expressing it as a regular circle since there is not a real limit to how the yaw is expressed
            * meaning if we do the check dynamically like we used to, it will fail when resetting or on larger rotations
             */
            final float circleX = toRegularCircle(previous);
            final float circleY = toRegularCircle(current);

            /*
            * This is the valid expression to the delta which is going to help us understand the direction
            * the player moved their head to get a more valid reading on their inversion.
             */
            final double polarX = MathUtils.getAngleDelta(circleX + distance, circleY);
            final double polarY = MathUtils.getAngleDelta(circleX - distance, circleY);

            /*
            * This is simply printing the result regularly without any issues. Simply, if a direction change,
            * print -1 which will change the "way" we're running the prediction, otherwise print 1 which wont change it
             */
            result = polarX < polarY ? 1 : -1;
        }

        /*
        * This is the check for the pitch variant of this. This has to be checked differently since one is
        * a vertical value when the other one is an expression of a circle. They are not the same.
         */
        else {
            final float distance = current - previous;

            /*
            * The check for pitch is the same we did before. It is much simpler since the rotation does not need
            * to be expressed in the likes of a circle since it is clamped normally from the client. So the method is
            * not as complex in comparison to the one we have for yaw. However, this should work fine as mentioned.
             */
            result = distance > 0 ? 1 : -1;
        }

        return result;
    }

    /*
     * This is an attempt to reverse the logistics of cinematic camera without having to run a full on prediction using
     * mouse filters. Otherwise, we would need to run more heavy calculations which is not really production friendly.
     * It may be more accurate but it is not really worth it if in the end of the day we're eating server performance.
     */
    public static double getGrid(final List<Float> entry) {
        /*
         * We're creating the variables average min and max to start calculating the possibility of cinematic camera.
         * Why does this work? Cinematic camera is essentially a slowly increasing slowdown (which is why cinematic camera
         * becomes slower the more you use it) which in turn makes it so the min max and average are extremely close together.
         */
        double average = 0.0;
        double min = 0.0, max = 0.0;

        /*
         * These are simple min max calculations done manually for the sake of simplicity. We're using the numbers 0.0
         * since we also want to account for the possibility of a negative number. If there are no negative numbers then
         * there is absolutely no need for us to care about that number other than getting the max.
         */
        for (final double number : entry) {
            if (number < min) min = number;
            if (number > max) max = number;

            /*
             * Instead of having a sum variable we can use an average variable which we divide
             * right after the loop is over. Smart programming trick if you want to use it.
             */
            average += number;
        }

        /*
         * We're dividing the average by the length since this is the formula to getting the average.
         * Specifically its (sum(n) / length(n)) = average(n) -- with n being the entry set we're analyzing.
         */
        average /= entry.size();

        /*
         * This is going to estimate how close the average and the max were together with the possibility of a min
         * variable which is going to represent a negative variable since the preset variable on min is 0.0.
         */
        return (max - average) - min;
    }

    public enum InverseType {
        YAW,
        PITCH
    }
}
