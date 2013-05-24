/** License information:
 *    Component: javaslicer-traceReader
 *    Package:   de.unisb.cs.st.javaslicer.traceResult
 *    Class:     PrintUniqueUntracedMethods
 *    Filename:  javaslicer-traceReader/src/main/java/de/unisb/cs/st/javaslicer/traceResult/PrintUniqueUntracedMethods.java
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
package de.unisb.cs.st.javaslicer.traceResult;

import java.util.HashSet;

import org.objectweb.asm.Type;

import de.hammacher.util.maps.IntegerMap;
import de.unisb.cs.st.javaslicer.common.classRepresentation.InstructionInstance;
import de.unisb.cs.st.javaslicer.common.classRepresentation.instructions.MethodInvocationInstruction;


public class PrintUniqueUntracedMethods implements UntracedCallVisitor {

    private static class MethodSignature {
        String internalClassName, methodName, methodDesc;

        public MethodSignature(String internalClassName, String methodName,
                String methodDesc) {
            assert (internalClassName != null && methodName != null && methodDesc != null);
            this.internalClassName = internalClassName;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.internalClassName.hashCode();
            result = prime * result + this.methodDesc.hashCode();
            result = prime * result + this.methodName.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MethodSignature other = (MethodSignature) obj;
            if (!this.internalClassName.equals(other.internalClassName))
                return false;
            if (!this.methodDesc.equals(other.methodDesc))
                return false;
            if (!this.methodName.equals(other.methodName))
                return false;
            return true;
        }

    }

    private final IntegerMap<Object> reportedCalls = new IntegerMap<Object>();
    private final HashSet<MethodSignature> reportedMethods = new HashSet<MethodSignature>();

    @Override
	public void visitUntracedMethodCall(InstructionInstance instrInstance)
            throws InterruptedException {
        MethodInvocationInstruction mtdInv = (MethodInvocationInstruction)instrInstance.getInstruction();
        if (this.reportedCalls.containsKey(mtdInv.getIndex()))
            return;
        this.reportedCalls.put(mtdInv.getIndex(), null);

        MethodSignature sig = new MethodSignature(mtdInv.getInvokedInternalClassName(),
            mtdInv.getInvokedMethodName(), mtdInv.getInvokedMethodDesc());
        if (this.reportedMethods.contains(sig))
            return;
        this.reportedMethods.add(sig);
        System.out.format("\rWarning: Untraced method %s.%s %s\n",
            Type.getObjectType(sig.internalClassName).getClassName(),
            sig.methodName,
            sig.methodDesc);
    }

}
