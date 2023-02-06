package cn.suyako.framework.build;

import cn.suyako.framework.core.Pipeline;
import cn.suyako.framework.core.PipelineContext;
import cn.suyako.framework.core.WorkHandler;
import cn.suyako.framework.exception.BuildException;
import org.w3c.dom.*;

import java.util.*;

public class PipelineBuilder {
    public Pipeline<? extends PipelineContext> build(Element pipelineNode) throws BuildException {
        String context = pipelineNode.getAttributes().getNamedItem("context").getNodeValue();
        String id = pipelineNode.getAttributes().getNamedItem("id").getNodeValue();
        Class<? extends PipelineContext> klass;
        try {
            klass = Class.forName(context).asSubclass(PipelineContext.class);
        } catch (ClassNotFoundException | ClassCastException ex) {
            throw new BuildException(
                    String.format("failed to find class %s or this class is not a subclass of PipelineContext", context), ex
            );
        }
        Element main = (Element) pipelineNode.getElementsByTagName("main").item(0);
        return build(main, id, klass);
    }

    private <T extends PipelineContext> Pipeline<T> build(Element node, String id, Class<T> type) throws BuildException {
        Map<String, WorkHandler<T>> dict = getAllHandlers(node, type);
        Pipeline<T> pipeline = new Pipeline<>(id);
        build(node, pipeline, dict);
        if (pipeline.getHandlerSize() == 0) {
            throw new BuildException("pipeline %s must have at least one user handler");
        }
        return pipeline;
    }

    @SuppressWarnings("unchecked")
    private <T extends PipelineContext> Map<String, WorkHandler<T>> getAllHandlers(Element node, Class<T> type) throws BuildException {
        Map<String, WorkHandler<T>> dict = new HashMap<>();
        NodeList handlerNodes = node.getElementsByTagName("handler");
        for (int i = 0; i < handlerNodes.getLength(); i++) {
            Node handlerNode = handlerNodes.item(i);
            String clazz = handlerNode.getAttributes().getNamedItem("class").getNodeValue();
            String id = handlerNode.getAttributes().getNamedItem("id").getNodeValue();
            Object instance;
            try {
                Class<?> klass = Class.forName(clazz);
                instance = klass.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                throw new BuildException(String.format("failed to instantiate %s", clazz), ex);
            }
            if (!(instance instanceof WorkHandler)) {
                throw new BuildException(String.format("%s is not a subclass of WorkHandler", clazz));
            }
            WorkHandler<T> workHandler = (WorkHandler<T>) instance;
            Class<?> generic = BuilderUtils.getHandlerContextType(workHandler);
            if (generic != type) {
                throw new BuildException(
                        String.format("pipeline requires type %s, while handler %s requires type %s",
                                type, clazz, generic)
                );
            }
            workHandler.setId(id);
            dict.put(id, workHandler);
        }
        return dict;
    }

    // 1. connect all topmost handler in series;
    // 2. find prev & next handler about branches, build a sub-pipeline which connect prev & next handler,
    //    and then overwrite the structure of this sub-pipeline
    private <T extends PipelineContext> void build(Element node, Pipeline<T> pipeline,
                                                   Map<String, WorkHandler<T>> dict) throws BuildException {
        NodeList handlerOrBranchesNodes = node.getChildNodes();
        for (int i = 0; i < handlerOrBranchesNodes.getLength(); i++) {
            Node item = handlerOrBranchesNodes.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE || !item.getNodeName().equals("handler")) continue;
            Element handlerNode = (Element) item;
            String id = handlerNode.getAttributes().getNamedItem("id").getNodeValue();
            WorkHandler<T> handler = dict.get(id);
            pipeline.addLast(handler);
        }
        // a branchesNode must have a prev and next handler in same level, you can build a virtual handler when you
        // really needs a branchesNode in the head or the tail of its level
        WorkHandler<T> headHandler = pipeline.getHead(), tailHandler = pipeline.getTail();
        for (int i = 0; i < handlerOrBranchesNodes.getLength(); i++) {
            Node item = handlerOrBranchesNodes.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE || !item.getNodeName().equals("branches")) continue;
            Element branchesNode = (Element) item;
            String prevHandlerName = getSiblingHandler(branchesNode, true, 1);
            String nextHandlerName = getSiblingHandler(branchesNode, false, 1);
            if (prevHandlerName == null || nextHandlerName == null) {
                throw new BuildException("a branches node not in top level must have an explicit prev handler and next handler");
            }
            WorkHandler<T> prevHandler = dict.get(prevHandlerName);
            WorkHandler<T> nextHandler = dict.get(nextHandlerName);
            pipeline.setHead(prevHandler);
            pipeline.setTail(nextHandler);
            buildBranches(branchesNode, pipeline, dict);
        }
        pipeline.setHead(headHandler);
        pipeline.setTail(tailHandler);
    }

    private String getSiblingHandler(Node node, boolean direction, int limit) {
        Node domain = node;
        int r = 0;
        nested:
        while (domain != null && r < limit) {
            while (node != null) {
                if (node.getNodeName().equals("handler")) break nested;
                node = direction ? node.getPreviousSibling() : node.getNextSibling();
            }
            domain = domain.getParentNode();
            r += 1;
        }
        if (node == null) {
            return null;
        } else {
            return node.getAttributes().getNamedItem("id").getNodeValue();
        }
    }

    private <T extends PipelineContext> void buildBranches(Element node, Pipeline<T> pipeline,
                                                           Map<String, WorkHandler<T>> dict) throws BuildException{
        NodeList branchesNode = node.getElementsByTagName("branch");
        for (int i = 0; i < branchesNode.getLength(); i++) {
            pipeline.go(0);
            Element branchNode = (Element) branchesNode.item(i);
            build(branchNode, pipeline, dict);
        }
    }
}
