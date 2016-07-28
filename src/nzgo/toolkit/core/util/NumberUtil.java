package nzgo.toolkit.core.util;

/**
 * NumberUtil
 * @author Walter Xie
 */
public class NumberUtil {

    public static Number sum(Number[] array) {
        assert array != null;

        if (array.length < 1)
            return 0;

        if(array[0] instanceof Double) {
            double sum = 0;
            for (int i = 0; i < array.length; i++)
                sum+=array[i].doubleValue();
            return sum;
        } else if(array[0] instanceof Integer) {
            int sum = 0;
            for (int i = 0; i < array.length; i++)
                sum+=array[i].intValue();
            return sum;
        } else if(array[0] instanceof Long) {
            long sum = 0;
            for (int i = 0; i < array.length; i++)
                sum+=array[i].longValue();
            return sum;
        } else {
            float sum = 0;
            for (int i = 0; i < array.length; i++)
                sum+=array[i].floatValue();
            return sum;
        }
    }

    // http://stackoverflow.com/questions/2721390/how-to-add-two-java-lang-numbers
    public static Number sum(Number a, Number b) {
        if(a instanceof Double || b instanceof Double) {
            return a.doubleValue() + b.doubleValue();
        } else if(a instanceof Integer || b instanceof Integer) {
            return a.intValue() + b.intValue();
        } else if(a instanceof Long || b instanceof Long) {
            return a.longValue() + b.longValue();
        } else {
            return a.floatValue() + b.floatValue();
        }
    }

    // if a < b, return -1; if a > b, return 1
    public static Number compare(Number a, Number b) {
        if(a instanceof Double || b instanceof Double) {
            return Double.compare(a.doubleValue(), b.doubleValue());
        } else if(a instanceof Integer || b instanceof Integer) {
            return Integer.compare(a.intValue(), b.intValue());
        } else if(a instanceof Long || b instanceof Long) {
            return Long.compare(a.longValue(), b.longValue());
        } else {
            return Float.compare(a.floatValue(), b.floatValue());
        }
    }

    public static Number parseNumber(String string, Class<? extends Number> numType) {
        if (numType.isAssignableFrom(Integer.class)) {
            return Integer.valueOf(string);
        } else if (numType.isAssignableFrom(Double.class)) {
            return Double.valueOf(string);
        } else if (numType.isAssignableFrom(Long.class)) {
            return Long.valueOf(string);
        } else if (numType.isAssignableFrom(Byte.class)) {
            return Byte.valueOf(string);
        } else if (numType.isAssignableFrom(Float.class)) {
            return Float.valueOf(string);
        } else if (numType.isAssignableFrom(Short.class)) {
            return Short.valueOf(string);
        }
        throw new NumberFormatException("Cannot parse " + string + " to number type " + numType);
    }

    public static <T extends Number> T[] parseNumbers(String[] strings) {
        T[] numbers = (T[]) new Object[strings.length];

        for (int i=0; i < strings.length; i++) {
            numbers[i] = (T) parseNumber(strings[i], numbers[i].getClass());
        }

        return numbers;
    }

}
