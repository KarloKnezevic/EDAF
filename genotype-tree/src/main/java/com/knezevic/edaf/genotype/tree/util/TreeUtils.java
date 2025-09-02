package com.knezevic.edaf.genotype.tree.util;

import com.knezevic.edaf.genotype.tree.FunctionNode;
import com.knezevic.edaf.genotype.tree.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class for manipulating program trees.
 */
public final class TreeUtils {

    private TreeUtils() {
    }

    /**
     * Traverses the tree and returns a flattened list of all its nodes.
     *
     * @param root The root node of the tree.
     * @return A list of all nodes.
     */
    public static List<Node> getAllNodes(Node root) {
        List<Node> nodes = new ArrayList<>();
        collectNodes(root, nodes);
        return nodes;
    }

    private static void collectNodes(Node node, List<Node> nodes) {
        nodes.add(node);
        for (Node child : node.getChildren()) {
            collectNodes(child, nodes);
        }
    }

    /**
     * Selects a random node from the tree.
     *
     * @param root   The root node of the tree.
     * @param random A random number generator.
     * @return A randomly selected node.
     */
    public static Node getRandomNode(Node root, Random random) {
        List<Node> allNodes = getAllNodes(root);
        return allNodes.get(random.nextInt(allNodes.size()));
    }

    /**
     * Replaces a target node in a tree with a new node.
     * Note: This method cannot replace the root node itself.
     *
     * @param root      The root of the tree to modify.
     * @param oldNode   The node to be replaced.
     * @param newNode   The new node to insert.
     */
    public static void replaceNode(Node root, Node oldNode, Node newNode) {
        // We can't replace the root with this method.
        // The caller must handle that case.
        if (root == oldNode) {
            return;
        }
        findAndReplace(root, oldNode, newNode);
    }

    private static boolean findAndReplace(Node current, Node target, Node replacement) {
        if (!(current instanceof FunctionNode)) {
            return false;
        }

        FunctionNode parent = (FunctionNode) current;
        List<Node> children = parent.getChildren();

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child == target) {
                parent.setChild(i, replacement);
                return true;
            }
            if (findAndReplace(child, target, replacement)) {
                return true;
            }
        }
        return false;
    }
}
