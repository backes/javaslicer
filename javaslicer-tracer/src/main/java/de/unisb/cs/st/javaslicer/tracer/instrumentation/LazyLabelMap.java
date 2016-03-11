package de.unisb.cs.st.javaslicer.tracer.instrumentation;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.LabelNode;


public class LazyLabelMap extends AbstractMap<LabelNode, LabelNode> {

    private final Map<LabelNode, LabelNode> map;

    public LazyLabelMap() {
        this.map = new HashMap<LabelNode, LabelNode>();
    }

    @Override
    public Set<java.util.Map.Entry<LabelNode, LabelNode>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public LabelNode get(Object label) {
        if (!(label instanceof LabelNode))
            return null;
        LabelNode newLabel = this.map.get(label);
        if (newLabel == null)
            this.map.put((LabelNode)label, newLabel = new LabelNode());
        return newLabel;
    }
}
