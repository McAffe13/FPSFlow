package dev.fpsflow.utils;

public final class MathUtils {

    private MathUtils() {}

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double squaredDistance(double x1, double y1, double z1,
                                         double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return dx * dx + dy * dy + dz * dz;
    }

    /** Combines multiple values into a single hash for cache keying. */
    public static long combineHash(long... values) {
        long result = 1L;
        for (long v : values) {
            result = 31L * result + v;
        }
        return result;
    }
}
