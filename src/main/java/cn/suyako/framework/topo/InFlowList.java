package cn.suyako.framework.topo;

import cn.suyako.framework.core.PipelineContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

@Getter
@Setter
@Slf4j
public class InFlowList<D extends PipelineContext> {
    private BlockingQueue<D> pool;
    private final List<Flow<? extends PipelineContext, D>> flows = new ArrayList<>();
    private Node<D> downNode;

    public D take() throws InterruptedException {
        return pool.take();
    }

    public void add(Flow<? extends PipelineContext, D> flow) {
        flows.add(flow);
        flow.setPool(pool);
    }

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
}
