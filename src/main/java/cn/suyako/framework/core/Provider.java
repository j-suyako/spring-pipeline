package cn.suyako.framework.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public abstract class Provider<T extends PipelineContext> {
    private String id;
    private Flow<? extends PipelineContext, T> target;

    public abstract T next();

    public abstract boolean hasNext();

    public void execute() {
        Thread thread = new Thread(() -> {
            while (hasNext()) {
                T t = next();
                try {
                    target.put(t);
                } catch (InterruptedException ex) {
                    log.error(String.format("provider %s exit", id), ex);
                    break;
                }
            }
        });
        thread.start();
    }

    public void setTarget(Flow<? extends PipelineContext, T> target) {
        this.target = target;
        target.addProvider(this);
    }
}
