package cn.suyako.framework.core;

import cn.suyako.framework.exception.RunningException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class WorkHandler<T extends PipelineContext> {
    private String id;
    private final List<WorkHandler<T>> invokeHandlers = new ArrayList<>();

    public WorkHandler() {
        id = this.getClass().getName();
    }

    public WorkHandler(String id) {
        this.id = id;
    }

    public void connect(WorkHandler<T> handler) {
        invokeHandlers.add(handler);
    }

    public void deconnect(WorkHandler<T> handler) {
        invokeHandlers.remove(handler);
    }

    public boolean isAdapt(T t) {return true;}

    public abstract void deal(T t) throws RunningException;

    public boolean isValid(T t) {return true;}

    public WorkHandler<T> invoke(T t) throws RunningException {
        return invokeHandlers.get(0);
    }

    public WorkHandler<T> getInvokeHandler(String id) throws RunningException {
        for (WorkHandler<T> workHandler : invokeHandlers) {
            if (workHandler.getId().equals(id)) {
                return workHandler;
            }
        }
        throw new RunningException(String.format("%s handler not connect with %s handler", id, this.id));
    }

    @Override
    public String toString() {
        return id;
    }

}
