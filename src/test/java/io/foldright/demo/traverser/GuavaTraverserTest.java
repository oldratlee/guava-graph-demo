package io.foldright.demo.traverser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.Traverser;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * @see <a href="https://juejin.cn/post/7418378848402653194">使用 Guava 轻松搞定树形结构，无需使用其他工具</a>
 */
@SuppressWarnings("UnstableApiUsage")
class GuavaTraverserTest {
    @Test
    void TraverserDemo() {
        // Traverser: Guava 提供的不可变对象，封装了遍历算法
        // breadthFirst 方法返回 Iterator 类型
        List<String> bfs = Streams.stream(nodeTraverser.breadthFirst(root))
                .map(Node::name)
                .toList();
        System.out.println("breadthFirst = " + bfs);

        List<String> dfsPostOrder = Streams.stream(nodeTraverser.depthFirstPostOrder(root))
                .map(Node::name)
                .toList();
        System.out.println("depthFirstPostOrder = " + dfsPostOrder);

        List<String> dfsPreOrder = Streams.stream(nodeTraverser.depthFirstPreOrder(root))
                .map(Node::name)
                .toList();
        System.out.println("depthFirstPreOrder = " + dfsPreOrder);
    }

    private record Node(String name, @NonNull List<Node> children) {}

    private static final Node root;

    static {
        // 构建树
        //    Root
        //   /    \
        // Child1 Child2
        //  |      |
        // Leaf1  Leaf2
        Node leaf1 = new Node("Leaf1", List.of());
        Node leaf2 = new Node("Leaf2", List.of());
        Node child1 = new Node("Child1", List.of(leaf1));
        Node child2 = new Node("Child2", List.of(leaf2));
        root = new Node("Root", List.of(child1, child2));
    }

    private static final Traverser<Node> nodeTraverser = Traverser.forTree(Node::children);

    @Test
    void forVsStream_ofTraverser() {
        // 普通实现
        Node targetNode = null;
        for (Node node : nodeTraverser.depthFirstPreOrder(root)) {
            if (node.name().equals("Leaf1")) {
                targetNode = node;
                break;
            }
        }
        System.out.println(targetNode);

        // 流式实现：更简单易懂
        final Optional<Node> find = Streams.stream(nodeTraverser.depthFirstPreOrder(root))
                .filter(node -> "Leaf2".equals(node.name()))
                .findFirst();
        System.out.println(find);
    }

    @Test
    void traverseBuiltGraphDemo() {
        // 使用 GraphBuilder 构建组织架构的下属关系（从上级指向下级）
        ImmutableGraph<OrganizationNode> organizationGraph = putOrgEdges(GraphBuilder
                .directed()    // 树是有方向的（从上级指向下级）
                .immutable());

        // 遍历和显示节点关系
        System.out.println("Organization Structure (Subordinate Relationships):");
        for (OrganizationNode node : organizationGraph.nodes()) {
            System.out.println(node + " has subordinates: ");
            for (OrganizationNode successor : organizationGraph.successors(node)) {
                System.out.println(" -> " + successor);
            }
        }
    }

    private static ImmutableGraph<OrganizationNode> putOrgEdges(
            ImmutableGraph.Builder<OrganizationNode> immutableGraphBuilder) {
        // 创建组织节点
        OrganizationNode ceo = new OrganizationNode("CEO", 42);
        OrganizationNode cto = new OrganizationNode("CTO", 300);
        OrganizationNode cfo = new OrganizationNode("CFO", 200);
        OrganizationNode devLead = new OrganizationNode("Dev Lead");
        OrganizationNode dev1 = new OrganizationNode("Developer 1");
        OrganizationNode dev2 = new OrganizationNode("Developer 2");
        OrganizationNode financeLead = new OrganizationNode("Finance Lead");
        OrganizationNode accountant1 = new OrganizationNode("Accountant 1");
        OrganizationNode accountant2 = new OrganizationNode("Accountant 2");

        return immutableGraphBuilder.putEdge(ceo, cto)          // CEO -> CTO
                .putEdge(ceo, cfo)          // CEO -> CFO
                .putEdge(cto, devLead)      // CTO -> Dev Lead
                .putEdge(devLead, dev1)     // Dev Lead -> Developer 1
                .putEdge(devLead, dev2)     // Dev Lead -> Developer 2
                .putEdge(cfo, financeLead)  // CFO -> Finance Lead
                .putEdge(financeLead, accountant1)  // Finance Lead -> Accountant 1
                .putEdge(financeLead, accountant2)  // Finance Lead -> Accountant 2
                .build();
    }

    record OrganizationNode(String name, int order) {
        OrganizationNode(String name) {
            this(name, 0);
        }
    }

    @Test
    void operation_filter() {
        Menu root = new Menu(1, ImmutableList.of(
                new Menu(11, ImmutableList.of()),
                new Menu(12, ImmutableList.of())
        ));
        Traverser<Menu> traverser = Traverser.forTree(Menu::subMenus);
        List<Menu> menus = Streams.stream(traverser.breadthFirst(root))
                .filter(x -> x.id() < 5)
                .toList();
        System.out.println(menus);
    }

    record Menu(int id, List<Menu> subMenus) {}

    @Test
    void operation_sort() {
        ImmutableGraph<OrganizationNode> organizationGraph = putOrgEdges(GraphBuilder
                .directed()
                // 创建时指定排序即可
                .nodeOrder(ElementOrder.sorted(Comparator.comparing(OrganizationNode::order)))
                .immutable());
        System.out.println(organizationGraph);

        System.out.println();
        for (OrganizationNode node : organizationGraph.nodes()) {
            System.out.println(node);
        }
    }
}
