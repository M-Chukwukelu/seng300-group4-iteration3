// Project 2 Iteration Group 3
//Julie Kim 10123567
//Aryaman Sandhu 30017164
//Arcleah Pascual 30056034
//Aoi Ueki 30179305
//Ernest Shukla 30156303
//Shawn Hanlon 10021510
//Jaimie Marchuk 30112841
//Sofia Rubio 30113733
//Maria Munoz 30175339
//Anne Lumumba 30171346
//Nathaniel Dafoe 30181948

import com.jjjwelectronics.Mass;
import com.jjjwelectronics.Numeral;
import com.jjjwelectronics.scanner.Barcode;
import com.thelocalmarketplace.hardware.BarcodedProduct;
import com.thelocalmarketplace.hardware.SelfCheckoutStationGold;
import com.thelocalmarketplace.hardware.external.ProductDatabases;
import org.junit.Before;
import org.junit.Test;
import powerutility.PowerGrid;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class UpdateCartTest {
    private SelfCheckoutSoftware checkout;
    private SelfCheckoutStationGold station;
    private BarcodedProduct product;
    private BarcodedProduct product2;
    private Barcode barcode;
    private Barcode barcode2;
    private Barcode barcode3;
    private BarcodedProduct product3;
    private TouchScreen touchScreen;
    private UpdateCart updateCart;
    private WeightDiscrepancy weightDiscrepancy;
    private Attendant attendant;


    @Before
    public void setUp() {
        PowerGrid.engageUninterruptiblePowerSource();
        SelfCheckoutStationGold.resetConfigurationToDefaults();
        station = new SelfCheckoutStationGold();
        checkout = SelfCheckoutSoftware.getInstance(station);
        checkout.turnOn();
        attendant = new Attendant(checkout);
        touchScreen = new TouchScreen(checkout);
        updateCart = new UpdateCart(checkout);
        weightDiscrepancy = checkout.weightDiscrepancy;

        Numeral[] testBarcode = new Numeral[4];
        testBarcode[0] = Numeral.nine;
        testBarcode[1] = Numeral.five;
        testBarcode[2] = Numeral.eight;
        testBarcode[3] = Numeral.eight;
        barcode = new Barcode(testBarcode);
        product = new BarcodedProduct(barcode, "test", 5, 100);

        Numeral[] testBarcode2 = new Numeral[2];
        testBarcode2[0] = Numeral.zero;
        testBarcode2[1] = Numeral.one;
        barcode2 = new Barcode(testBarcode2);
        product2 = new BarcodedProduct(barcode2, "test2", 100, 10);
        Numeral[] testBarcode3 = new Numeral[2];
        testBarcode3[0] = Numeral.nine;
        testBarcode3[1] = Numeral.one;
        barcode3 = new Barcode(testBarcode3);
        product3 = new BarcodedProduct(barcode3, "lightTest", 100, 0.01);


        ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode, product);
        ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode2, product2);
        ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode3, product3);
    }

    @Test
    public void testAddScannedItem() {
        updateCart.addScannedItem(barcode);

        // Verify that the product was added to the cart
        assertTrue(checkout.getBarcodedProductsInOrder().contains(product));

        // Verify that the expected total weight is updated
        assertEquals(new Mass(product.getExpectedWeight()),
                checkout.getExpectedTotalWeight());

        // Verify that the scanned item was added to the cart
        assertTrue(checkout.getBarcodedProductsInOrder().contains(product));

    }

    @Test
    public void testRemoveItem() {
        // Add an item first
        updateCart.addScannedItem(barcode);
        // Then remove it
        updateCart.removeItem(product);

        // Verify that the product was removed from the cart
        assertFalse(checkout.getBarcodedProductsInOrder().contains(product));

    }

    @Test
    public void testOrderTotalUpdate() {
        updateCart.addScannedItem(barcode);
        BigDecimal expectedTotal = new BigDecimal(5);
        // Verify that the order total has been changed correctly
        assertEquals(expectedTotal, checkout.getOrderTotal());
    }

    @Test
    public void testRemoveNonExistentItem() {
        updateCart.removeItem(product);
        // Verify the cart remains unchanged
        assertTrue(checkout.getBarcodedProductsInOrder().isEmpty());
    }

    @Test
    public void testAddSameItemMultipleTimes() {
        int quantity = 3;
        for (int i = 0; i < quantity; i++) {
            updateCart.addScannedItem(barcode);
        }
        BigDecimal expectedTotal = new BigDecimal(15);

        assertEquals(expectedTotal, checkout.getOrderTotal());
        assertEquals(new Mass(product.getExpectedWeight() * 3),
                checkout.getExpectedTotalWeight());
        assertTrue(checkout.getBarcodedProductsInOrder().contains(product));


    }

    @Test
    public void testAddMultipleDifferentItems() {
        // Add multiple different items
        updateCart.addScannedItem(barcode); // First item
        updateCart.addScannedItem(barcode2); // Second item
        BigDecimal expectedTotal = new BigDecimal(105);
        assertEquals(expectedTotal, checkout.getOrderTotal());
        assertEquals(new Mass(product.getExpectedWeight() + product2.getExpectedWeight()),
                checkout.getExpectedTotalWeight());
        assertTrue(checkout.getBarcodedProductsInOrder().contains(product));
        assertTrue(checkout.getBarcodedProductsInOrder().contains(product2));
    }

    @Test
    public void testSkipBaggingItem() {
        MockTouchScreen mockTouchScreen = new MockTouchScreen(checkout);
        checkout.setTestTouchScreen(mockTouchScreen);
        updateCart.addScannedItem(barcode);
        assertFalse(checkout.isBlocked());

    }

    @Test
    public void testLightItems() {
        updateCart.addScannedItem(barcode3);
        assertFalse(checkout.isBlocked());
    }

    @Test
    public void testBarcodeScanEvent() {
        updateCart.aBarcodeHasBeenScanned(station.handheldScanner, barcode);
        assertTrue(checkout.getBarcodedProductsInOrder().contains(product));
    }


    private static class MockTouchScreen extends TouchScreen {

        public MockTouchScreen(SelfCheckoutSoftware checkout) {
            super(checkout);
        }

        @Override
        public boolean skipBaggingItem() {
            return true;
        }
    }
}




