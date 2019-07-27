package cc.funkemunky.anticheat.api.utils;

import cc.funkemunky.api.tinyprotocol.packet.types.MathHelper;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MathUtils {
    public static double log2 = Math.log(2);
    public static long gcd(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    public static long gcd(long... input) {
        long result = input[0];
        for (int i = 1; i < input.length; i++) result = gcd(result, input[i]);
        return result;
    }

    public static double round(double value) {

        return ((double) Math.round(value * 10000 + 0.00005)) / 10000;
    }

    public static double normalize(double val, double min, double max) {
        if (max < min) return 0;
        return (val - min) / (max - min);
    }

    public static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    public static double combination(double n,double r) {
        double nFac=factorial(n);
        double rFac=factorial(r);
        double nMinusRFac=factorial((n-r));

        return nFac/(rFac * nMinusRFac);
    }

    public static double factorial(double n) {
        if(n==1 || n==0) return 1;
        for(double i=n;i>0;i--,n*=(i > 0 ? i : 1)) {}
        return n;
    }

    public static double getDelta(double one, double two) {
        double absOne = Math.abs(one), absTwo = Math.abs(two);
        return Math.max(absOne, absTwo) - Math.min(absOne, absTwo);
    }

    public static double bernoullis(double n,double k,double successProb) {

        double combo = MathUtils.combination(n, k);
        double q= 1 - successProb;
        return combo * Math.pow(successProb,k) * Math.pow(q,n-k);
    }

    public static  double log2(double a) {
        if(a == 0)
            return 0.0;
        return Math.log(a) / log2;
    }

    public static float getDistanceBetweenAngles(final float angle1, final float angle2) {
        float distance = Math.abs(angle1 - angle2) % 360.0f;

        if (distance > 180.0f) {
            distance = 360.0f - distance;
        }

        return distance;
    }

    public static double getHorizontalDistance(CustomLocation from, CustomLocation to) {
        double deltaX = to.getX() - from.getX();
        double deltaZ = to.getZ() - from.getZ();
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    public static double getAimbotOffset(CustomLocation playerLoc, CustomLocation entity) {

        Vector playerRotation = new Vector(playerLoc.getYaw(), playerLoc.getPitch(), 0.0F);
        float[] expectedRotation = getRotationFromPosition(playerLoc, entity);

        double deltaYaw = clamp180(playerRotation.getX() - expectedRotation[0]);

        double horizontalDistance = getHorizontalDistance(playerLoc, entity);
        double distance = getDistance3D(playerLoc, entity);

        double offsetX = deltaYaw * horizontalDistance * distance;

        return offsetX;
    }

    public static double getDistance3D(CustomLocation one, CustomLocation two) {
        double toReturn = 0.0D;
        double xSqr = (two.getX() - one.getX()) * (two.getX() - one.getX());
        double ySqr = (two.getY() - one.getY()) * (two.getY() - one.getY());
        double zSqr = (two.getZ() - one.getZ()) * (two.getZ() - one.getZ());
        double sqrt = Math.sqrt(xSqr + ySqr + zSqr);
        toReturn = Math.abs(sqrt);
        return toReturn;
    }

    public static double clamp180(double theta) {
        theta %= 360.0D;
        if (theta >= 180.0D) {
            theta -= 360.0D;
        }
        if (theta < -180.0D) {
            theta += 360.0D;
        }
        return theta;
    }

    public static float normalizeAngle(float yaw) {
        return yaw % 360;
    }

    public static double normalizeAngle(double yaw) {
        return yaw % 360;
    }

    public static float[] getRotationFromPosition(final CustomLocation playerLocation, final CustomLocation targetLocation) {
        final double xDiff = targetLocation.getX() - playerLocation.getX();
        final double zDiff = targetLocation.getZ() - playerLocation.getZ();
        final double yDiff = targetLocation.getY() - (playerLocation.getY() + 0.12);
        final double dist = MathHelper.sqrt(xDiff * xDiff + zDiff * zDiff);
        final float yaw = (float)(Math.atan2(zDiff, xDiff) * 180.0 / 3.141592653589793) - 90.0f;
        final float pitch = (float)(-(Math.atan2(yDiff, dist) * 180.0 / 3.141592653589793));
        return new float[] { yaw, pitch };
    }

    public static double permutation(double n,double r) {
        double nFac=MathUtils.factorial(n);
        double nMinusRFac=MathUtils.factorial((n-r));
        return nFac/nMinusRFac;
    }

    public static double hypotenuse(double a, double b) {
        double r;
        if (Math.abs(a) > Math.abs(b)) {
            r = b/a;
            r = Math.abs(a)*Math.sqrt(1+r*r);
        } else if (b != 0) {
            r = a/b;
            r = Math.abs(b)*Math.sqrt(1+r*r);
        } else {
            r = 0.0;
        }
        return r;
    }

    public static double euclideanDistance(float[] p,float[]  q) {

        double ret=0;
        for(int i=0;i < p.length;i++) {
            double diff = (q[i]-p[i]);
            double sq = Math.pow(diff,2);
            ret+=sq;
        }
        return ret;

    }

    public static double euclideanDistance(double[] p,double[]  q) {

        double ret = 0;
        for(int i = 0;i < p.length;i++) {
            double diff = (q[i]-p[i]);
            double sq=Math.pow(diff,2);
            ret += sq;
        }
        return ret;

    }

    public static double[] weightsFor(List<Double> vector) {
        /* split coordinate system */
        List<double[]> coords=coordSplit(vector);
        /* x vals */
        double[] x=coords.get(0);
        /* y vals */
        double[] y=coords.get(1);


        double meanX=sum(x)/x.length;
        double meanY=sum(y)/y.length;

        double sumOfMeanDifferences=sumOfMeanDifferences(x,y);
        double xDifferenceOfMean=sumOfMeanDifferencesOnePoint(x);

        double w_1=sumOfMeanDifferences/xDifferenceOfMean;

        double w_0=meanY  - (w_1) * meanX;

        //double w_1=(n*sumOfProducts(x,y) - sum(x) * sum(y))/(n*sumOfSquares(x) - Math.pow(sum(x),2));

        //	double w_0=(sum(y) - (w_1 * sum(x)))/n;

        double[] ret = new double[vector.size()];
        ret[0]=w_0;
        ret[1]=w_1;

        return ret;
    }

    public static List<double[]> coordSplit(List<Double> vector) {

        if(vector==null) return null;
        List<double[]> ret = new ArrayList<>();
        /* x coordinates */
        double[] xVals = new double[vector.size()/2];
        /* y coordinates */
        double[] yVals = new double[vector.size()/2];
        /* current points */
        int xTracker=0;
        int yTracker=0;
        for(int i=0;i<vector.size();i++) {
            //even value, x coordinate
            if(i%2==0) xVals[xTracker++]=vector.get(i);
                //y coordinate
            else yVals[yTracker++]=vector.get(i);
        }
        ret.add(xVals);
        ret.add(yVals);

        return ret;
    }

    public static double[] weightsFor(double[] vector) {

        /* split coordinate system */
        List<double[]> coords=coordSplit(vector);
        /* x vals */
        double[] x=coords.get(0);
        /* y vals */
        double[] y=coords.get(1);


        double meanX=sum(x)/x.length;
        double meanY=sum(y)/y.length;

        double sumOfMeanDifferences=sumOfMeanDifferences(x,y);
        double xDifferenceOfMean=sumOfMeanDifferencesOnePoint(x);

        double w_1=sumOfMeanDifferences/xDifferenceOfMean;

        double w_0=meanY  - (w_1) * meanX;




        double[] ret = new double[vector.length];
        ret[0]=w_0;
        ret[1]=w_1;

        return ret;
    }

    public static double sumOfMeanDifferences(double[] vector,double[] vector2) {
        double mean=sum(vector)/vector.length;
        double mean2=sum(vector2)/vector2.length;
        double ret=0;
        for(int i=0;i<vector.length;i++) {
            double vec1Diff=vector[i]-mean;
            double vec2Diff=vector2[i]-mean2;
            ret+=vec1Diff * vec2Diff;
        }
        return ret;
    }

    public static double sumOfMeanDifferencesOnePoint(double[] vector) {
        double mean=sum(vector)/vector.length;
        double ret=0;
        for(int i=0;i<vector.length;i++) {
            double vec1Diff=Math.pow(vector[i]-mean,2);
            ret+=vec1Diff;
        }
        return ret;
    }

    public static List<double[]> coordSplit(double[] vector) {

        if(vector==null) return null;
        List<double[]> ret = new ArrayList();
        /* x coordinates */
        double[] xVals = new double[vector.length/2];
        /* y coordinates */
        double[] yVals = new double[vector.length/2];
        /* current points */
        int xTracker=0;
        int yTracker=0;
        for(int i=0;i<vector.length;i++) {
            //even value, x coordinate
            if(i%2==0) xVals[xTracker++]=vector[i];
                //y coordinate
            else yVals[yTracker++]=vector[i];
        }
        ret.add(xVals);
        ret.add(yVals);

        return ret;
    }

    public static double correlation(double[] residuals,double targetAttribute[]) {
        double[] predictedValues = new double[residuals.length];
        for(int i=0;i<predictedValues.length;i++) {
            predictedValues[i]=targetAttribute[i] - residuals[i];
        }
        double ssErr=ssError(predictedValues,targetAttribute);
        double total=ssTotal(residuals,targetAttribute);
        return 1-(ssErr/total);
    }

    public static double ssTotal(double[] residuals,double[] targetAttribute) {
        return ssReg(residuals,targetAttribute) + ssError(residuals,targetAttribute);
    }

    public static double ssReg(double[] residuals,double[] targetAttribute) {
        double mean=sum(targetAttribute)/targetAttribute.length;
        double ret=0;
        for(int i=0;i<residuals.length;i++) {
            ret+=Math.pow(residuals[i]-mean,2);
        }
        return ret;
    }

    public static double sum(double[] nums) {

        double ret=0;
        for(double d : nums) ret+=d;

        return ret;
    }

    public static double ssError(double[] predictedValues,double[] targetAttribute) {
        double ret=0;
        for(int i=0;i<predictedValues.length;i++) {
            ret+=Math.pow(targetAttribute[i]-predictedValues[i],2);
        }
        return ret;

    }

    public static long lcm(long a, long b) {
        return a * (b / gcd(a, b));
    }

    public static long lcm(long... input) {
        long result = input[0];
        for (int i = 1; i < input.length; i++) result = lcm(result, input[i]);
        return result;
    }

    public static boolean isPrimeNumber(long n) {
        //check if n is a multiple of 2
        if (n%2==0) return false;
        //if not, then just check the odds
        for(int i=3;i*i<=n;i+=2) {
            if(n%i==0)
                return false;
        }
        return true;
    }
}
