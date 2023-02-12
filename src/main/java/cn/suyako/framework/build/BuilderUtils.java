package cn.suyako.framework.build;

import cn.suyako.framework.core.*;
import cn.suyako.framework.exception.BuildException;
import cn.suyako.framework.topo.Flow;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuilderUtils {
    public static <T extends PipelineContext> Class<T> getPipelineContextType(Pipeline<T> pipeline) throws BuildException {
        WorkHandler<T> workHandler = pipeline.getHead().getInvokeHandlers().get(0);  // a valid pipeline always has at least one user handler
        return getHandlerContextType(workHandler);
    }

    @SuppressWarnings("unchecked")
    public static <T extends PipelineContext> Class<T> getHandlerContextType(WorkHandler<T> workHandler) throws BuildException {
        List<Method> methods = new ArrayList<>(Arrays.asList(workHandler.getClass().getMethods()));
        methods.removeIf(e -> !e.getName().equals("deal")
                || Arrays.stream(e.getParameterTypes()).anyMatch(c -> c == PipelineContext.class));
        if (methods.size() != 1) {
            throw new BuildException(String.format("there should be only one 'deal' method in %s", workHandler.getClass()));
        }
        Method dealMethod = methods.get(0);
        return (Class<T>) dealMethod.getParameterTypes()[0];
    }

    @SuppressWarnings("unchecked")
    public static <U extends PipelineContext, D extends PipelineContext> Class<U> getUpStreamContextType(Flow<U, D> flow) throws BuildException {
        List<Method> methods = new ArrayList<>(Arrays.asList(flow.getClass().getMethods()));
        methods.removeIf(e -> !e.getName().equals("acquire")
                || Arrays.stream(e.getParameterTypes()).anyMatch(c -> c == PipelineContext.class));
        if (methods.size() != 1) {
            throw new BuildException(String.format("there should be only one 'acquire' method in %s", flow.getClass()));
        }
        Method acquireMethod = methods.get(0);
        return (Class<U>) acquireMethod.getParameterTypes()[0];
    }

    @SuppressWarnings("unchecked")
    public static <U extends PipelineContext, D extends PipelineContext> Class<D> getDownStreamContextType(Flow<U, D> flow) throws BuildException {
        Method takeMethod;
        try {
            takeMethod = flow.getClass().getMethod("take");
        } catch (NoSuchMethodException ex) {
            throw new BuildException("no chance exist");
        }
        return (Class<D>) takeMethod.getReturnType();
    }

    @SuppressWarnings("unchecked")
    public static <T extends PipelineContext> Class<T> getProviderContextType(Provider<T> provider) throws BuildException {
        Method nextMethod;
        try {
            nextMethod = provider.getClass().getMethod("next");
        } catch (NoSuchMethodException ex) {
            throw new BuildException("no chance exist");
        }
        return (Class<T>) nextMethod.getReturnType();
    }

    public static Element getUniqueElementByName(Element root, String name) throws BuildException {
        String id = root.getAttributes().getNamedItem("id").getNodeValue();
        NodeList targetNodes = root.getElementsByTagName(name);
        if (targetNodes.getLength() != 1) {
            throw new BuildException(String.format("each %s must has one and only one %s, while %s has %d",
                    root.getNodeName(), name, id, targetNodes.getLength()));
        }
        return (Element) targetNodes.item(0);
    }
}
