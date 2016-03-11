package de.unisb.cs.st.javaslicer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;



public class IntegrationTest {

    private void createTrace(Class<?> classUnderTest, String[] args,
            File traceFile) throws IOException {

        File javaPath = new File(System.getProperty("java.home"));
        javaPath = new File(javaPath, "bin");
        javaPath = new File(javaPath, "java" +
          (System.getProperty("os.name").toLowerCase().contains("windows") ? ".exe" : ""));

        File globalAssemblyDir;
        try {
            globalAssemblyDir = new File(this.getClass().getResource("/").toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        globalAssemblyDir = globalAssemblyDir.getParentFile();
        File tracerJarFile = null;
        for (int i = 0; ; ++i) {
            tracerJarFile = new File(new File(globalAssemblyDir, "assembly"), "tracer.jar");
            if (tracerJarFile.isFile())
                break;
            globalAssemblyDir = globalAssemblyDir.getParentFile();
            assertTrue("no assembly dir found", i < 5 && globalAssemblyDir != null);
        }

        List<String> command = new ArrayList<>();
        command.add(javaPath.getAbsolutePath());
        command.add("-javaagent:"+tracerJarFile.getAbsolutePath()+
            "=tracefile:"+traceFile.getAbsolutePath());
        command.add("-cp");
        command.add(System.getProperty("java.class.path"));
        command.add(classUnderTest.getCanonicalName());
        command.addAll(Arrays.asList(args));

        /*
        StringBuilder commandSb = new StringBuilder();
        for (String s : command)
            commandSb.append(commandSb.length() == 0 ? "" : " ").append(s);
        System.out.println("Executing: " + commandSb);
        */

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();

        Process proc = pb.start();
        int ret = -1;
        try {
            ret = proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("interrupted");
        } finally {
            proc.destroyForcibly();
        }
        assertEquals("tracer failed", 0, ret);
    }

    private void checkInnerClassLines(File traceFile, Class<?> classUnderTest, String criterion,
            int[] expectedLines) throws IOException {
        String className = classUnderTest.getCanonicalName();

        List<Instruction> instSlice = Utils.getSlice(traceFile, "main", criterion);

        Set<Integer> seenLines = new HashSet<>();
        for (Instruction insn : instSlice) {
            if (insn.getMethod().getReadClass().getName().equals(className))
                seenLines.add(Integer.valueOf(insn.getLineNumber()));
        }

        int[] seenLinesArr = new int[seenLines.size()];
        int i = 0;
        for (Integer i0 : seenLines)
            seenLinesArr[i++] = i0;
        Arrays.sort(seenLinesArr);
        Arrays.sort(expectedLines);

        if (!Arrays.equals(seenLinesArr, expectedLines)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Expected " + expectedLines.length + " lines, got " + seenLinesArr.length);
            for (int line : expectedLines)
                if (Arrays.binarySearch(seenLinesArr, line) < 0)
                    sb.append(System.getProperty("line.separator")).
                       append("- line ").append(line);
            for (int line : seenLinesArr)
                if (Arrays.binarySearch(expectedLines, line) < 0)
                    sb.append(System.getProperty("line.separator")).
                       append("+ line ").append(line);
            fail(sb.toString());
        }
    }

    protected void checkInnerClassLines(Class<?> classUnderTest,
            String[] args, String criterion, int[] expectedLines) throws IOException {

        File traceFile = File.createTempFile("javaslicer-test-", ".trace");

        createTrace(classUnderTest, args, traceFile);
        checkInnerClassLines(traceFile, classUnderTest, criterion, expectedLines);
    }

}
