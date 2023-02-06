package cn.suyako.framework.core;

import cn.suyako.framework.basic.HeadHandler;
import cn.suyako.framework.basic.TailHandler;
import cn.suyako.framework.exception.BuildException;
import cn.suyako.framework.exception.RunningException;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Pipeline<T extends PipelineContext> {
    private String id;
    private WorkHandler<T> head;
    private WorkHandler<T> tail;
    private WorkHandler<T> curr;
    private final Map<String, WorkHandler<T>> handlerResources = new HashMap<>();

    public Pipeline(String id) {
        this.id = id;
        head = new HeadHandler<>();
        tail = new TailHandler<>();
        curr = head;
        head.connect(tail);
    }

    public Pipeline(String id, WorkHandler<T> head, WorkHandler<T> tail) {
        this.id = id;
        this.head = head;
        this.tail = tail;
        curr = this.head;
        this.head.connect(this.tail);
    }

    public void addLast(WorkHandler<T> handler) throws BuildException {
        if (handlerResources.containsKey(handler.getId())) {
            throw new BuildException(String.format("handler name %s is duplicated in %s pipeline", handler.getId(), id));
        }
        curr.connect(handler);
        curr.deconnect(tail);
        curr = handler;
        curr.connect(tail);
        handlerResources.put(curr.getId(), curr);
    }

    public void go(int headOrTail) {
        if (headOrTail == 0)
            curr = head;
        else if (headOrTail == -1)
            curr = tail;
    }

    public void run(T t) throws RunningException {
        WorkHandler<T> pointer = head;
        do {
            if (!pointer.isAdapt(t)) {
                continue;
            }
            t.addHandler(pointer);
            pointer.deal(t);
            if (!pointer.isValid(t)) {
                break;
            }
        } while ((pointer = pointer.invoke(t)) != null);
    }

    public WorkHandler<T> getHandler(String id) {
        return handlerResources.get(id);
    }

    public int getHandlerSize() {
        return handlerResources.size();
    }
}
