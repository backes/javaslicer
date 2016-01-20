package de.unisb.cs.st.javaslicer;

public interface SliceEntryFilter {
    boolean keepEntry(SliceEntry entry);
}