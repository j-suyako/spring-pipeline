package cn.suyako.framework.build;

import cn.suyako.framework.basic.VirtualFlow;
import cn.suyako.framework.core.*;
import cn.suyako.framework.exception.BuildException;
import cn.suyako.framework.topo.Flow;
import cn.suyako.framework.topo.Node;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Map;

@Component
public class NodeBuilder {
    @Value("#{builderResources.pipelineResources}")
    private Map<String, Pipeline<? extends PipelineContext>> pipelineResources;
    @Value("#{builderResources.flowResources}")
    private Map<String, Flow<? extends PipelineContext, ? extends PipelineContext>> flowResources;

    public Node<? extends PipelineContext> build(Element nodeNode) throws BuildException {
        String context = nodeNode.getAttributes().getNamedItem("context").getNodeValue();
        String id = nodeNode.getAttributes().getNamedItem("id").getNodeValue();
        Class<? extends PipelineContext> klass;
        try {
            klass = Class.forName(context).asSubclass(PipelineContext.class);
        } catch (ClassNotFoundException | ClassCastException ex) {
            throw new BuildException(
                    String.format("failed to find class %s or this class is not a subclass of PipelineContext", context), ex
            );
        }
        return build(nodeNode, id, klass);
    }

    private <T extends PipelineContext> Node<T> build(Element root, String id, Class<T> type) throws BuildException {
        Node<T> node = new Node<>(id);
        if (root.getElementsByTagName("in-flows").getLength() != 0) {
            Element inFlowsNode = BuilderUtils.getUniqueElementByName(root, "in-flows");
            node.setInFlowPool(inFlowsNode.getAttribute("pool"));
            NodeList inFlowNodes = inFlowsNode.getElementsByTagName("in-flow");
            for (int i = 0; i < inFlowNodes.getLength(); i++) {
                Element inFlowNode = (Element) inFlowNodes.item(i);
                registerInFlow(node, inFlowNode, type);
            }
        }
        Element pipelineNode = BuilderUtils.getUniqueElementByName(root, "pipeline");
        registerPipeline(node, pipelineNode, type);
        if (root.getElementsByTagName("out-flows").getLength() != 0) {
            Element outFlowsNode = BuilderUtils.getUniqueElementByName(root, "out-flows");
            NodeList outFlowNodes = outFlowsNode.getElementsByTagName("out-flow");
            for (int i = 0; i < outFlowNodes.getLength(); i++) {
                Element outFlowNode = (Element) outFlowNodes.item(i);
                registerOutFlow(node, outFlowNode, type);
            }
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    private <T extends PipelineContext> void registerInFlow(Node<T> node, Element flowNode, Class<T> type) throws BuildException {
        String flowID = flowNode.getAttributes().getNamedItem("id").getNodeValue();
        Flow<? extends PipelineContext, ? extends PipelineContext> flow = flowResources.get(flowID);
        Class<? extends PipelineContext> flowType = BuilderUtils.getDownStreamContextType(flow);
        if (flow.getClass() != VirtualFlow.class && flowType != type) {
            throw new BuildException(String.format("%s node and its inflow has no consistency context", node.getId()));
        }
        node.addIn((Flow<? extends PipelineContext, T>) flow);
    }

    @SuppressWarnings("unchecked")
    private <T extends PipelineContext> void registerPipeline(Node<T> node, Element pipelineNode, Class<T> type) throws BuildException {
        String pipelineID = pipelineNode.getAttributes().getNamedItem("id").getNodeValue();
        Pipeline<? extends PipelineContext> pipeline = pipelineResources.get(pipelineID);
        Class<? extends PipelineContext> pipelineType = BuilderUtils.getPipelineContextType(pipeline);
        if (pipelineType != type) {
            throw new BuildException(String.format("%s node and its pipeline has no consistency context", node.getId()));
        }
        node.setPipeline((Pipeline<T>) pipeline);
    }

    @SuppressWarnings("unchecked")
    private <T extends PipelineContext> void registerOutFlow(Node<T> node, Element flowNode, Class<T> type) throws BuildException {
        String flowID = flowNode.getAttributes().getNamedItem("id").getNodeValue();
        Flow<? extends PipelineContext, ? extends PipelineContext> flow = flowResources.get(flowID);
        Class<? extends PipelineContext> flowType = BuilderUtils.getUpStreamContextType(flow);
        if (flowType != type) {
            throw new BuildException(String.format("%s node and its outflow has no consistency context", node.getId()));
        }
        node.addOut((Flow<T, ? extends PipelineContext>) flow);
    }
}
