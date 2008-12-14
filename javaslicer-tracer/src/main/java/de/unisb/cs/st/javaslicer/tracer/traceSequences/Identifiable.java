package de.unisb.cs.st.javaslicer.tracer.traceSequences;

public interface Identifiable {

    // the name should not occure in any other class
    long __tracing_get_object_id();

}
