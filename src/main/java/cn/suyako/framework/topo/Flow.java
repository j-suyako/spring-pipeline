package cn.suyako.framework.topo;

import cn.suyako.framework.core.PipelineContext;
import cn.suyako.framework.core.Provider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

@Slf4j
@Getter
@Setter
public abstract class Flow<U extends PipelineContext, D extends PipelineContext> {
    private String id;
    private BlockingQueue<D> pool;
    private Node<U> upStream;
    private Node<D> downStream;
    private final Map<String, Provider<D>> registerProviders = new HashMap<>();

    public Flow() {
        id = this.getClass().getName();
    }

    public D take() throws InterruptedException {
        return pool.take();
    }

    public void put(D d) throws InterruptedException {
        pool.put(d);
    }

    public abstract void acquire(U u);

    public void addProvider(Provider<D> provider) {
        registerProviders.put(provider.getId(), provider);
    }
}
