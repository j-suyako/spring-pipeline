package cn.suyako.framework.topo;

import cn.suyako.framework.core.PipelineContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
public class OutFlowList<U extends PipelineContext> {
    private final List<Flow<U, ? extends PipelineContext>> flows = new ArrayList<>();
    private Node<U> upNode;

    public void add(Flow<U, ? extends PipelineContext> flow) {
        flows.add(flow);
    }

    public void acquire(U context) {
        for (Flow<U, ? extends PipelineContext> flow : flows) {
            try {
                flow.acquire(context);
            } catch (Exception ex) {
                log.error(String.format("flow %s acquire context error from upstream node %s", flow.getId(), upNode.getId()));
            }
        }
    }
}
