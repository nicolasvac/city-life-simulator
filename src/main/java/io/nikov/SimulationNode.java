package io.nikov;

import java.util.Objects;

public abstract class SimulationNode {
    public final String key;

    protected SimulationNode(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimulationNode that)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
