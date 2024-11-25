package dev.morling.onebrc;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CalculateAverage_vasudev_baseline {
    final public static String fileName = "./measurements.txt";

    public static void printMap(ConcurrentHashMap<String, CityStats> statsMap) {
        StringBuilder output = new StringBuilder("{");
        statsMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey()) // Sort entries by city name
                .forEach(entry -> {
                    String city = entry.getKey();
                    CityStats stats = entry.getValue();
                    output.append(city)
                            .append("=")
                            .append(String.format("%.1f/%.1f/%.1f", stats.min, stats.getMean(), stats.max))
                            .append(", ");
                });
        if (output.length() > 1) {
            output.setLength(output.length() - 2); // Remove trailing comma and space
        }
        output.append("}");
        System.out.println(output);
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        // TreeMap to store statistics for each city in sorted order
        ConcurrentHashMap<String, CityStats> statsMap = new ConcurrentHashMap<>();

        try {
            Files.lines(Paths.get(fileName))
                    .parallel()// Stream each line from the file
                    .forEach(line -> {
                        //System.out.println(Thread.currentThread().getName() + " processing: " + line);
                        String[] arr = line.split(";");
                        String city = arr[0];
                        double temp = Double.parseDouble(arr[1]);
                        statsMap.computeIfAbsent(city, k -> new CityStats()).update(temp);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Print the statistics for each city in the desired format
        printMap(statsMap);

        long endTime = System.nanoTime();
        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;

        System.out.println("Time taken: " + durationInSeconds + " seconds");
    }

    static class CityStats {
        double sum = 0.0;
        int count = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        // Synchronized update method
        synchronized void update(double temp) {
            sum += temp;
            count++;
            max = Math.max(max, temp);
            min = Math.min(min, temp);
        }

        double getMean() {
            return sum / count;
        }
    }
}

// Class to hold statistics for each city
