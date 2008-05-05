package de.unisb.cs.st.javaslicer.tracer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.instrument.Instrumentation;

public class LogFileAgent {

    public static class WriteLogfileThread extends Thread {

        private final Tracer tracer;
        private final File logFile;
        private final boolean debug;

        public WriteLogfileThread(final Tracer tracer, final File logFile, final boolean debug) {
            this.tracer = tracer;
            this.logFile = logFile;
            this.debug = debug;
        }

        @Override
        public void run() {
            if (debug)
                System.out.println("DEBUG: writing trace to output file: " + logFile.getAbsolutePath());

            ObjectOutputStream str = null;
            try {
                str = new ObjectOutputStream(new FileOutputStream(logFile));
                str.writeObject(tracer);
            } catch (final IOException e) {
                System.err.println("ERROR: can not write to \"" + logFile.getAbsolutePath() + "\": " + e);
                System.exit(1);
            } finally {
                try {
                    if (str != null)
                        str.close();
                } catch (final IOException e) {
                    System.err.println("ERROR: can not close file \"" + logFile.getAbsolutePath() + "\": " + e);
                    System.exit(1);
                }
            }

            if (debug)
                System.out.println("DEBUG: trace written successfully");
        }

    }

    public static void premain(final String agentArgs, final Instrumentation inst) {
        String logFilename = null;
        boolean debug = false;
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
                    debug = true;
                } else if ("false".equalsIgnoreCase(value)) {
                    debug = false;
                } else {
                    System.err.println("ERROR: illegal value for \"debug\" argument: \"" + value + "\"");
                    System.exit(1);
                }
            }
        }

        if (logFilename == null) {
            System.err.println("ERROR: no logfile specified");
            System.exit(1);
        }

        final File logFile = new File(logFilename);
        if (logFile.exists() && (!logFile.canWrite() || !logFile.delete())) {
            System.err.println("ERROR: Cannot write logfile \"" + logFile.getAbsolutePath() + "\"");
            System.exit(1);
        }

        final Tracer tracer = new Tracer();
        inst.addTransformer(tracer);
        Runtime.getRuntime().addShutdownHook(new WriteLogfileThread(tracer, logFile, debug));
    }

}
