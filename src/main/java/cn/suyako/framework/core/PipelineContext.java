package cn.suyako.framework.core;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class PipelineContext {
    private final CountDownLatch count = new CountDownLatch(1);
    private final List<WorkHandler<? extends PipelineContext>> handlers = new ArrayList<>();

    public void addHandler(WorkHandler<? extends PipelineContext> handler) {
        handlers.add(handler);
    }

    public void release() {
        count.countDown();
    }

    public boolean await(Long timeout) throws InterruptedException {
        return count.await(timeout, TimeUnit.SECONDS);
    }
}
