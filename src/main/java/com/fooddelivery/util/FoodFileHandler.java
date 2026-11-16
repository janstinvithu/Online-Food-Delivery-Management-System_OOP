package com.fooddelivery.util;

import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * File I/O handler for the Food Menu subsystem.
 * Handles CSV serialization and polymorphic deserialization for FoodItem subclasses (MainCourse and Beverage).
 */
public class FoodFileHandler {

    private static final Logger log = LoggerFactory.getLogger(FoodFileHandler.class);

    public static final String CSV_HEADER = "TYPE,NAME,DESCRIPTION,PRICE,PREPARATION_TIME,EXTRA1,EXTRA2,EXTRA3,EXTRA4,EXTRA5";

    /**
     * Serializes a list of FoodItem entities into CSV written to a Writer.
     */
    public static void exportMenuToCsv(List<FoodItem> items, Writer writer) throws IOException {
        BufferedWriter bw = (writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer);
        bw.write(CSV_HEADER);
        bw.newLine();

        if (items != null) {
            for (FoodItem item : items) {
                if (item instanceof MainCourse mc) {
                    bw.write(String.format("MAIN_COURSE,%s,%s,%s,%d,%s,%b,%s,%s,",
                            escapeCsv(mc.getName()),
                            escapeCsv(mc.getDescription()),
                            mc.getPrice().toPlainString(),
                            mc.getPreparationTimeMinutes(),
                            escapeCsv(mc.getCuisineType()),
                            mc.isVegetarian(),
                            mc.getSpiceLevel(),
                            escapeCsv(mc.getServingPortion())));
                } else if (item instanceof Beverage bev) {
                    bw.write(String.format("BEVERAGE,%s,%s,%s,%d,%s,%d,%b,%d,%s",
                            escapeCsv(bev.getName()),
                            escapeCsv(bev.getDescription()),
                            bev.getPrice().toPlainString(),
                            bev.getPreparationTimeMinutes(),
                            escapeCsv(bev.getVolumeSize()),
                            bev.getVolumeMl(),
                            bev.isCold(),
                            bev.getCaffeineContentMg(),
                            bev.getSugarLevel()));
                }
                bw.newLine();
            }
        }
        bw.flush();
    }

    /**
     * Exports a list of FoodItems to a CSV String.
     */
    public static String exportMenuToCsvString(List<FoodItem> items) throws IOException {
        StringWriter sw = new StringWriter();
        exportMenuToCsv(items, sw);
        return sw.toString();
    }

    /**
     * Exports a list of FoodItems to a local File on disk.
     */
    public static void exportMenuToFile(List<FoodItem> items, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            exportMenuToCsv(items, writer);
        }
    }

    /**
     * Deserializes a CSV stream from a Reader into concrete FoodItem instances.
     */
    public static List<FoodItem> loadMenuFromCsv(Reader reader) throws IOException {
        List<FoodItem> foodList = new ArrayList<>();
        BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);

        String line;
        int lineNumber = 0;
        while ((line = br.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // skip blank lines or comments
            }
            if (lineNumber == 1 && line.toUpperCase().startsWith("TYPE,")) {
                continue; // skip header row
            }

            try {
                FoodItem item = parseCsvLine(line);
                if (item != null) {
                    foodList.add(item);
                }
            } catch (Exception ex) {
                log.warn("Skipping malformed CSV line {}: '{}' - Reason: {}", lineNumber, line, ex.getMessage());
            }
        }
        return foodList;
    }

    /**
     * Deserializes CSV content from a String into FoodItem instances.
     */
    public static List<FoodItem> loadMenuFromString(String csvContent) throws IOException {
        if (csvContent == null || csvContent.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return loadMenuFromCsv(new StringReader(csvContent));
    }

    /**
     * Deserializes a CSV file from disk into FoodItem instances.
     */
    public static List<FoodItem> loadMenuFromFile(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Menu file does not exist: " + file.getAbsolutePath());
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return loadMenuFromCsv(reader);
        }
    }

    /**
     * Parses an individual line of CSV into either a MainCourse or a Beverage.
     */
    public static FoodItem parseCsvLine(String line) {
        List<String> tokens = parseTokens(line);
        if (tokens.size() < 5) {
            throw new IllegalArgumentException("Insufficient columns in line: expected at least 5 tokens, got " + tokens.size());
        }

        String type = tokens.get(0).trim().toUpperCase();
        String name = unescapeCsv(tokens.get(1));
        String description = unescapeCsv(tokens.get(2));
        BigDecimal price = new BigDecimal(tokens.get(3).trim());
        int preparationTimeMinutes = Integer.parseInt(tokens.get(4).trim());

        if ("MAIN_COURSE".equals(type)) {
            String cuisineType = (tokens.size() > 5) ? unescapeCsv(tokens.get(5)) : "Sri Lankan";
            boolean vegetarian = (tokens.size() > 6) && Boolean.parseBoolean(tokens.get(6).trim());
            String spiceLevel = (tokens.size() > 7 && !tokens.get(7).trim().isEmpty()) ? tokens.get(7).trim().toUpperCase() : "MEDIUM";
            String servingPortion = (tokens.size() > 8 && !tokens.get(8).trim().isEmpty()) ? unescapeCsv(tokens.get(8)) : "Standard Portion";

            return new MainCourse(name, description, price, preparationTimeMinutes, cuisineType, vegetarian, spiceLevel, servingPortion);

        } else if ("BEVERAGE".equals(type)) {
            String volumeSize = (tokens.size() > 5 && !tokens.get(5).trim().isEmpty()) ? unescapeCsv(tokens.get(5)) : "Regular (350ml)";
            int volumeMl = (tokens.size() > 6 && !tokens.get(6).trim().isEmpty()) ? Integer.parseInt(tokens.get(6).trim()) : 350;
            boolean cold = (tokens.size() <= 7) || Boolean.parseBoolean(tokens.get(7).trim());
            int caffeineContentMg = (tokens.size() > 8 && !tokens.get(8).trim().isEmpty()) ? Integer.parseInt(tokens.get(8).trim()) : 0;
            String sugarLevel = (tokens.size() > 9 && !tokens.get(9).trim().isEmpty()) ? tokens.get(9).trim().toUpperCase() : "ZERO";

            return new Beverage(name, description, price, preparationTimeMinutes, volumeSize, volumeMl, cold, caffeineContentMg, sugarLevel);
        } else {
            throw new IllegalArgumentException("Unknown food item type: " + type);
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String unescapeCsv(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).replace("\"\"", "\"");
        }
        return trimmed;
    }

    private static List<String> parseTokens(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }
}

