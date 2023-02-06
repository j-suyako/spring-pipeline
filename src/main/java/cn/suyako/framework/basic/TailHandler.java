package cn.suyako.framework.basic;

import cn.suyako.framework.core.PipelineContext;
import cn.suyako.framework.core.WorkHandler;

public class TailHandler<T extends PipelineContext> extends WorkHandler<T> {
    @Override
    public boolean isAdapt(T t) {
        return false;
    }

    @Override
    public void deal(T t) {

    }

    @Override
    public WorkHandler<T> invoke(T t) {
        return null;
    }
}
