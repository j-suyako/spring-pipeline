package cn.suyako.framework.build;

import cn.suyako.framework.core.*;
import cn.suyako.framework.exception.BuildException;
import cn.suyako.framework.topo.Flow;
import cn.suyako.framework.topo.Node;
import cn.suyako.framework.topo.Topology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

@Configuration
public class TopologyBuilder {
    @Value("${pipeline.topology}")
    private String xmlFile;
    @Autowired
    private BuilderResources resources;
    @Autowired
    private PipelineBuilder pipelineBuilder;
    @Autowired
    private FlowBuilder flowBuilder;
    @Autowired
    private ProviderBuilder providerBuilder;
    @Autowired
    private NodeBuilder nodeBuilder;

    @Bean("topology")
    public Topology build() throws BuildException {
        File f = new File(xmlFile);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(f);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new BuildException("parse pipeline xml file failed", ex);
        }
        Element topologyNode = document.getDocumentElement();
        buildPipelines(topologyNode);
        buildFlows(topologyNode);
        buildProviders(topologyNode);
        buildNodes(topologyNode);
        String id = topologyNode.getAttribute("id");
        int threads = Integer.parseInt(topologyNode.getAttribute("threads"));
        Topology topology = new Topology(id, threads);
        topology.addProviders(resources.getProviderResources().values());
        topology.addNodes(resources.getNodeResources().values());
        return topology;
    }

    private void buildPipelines(Element root) throws BuildException {
        Element pipelinesNode = BuilderUtils.getUniqueElementByName(root, "pipelines");
        NodeList pipelineNodes = pipelinesNode.getElementsByTagName("pipeline");
        for (int i = 0; i < pipelineNodes.getLength(); i++) {
            Element pipelineNode = (Element) pipelineNodes.item(i);
            Pipeline<? extends PipelineContext> pipeline = pipelineBuilder.build(pipelineNode);
            resources.put(pipeline.getId(), pipeline);
        }
    }

    private void buildFlows(Element root) throws BuildException {
        Element flowsNode = BuilderUtils.getUniqueElementByName(root, "flows");
        NodeList flowNodes = flowsNode.getElementsByTagName("flow");
        for (int i = 0; i < flowNodes.getLength(); i++) {
            Element flowNode = (Element) flowNodes.item(i);
            Flow<? extends PipelineContext, ? extends PipelineContext> flow = flowBuilder.build(flowNode);
            resources.put(flow.getId(), flow);
        }
    }

    private void buildProviders(Element root) throws BuildException {
        Element providersNode = BuilderUtils.getUniqueElementByName(root, "providers");
        NodeList providerNodes = providersNode.getElementsByTagName("provider");
        for (int i = 0; i < providerNodes.getLength(); i++) {
            Element providerNode = (Element) providerNodes.item(i);
            Provider<? extends PipelineContext> provider = providerBuilder.build(providerNode);
            resources.put(provider.getId(), provider);
        }
    }

    private void buildNodes(Element root) throws BuildException {
        Element nodesNode = BuilderUtils.getUniqueElementByName(root, "nodes");
        NodeList nodeNodes = nodesNode.getElementsByTagName("node");
        for (int i = 0; i < nodeNodes.getLength(); i++) {
            Element nodeNode = (Element) nodeNodes.item(i);
            Node<? extends PipelineContext> node = nodeBuilder.build(nodeNode);
            resources.put(node.getId(), node);
        }
    }
}
