package cn.suyako.framework.build;

import cn.suyako.framework.core.*;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("builderResources")
@Getter
public class BuilderResources {
    private final Map<String, Pipeline<? extends PipelineContext>> pipelineResources = new HashMap<>();
    private final Map<String, Flow<? extends PipelineContext, ? extends PipelineContext>> flowResources = new HashMap<>();
    private final Map<String, Provider<? extends PipelineContext>> providerResources = new HashMap<>();
    private final Map<String, Node<? extends PipelineContext>> nodeResources = new HashMap<>();

    public void put(String id, Pipeline<? extends PipelineContext> pipeline) {
        pipelineResources.put(id, pipeline);
    }

    public void put(String id, Flow<? extends PipelineContext, ? extends PipelineContext> flow) {
        flowResources.put(id, flow);
    }

    public void put(String id, Provider<? extends PipelineContext> provider) {
        providerResources.put(id, provider);
    }

    public void put(String id, Node<? extends PipelineContext> node) {
        nodeResources.put(id, node);
    }
}
