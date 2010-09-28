/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer.instrumentation
 *    Class:     JSRInliner
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/instrumentation/JSRInliner.java
 *
 * This file is part of the JavaSlicer tool, developed by Clemens Hammacher at Saarland University.
 * See http://www.st.cs.uni-saarland.de/javaslicer/ for more information.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/ or send a
 * letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 */
package de.unisb.cs.st.javaslicer.tracer.instrumentation;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

public class JSRInliner extends ClassAdapter {

    public JSRInliner(final ClassVisitor cv) {
        super(cv);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
        final MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        return visitor == null ? null : new JSRInlinerAdapter(visitor, access, name, desc, signature, exceptions);
    }

}
