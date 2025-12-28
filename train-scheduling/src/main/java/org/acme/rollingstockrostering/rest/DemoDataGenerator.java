package org.acme.rollingstockrostering.rest;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.rollingstockrostering.domain.*;

/**
 * DemoDataGenerator - Generates test data for the Rolling Stock Rostering problem
 * 
 * Creates:
 * - 5-10 stations with coordinates
 * - 3-5 routes connecting stations
 * - 5-8 trains with different capacities
 * - 2-3 depots
 * - Hourly passenger demand
 * - Unassigned departure times (planning entities)
 */
@ApplicationScoped
public class DemoDataGenerator {
    
    private static final Random RANDOM = new Random(37); // Fixed seed for reproducibility
    
    /**
     * Generates a complete RollingStockSchedule with demo data
     */
    public RollingStockSchedule generateDemoData() {
        return generateDefaultDataset();
    }
    
    /**
     * Generates default size dataset (99 departures)
     */
    public static RollingStockSchedule generateDefaultDataset() {
        return generateDataset(6, 22, 2); // 6 AM to 10 PM, every 2 hours
    }
    
    /**
     * Generates small dataset for quick testing (50 departures)
     */
    public static RollingStockSchedule generateSmallDataset() {
        return generateDataset(8, 18, 2); // 8 AM to 6 PM, every 2 hours
    }
    
    /**
     * Generates large dataset for stress testing (200 departures)
     */
    public static RollingStockSchedule generateLargeDataset() {
        return generateDataset(6, 22, 1); // 6 AM to 10 PM, every hour
    }
    
    /**
     * Generates dataset with configurable time range
     */
    private static RollingStockSchedule generateDataset(int startHour, int endHour, int intervalHours) {
        // Create configuration
        Konfiguracija konfiguracija = new Konfiguracija(
                Duration.ofMinutes(5), // Minimum 5 minutes between trains
                Duration.ofMinutes(2)  // 2 minutes station stop
        );
        
        // Create stations
        List<Stacija> stacijas = generateStacijas();
        
        // Create routes
        List<Marsruts> marsruti = generateMarsruti(stacijas);
        
        // Create trains (more trains for larger datasets)
        List<Vilciens> vilcieni = generateVilcieni(intervalHours == 1 ? 40 : 30);
        
        // Create depots
        List<Depo> depo = generateDepo(vilcieni, stacijas);
        
        // Create passenger demand
        List<CilvekuPieprasijums> cilvekuPieprasijumi = generateCilvekuPieprasijums(
                stacijas, marsruti
        );
        
        // Create unassigned departure times (planning entities)
        List<AtiesanasLaiks> atiesanasLaiki = generateAtiesanasLaiki(
                stacijas, marsruti, cilvekuPieprasijumi, startHour, endHour, intervalHours
        );
        
        return new RollingStockSchedule(
                vilcieni,
                stacijas,
                marsruti,
                depo,
                cilvekuPieprasijumi,
                konfiguracija,
                atiesanasLaiki
        );
    }
    
