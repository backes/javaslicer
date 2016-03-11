package de.unisb.cs.st.javaslicer.integration;

import java.io.IOException;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.IntegrationTest;
import de.unisb.cs.st.javaslicer.integration.src.MohammedsCode;


public class SmallTests extends IntegrationTest {

    @Test
    public void testMohammedsExample() throws IOException {
        int[] expectedLines = new int[] {9, 10, 11, 15, 16};
        String criterion = MohammedsCode.class.getCanonicalName()+".main:11:*";
        String[] args = new String[] {};
        checkInnerClassLines(MohammedsCode.class, args, criterion, expectedLines);
    }

}