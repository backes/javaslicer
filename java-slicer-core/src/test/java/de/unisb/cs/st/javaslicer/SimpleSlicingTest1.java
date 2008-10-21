package de.unisb.cs.st.javaslicer;

import org.junit.Test;


public class SimpleSlicingTest1 {

    @Test
    public void test() {
        Slicer.main(new String[] { "traces/simple1", "de.unisb.cs.st.javaslicer.tracedCode.Simple1.main:10:d" });
    }

}
