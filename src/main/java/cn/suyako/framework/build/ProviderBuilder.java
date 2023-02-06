package cn.suyako.framework.build;

import cn.suyako.framework.basic.VirtualFlow;
import cn.suyako.framework.core.Flow;
import cn.suyako.framework.core.PipelineContext;
import cn.suyako.framework.core.Provider;
import cn.suyako.framework.exception.BuildException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.util.Map;

public class ProviderBuilder {
    private final Map<String, Flow<? extends PipelineContext, ? extends PipelineContext>> flowsResources;

    public ProviderBuilder(Map<String, Flow<? extends PipelineContext, ? extends PipelineContext>> flowsResources) {
        this.flowsResources = flowsResources;
    }

    public Provider<? extends PipelineContext> build(Element providerNode) throws BuildException {
        NamedNodeMap attributes = providerNode.getAttributes();
        String clazz = attributes.getNamedItem("class").getNodeValue();
        String id = attributes.getNamedItem("id").getNodeValue();
        String target = attributes.getNamedItem("target").getNodeValue();
        Object instance;
        try {
            Class<?> klass = Class.forName(clazz);
            instance = klass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new BuildException(String.format("failed to instantiate %s", clazz), ex);
        }
        if (!(instance instanceof Provider)) {
            throw new BuildException(String.format("%s is not a subclass of provider", clazz));
        }
        Provider<? extends PipelineContext> provider = (Provider<? extends PipelineContext>) instance;
        Flow<? extends PipelineContext, ? extends PipelineContext> targetFlow = flowsResources.get(target);
        provider.setId(id);
        helpRegister(provider, targetFlow);
        return provider;
    }

    @SuppressWarnings("unchecked")
    private <T extends PipelineContext> void helpRegister(Provider<T> provider, Flow<? extends PipelineContext, ? extends PipelineContext> flow) throws BuildException{
        Class<T> providerContextType = BuilderUtils.getProviderContextType(provider);
        Class<? extends PipelineContext> flowDownContextType = BuilderUtils.getDownStreamContextType(flow);
        if (flow.getClass() != VirtualFlow.class && providerContextType != flowDownContextType) {
            throw new BuildException(
                    String.format("context type of %s provider(%s) is not equal to " +
                            "the downstream context type of %s flow(%s)", provider.getId(), providerContextType,
                            flow.getId(), flowDownContextType));
        }
        Flow<? extends PipelineContext, T> temp = (Flow<? extends PipelineContext, T>) flow;
        provider.setTarget(temp);
    }
}
