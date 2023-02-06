package cn.suyako.framework.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

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
        pool = new LinkedBlockingQueue<>();
    }

    public D take() throws InterruptedException {
        return pool.take();
    }

    public void put(D d) throws InterruptedException {
        pool.put(d);
    }

    public abstract void acquire(U u);

    public void setPool(String type) {
        switch (type) {
            case "default":
                this.pool = new LinkedBlockingQueue<>();
                break;
            case "priority":
                this.pool = new PriorityBlockingQueue<>();
                break;
            default:
                log.warn(String.format("%s pool parameter is not support", type));
                this.pool = new LinkedBlockingQueue<>();
                break;
        }
    }

    public void addProvider(Provider<D> provider) {
        registerProviders.put(provider.getId(), provider);
    }
}
