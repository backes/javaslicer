package de.unisb.cs.st.javaslicer.tracer;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

import de.unisb.cs.st.javaslicer.tracer.exceptions.TracerException;

public class TracerAgent {

    public static class WriteTracefileThread extends Thread {

        private final Tracer tracer;

        public WriteTracefileThread(final Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        public void run() {
            try {
                this.tracer.finish();
            } catch (final IOException e) {
                Tracer.error(e);
            }
            Tracer.printFinalUserInfo();
        }

    }

    public static void premain(final String agentArgs, final Instrumentation inst) {
        try {
            String logFilename = null;
            final String[] args = agentArgs == null || agentArgs.length() == 0 ? new String[0] : agentArgs.split(",");
            for (final String arg : args) {
                final String[] parts = arg.split(":");
                if (parts.length > 2) {
                    System.err.println("ERROR: unknown argument: \"" + arg + "\"");
                    System.exit(1);
                }
                final String key = parts[0];
                final String value = parts.length < 2 ? null : parts[1];

                if ("logfile".equalsIgnoreCase(key)) {
                    if (value == null) {
                        System.err.println("ERROR: expecting value for \"logfile\" argument");
                        System.exit(1);
                    }
                    logFilename = value;
                } else if ("debug".equalsIgnoreCase(key)) {
                    if (value == null || "true".equalsIgnoreCase(value)) {
                        Tracer.debug = true;
                    } else if ("false".equalsIgnoreCase(value)) {
                        Tracer.debug = false;
                    } else {
                        System.err.println("ERROR: illegal value for \"debug\" argument: \"" + value + "\"");
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

            Tracer.newInstance(logFile);
            final Tracer tracer = Tracer.getInstance();
            try {
                tracer.add(inst, true);
            } catch (final TracerException e) {
                System.err.println("ERROR: could not add instrumenting agent:");
                e.printStackTrace(System.err);
                System.exit(1);
            }
            Runtime.getRuntime().addShutdownHook(new WriteTracefileThread(tracer));
        } catch (final Throwable t) {
            System.err.println("ERROR in premain method:");
            t.printStackTrace(System.err);
        }
    }

}
