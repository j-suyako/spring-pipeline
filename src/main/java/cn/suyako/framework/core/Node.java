package cn.suyako.framework.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

@Getter
@Setter
@Slf4j
public class Node<T extends PipelineContext> {
    private class Task implements Runnable {
        private final T context;

        public Task(T context) {
            this.context = context;
        }

        @Override
        public void run() {
            try {
                pipeline.run(context);
            } catch (Exception ex) {
                log.error(String.format("node %s run error", id), ex);
            }
            outFlows.acquire(context);
        }
    }
    private String id;
    private Pipeline<T> pipeline;
    private final InFlowList<T> inFlows = new InFlowList<>();
    private final OutFlowList<T> outFlows = new OutFlowList<>();

    public Node(String id) {
        this.id = id;
        inFlows.setDownNode(this);
        outFlows.setUpNode(this);
    }

    public void execute(ThreadPoolExecutor executor) {
        Thread thread = new Thread(() -> {
            while (true) {
                T t;
                try {
                    t = inFlows.take();
                } catch (InterruptedException ex) {
                    log.error(String.format("%s exit", id), ex);
                    break;
                }
                Task task = new Task(t);
                executor.submit(task);
            }
        });
        thread.start();
    }

    public void addOut(Flow<T, ? extends PipelineContext> flow) {
        outFlows.add(flow);
        flow.setUpStream(this);
    }

    public void addIn(Flow<? extends PipelineContext, T> flow) {
        inFlows.add(flow);
        flow.setDownStream(this);
    }

    public void setInFlowPool(String type) {
        type = type.isEmpty() ? "default" : type;
        inFlows.setPool(type);
    }
}