    /**
     * Generate 8 stations in Latvia
     */
    private static List<Stacija> generateStacijas() {
        List<Stacija> stacijas = new ArrayList<>();
        
        // Rīga (Savienojas ar Vagonu parks, Torņakalns, Zemitāni)
        stacijas.add(createStacija(1L, "Rīga", 56.9496, 24.1052, Arrays.asList(2L, 3L, 4L)));

        // Maršruts: Rīga - Indra | Zilupe | Gulbene
        // Stacijas:
        // Vagonu parks
        stacijas.add(createStacija(2L, "Vagonu parks", 56.9350, 24.1200, Arrays.asList(1L, 5L)));
        // Jāņavārti
        stacijas.add(createStacija(5L, "Jāņavārti", 56.9280, 24.1350, Arrays.asList(2L, 6L)));
        // Daugmale
        stacijas.add(createStacija(6L, "Daugmale", 56.9200, 24.1500, Arrays.asList(5L, 7L)));
        // Šķirotava
        stacijas.add(createStacija(7L, "Šķirotava", 56.9100, 24.1650, Arrays.asList(6L, 8L)));
        // Gaisma
        stacijas.add(createStacija(8L, "Gaisma", 56.9000, 24.1800, Arrays.asList(7L, 9L)));
        // Rumbula
        stacijas.add(createStacija(9L, "Rumbula", 56.8900, 24.1950, Arrays.asList(8L, 10L)));
        // Dārziņi
        stacijas.add(createStacija(10L, "Dārziņi", 56.8800, 24.2100, Arrays.asList(9L, 11L)));
        // Dole
        stacijas.add(createStacija(11L, "Dole", 56.8700, 24.2250, Arrays.asList(10L, 12L)));
        // Salspils
        stacijas.add(createStacija(12L, "Salspils", 56.8600, 24.2400, Arrays.asList(11L, 13L)));
        // Saulkalne
        stacijas.add(createStacija(13L, "Saulkalne", 56.8500, 24.2550, Arrays.asList(12L, 14L)));
        // Ikšķile
        stacijas.add(createStacija(14L, "Ikšķile", 56.8400, 24.2700, Arrays.asList(13L, 15L)));
        // Jaunogre
        stacijas.add(createStacija(15L, "Jaunogre", 56.8300, 24.2850, Arrays.asList(14L, 16L)));
        // Ogre
        stacijas.add(createStacija(16L, "Ogre", 56.8200, 24.6000, Arrays.asList(15L, 17L)));
        // Pārogre
        stacijas.add(createStacija(17L, "Pārogre", 56.8100, 24.6150, Arrays.asList(16L, 18L)));
        // Ciemupe
        stacijas.add(createStacija(18L, "Ciemupe", 56.8000, 24.6300, Arrays.asList(17L, 19L)));
        // Ķegums
        stacijas.add(createStacija(19L, "Ķegums", 56.7900, 24.7000, Arrays.asList(18L, 20L)));
        // Lielvārde
        stacijas.add(createStacija(20L, "Lielvārde", 56.7800, 24.8000, Arrays.asList(19L, 21L)));
        // Kaibala
        stacijas.add(createStacija(21L, "Kaibala", 56.7700, 24.8500, Arrays.asList(20L, 22L)));
        // Jumprava
        stacijas.add(createStacija(22L, "Jumprava", 56.7600, 24.9000, Arrays.asList(21L, 23L)));
        // Skrīveri
        stacijas.add(createStacija(23L, "Skrīveri", 56.7500, 25.0000, Arrays.asList(22L, 24L)));
        // Muldakmens
        stacijas.add(createStacija(24L, "Muldakmens", 56.7400, 25.1000, Arrays.asList(23L, 25L)));
        // Aizkraukle
        stacijas.add(createStacija(25L, "Aizkraukle", 56.6050, 25.2550, Arrays.asList(24L, 26L)));
        // Koknese
        stacijas.add(createStacija(26L, "Koknese", 56.6500, 25.4300, Arrays.asList(25L, 27L)));
        // Alotene
        stacijas.add(createStacija(27L, "Alotene", 56.6800, 25.5000, Arrays.asList(26L, 28L)));
        // Pļaviņas
        stacijas.add(createStacija(28L, "Pļaviņas", 56.6170, 25.7190, Arrays.asList(27L, 29L)));
        // Krustpils (Savienojas ar Pļaviņas, Trepe, Kūkas un Jaunskalsnava)
        stacijas.add(createStacija(29L, "Krustpils", 56.5095, 26.1840, Arrays.asList(28L, 30L, 38L, 53L)));

        // Maršruts: Rīga - Indra
        // Stacijas:
        // Trepe
        stacijas.add(createStacija(30L, "Trepe", 56.4900, 26.2500, Arrays.asList(29L, 31L)));
        // Līvāni
        stacijas.add(createStacija(31L, "Līvāni", 56.3540, 26.1730, Arrays.asList(30L, 32L)));
        // Jersika
        stacijas.add(createStacija(32L, "Jersika", 56.2900, 26.3000, Arrays.asList(31L, 33L)));
        // Nīcgale
        stacijas.add(createStacija(33L, "Nīcgale", 56.2500, 26.3500, Arrays.asList(32L, 34L)));
        // Vabole
        stacijas.add(createStacija(34L, "Vabole", 56.2000, 26.4000, Arrays.asList(33L, 35L)));
        // Līksna
        stacijas.add(createStacija(35L, "Līksna", 56.1500, 26.4500, Arrays.asList(34L, 36L)));
        // Daugavpils
        stacijas.add(createStacija(36L, "Daugavpils", 55.8750, 26.5360, Arrays.asList(35L, 37L)));
        // Krāslava
        stacijas.add(createStacija(37L, "Krāslava", 55.8950, 27.1680, Arrays.asList(36L, 38L)));
        // Indra
        stacijas.add(createStacija(38L, "Indra", 56.1000, 27.5000, Arrays.asList(37L)));

        // Maršruts: Rīga - Zilupe
        // Stacijas:
        // Kūkas
        stacijas.add(createStacija(39L, "Kūkas", 56.5500, 26.3000, Arrays.asList(29L, 40L)));
        // Mežāre
        stacijas.add(createStacija(40L, "Mežāre", 56.6000, 26.4000, Arrays.asList(39L, 41L)));
        // Atašiene
        stacijas.add(createStacija(41L, "Atašiene", 56.6500, 26.5000, Arrays.asList(40L, 42L)));
        // Stirniene
        stacijas.add(createStacija(42L, "Stirniene", 56.7000, 26.6000, Arrays.asList(41L, 43L)));
        // Varakļāņi
        stacijas.add(createStacija(43L, "Varakļāņi", 56.6050, 26.7500, Arrays.asList(42L, 44L)));
        // Viļāni
        stacijas.add(createStacija(44L, "Viļāni", 56.5530, 26.9270, Arrays.asList(43L, 45L)));
        // Sakstagals
        stacijas.add(createStacija(45L, "Sakstagals", 56.5000, 27.1000, Arrays.asList(44L, 46L)));
        // Rēzekne
        stacijas.add(createStacija(46L, "Rēzekne", 56.5100, 27.3400, Arrays.asList(45L, 47L)));
        // Taudejāņi
        stacijas.add(createStacija(47L, "Taudejāņi", 56.5500, 27.5000, Arrays.asList(46L, 48L)));
        // Cirma
        stacijas.add(createStacija(48L, "Cirma", 56.6000, 27.6000, Arrays.asList(47L, 49L)));
        // Ludza
        stacijas.add(createStacija(49L, "Ludza", 56.5450, 27.7160, Arrays.asList(48L, 50L)));
        // Istalsna
        stacijas.add(createStacija(50L, "Istalsna", 56.6000, 27.8000, Arrays.asList(49L, 51L)));
        // Nerza
        stacijas.add(createStacija(51L, "Nerza", 56.6500, 27.9000, Arrays.asList(50L, 52L)));
        // Briģi
        stacijas.add(createStacija(52L, "Briģi", 56.7000, 28.0000, Arrays.asList(51L, 53L)));
        // Zilupe
        stacijas.add(createStacija(53L, "Zilupe", 56.3870, 28.1240, Arrays.asList(52L)));

        // Maršruts: Rīga - Gulbene
        // Stacijas:
        // Jaunkalsnava
        stacijas.add(createStacija(54L, "Jaunkalsnava", 56.5500, 26.3500, Arrays.asList(29L, 55L)));
        // Kalsnava
        stacijas.add(createStacija(55L, "Kalsnava", 56.6000, 26.4500, Arrays.asList(54L, 56L)));
        // Mārciena
        stacijas.add(createStacija(56L, "Mārciena", 56.6800, 26.5500, Arrays.asList(55L, 57L)));
        // Madona
        stacijas.add(createStacija(57L, "Madona", 56.8550, 26.2180, Arrays.asList(56L, 58L)));
        // Cesvaine
        stacijas.add(createStacija(58L, "Cesvaine", 56.9680, 26.3080, Arrays.asList(57L, 59L)));
        // Jaungulbene
        stacijas.add(createStacija(59L, "Jaungulbene", 57.1000, 26.5000, Arrays.asList(58L, 60L)));
        // Gulbene
        stacijas.add(createStacija(60L, "Gulbene", 57.1730, 26.7530, Arrays.asList(59L)));

        // Maršruts: Rīga - Liepāja | Tukums II
        // Stacijas:
        // Torņakalns (Savienojas ar Rīga, Bieriņi / Bērnu slimnīca un Zasulauks)
        stacijas.add(createStacija(3L, "Torņakalns", 56.9350, 24.0800, Arrays.asList(1L, 61L, 87L)));

        // Maršruts: Rīga - Liepāja
        // Stacijas:
        // Bieriņi / Bērnu slimnīca
        stacijas.add(createStacija(61L, "Bieriņi / Bērnu slimnīca", 56.9200, 24.0500, Arrays.asList(3L, 62L)));
        // BA Turība
        stacijas.add(createStacija(62L, "BA Turība", 56.9100, 24.0300, Arrays.asList(61L, 63L)));
        // Tīraine
        stacijas.add(createStacija(63L, "Tīraine", 56.9000, 24.0100, Arrays.asList(62L, 64L)));
        // Medemciems
        stacijas.add(createStacija(64L, "Medemciems", 56.8800, 23.9800, Arrays.asList(63L, 65L)));
        // Jaunolaine
        stacijas.add(createStacija(65L, "Jaunolaine", 56.8500, 23.9500, Arrays.asList(64L, 66L)));
        // Olaine
        stacijas.add(createStacija(66L, "Olaine", 56.7950, 23.9390, Arrays.asList(65L, 67L)));
        // Dalbe
        stacijas.add(createStacija(67L, "Dalbe", 56.7500, 23.9000, Arrays.asList(66L, 68L)));
        // Cena
        stacijas.add(createStacija(68L, "Cena", 56.7000, 23.8500, Arrays.asList(67L, 69L)));
        // Ozolnieki
        stacijas.add(createStacija(69L, "Ozolnieki", 56.6880, 23.7820, Arrays.asList(68L, 70L)));
        // Cukurfabrika
        stacijas.add(createStacija(70L, "Cukurfabrika", 56.6500, 23.7300, Arrays.asList(69L, 71L)));
        // Jelgava
        stacijas.add(createStacija(71L, "Jelgava", 56.6530, 23.7130, Arrays.asList(70L, 72L)));
        // Dobele
        stacijas.add(createStacija(72L, "Dobele", 56.6250, 23.2800, Arrays.asList(71L, 73L)));
        // Biksti
        stacijas.add(createStacija(73L, "Biksti", 56.6000, 22.9000, Arrays.asList(72L, 74L)));
        // Saldus
        stacijas.add(createStacija(74L, "Saldus", 56.6650, 22.4930, Arrays.asList(73L, 75L)));
        // Skrunda
        stacijas.add(createStacija(75L, "Skrunda", 56.6670, 22.0110, Arrays.asList(74L, 76L)));
        // Liepāja
        stacijas.add(createStacija(76L, "Liepāja", 56.5050, 21.0110, Arrays.asList(75L)));

        // Maršruts: Rīga - Tukums II
        // Stacijas:
        // Zasulauks
        stacijas.add(createStacija(87L, "Zasulauks", 56.9300, 24.0600, Arrays.asList(3L, 88L)));
        // Depo
        stacijas.add(createStacija(88L, "Depo", 56.9250, 24.0400, Arrays.asList(87L, 89L)));
        // Zolitūde
        stacijas.add(createStacija(89L, "Zolitūde", 56.9380, 24.0520, Arrays.asList(88L, 90L)));
        // Imanta
        stacijas.add(createStacija(90L, "Imanta", 56.9490, 24.0210, Arrays.asList(89L, 91L)));
        // Babīte
        stacijas.add(createStacija(91L, "Babīte", 56.9550, 23.9540, Arrays.asList(90L, 92L)));
        // Priedaine
        stacijas.add(createStacija(92L, "Priedaine", 56.9600, 23.9200, Arrays.asList(91L, 93L)));
        // Lielupe
        stacijas.add(createStacija(93L, "Lielupe", 56.9680, 23.8200, Arrays.asList(92L, 94L)));
        // Bulduri
        stacijas.add(createStacija(94L, "Bulduri", 56.9700, 23.7750, Arrays.asList(93L, 95L)));
        // Dzintari
        stacijas.add(createStacija(95L, "Dzintari", 56.9730, 23.7630, Arrays.asList(94L, 96L)));
        // Majori
        stacijas.add(createStacija(96L, "Majori", 56.9750, 23.7500, Arrays.asList(95L, 97L)));
        // Dubulti
        stacijas.add(createStacija(97L, "Dubulti", 56.9800, 23.7300, Arrays.asList(96L, 98L)));
        // Jaundubulti
        stacijas.add(createStacija(98L, "Jaundubulti", 56.9850, 23.7100, Arrays.asList(97L, 99L)));
        // Pumpuri
        stacijas.add(createStacija(99L, "Pumpuri", 57.0000, 23.6800, Arrays.asList(98L, 100L)));
        // Melluži
        stacijas.add(createStacija(100L, "Melluži", 57.0100, 23.6500, Arrays.asList(99L, 101L)));
        // Asari
        stacijas.add(createStacija(101L, "Asari", 57.0200, 23.6200, Arrays.asList(100L, 102L)));
        // Vaivari
        stacijas.add(createStacija(102L, "Vaivari", 57.0300, 23.5900, Arrays.asList(101L, 103L)));
        // Sloka
        stacijas.add(createStacija(103L, "Sloka", 57.0400, 23.5600, Arrays.asList(102L, 104L)));
        // Kūdra
        stacijas.add(createStacija(104L, "Kūdra", 57.0500, 23.5300, Arrays.asList(103L, 105L)));
        // Ķemeri
        stacijas.add(createStacija(105L, "Ķemeri", 56.9960, 23.5160, Arrays.asList(104L, 106L)));
        // Smārde
        stacijas.add(createStacija(106L, "Smārde", 56.9800, 23.4500, Arrays.asList(105L, 107L)));
        // Milzkalne
        stacijas.add(createStacija(107L, "Milzkalne", 56.9600, 23.3800, Arrays.asList(106L, 108L)));
        // Tukums I
        stacijas.add(createStacija(108L, "Tukums I", 56.9680, 23.1550, Arrays.asList(107L, 109L)));
        // Tukums II
        stacijas.add(createStacija(109L, "Tukums II", 56.9700, 23.1400, Arrays.asList(108L)));

        // Maršruts: Rīga - Valga | Skulte
        // Stacijas:
        // Zemitāni (Savienojas ar Rīga, Čiekurkalns un Brasa)
        stacijas.add(createStacija(4L, "Zemitāni", 56.9600, 24.1300, Arrays.asList(1L, 110L, 130L)));

        // Maršruts: Rīga - Valga
        // Stacijas:
        // Čiekurkalns
        stacijas.add(createStacija(110L, "Čiekurkalns", 56.9700, 24.1500, Arrays.asList(4L, 111L)));
        // Šmerlis
        stacijas.add(createStacija(111L, "Šmerlis", 56.9850, 24.1700, Arrays.asList(110L, 112L)));
        // Jugla
        stacijas.add(createStacija(112L, "Jugla", 57.0280, 24.3260, Arrays.asList(111L, 113L)));
        // Garkalne
        stacijas.add(createStacija(113L, "Garkalne", 57.0500, 24.4000, Arrays.asList(112L, 114L)));
        // Krievupe
        stacijas.add(createStacija(114L, "Krievupe", 57.0800, 24.5000, Arrays.asList(113L, 115L)));
        // Vangaži
        stacijas.add(createStacija(115L, "Vangaži", 57.0900, 24.5340, Arrays.asList(114L, 116L)));
        // Inčukalns
        stacijas.add(createStacija(116L, "Inčukalns", 57.1000, 24.6860, Arrays.asList(115L, 117L)));
        // Egļupe
        stacijas.add(createStacija(117L, "Egļupe", 57.1200, 24.7500, Arrays.asList(116L, 118L)));
        // Sigulda
        stacijas.add(createStacija(118L, "Sigulda", 57.1540, 24.8530, Arrays.asList(117L, 119L)));
        // Līgatne
        stacijas.add(createStacija(119L, "Līgatne", 57.2340, 25.0410, Arrays.asList(118L, 120L)));
        // Ieriķi
        stacijas.add(createStacija(120L, "Ieriķi", 57.2800, 25.1500, Arrays.asList(119L, 121L)));
        // Melturi
        stacijas.add(createStacija(121L, "Melturi", 57.3000, 25.2000, Arrays.asList(120L, 122L)));
        // Āraiši
        stacijas.add(createStacija(122L, "Āraiši", 57.3100, 25.2700, Arrays.asList(121L, 123L)));
        // Cēsis
        stacijas.add(createStacija(123L, "Cēsis", 57.3120, 25.2670, Arrays.asList(122L, 124L)));
        // Jānamuiža
        stacijas.add(createStacija(124L, "Jānamuiža", 57.3500, 25.3500, Arrays.asList(123L, 125L)));
        // Lode
        stacijas.add(createStacija(125L, "Lode", 57.4000, 25.4500, Arrays.asList(124L, 126L)));
        // Valmiera
        stacijas.add(createStacija(126L, "Valmiera", 57.5380, 25.4270, Arrays.asList(125L, 127L)));
        // Strenči
        stacijas.add(createStacija(127L, "Strenči", 57.6260, 25.6840, Arrays.asList(126L, 128L)));
        // Lugaži
        stacijas.add(createStacija(128L, "Lugaži", 57.7000, 25.9000, Arrays.asList(127L, 129L)));
        // Valga
        stacijas.add(createStacija(129L, "Valga", 57.7780, 26.0460, Arrays.asList(128L)));

        // Maršruts: Rīga - Skulte
        // Stacijas:
        // Brasa
        stacijas.add(createStacija(130L, "Brasa", 56.9800, 24.1600, Arrays.asList(4L, 131L)));
        // Sarkandaugava
        stacijas.add(createStacija(131L, "Sarkandaugava", 57.0000, 24.1800, Arrays.asList(130L, 132L)));
        // Dauderi
        stacijas.add(createStacija(132L, "Dauderi", 57.0200, 24.2000, Arrays.asList(131L, 133L)));
        // Mangaļi
        stacijas.add(createStacija(133L, "Mangaļi", 57.0400, 24.2200, Arrays.asList(132L, 134L)));
        // Ziemeļblāzma
        stacijas.add(createStacija(134L, "Ziemeļblāzma", 57.0600, 24.2400, Arrays.asList(133L, 135L)));
        // Vecdaugava
        stacijas.add(createStacija(135L, "Vecdaugava", 57.0800, 24.2600, Arrays.asList(134L, 136L)));
        // Vecāķi
        stacijas.add(createStacija(136L, "Vecāķi", 57.0900, 24.2800, Arrays.asList(135L, 137L)));
        // Kalngale
        stacijas.add(createStacija(137L, "Kalngale", 57.1000, 24.3000, Arrays.asList(136L, 138L)));
        // Garciems
        stacijas.add(createStacija(138L, "Garciems", 57.1200, 24.3200, Arrays.asList(137L, 139L)));
        // Garupe
        stacijas.add(createStacija(139L, "Garupe", 57.1400, 24.3400, Arrays.asList(138L, 140L)));
        // Carnikava
        stacijas.add(createStacija(140L, "Carnikava", 57.1310, 24.2140, Arrays.asList(139L, 141L)));
        // Gauja
        stacijas.add(createStacija(141L, "Gauja", 57.1600, 24.3800, Arrays.asList(140L, 142L)));
        // Lilaste
        stacijas.add(createStacija(142L, "Lilaste", 57.1800, 24.4000, Arrays.asList(141L, 143L)));
        // Inčupe
        stacijas.add(createStacija(143L, "Inčupe", 57.2000, 24.4200, Arrays.asList(142L, 144L)));
        // Pabaži
        stacijas.add(createStacija(144L, "Pabaži", 57.2200, 24.4400, Arrays.asList(143L, 145L)));
        // Saulkrasti
        stacijas.add(createStacija(145L, "Saulkrasti", 57.2650, 24.4130, Arrays.asList(144L, 146L)));
        // Ķīšupe
        stacijas.add(createStacija(146L, "Ķīšupe", 57.2900, 24.4500, Arrays.asList(145L, 147L)));
        // Zvejniekciems
        stacijas.add(createStacija(147L, "Zvejniekciems", 57.3100, 24.4700, Arrays.asList(146L, 148L)));
        // Skulte
        stacijas.add(createStacija(148L, "Skulte", 57.3300, 24.5000, Arrays.asList(147L)));
        
        return stacijas;
    }
    
