package de.unisb.cs.st.javaslicer;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction;

public class SliceEntry {

    public final String method;
    public final String line;
    public final String instr;

    public SliceEntry(String method, String line, String instr) {
        this.method = method;
        this.line = line;
        this.instr = instr;
    }

    public SliceEntry(Instruction instr) {
        this(instr.getMethod().getReadClass().getName()+"."+instr.getMethod().getName(),
            Integer.toString(instr.getLineNumber()),
            instr.toString());
    }

    @Override
    public String toString() {
        return this.method+":"+this.line+" "+this.instr;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.instr.hashCode();
        result = prime * result + this.line.hashCode();
        result = prime * result + this.method.hashCode();
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
        SliceEntry other = (SliceEntry) obj;
        if (!this.instr.equals(other.instr))
            return false;
        if (!this.line.equals(other.line))
            return false;
        if (!this.method.equals(other.method))
            return false;
        return true;
    }

}