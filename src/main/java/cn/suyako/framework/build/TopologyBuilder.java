package cn.suyako.framework.build;

import cn.suyako.framework.core.*;
import cn.suyako.framework.exception.BuildException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TopologyBuilder {
    private final Map<String, Pipeline<? extends PipelineContext>> pipelineResources = new HashMap<>();
    private final Map<String, Flow<? extends PipelineContext, ? extends PipelineContext>> flowResources = new HashMap<>();
    private final Map<String, Provider<? extends PipelineContext>> providerResources = new HashMap<>();
    private final Map<String, Node<? extends PipelineContext>> nodeResources = new HashMap<>();
    private final PipelineBuilder pipelineBuilder = new PipelineBuilder();
    private final FlowBuilder flowBuilder = new FlowBuilder();
    private final ProviderBuilder providerBuilder = new ProviderBuilder(flowResources);
    private final NodeBuilder nodeBuilder = new NodeBuilder(pipelineResources, flowResources);

    public Topology build(String xmlFile) throws Exception {
        File f = new File(xmlFile);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);
        Element topologyNode = document.getDocumentElement();
        buildPipelines(topologyNode);
        buildFlows(topologyNode);
        buildProviders(topologyNode);
        buildNodes(topologyNode);
        String id = topologyNode.getAttribute("id");
        int threads = Integer.parseInt(topologyNode.getAttribute("threads"));
        Topology topology = new Topology(id, threads);
        for (String providerId : providerResources.keySet()) {
            topology.addProvider(providerResources.get(providerId));
        }
        for (String nodeId : nodeResources.keySet()) {
            topology.addNode(nodeResources.get(nodeId));
        }
        return topology;
    }

    public void buildPipelines(Element root) throws BuildException {
        Element pipelinesNode = BuilderUtils.getUniqueElementByName(root, "pipelines");
        NodeList pipelineNodes = pipelinesNode.getElementsByTagName("pipeline");
        for (int i = 0; i < pipelineNodes.getLength(); i++) {
            Element pipelineNode = (Element) pipelineNodes.item(i);
            Pipeline<? extends PipelineContext> pipeline = pipelineBuilder.build(pipelineNode);
            pipelineResources.put(pipeline.getId(), pipeline);
        }
    }

    public void buildFlows(Element root) throws BuildException {
        Element flowsNode = BuilderUtils.getUniqueElementByName(root, "flows");
        NodeList flowNodes = flowsNode.getElementsByTagName("flow");
        for (int i = 0; i < flowNodes.getLength(); i++) {
            Element flowNode = (Element) flowNodes.item(i);
            Flow<? extends PipelineContext, ? extends PipelineContext> flow = flowBuilder.build(flowNode);
            flowResources.put(flow.getId(), flow);
        }
    }

    public void buildProviders(Element root) throws BuildException {
        Element providersNode = BuilderUtils.getUniqueElementByName(root, "providers");
        NodeList providerNodes = providersNode.getElementsByTagName("provider");
        for (int i = 0; i < providerNodes.getLength(); i++) {
            Element providerNode = (Element) providerNodes.item(i);
            Provider<? extends PipelineContext> provider = providerBuilder.build(providerNode);
            providerResources.put(provider.getId(), provider);
        }
    }

    public void buildNodes(Element root) throws BuildException {
        Element nodesNode = BuilderUtils.getUniqueElementByName(root, "nodes");
        NodeList nodeNodes = nodesNode.getElementsByTagName("node");
        for (int i = 0; i < nodeNodes.getLength(); i++) {
            Element nodeNode = (Element) nodeNodes.item(i);
            Node<? extends PipelineContext> node = nodeBuilder.build(nodeNode);
            nodeResources.put(node.getId(), node);
        }
    }
}