    private static Stacija createStacija(Long id, String nosaukums, double lat, double lon, 
                                   List<Long> kaimini) {
        Stacija stacija = new Stacija(id, nosaukums, new GeoCoordinates(lat, lon));
        stacija.setKaiminiStacijas(kaimini);
        return stacija;
    }
    
    /**
     * Generate routes covering all major lines
     */
    private static List<Marsruts> generateMarsruti(List<Stacija> stacijas) {
        List<Marsruts> marsruti = new ArrayList<>();

        // Route 1: Rīga -> Liepāja (long distance, western Latvia)
        marsruti.add(new Marsruts(1L, "Rīga-Liepāja", Arrays.asList(
            1L, 3L, 61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L, 71L, 72L, 73L, 74L, 75L, 76L
        )));

        // Route 2: Rīga -> Tukums II (Jūrmala coastal route, high frequency)
        marsruti.add(new Marsruts(2L, "Rīga-Tukums II", Arrays.asList(
            1L, 3L, 87L, 88L, 89L, 90L, 91L, 92L, 93L, 94L, 95L, 96L, 97L, 98L, 99L, 100L, 101L, 102L, 103L, 104L, 105L, 106L, 107L, 108L, 109L
        )));

        // Route 3: Rīga -> Valga (northern route through Sigulda, Cēsis, Valmiera)
        marsruti.add(new Marsruts(3L, "Rīga-Valga", Arrays.asList(
            1L, 4L, 110L, 111L, 112L, 113L, 114L, 115L, 116L, 117L, 118L, 119L, 120L, 121L, 122L, 123L, 124L, 125L, 126L, 127L, 128L, 129L
        )));

        // Route 4: Rīga -> Skulte (coastal route to the north)
        marsruti.add(new Marsruts(4L, "Rīga-Skulte", Arrays.asList(
            1L, 4L, 130L, 131L, 132L, 133L, 134L, 135L, 136L, 137L, 138L, 139L, 140L, 141L, 142L, 143L, 144L, 145L, 146L, 147L, 148L
        )));

        // Route 5: Rīga -> Ogre (popular commuter route)
        marsruti.add(new Marsruts(5L, "Rīga-Ogre", Arrays.asList(
            1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L
        )));

        // Route 6: Rīga -> Indra (long distance eastern route via Daugavpils)
        marsruti.add(new Marsruts(6L, "Rīga-Indra", Arrays.asList(
            1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L
        )));

        // Route 7: Rīga -> Zilupe (eastern route via Rēzekne)
        marsruti.add(new Marsruts(7L, "Rīga-Zilupe", Arrays.asList(
            1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L, 51L, 52L, 53L
        )));

        // Route 8: Rīga -> Gulbene (route via Madona)
        marsruti.add(new Marsruts(8L, "Rīga-Gulbene", Arrays.asList(
            1L, 2L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 54L, 55L, 56L, 57L, 58L, 59L, 60L
        )));

        // Route 9: Rīga -> Jelgava (commuter route, partial Liepāja line)
        marsruti.add(new Marsruts(9L, "Rīga-Jelgava", Arrays.asList(
            1L, 3L, 61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L, 71L
        )));

        // Route 10: Rīga -> Jūrmala (short commuter route, partial Tukums line)
        marsruti.add(new Marsruts(10L, "Rīga-Jūrmala", Arrays.asList(
            1L, 3L, 87L, 88L, 89L, 90L, 91L, 92L, 93L, 94L, 95L, 96L, 97L
        )));

        return marsruti;
    }
    
