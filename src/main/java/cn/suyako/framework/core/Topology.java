package cn.suyako.framework.core;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Topology {
    private String id;
    private ThreadPoolExecutor executor;
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

    public void addProvider(Provider<? extends PipelineContext> provider) {
        providers.add(provider);
    }

    public void execute() {
        for (Provider<?> provider : providers) {
            provider.execute();
        }
        for (Node<?> node : nodes) {
            node.execute(executor);
        }
    }
}
