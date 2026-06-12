package Main;

import Excel.ExcelReader;
import Excel.ExcelResult;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ExcelReaderBenchmark {

    public static void main(String[] args) throws Exception {
        var filePath = args.length > 0 ? args[0] : "db/test_exc.xlsx";
        var warmup   = args.length > 1 ? Integer.parseInt(args[1]) : 5;
        var runs     = args.length > 2 ? Integer.parseInt(args[2]) : 30;
        var printRuns = args.length <= 3 || Boolean.parseBoolean(args[3]);

        var readMethod = findReadMethod();

        System.out.println("ExcelReader benchmark");
        System.out.println("method: " + readMethod.getName());
        System.out.println("file: " + filePath);
        System.out.println("warmup: " + warmup);
        System.out.println("runs: " + runs);
        System.out.println("printRuns: " + printRuns);
        System.out.println();

        for(var i = 0; i < warmup; i++) {
            invokeQuietly(readMethod, filePath);
        }

        var times = new long[runs];
        var commandCount = 0;
        var tableCount = 0;
        for(var i = 0; i < runs; i++) {
            var start = System.nanoTime();
            var result = invokeQuietly(readMethod, filePath);
            var elapsed = System.nanoTime() - start;

            if(result == null) throw new IllegalStateException("ExcelReader returned null");
            commandCount = collectionSize(result, "commands");
            tableCount = collectionSize(result, "tableDataList", "blockDataList");
            times[i] = elapsed;
            if(printRuns) System.out.printf("run %02d: %.3f ms%n", i + 1, elapsed / 1_000_000.0);
        }

        printSummary(times, commandCount, tableCount);
    }

    private static Method findReadMethod() throws NoSuchMethodException {
        try {
            return ExcelReader.class.getMethod("read", String.class);
        } catch(NoSuchMethodException _) {
            return ExcelReader.class.getMethod("Read", String.class);
        }
    }

    private static ExcelResult invokeQuietly(Method readMethod, String filePath) throws Exception {
        var originalOut = System.out;
        var originalErr = System.err;
        try(
            var nullOut = new PrintStream(OutputStream.nullOutputStream());
            var nullErr = new PrintStream(OutputStream.nullOutputStream())
        ) {
            System.setOut(nullOut);
            System.setErr(nullErr);
            return invoke(readMethod, filePath);
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    private static ExcelResult invoke(Method readMethod, String filePath) throws Exception {
        try {
            var target = Modifier.isStatic(readMethod.getModifiers()) ? null : newExcelReader();
            return (ExcelResult) readMethod.invoke(target, filePath);
        } catch(InvocationTargetException e) {
            var cause = e.getCause();
            if(cause instanceof Exception exception) throw exception;
            if(cause instanceof Error error) throw error;
            throw e;
        }
    }

    private static Object newExcelReader() throws Exception {
        var constructor = ExcelReader.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private static int collectionSize(Object source, String... methodNames) throws Exception {
        for(var methodName : methodNames) {
            try {
                var value = source.getClass().getMethod(methodName).invoke(source);
                if(value instanceof java.util.Collection<?> collection) return collection.size();
                if(value instanceof java.util.Map<?, ?> map) return map.size();
                return -1;
            } catch(NoSuchMethodException _) {}
        }
        return -1;
    }

    private static void printSummary(long[] times, int commandCount, int tableCount) {
        Arrays.sort(times);

        long sum = 0;
        for(var time : times) sum += time;

        var runs = times.length;
        System.out.println();
        System.out.println("result:");
        System.out.println("commands: " + commandCount);
        System.out.println("tables: " + tableCount);
        System.out.println();
        System.out.printf("avg: %.3f ms%n", sum / (double)runs / 1_000_000.0);
        System.out.printf("min: %.3f ms%n", times[0] / 1_000_000.0);
        System.out.printf("p50: %.3f ms%n", percentile(times, 0.50) / 1_000_000.0);
        System.out.printf("p95: %.3f ms%n", percentile(times, 0.95) / 1_000_000.0);
        System.out.printf("max: %.3f ms%n", times[runs - 1] / 1_000_000.0);
    }

    private static long percentile(long[] sortedTimes, double percentile) {
        var index = (int)Math.ceil(sortedTimes.length * percentile) - 1;
        index = Math.max(0, Math.min(index, sortedTimes.length - 1));
        return sortedTimes[index];
    }
}
