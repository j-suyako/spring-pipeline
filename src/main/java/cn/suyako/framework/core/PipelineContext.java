package cn.suyako.framework.core;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PipelineContext {
    private final List<WorkHandler<? extends PipelineContext>> handlers = new ArrayList<>();

    public void addHandler(WorkHandler<? extends PipelineContext> handler) {
        handlers.add(handler);
    }
}
