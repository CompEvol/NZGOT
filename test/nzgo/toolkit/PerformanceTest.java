package nzgo.toolkit;


import java.text.NumberFormat;


/**
 * not unit test
 * test1: http://www.vogella.com/tutorials/JavaPerformance/article.html
 * test2: http://stackoverflow.com/questions/74674/how-to-do-i-check-cpu-and-memory-usage-in-java
 *
 * @author Walter Xie
 */
public class PerformanceTest {
    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    public static void main(String[] args) {
        // program to test
        programToTest();

        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();

        // ++++++++++++++ test1: Memory Consumption = total used memory - free memory  +++++++++++
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is "+ bytesToMegabytes(memory) + "megabytes  = " + memory + " bytes." );

        // ++++++++++++++ test2: Memory Consumption = total used memory - free memory  +++++++++++
        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        // However, these should be taken only a an estimate...
        sb.append("free memory: " + format.format(freeMemory / 1024) + "<br/>");
        sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "<br/>");
        sb.append("max memory: " + format.format(maxMemory / 1024) + "<br/>");
        sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "<br/>");
    }

    public static void programToTest() {



    }
}

