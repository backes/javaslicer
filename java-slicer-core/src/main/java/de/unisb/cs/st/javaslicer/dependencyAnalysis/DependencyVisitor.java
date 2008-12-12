package de.unisb.cs.st.javaslicer.dependencyAnalysis;

import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction;
import de.unisb.cs.st.javaslicer.tracer.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.variables.Variable;

public interface DependencyVisitor {

	enum DataDependencyType {
		READ_AFTER_WRITE,
		WRITE_AFTER_READ,
	}

	void visitDataDependency(Instruction from, Instruction to,
			Variable var, DataDependencyType type);

	void visitControlDependency(Instruction from, Instruction to);

	void visitInstructionExecution(Instance instance);

}
