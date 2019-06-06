package cc.funkemunky.anticheat.api.utils;

public class MathUtils {
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
