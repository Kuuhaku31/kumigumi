package Def;

public class Test {
    public static void main(String[] args) {
        System.out.println("This is a test class.");

        if (args.length > 0) {
            System.out.println("Arguments passed:");
            for (String arg : args) {
                System.out.println(arg);
            }
        } else {
            System.out.println("No arguments passed.");
        }

        var envName = "JAVA_HOME";
        var envValue = System.getenv(envName);
        if (envValue != null) {
            System.out.println(envName + ": " + envValue);
        } else {
            System.out.println(envName + " is not set.");
        }

        // 打印当前时间（带时区信息）
        var currentTime = java.time.ZonedDateTime.now();
        System.out.println("Current time with timezone: " + currentTime);

        var timeString = "2025-12-18 15:34:18";
        var formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var localDateTime = java.time.LocalDateTime.parse(timeString, formatter);
        var zonedDateTime = localDateTime.atZone(java.time.ZoneId.systemDefault());
        System.out.println("Parsed time with timezone: " + zonedDateTime);

        var timeString2 = "2025-12-18T15:34:18+08:00";
        var zonedDateTime2 = java.time.ZonedDateTime.parse(timeString2);
        System.out.println("Parsed time with timezone 2: " + zonedDateTime2);
    }
}