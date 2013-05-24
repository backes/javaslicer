/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.dependences
 *    Class:     DependenceExtractorTest
 *    Filename:  javaslicer-core/src/test/java/de/unisb/cs/st/javaslicer/dependences/DependenceExtractorTest.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * JavaSlicer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JavaSlicer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaSlicer. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.javaslicer.dependences;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.hammacher.util.IntHolder;
import de.hammacher.util.Pair;
import de.unisb.cs.st.javaslicer.AbstractSlicingTest;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DataDependenceType;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesExtractor;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.DependencesVisitorAdapter;
import de.unisb.cs.st.javaslicer.dependenceAnalysis.VisitorCapability;
import de.unisb.cs.st.javaslicer.traceResult.ThreadId;
import de.unisb.cs.st.javaslicer.traceResult.TraceResult;
import de.unisb.cs.st.javaslicer.variables.ArrayElement;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.ObjectField;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class DependenceExtractorTest extends DependencesVisitorAdapter<InstructionInstance> {

    private Map<Pair<InstructionInstance, Variable>, IntHolder> pendingDataDependences;
    private Set<Long> createdObjects;
    private Set<Long> seenObjects;
    private int pending = 0;
    private int discarded = 0;
    private int events = 0;

    @Before
    public void init() {
        this.pendingDataDependences = new HashMap<Pair<InstructionInstance,Variable>, IntHolder>();
        this.createdObjects = new HashSet<Long>();
        this.seenObjects = new HashSet<Long>();
        this.outputNr = this.pending = this.discarded = this.events = 0;
    }

    @After
    public void shutdown() {
        this.pendingDataDependences = null;
    }

    @Test
    public void testPendingSymmetrieOnMethod1() throws URISyntaxException, IOException, InterruptedException {
        final File traceFile = new File(AbstractSlicingTest.class.getResource("/traces/method1").toURI());
        test(traceFile);
    }

    @Test @Ignore
    public void testPendingSymmetrieOnPmd() throws IOException, InterruptedException {
        final File traceFile = new File("/Users/clemens/Studium/Bachelor-Thesis/SVN/java-slicer/javaslicer-tracer/dacapo_uncomp/jython.trace");
        test(traceFile);
    }

    private void test(final File traceFile) throws IOException, InterruptedException {
        TraceResult traceResult = new TraceResult(traceFile);

        DependencesExtractor<InstructionInstance> extractor = DependencesExtractor.forTrace(traceResult);
        extractor.registerVisitor(this,
            VisitorCapability.PENDING_DATA_DEPENDENCES_ALL,
            VisitorCapability.OBJECT_CREATION);

        for (ThreadId thread: traceResult.getThreads()) {
            if (thread.getThreadName().equals("main")) {
                extractor.processBackwardTrace(thread);
                break;
            }
        }
        Assert.assertEquals("still pending dependencies", this.pendingDataDependences.size(), 0);
    }

    @Override
    public void discardPendingDataDependence(InstructionInstance from,
            Variable var, DataDependenceType type) {
        Pair<InstructionInstance, Variable> pair = new Pair<InstructionInstance, Variable>(from, var);
        IntHolder oldInt = this.pendingDataDependences.get(pair);
        Assert.assertNotNull("Missing pending dependency", oldInt);
        if (oldInt.decrementAndGet() == 0) {
            this.pendingDataDependences.remove(pair);
        }

        if (var instanceof ObjectField) {
            long objId = ((ObjectField)var).getObjectId();
            this.seenObjects.add(objId);
            Assert.assertFalse(this.createdObjects.contains(objId));
        } else if (var instanceof ArrayElement) {
            long objId = ((ArrayElement)var).getArrayId();
            this.seenObjects.add(objId);
            Assert.assertFalse(this.createdObjects.contains(objId));
        }
    }

    int outputNr = 0;
    //Variable varWithMax = null;
    @Override
    public void visitObjectCreation(long objectId, InstructionInstance instrInstance) {
        this.createdObjects.add(objectId);
        this.seenObjects.add(objectId);
        if (++this.outputNr % 10000 == 0) {
            int stackEntries = 0;
            int localVariables = 0;
            int objectFields = 0;
            int arrayElems = 0;
            int maxVar = 0;
            //List<InstructionInstance> instancesOnOldVarWithMax = new ArrayList<InstructionInstance>();
            Map<Variable, IntHolder> varCount = new HashMap<Variable, IntHolder>();
            Set<InstructionInstance> instances = new HashSet<InstructionInstance>();
            HashSet<Long> aliveObjects = new HashSet<Long>();
            for (Pair<InstructionInstance, Variable> p: this.pendingDataDependences.keySet()) {
                Variable var = p.getSecond();
                IntHolder h = varCount.get(var);
                if (h == null) {
                    varCount.put(var, new IntHolder(1));
                } else {
                    maxVar = Math.max(maxVar, h.incrementAndGet());
                }
                //if (var.equals(this.varWithMax))
                //    instancesOnOldVarWithMax.add(p.getFirst());
                instances.add(p.getFirst());
                if (var instanceof StackEntry) {
                    ++stackEntries;
                } else if (var instanceof LocalVariable) {
                    ++localVariables;
                } else if (var instanceof ObjectField) {
                    ++objectFields;
                    aliveObjects.add(((ObjectField)var).getObjectId());
                    Assert.assertTrue("pending dependences from illegal object", ((ObjectField)var).getObjectId() != objectId);
                } else if (var instanceof ArrayElement) {
                    ++arrayElems;
                    aliveObjects.add(((ArrayElement)var).getArrayId());
                    Assert.assertTrue("pending dependences from illegal object", ((ArrayElement)var).getArrayId() != objectId);
                }
            }

            //Entry<Variable, IntHolder>[] invVarCount = varCount.entrySet().toArray(new Entry[varCount.size()]);
            //Arrays.sort(invVarCount, new Comparator<Entry<Variable, IntHolder>>() {
            //    public int compare(Entry<Variable, IntHolder> o1,
            //            Entry<Variable, IntHolder> o2) {
            //        return o1.getValue().get() - o2.getValue().get();
            //    }
            //});
            //this.varWithMax = invVarCount[invVarCount.length-1].getKey();

            System.out.format("stack: %7d, local var: %7d, fields: %7d, array elem: %7d%nseen obj: %7d, created obj: %7d, alive obj: %7d%nmaxVar: %7d, vars: %7d, instances: %7d%n",
                stackEntries, localVariables, objectFields, arrayElems,
                this.seenObjects.size(), this.createdObjects.size(), aliveObjects.size(), maxVar, varCount.size(), instances.size());
        }

    }

    @Override
    public void visitPendingDataDependence(InstructionInstance from,
            Variable var, DataDependenceType type) {
        ++this.pending;
        if (++this.events % 1000000 == 0) {
            System.out.format("Pending: %10d, discarded: %10d, outstanding: %10d%n", this.pending, this.discarded, this.pending - this.discarded);
        }
        Pair<InstructionInstance, Variable> pair = new Pair<InstructionInstance, Variable>(from, var);
        IntHolder oldInt = this.pendingDataDependences.get(pair);
        if (oldInt == null) {
            this.pendingDataDependences.put(pair, new IntHolder(1));
        } else {
            oldInt.incrementAndGet();
        }

        if (var instanceof ObjectField) {
            long objId = ((ObjectField)var).getObjectId();
            this.seenObjects.add(objId);
            Assert.assertFalse(this.createdObjects.contains(objId));
        } else if (var instanceof ArrayElement) {
            long objId = ((ArrayElement)var).getArrayId();
            this.seenObjects.add(objId);
            Assert.assertFalse(this.createdObjects.contains(objId));
        }
    }

}
