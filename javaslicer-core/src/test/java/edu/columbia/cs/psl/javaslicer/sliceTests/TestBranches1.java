package edu.columbia.cs.psl.javaslicer.sliceTests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class TestBranches1 extends AbstractSlicingTest{
	@Test
	public void testBranches() throws Exception {
		final List<Instruction> slice = getSliceNew("target/trace.edu.columbia.cs.psl.javaslicer.tracedTests.TestBranches1", "main", "edu.columbia.cs.psl.javaslicer.tracedTests.TestBranches1.testBranches:21:{d}");
//        checkSlice(slice, new String[] {
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ALOAD 0",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ICONST_0",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 AALOAD",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ICONST_0",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 INVOKEVIRTUAL java/lang/String.charAt(I)C",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 BIPUSH 48",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ISUB",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:20 ISTORE 1", // definition of a (== 1)
//
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ICONST_2",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ILOAD 1",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 IMUL",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:22 ISTORE 2", // definition of b (== 2)
//
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ILOAD 1",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ICONST_5",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 IF_ICMPGE L0",
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ILOAD 2",
//                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 GOTO L1",
//                //"de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:11 ILOAD 3", // instruction after ":" unused
//                "de.unisb.cs.st.javaslicer.tracedCode.Branches1.main:24 ISTORE 4", // definition of d
//            });
//		System.out.println("Slice is " + slice);
	}
}
