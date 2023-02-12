package cn.suyako.framework.build;

import cn.suyako.framework.topo.Flow;
import cn.suyako.framework.core.PipelineContext;
import cn.suyako.framework.exception.BuildException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;


@Component
public class FlowBuilder {
    public Flow<? extends PipelineContext, ? extends PipelineContext> build(Element flowNode) throws BuildException {
        NamedNodeMap attributes = flowNode.getAttributes();
        String clazz = attributes.getNamedItem("class").getNodeValue();
        String id = attributes.getNamedItem("id").getNodeValue();
        Object instance;
        try {
            Class<?> klass = Class.forName(clazz);
            instance = klass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new BuildException(String.format("failed to instantiate %s", clazz), ex);
        }
        if (!(instance instanceof Flow)) {
            throw new BuildException(String.format("%s is not a subclass of flow", clazz));
        }
        Flow<? extends PipelineContext, ? extends PipelineContext> flow =
                (Flow<? extends PipelineContext, ? extends PipelineContext>) instance;
        flow.setId(id);
        return flow;
    }
}
