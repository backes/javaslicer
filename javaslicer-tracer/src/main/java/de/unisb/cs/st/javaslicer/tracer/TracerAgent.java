/** License information:
 *    Component: javaslicer-tracer
 *    Package:   de.unisb.cs.st.javaslicer.tracer
 *    Class:     TracerAgent
 *    Filename:  javaslicer-tracer/src/main/java/de/unisb/cs/st/javaslicer/tracer/TracerAgent.java
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
package de.unisb.cs.st.javaslicer.tracer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;

import de.unisb.cs.st.javaslicer.common.exceptions.TracerException;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.TraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.gzip.GZipTraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.sequitur.SequiturTraceSequenceFactory;
import de.unisb.cs.st.javaslicer.tracer.traceSequences.uncompressed.UncompressedTraceSequenceFactory;

public class TracerAgent {

    public static class WriteTracefileThread extends UntracedThread {

        private final Tracer tracer;

        public WriteTracefileThread(final Tracer tracer) {
            super("tracing finisher");
            this.tracer = tracer;
        }

        @Override
        public void run() {
            try {
                this.tracer.finish();
            } catch (final IOException e) {
                this.tracer.error(e);
            }
            this.tracer.printFinalUserInfo();
        }

    }

    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            // find the name of the jar file
            URL url = ClassLoader.getSystemResource(TracerAgent.class.getName().replace('.', '/') + ".class");
            String urlFile = url == null ? "" : url.getFile();
            int bangIndex = urlFile.indexOf('!');
            if (url == null || !"jar".equals(url.getProtocol()) || !urlFile.startsWith("file:") || bangIndex == -1) {
                System.err.println("ERROR: Can't find jar file of the tracer. Expected pattern \"jar:file:<path-to-jar>!<classname>.class\" in url: " + url);
                System.exit(1);
            }

            File tracerJarFile = new File(urlFile.substring(5, bangIndex));
            if (!"tracer.jar".equals(tracerJarFile.getName())) {
                System.err.println("ERROR: The Tracer has to be loaded from a file named 'tracer.jar'.");
                System.err.println("       Don't rename it, because exactly this filename is added to the boot class path from the MANIFEST.MF.");
                System.err.println("       It seems that the file was renamed to '" + tracerJarFile.getName() + "'.");
                System.exit(1);
            }

            /* Adding the tracer.jar to the bootstrap path is done instead in the MANIFEST.
             * This way, unfortunately, you cannot rename the tracer.jar.
             * If appending it to the bootstrapclasspath here, then some classes have already been loaded by another classloader,
             * which leads to exceptions, at least on mac os:
             * > java.lang.LinkageError: loader constraint violation: when resolving method "..." the class loader
             * > (instance of sun/misc/Launcher$AppClassLoader) of the current class, de/unisb/cs/st/javaslicer/tracer/TracerAgent,
             * > and the class loader (instance of <bootloader>) for resolved class, de/unisb/cs/st/javaslicer/tracer/Tracer,
             * > have different Class objects for the type de/unisb/cs/st/javaslicer/tracer/traceSequences/TraceSequenceFactory
             * > used in the signature
             */
            /*
            // add my own jar file to the bootstrap path:
            inst.appendToBootstrapClassLoaderSearch(new JarFile(urlFile.substring(5, bangIndex), false));
            */

            String logFilename = null;
            final String[] args = agentArgs == null || agentArgs.length() == 0 ? new String[0] : agentArgs.split(",");

            boolean debug = false;
            boolean check = false;
            TraceSequenceFactory seqFac = null;

            for (final String arg : args) {
                final int colonPos = arg.indexOf(':');
                final String key = colonPos == -1 ? arg : arg.substring(0, colonPos);
                final String value = colonPos == -1 ? null : arg.substring(colonPos+1);

                if ("help".equalsIgnoreCase(key) || "h".equalsIgnoreCase(key)) {
                	String format = "%20s     %s%n";
                    System.out.println("Use the java agent this way: java -javaagent:tracer.jar=<option[:value]>,<option[:value]>,... -jar program.jar <programoptions>");
                	System.out.println("These are the available agent options:");
                	System.out.format(format, "check", "(true/false): do check the instrumented bytecode using ASM validators");
                	System.out.format(format, "compression", "(none/gzip/sequitur): select the compression algorithm for the trace file");
                	System.out.format(format, "debug", "(true/false): do additional checks and verbose output");
                	System.out.format(format, "help", "print this help");
                	System.out.format(format, "tracefile", "the output destination for the trace file");
                } else if ("logfile".equalsIgnoreCase(key) || "tracefile".equalsIgnoreCase(key)) {
                    if (value == null) {
                        System.err.println("ERROR: expecting value for \"logfile\" argument");
                        System.exit(1);
                    }
                    logFilename = value;
                } else if ("debug".equalsIgnoreCase(key)) {
                    if (value == null || "true".equalsIgnoreCase(value)) {
                        debug = true;
                    } else if ("false".equalsIgnoreCase(value)) {
                        debug = false;
                    } else {
                        System.err.println("ERROR: illegal value for \"debug\" argument: \"" + value + "\"");
                        System.exit(1);
                    }
                } else if ("check".equalsIgnoreCase(key)) {
                    if (value == null || "true".equalsIgnoreCase(value)) {
                        check = true;
                    } else if ("false".equalsIgnoreCase(value)) {
                        check = false;
                    } else {
                        System.err.println("ERROR: illegal value for \"check\" argument: \"" + value + "\"");
                        System.exit(1);
                    }
                } else if ("compression".equalsIgnoreCase(key)) {
                    if ("none".equalsIgnoreCase(value) || "uncompressed".equalsIgnoreCase(value)) {
                        seqFac = new UncompressedTraceSequenceFactory();
                    } else if ("gzip".equalsIgnoreCase(value)) {
                        seqFac = new GZipTraceSequenceFactory();
                    } else if ("sequitur".equalsIgnoreCase(value)) {
                        seqFac = new SequiturTraceSequenceFactory();
                    } else {
                        System.err.println("Unknown compression method: " + value);
                        System.exit(1);
                    }
                } else {
                    System.err.println("Unknown argument: " + key);
                    System.exit(1);
                }
            }

            if (logFilename == null) {
                System.err.println("ERROR: no logfile specified");
                System.exit(1);
            }

            final File logFile = new File(logFilename);
            if (logFile.exists() && !logFile.canWrite()) {
                System.err.println("ERROR: Cannot write logfile \"" + logFile.getAbsolutePath() + "\"");
                System.exit(1);
            }

            if (seqFac == null)
                seqFac = new UncompressedTraceSequenceFactory();

            try {
                Tracer.newInstance(logFile, debug, check, seqFac, inst);
            } catch (final FileNotFoundException e) {
                System.err.println("ERROR: cannot create trace file: " + e.getMessage());
            }
            final Tracer tracer = Tracer.getInstance();
            try {
                tracer.add(inst, true);
            } catch (final TracerException e) {
                System.err.println("ERROR: could not add instrumenting agent:");
                e.printStackTrace(System.err);
                System.exit(1);
            }
            final ThreadTracer tt = tracer.getThreadTracer();
            tt.pauseTracing();
            Runtime.getRuntime().addShutdownHook(new WriteTracefileThread(tracer));
            tt.resumeTracing();
        } catch (final Throwable t) {
            System.err.println("ERROR in premain method:");
            t.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
