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

    public abstract T next() throws Exception;

    public abstract boolean hasNext();

    public void execute() {
        Thread thread = new Thread(() -> {
            while (hasNext()) {
                T t;
                try {
                    t = next();
                } catch (Exception ex) {
                    log.error(String.format("provider %s exit when retrieve elements", id), ex);
                    break;
                }
                try {
                    target.put(t);
                } catch (InterruptedException ex) {
                    log.error(String.format("provider %s exit when put element to flow", id), ex);
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
