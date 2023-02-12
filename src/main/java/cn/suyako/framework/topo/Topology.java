package cn.suyako.framework.topo;

import cn.suyako.framework.core.PipelineContext;
import cn.suyako.framework.core.Provider;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Topology {
    private String id;
    private ThreadPoolExecutor executor;
    private boolean executed = false;
    private final List<Node<? extends PipelineContext>> nodes = new ArrayList<>();
    private final List<Provider<? extends PipelineContext>> providers = new ArrayList<>();

    public Topology(String id, int threads) {
        this.id = id;
        executor = new ThreadPoolExecutor(threads, threads, Long.MAX_VALUE,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public void addNode(Node<? extends PipelineContext> node) {
        nodes.add(node);
    }

    public void addNodes(Collection<Node<? extends PipelineContext>> nodes) {
        this.nodes.addAll(nodes);
    }

    public void addProvider(Provider<? extends PipelineContext> provider) {
        providers.add(provider);
    }

    public void addProviders(Collection<Provider<? extends PipelineContext>> providers) {
        this.providers.addAll(providers);
    }

    public synchronized void execute() {
        if (executed)
            return;
        for (Provider<?> provider : providers) {
            provider.execute();
        }
        for (Node<?> node : nodes) {
            node.execute(executor);
        }
        executed = true;
    }
}
