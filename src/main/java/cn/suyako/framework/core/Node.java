package cn.suyako.framework.core;

import cn.suyako.framework.exception.RunningException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
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
            for (Flow<T, ? extends PipelineContext> flow : outFlows) {
                try {
                    flow.acquire(context);
                } catch (Exception ex) {
                    log.error(String.format("flow %s acquire context error from upstream node %s", flow.getId(), id));
                }
            }
        }
    }
    private String id;
    private Pipeline<T> pipeline;
    private Flow<? extends PipelineContext, T> inFlow;
    private final List<Flow<T, ? extends PipelineContext>> outFlows = new ArrayList<>();

    public Node(String id) {
        this.id = id;
    }

    public void execute(ThreadPoolExecutor executor) {
        Thread thread = new Thread(() -> {
            while (true) {
                T t;
                try {
                    t = inFlow.take();
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

    public void setInFlow(Flow<? extends PipelineContext, T> inFlow) {
        this.inFlow = inFlow;
        inFlow.setDownStream(this);
    }
}
