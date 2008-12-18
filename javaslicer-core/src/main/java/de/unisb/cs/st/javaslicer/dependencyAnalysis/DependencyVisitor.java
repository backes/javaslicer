package de.unisb.cs.st.javaslicer.dependencyAnalysis;

import de.unisb.cs.st.javaslicer.common.classRepresentation.Instruction.Instance;
import de.unisb.cs.st.javaslicer.variables.Variable;

public interface DependencyVisitor {

	enum DataDependencyType {
		READ_AFTER_WRITE,
		WRITE_AFTER_READ,
	}

	void visitDataDependency(Instance from, Instance to,
			Variable var, DataDependencyType type);

	void visitControlDependency(Instance from, Instance to);

	void visitInstructionExecution(Instance instance);

}
