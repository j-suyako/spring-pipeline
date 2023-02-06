package cn.suyako.framework.basic;

import cn.suyako.framework.core.Flow;
import cn.suyako.framework.core.PipelineContext;

public class VirtualFlow<U extends PipelineContext, D extends PipelineContext> extends Flow<U, D> {
    @Override
    public void acquire(U u) {

    }
}
