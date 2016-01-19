/** License information:
 *    Component: javaslicer-core
 *    Package:   de.unisb.cs.st.javaslicer.instructionSimulation
 *    Class:     SimulationEnvironment
 *    Filename:  javaslicer-core/src/main/java/de/unisb/cs/st/javaslicer/instructionSimulation/SimulationEnvironment.java
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
package de.unisb.cs.st.javaslicer.instructionSimulation;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.hammacher.util.iterators.EmptyIterator;
import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.common.classRepresentation.ReadMethod;
import de.unisb.cs.st.javaslicer.variables.LocalVariable;
import de.unisb.cs.st.javaslicer.variables.StackEntry;
import de.unisb.cs.st.javaslicer.variables.Variable;


public class SimulationEnvironment {

	public static class AllVariables extends AbstractCollection<Variable> {

		public static class Itr implements Iterator<Variable> {

			private final LocalVariable[] localVars;
			private final StackEntry[] stackEntries;
			private int posLocalVars;
			private int posStackEntries;

			public Itr(LocalVariable[] localVars, StackEntry[] stackEntries) {
				this.localVars = localVars;
				this.stackEntries = stackEntries;
				int firstLocalVar = 0;
				while (firstLocalVar < localVars.length && localVars[firstLocalVar] == null)
					++firstLocalVar;
				int firstStackEntry = 0;
				if (firstLocalVar == localVars.length) {
					while (firstStackEntry < stackEntries.length && stackEntries[firstStackEntry] == null)
						++firstStackEntry;
				}
				this.posLocalVars = firstLocalVar;
				this.posStackEntries = firstStackEntry;
			}

			@Override
			public boolean hasNext() {
				return this.posStackEntries == this.stackEntries.length;
			}

			@Override
			public Variable next() {
				if (!hasNext())
					throw new NoSuchElementException();
				Variable ret;
				if (this.posLocalVars == this.localVars.length) {
					ret = this.stackEntries[this.posStackEntries];
					do {
						++this.posStackEntries;
					} while (this.posStackEntries < this.stackEntries.length && this.stackEntries[this.posStackEntries] == null);
				} else {
					ret = this.localVars[this.posLocalVars];
					do {
						++this.posLocalVars;
					} while (this.posLocalVars < this.localVars.length && this.localVars[this.posLocalVars] == null);
					if (this.posLocalVars == this.localVars.length)
						while (this.posStackEntries < this.stackEntries.length && this.stackEntries[this.posStackEntries] == null)
							++this.posStackEntries;
				}
				return ret;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		}

		private int count;
		private final LocalVariable[] localVars;
		private final StackEntry[] stackEntries;

		public AllVariables(LocalVariable[] localVariables,
				StackEntry[] stackEntries) {
			this.count = 0;
			for (LocalVariable var: localVariables)
				if (var != null)
					++this.count;
			for (StackEntry e: stackEntries)
				if (e != null)
					++this.count;
			this.localVars = localVariables;
			this.stackEntries = stackEntries;
		}

		@Override
		public int size() {
			return this.count;
		}

		@Override
		public Iterator<Variable> iterator() {
			return this.count == 0 ? EmptyIterator.<Variable>getInstance() : new Itr(this.localVars, this.stackEntries);
		}

	}

	public static class CachedLocalVariables extends AbstractList<LocalVariable> {

		private final LocalVariable[] entries;
		private final int offset;
		private final int amount;

		public CachedLocalVariables(LocalVariable[] entries, int offset,
				int amount) {
			this.entries = entries;
			this.offset = offset;
			this.amount = amount;
		}

		@Override
		public LocalVariable get(int index) {
			assert (index < this.amount);
			return this.entries[this.offset + index];
		}

		@Override
		public int size() {
			return this.amount;
		}

	}

	public static class CachedStackEntries extends AbstractList<StackEntry> {

		private final StackEntry[] entries;
		private final int offset;
		private final int amount;

		public CachedStackEntries(StackEntry[] entries, int offset,
				int amount) {
			this.entries = entries;
			this.offset = offset;
			this.amount = amount;
		}

		@Override
		public StackEntry get(int index) {
			assert (index < this.amount);
			return this.entries[this.offset + index];
		}

		@Override
		public int size() {
			return this.amount;
		}

	}

	public long[] frames;
	public int[] opStack;
	public int[] minOpStack;
	private StackEntry[][] cachedStackEntries;
	private LocalVariable[][] cachedLocalVariables;

    /**
     * <code>true</code> if the next visited instruction in this frame must
     * have thrown an exception
     */
	public boolean[] throwsException;
	public ReadMethod removedMethod;
	public Instruction[] lastInstruction;
	public ReadMethod[] method;

    /**
     * <code>true</code> if this frame was aborted abnormally (NOT by a RETURN
     * instruction), or catched an exception. In both cases, the control flow
     * was interrupted, so the stack entry indexes cannot be computed precisely
     * any more.
     */
	public boolean[] interruptedControlFlow;

	public SimulationEnvironment(long[] frames, int[] opStack,
			int[] minOpStack,
			StackEntry[][] cachedStackEntries,
			LocalVariable[][] cachedLocalVariables,
			boolean[] throwsException,
			Instruction[] lastInstruction,
			ReadMethod[] method,
			boolean[] interruptedControlFlow) {
		this.frames = frames;
		this.opStack = opStack;
		this.minOpStack = minOpStack;
		this.cachedStackEntries = cachedStackEntries;
		this.cachedLocalVariables = cachedLocalVariables;
		this.throwsException = throwsException;
		this.lastInstruction = lastInstruction;
		this.method = method;
		this.interruptedControlFlow = interruptedControlFlow;
	}

	public void reallocate(long[] newFrames, int[] newOpStack,
			int[] newMinOpStack,
			StackEntry[][] newCachedStackEntries,
			LocalVariable[][] newCachedLocalVariables,
			boolean[] newThrowsException,
			Instruction[] newLastInstruction,
			ReadMethod[] newMethod,
			boolean[] newInterruptedControlFlow) {
		this.frames = newFrames;
		this.opStack = newOpStack;
		this.minOpStack = newMinOpStack;
		this.cachedStackEntries = newCachedStackEntries;
		this.cachedLocalVariables = newCachedLocalVariables;
		this.throwsException = newThrowsException;
		this.lastInstruction = newLastInstruction;
		this.method = newMethod;
		this.interruptedControlFlow = newInterruptedControlFlow;
	}

	public LocalVariable getLocalVariable(int stackDepth, int varIndex) {
		LocalVariable[] cached = this.cachedLocalVariables[stackDepth];

		if (cached.length <= varIndex) {
			this.cachedLocalVariables[stackDepth] = cached = Arrays.copyOf(cached,
				2*Math.max(Integer.highestOneBit(varIndex), cached.length));
		}

		LocalVariable entry = cached[varIndex];
		if (entry == null)
			cached[varIndex] = entry = new LocalVariable(this.frames[stackDepth], varIndex, this.method[stackDepth]);

		assert (entry.getFrame() == this.frames[stackDepth] && entry.getVarIndex() == varIndex);
		return entry;
	}

	public Collection<LocalVariable> getLocalVariables(int stackDepth, int index, int amount) {
		if (amount <= 1)
			return amount == 0 ? Collections.<LocalVariable>emptySet() : Collections.singleton(getLocalVariable(stackDepth, index));

		LocalVariable[] cached = this.cachedLocalVariables[stackDepth];

		if (cached.length < index + amount) {
			this.cachedLocalVariables[stackDepth] = cached = Arrays.copyOf(cached,
				2*Math.max(Integer.highestOneBit(index+amount), cached.length));
		}

		for (int i = 0; i < amount; ++i) {
			if (cached[index + i] == null)
				cached[index + i] = new LocalVariable(this.frames[stackDepth], index + i, this.method[stackDepth]);
			assert (cached[index + i].getFrame() == this.frames[stackDepth] && cached[index + i].getVarIndex() == index+i);
		}

		return new CachedLocalVariables(cached, index, amount);
	}

	public StackEntry getOpStackEntry(int stackDepth, int stackOffset) {
		if (stackOffset < 0) {
			if (stackOffset < this.minOpStack[stackDepth])
				this.minOpStack[stackDepth] = stackOffset;
			return new StackEntry(this.frames[stackDepth], stackOffset);
		}

		StackEntry[] cached = this.cachedStackEntries[stackDepth];

		if (cached.length <= stackOffset) {
			this.cachedStackEntries[stackDepth] = cached = Arrays.copyOf(cached,
				2*Math.max(Integer.highestOneBit(stackOffset), cached.length));
		}

		StackEntry entry = cached[stackOffset];
		if (entry == null)
			cached[stackOffset] = entry = new StackEntry(this.frames[stackDepth], stackOffset);
		assert (entry.getFrame() == this.frames[stackDepth] && entry.getIndex() == stackOffset);
		return entry;
	}

	public List<StackEntry> getOpStackEntries(int stackDepth, int stackOffset, int amount) {
		if (amount <= 1)
			return amount == 0 ? Collections.<StackEntry>emptyList() : Collections.singletonList(getOpStackEntry(stackDepth, stackOffset));

		if (stackOffset < 0) {
			if (stackOffset < this.minOpStack[stackDepth])
				this.minOpStack[stackDepth] = stackOffset;
			// unoptimized:
			List<StackEntry> list = new ArrayList<StackEntry>();
			for (int i = 0; i < amount; ++i)
				list.add(getOpStackEntry(stackDepth, stackOffset + i));
			return list;
		}

		StackEntry[] cached = this.cachedStackEntries[stackDepth];

		if (cached.length < stackOffset + amount) {
			this.cachedStackEntries[stackDepth] = cached = Arrays.copyOf(cached,
				2*Math.max(Integer.highestOneBit(stackOffset+amount), cached.length));
		}

		for (int i = 0; i < amount; ++i) {
			if (cached[stackOffset + i] == null)
				cached[stackOffset + i] = new StackEntry(this.frames[stackDepth], stackOffset + i);
			assert (cached[stackOffset + i].getFrame() == this.frames[stackDepth] && cached[stackOffset + i].getIndex() == stackOffset+i);
		}

		return new CachedStackEntries(cached, stackOffset, amount);
	}

	public int getAndIncOpStack(int stackDepth) {
		return this.opStack[stackDepth]++;
	}

	public int incAndGetOpStack(int stackDepth) {
		return ++this.opStack[stackDepth];
	}

	public int getAndDecOpStack(int stackDepth) {
		return this.opStack[stackDepth]--;
	}

	public int decAndGetOpStack(int stackDepth) {
		return --this.opStack[stackDepth];
	}

	public int getAndAddOpStack(int stackDepth, int amount) {
		int old = this.opStack[stackDepth];
		this.opStack[stackDepth] += amount;
		return old;
	}

	public int addAndGetOpStack(int stackDepth, int amount) {
		return this.opStack[stackDepth] += amount;
	}

	public int getAndSubOpStack(int stackDepth, int amount) {
		int old = this.opStack[stackDepth];
		this.opStack[stackDepth] -= amount;
		return old;
	}

	public int subAndGetOpStack(int stackDepth, int amount) {
		return this.opStack[stackDepth] -= amount;
	}

	public int getOpStack(int stackDepth) {
		return this.opStack[stackDepth];
	}

	public Collection<Variable> getAllVariables(int stackDepth) {
		return new AllVariables(this.cachedLocalVariables[stackDepth], this.cachedStackEntries[stackDepth]);
	}

}
