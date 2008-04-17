package de.unisb.cs.st;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    /**
     * Tests setter and getter for localized string.
     */
    public void testLocalized() {
        App a = new App();
        assertEquals(a.getLocalized(), "Hello World!");
        String german = "Hallo Welt!";
        a.setLocalized(german);
        assertEquals(a.getLocalized(), german);
    }

}
