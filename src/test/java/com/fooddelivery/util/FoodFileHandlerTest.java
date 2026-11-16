package com.fooddelivery.util;

import com.fooddelivery.model.food.Beverage;
import com.fooddelivery.model.food.FoodItem;
import com.fooddelivery.model.food.MainCourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FoodFileHandlerTest {

    private MainCourse kottu;
    private Beverage ceylonTea;

    @BeforeEach
    void setUp() {
        kottu = new MainCourse(
                "Jaffna Spiced Mutton Kottu",
                "Godamba roti stir-fried with mutton, leeks, and curry",
                new BigDecimal("2250.00"),
                20,
                "Northern Sri Lankan",
                false,
                "EXTRA_HOT",
                "Large Plate (500g)"
        );

        ceylonTea = new Beverage(
                "Ceylon Nuwara Eliya Pekoe Tea",
                "Pure Ceylon high-grown tea with jaggery",
                new BigDecimal("450.00"),
                5,
                "Tea Pot (350ml)",
                350,
                false,
                65,
                "LOW"
        );
    }

    @Test
    void testExportMenuToCsvString() throws IOException {
        String csv = FoodFileHandler.exportMenuToCsvString(List.of(kottu, ceylonTea));
        assertNotNull(csv);
        assertTrue(csv.contains(FoodFileHandler.CSV_HEADER));
        assertTrue(csv.contains("MAIN_COURSE"));
        assertTrue(csv.contains("Jaffna Spiced Mutton Kottu"));
        assertTrue(csv.contains("BEVERAGE"));
        assertTrue(csv.contains("Ceylon Nuwara Eliya Pekoe Tea"));
        assertTrue(csv.contains("450.00"));
    }

    @Test
    void testLoadMenuFromCsvString() throws IOException {
        String csv = """
                TYPE,NAME,DESCRIPTION,PRICE,PREPARATION_TIME,EXTRA1,EXTRA2,EXTRA3,EXTRA4,EXTRA5
                MAIN_COURSE,Vegetable Wok Fried Rice,Aromatic jasmine rice with cashews,1350.00,15,Sri Lankan Fusion,true,MEDIUM,Standard Bowl (400g),
                BEVERAGE,Colombo Royal Faluda,Chilled rose syrup milk,650.00,8,Tall Glass (400ml),400,true,0,HIGH
                """;

        List<FoodItem> items = FoodFileHandler.loadMenuFromString(csv);
        assertEquals(2, items.size());

        assertTrue(items.get(0) instanceof MainCourse);
        MainCourse mc = (MainCourse) items.get(0);
        assertEquals("Vegetable Wok Fried Rice", mc.getName());
        assertEquals(new BigDecimal("1350.00"), mc.getPrice());
        assertTrue(mc.isVegetarian());
        assertEquals("MEDIUM", mc.getSpiceLevel());
        assertEquals("Sri Lankan Fusion", mc.getCuisineType());

        assertTrue(items.get(1) instanceof Beverage);
        Beverage bev = (Beverage) items.get(1);
        assertEquals("Colombo Royal Faluda", bev.getName());
        assertEquals(new BigDecimal("650.00"), bev.getPrice());
        assertEquals(400, bev.getVolumeMl());
        assertTrue(bev.isCold());
        assertEquals("HIGH", bev.getSugarLevel());
    }

    @Test
    void testLoadMenuWithQuotedCommas() throws IOException {
        String csv = """
                TYPE,NAME,DESCRIPTION,PRICE,PREPARATION_TIME,EXTRA1,EXTRA2,EXTRA3,EXTRA4,EXTRA5
                MAIN_COURSE,"Chicken Kottu Special","Shredded roti, leeks, onions, and gravy",1750.00,20,"Sri Lankan, Jaffna",false,HOT,"500g Plate, with Gravy",
                """;

        List<FoodItem> items = FoodFileHandler.loadMenuFromString(csv);
        assertEquals(1, items.size());
        MainCourse mc = (MainCourse) items.get(0);
        assertEquals("Chicken Kottu Special", mc.getName());
        assertEquals("Shredded roti, leeks, onions, and gravy", mc.getDescription());
        assertEquals("Sri Lankan, Jaffna", mc.getCuisineType());
        assertEquals("500g Plate, with Gravy", mc.getServingPortion());
    }

    @Test
    void testGracefulHandlingOfMalformedLines() throws IOException {
        String csv = """
                TYPE,NAME,DESCRIPTION,PRICE,PREPARATION_TIME,EXTRA1,EXTRA2,EXTRA3,EXTRA4,EXTRA5
                # This is a comment
                CORRUPTED_TYPE,Some Food,Bad Record
                
                MAIN_COURSE,Good Dish,Delicious,1000.00,15,Fusion,true,MILD,Portion
                """;

        List<FoodItem> items = FoodFileHandler.loadMenuFromString(csv);
        assertEquals(1, items.size());
        assertEquals("Good Dish", items.get(0).getName());
    }

    @Test
    void testExportAndImportFile(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("exported_menu.csv").toFile();
        FoodFileHandler.exportMenuToFile(List.of(kottu, ceylonTea), file);

        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        List<FoodItem> loaded = FoodFileHandler.loadMenuFromFile(file);
        assertEquals(2, loaded.size());
        assertEquals(kottu.getName(), loaded.get(0).getName());
        assertEquals(ceylonTea.getName(), loaded.get(1).getName());
    }
}