    /**
     * Generate trains with varying capacities
     */
    private static List<Vilciens> generateVilcieni(int count) {
        List<Vilciens> vilcieni = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            int capacity;
            if (i < count / 3) {
                capacity = 100 + (i * 10); // Small: 100, 110, 120...
            } else if (i < 2 * count / 3) {
                capacity = 200 + ((i - count/3) * 10); // Medium: 200, 210, 220...
            } else {
                capacity = 280 + ((i - 2*count/3) * 10); // Large: 280, 290, 300...
            }
            vilcieni.add(new Vilciens((long)(i + 1), capacity));
        }
        
        return vilcieni;
    }
    
    /**
     * Generate depots for all trains
     */
    private static List<Depo> generateDepo(List<Vilciens> vilcieni, List<Stacija> stacijas) {
        List<Depo> depo = new ArrayList<>();

        // Create depots for all trains distributed across major stations
        for (int i = 0; i < vilcieni.size(); i++) {
            Long vilciensId = vilcieni.get(i).getId();
            Long stacijaId;

            // Distribute trains across major depot locations
            int mod = i % 5;
            if (mod == 0) {
                stacijaId = 1L; // Rīga (main hub)
            } else if (mod == 1) {
                stacijaId = 2L; // Vagonu parks (main depot)
            } else if (mod == 2) {
                stacijaId = 71L; // Jelgava
            } else if (mod == 3) {
                stacijaId = 88L; // Depo (on Jūrmala line)
            } else {
                stacijaId = 16L; // Ogre
            }

            depo.add(new Depo((long)(i + 1), vilciensId, stacijaId));
        }

        return depo;
    }
    
    /**
     * Generate hourly passenger demand (6 AM to 10 PM)
     */
    private static List<CilvekuPieprasijums> generateCilvekuPieprasijums(
            List<Stacija> stacijas, List<Marsruts> marsruti) {
        List<CilvekuPieprasijums> pieprasijumi = new ArrayList<>();
        long id = 1L;
        
        // Generate demand for each route and station
        for (Marsruts marsruts : marsruti) {
            for (Long stacijaId : marsruts.getStacijas()) {
                // Generate hourly demand from 6 AM to 10 PM
                for (int hour = 6; hour <= 22; hour++) {
                    int demand = generateDemand(marsruts.getId(), hour);
                    pieprasijumi.add(new CilvekuPieprasijums(
                            id++,
                            stacijaId,
                            marsruts.getId(),
                            LocalTime.of(hour, 0),
                            demand
                    ));
                }
            }
        }
        
        return pieprasijumi;
    }
    
    /**
     * Generate realistic passenger demand based on route and time
     */
    private static int generateDemand(Long marsrutaId, int hour) {
        // Morning rush hour (7-9 AM)
        if (hour >= 7 && hour <= 9) {
            return 50 + RANDOM.nextInt(100); // 50-150 passengers
        }
        // Evening rush hour (5-7 PM)
        else if (hour >= 17 && hour <= 19) {
            return 40 + RANDOM.nextInt(80); // 40-120 passengers
        }
        // Mid-day
        else if (hour >= 10 && hour <= 16) {
            return 20 + RANDOM.nextInt(40); // 20-60 passengers
        }
        // Early morning / late evening
        else {
            return 10 + RANDOM.nextInt(20); // 10-30 passengers
        }
    }
    
    /**
     * Generate unassigned departure times (planning entities)
     * 
     * Creates departures every 30-60 minutes for each route
     */
    private static List<AtiesanasLaiks> generateAtiesanasLaiki(
            List<Stacija> stacijas, 
            List<Marsruts> marsruti,
            List<CilvekuPieprasijums> cilvekuPieprasijumi,
            int startHour,
            int endHour,
            int intervalHours) {
        List<AtiesanasLaiks> atiesanasLaiki = new ArrayList<>();
        long id = 1L;
        
        // Generate departures for each route with time offsets to avoid conflicts
        for (Marsruts marsruts : marsruti) {
            int offsetMinutes = getRouteOffset(marsruts.getId());
            
            // Generate departures with specified time range and interval
            for (int hour = startHour; hour <= endHour; hour += intervalHours) {
                LocalTime startTime = LocalTime.of(hour, offsetMinutes);
                
                // Create departure for each station on the route with progressive times
                int stationIndex = 0;
                for (Long stacijaId : marsruts.getStacijas()) {
                    // Add travel time between stations (30 minutes per station hop)
                    LocalTime stationTime = startTime.plusMinutes(stationIndex * 30L);
                    
                    int cilvekuDelta = getExpectedPassengers(
                            stacijaId, marsruts.getId(), stationTime, cilvekuPieprasijumi
                    );
                    
                    atiesanasLaiki.add(new AtiesanasLaiks(
                            id++,
                            stacijaId,
                            marsruts.getId(),
                            stationTime,
                            cilvekuDelta
                    ));
                    // vilciensId is null (unassigned) - Timefold will assign it
                    
                    stationIndex++;
                }
            }
        }
        
        return atiesanasLaiki;
    }
    
    /**
     * Generate unassigned departure times (planning entities) - Legacy method
     * 
     * Creates departures every 30-60 minutes for each route
     */
    private List<AtiesanasLaiks> generateAtiesanasLaiki(
            List<Stacija> stacijas, 
            List<Marsruts> marsruti,
            List<CilvekuPieprasijums> cilvekuPieprasijumi) {
        List<AtiesanasLaiks> atiesanasLaiki = new ArrayList<>();
        long id = 1L;
        
        // Generate departures for each route with time offsets to avoid conflicts
        for (Marsruts marsruts : marsruti) {
            int offsetMinutes = getRouteOffset(marsruts.getId());
            
            // Generate departures from 6 AM to 10 PM - every 2 hours
            for (int hour = 6; hour <= 22; hour += 2) {
                LocalTime laiks = LocalTime.of(hour, offsetMinutes);
                
                // Create departure for each station on the route
                for (Long stacijaId : marsruts.getStacijas()) {
                    int cilvekuDelta = getExpectedPassengers(
                            stacijaId, marsruts.getId(), laiks, cilvekuPieprasijumi
                    );
                    
                    atiesanasLaiki.add(new AtiesanasLaiks(
                            id++,
                            stacijaId,
                            marsruts.getId(),
                            laiks,
                            cilvekuDelta
                    ));
                    // vilciensId is null (unassigned) - Timefold will assign it
                }
            }
        }
        
        return atiesanasLaiki;
    }
    
    /**
     * Get time offset for route to prevent conflicts
     * Each route starts at different minute to avoid simultaneous departures
     */
    private static int getRouteOffset(Long marsrutaId) {
        switch (marsrutaId.intValue()) {
            case 1: return 0;   // Rīga-Liepāja: 06:00, 08:00, 10:00...
            case 2: return 15;  // Rīga-Jūrmala: 06:15, 08:15, 10:15...
            case 3: return 30;  // Rīga-Valmiera: 06:30, 08:30, 10:30...
            case 4: return 45;  // Rīga-Ogre: 06:45, 08:45, 10:45...
            default: return 0;
        }
    }
    
    /**
     * Get expected passengers for a departure based on demand
     */
    private static int getExpectedPassengers(Long stacijaId, Long marsrutaId, LocalTime laiks,
                                       List<CilvekuPieprasijums> cilvekuPieprasijumi) {
        // Find matching demand (simplified: use exact hour match)
        return cilvekuPieprasijumi.stream()
                .filter(p -> p.getStacijasId().equals(stacijaId))
                .filter(p -> p.getMarsrutaId().equals(marsrutaId))
                .filter(p -> p.getStunda().getHour() == laiks.getHour())
                .mapToInt(CilvekuPieprasijums::getCilvekuSkaits)
                .findFirst()
                .orElse(0);
    }
}
