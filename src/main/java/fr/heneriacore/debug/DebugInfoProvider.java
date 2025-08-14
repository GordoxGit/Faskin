package fr.heneriacore.debug;

import fr.heneriacore.metrics.MetricsCollector;

import java.util.Map;

/**
 * Provides debug information about network metrics.
 */
public class DebugInfoProvider {
    private final MetricsCollector metrics;

    public DebugInfoProvider(MetricsCollector metrics) {
        this.metrics = metrics;
    }

    public Map<String, Object> getDebugInfo() {
        return metrics.snapshot();
    }
}
