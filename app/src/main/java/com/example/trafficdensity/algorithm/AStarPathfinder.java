package com.example.trafficdensity.algorithm; // Đảm bảo đúng package của bạn

import android.location.Location; // Cần để tính khoảng cách
import android.util.Log;

import com.example.trafficdensity.data.model.Node; // Import Node
import com.example.trafficdensity.data.model.Edge; // Import Edge

import java.util.ArrayList;
import java.util.Collections; // Import Collections
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue; // Import PriorityQueue
import java.util.Set;
import java.util.Locale; // Import Locale

// --- Lớp triển khai thuật toán tìm đường A* ---
public class AStarPathfinder {

    private static final String TAG = "AStarPathfinder";

    // Đồ thị: Map từ Node ID đến Node và Map từ Node ID nguồn đến danh sách các cạnh đi ra
    private final Map<String, Node> graphNodes; // Map từ ID đến Node
    private final Map<String, List<Edge>> graphEdges; // Map từ ID node nguồn đến danh sách cạnh


    // --- Constructor mới: Nhận vào Map của nodes và Map của edges ---
    public AStarPathfinder(Map<String, Node> graphNodes, Map<String, List<Edge>> graphEdges) {
        this.graphNodes = graphNodes; // Sử dụng trực tiếp map nodes
        this.graphEdges = graphEdges; // Sử dụng trực tiếp map edges
        Log.d(TAG, "AStarPathfinder initialized with " + graphNodes.size() + " nodes and " + countEdgesInMap() + " edges in map.");
    }

    // Helper để đếm tổng số cạnh trong map
    private int countEdgesInMap() {
        int count = 0;
        for (List<Edge> edges : graphEdges.values()) {
            if (edges != null) { // Kiểm tra null
                count += edges.size();
            }
        }
        return count;
    }


    /**
     * Lớp helper để lưu trữ thông tin về một node trong quá trình tìm kiếm A*.
     * Triển khai Comparable để sử dụng trong PriorityQueue (sắp xếp theo f_cost).
     */
    private static class NodeInfo implements Comparable<NodeInfo> {
        Node node; // Node hiện tại
        double g_cost; // Chi phí từ điểm bắt đầu đến node này (tích lũy sigmoid(khoảng cách))
        double h_cost; // Chi phí heuristic (density của node)
        double f_cost; // Tổng chi phí: g_cost + h_cost
        Node parent; // Node trước đó trong đường đi tối ưu

        NodeInfo(Node node, double g_cost, double h_cost, Node parent) {
            this.node = node;
            this.g_cost = g_cost;
            this.h_cost = h_cost;
            this.f_cost = g_cost + h_cost;
            this.parent = parent;
        }

        @Override
        public int compareTo(NodeInfo other) {
            // So sánh dựa trên f_cost (ưu tiên f_cost nhỏ hơn)
            return Double.compare(this.f_cost, other.f_cost);
        }

        @Override
        public String toString() {
            return "NodeInfo{" +
                    "node=" + node.getId() +
                    ", g=" + String.format(Locale.US, "%.4f", g_cost) + // Định dạng để thấy rõ hơn giá trị sigmoid
                    ", h=" + String.format(Locale.US, "%.2f", h_cost) +
                    ", f=" + String.format(Locale.US, "%.4f", f_cost) +
                    ", parent=" + (parent != null ? parent.getId() : "null") +
                    '}';
        }
    }

    /**
     * Hàm sigmoid để scale giá trị.
     * S(x) = 1 / (1 + e^(-x))
     * @param x Giá trị đầu vào
     * @return Giá trị sau khi áp dụng sigmoid (trong khoảng 0 đến 1)
     */
    public static double sigmoid(float x) {
        return 1.0 / (1.0 + Math.exp(-Math.sqrt(x)));
    }

    /**
     * Tính toán chi phí G(t) cho một cạnh.
     * G(t) của cạnh = sigmoid(khoảng cách cạnh / scaling_factor).
     * Scaling factor giúp điều chỉnh độ nhạy của sigmoid với khoảng cách.
     * @param edge Cạnh cần tính chi phí
     * @return Chi phí G(t) cho cạnh
     */
    private double calculateEdgeGCost(Edge edge) {
        double distance_meters = edge.getWeight(); // Trọng số cạnh là khoảng cách

        // --- Áp dụng Sigmoid Scaling cho khoảng cách ---
        // Cần một scaling_factor để điều chỉnh độ "nhạy" của sigmoid với khoảng cách
        // Chọn scaling_factor sao cho khoảng cách trung bình/lớn trong đồ thị
        // nằm trong vùng "tăng trưởng" của sigmoid (ví dụ: từ -5 đến 5).
        // Nếu khoảng cách là 1000m, muốn sigmoid(1000 / scaling_factor) ~ 0.5,
        // thì 1000 / scaling_factor ~ 0, scaling_factor rất lớn.
        // Nếu muốn khoảng cách 1000m map đến giá trị lớn hơn, scaling_factor nhỏ hơn.
        // Ví dụ: nếu scaling_factor = 500, khoảng cách 1000m -> sigmoid(2) ~ 0.88
        // Nếu scaling_factor = 1000, khoảng cách 1000m -> sigmoid(1) ~ 0.73
        // Nếu scaling_factor = 2000, khoảng cách 1000m -> sigmoid(0.5) ~ 0.62
        // Chọn một giá trị phù hợp với phạm vi khoảng cách giữa các node của bạn.
        // Giá trị này cần được điều chỉnh thực tế.
        float distance_scaling_factor = 1000.0f; // <-- ĐIỀU CHỈNH GIÁ TRỊ NÀY

        float scaled_distance = (float) distance_meters / distance_scaling_factor;
        float sigmoid_scaled_distance = (float) sigmoid(scaled_distance);

        // Trả về giá trị sigmoid của khoảng cách đã scale làm chi phí cho cạnh.
        // G(t) tích lũy sẽ là tổng các giá trị sigmoid này.
        return sigmoid_scaled_distance;
    }

    /**
     * Tính toán chi phí heuristic h(t) cho một node.
     * Theo yêu cầu, h(t) = density của node.
     * @param node Node hiện tại
     * @param goalNode Node đích (không dùng trong công thức h(t) này theo yêu cầu)
     * @return Chi phí heuristic h(t) (density)
     */
    private double calculateHCost(Node node, Node goalNode) {
        // Theo yêu cầu, h(t) = density của node.
        // Density đã là giá trị từ 0.0 đến 1.0.
        // Đây là heuristic không chuẩn cho việc tìm đường đi ngắn nhất theo khoảng cách
        // nhưng phù hợp với mục tiêu "tránh kẹt xe" bằng cách ưu tiên node mật độ thấp.
        return node.getDensity(); // <-- h(t) = density (trong khoảng [0, 1])
    }

    /**
     * Tìm đường đi từ node bắt đầu đến node đích sử dụng thuật toán A*.
     * @param startNode Node bắt đầu
     * @param goalNode Node đích
     * @return Danh sách các Node tạo thành đường đi, hoặc null nếu không tìm thấy đường đi.
     */
    public List<Node> findPath(Node startNode, Node goalNode) { // Nhận vào Node object thay vì ID
        Log.d(TAG, "Starting A* search from " + startNode.getId() + " to " + goalNode.getId());

        // Kiểm tra node bắt đầu và kết thúc có tồn tại trong đồ thị không
        if (!graphNodes.containsKey(startNode.getId()) || !graphNodes.containsKey(goalNode.getId())) {
            Log.e(TAG, "Start or goal node not found in the graphNodes map.");
            return null; // Node không tồn tại
        }

        // Lấy node chính xác từ map (đảm bảo làm việc với các đối tượng trong đồ thị)
        startNode = graphNodes.get(startNode.getId());
        goalNode = graphNodes.get(goalNode.getId());


        // Open set: Các node đã được khám phá nhưng chưa được xử lý đầy đủ.
        // PriorityQueue sẽ tự động sắp xếp theo f_cost (nhờ NodeInfo implements Comparable).
        PriorityQueue<NodeInfo> openSet = new PriorityQueue<>();

        // Closed set: Các node đã được xử lý đầy đủ.
        Set<String> closedSet = new HashSet<>();

        // Map để lưu trữ thông tin NodeInfo cho mỗi node đã được khám phá
        Map<String, NodeInfo> nodeInfoMap = new HashMap<>();

        // Khởi tạo node bắt đầu
        NodeInfo startNodeInfo = new NodeInfo(startNode, 0.0, calculateHCost(startNode, goalNode), null);
        openSet.add(startNodeInfo);
        nodeInfoMap.put(startNode.getId(), startNodeInfo);

        Log.d(TAG, "Initial Open Set: " + openSet);


        // Bắt đầu vòng lặp A*
        while (!openSet.isEmpty()) {
            // Lấy node có f_cost nhỏ nhất từ open set
            NodeInfo currentNodeInfo = openSet.poll();
            Node currentNode = currentNodeInfo.node;

            Log.d(TAG, "Processing node: " + currentNode.getId() + " (f=" + String.format(Locale.US, "%.4f", currentNodeInfo.f_cost) + ", g=" + String.format(Locale.US, "%.4f", currentNodeInfo.g_cost) + ", h=" + String.format(Locale.US, "%.2f", currentNodeInfo.h_cost) + ")");


            // Nếu node hiện tại là node đích, đã tìm thấy đường đi
            if (currentNode.getId().equals(goalNode.getId())) {
                Log.d(TAG, "Goal node reached: " + goalNode.getId());
                // Xây dựng đường đi từ node đích ngược về node bắt đầu bằng cách theo dõi parent
                List<Node> path = new ArrayList<>();
                NodeInfo backtrackNodeInfo = currentNodeInfo;
                while (backtrackNodeInfo != null) {
                    path.add(backtrackNodeInfo.node);
                    backtrackNodeInfo = nodeInfoMap.get(backtrackNodeInfo.parent != null ? backtrackNodeInfo.parent.getId() : null);
                }
                Collections.reverse(path); // Đảo ngược danh sách để có đường đi từ start đến goal
                Log.d(TAG, "Path found: " + path.size() + " nodes.");
                return path; // Trả về đường đi
            }

            // Thêm node hiện tại vào closed set
            closedSet.add(currentNode.getId());

            // Lấy danh sách các cạnh đi ra từ node hiện tại từ graphEdges map
            List<Edge> neighbors = graphEdges.get(currentNode.getId());
            if (neighbors == null) {
                neighbors = new ArrayList<>(); // Không có cạnh đi ra
            }

            Log.d(TAG, "Exploring neighbors of " + currentNode.getId() + " (" + neighbors.size() + " neighbors)");

            // Duyệt qua các node lân cận
            for (Edge edge : neighbors) {
                Node neighborNode = edge.getDestination();

                // Bỏ qua node lân cận nếu nó đã nằm trong closed set
                if (closedSet.contains(neighborNode.getId())) {
                    Log.d(TAG, "  Neighbor " + neighborNode.getId() + " is in closed set. Skipping.");
                    continue;
                }

                // Tính toán chi phí G(t) từ start đến node lân cận thông qua node hiện tại
                // G_new = G_current + G_cost_of_edge
                double g_cost_edge = calculateEdgeGCost(edge); // Chi phí G(t) cho cạnh này
                double tentative_g_cost = currentNodeInfo.g_cost + g_cost_edge; // Tổng G(t) từ start đến neighbor qua current

                Log.d(TAG, "  Neighbor: " + neighborNode.getId() + ", Edge G-cost: " + String.format(Locale.US, "%.4f", g_cost_edge) + ", Tentative G-cost: " + String.format(Locale.US, "%.4f", tentative_g_cost));


                // Kiểm tra xem node lân cận đã có trong open set chưa
                NodeInfo neighborNodeInfo = nodeInfoMap.get(neighborNode.getId());

                if (neighborNodeInfo == null || tentative_g_cost < neighborNodeInfo.g_cost) {
                    // Nếu node lân cận chưa được khám phá hoặc tìm thấy đường đi tốt hơn đến nó
                    if (neighborNodeInfo == null) {
                        // Node lân cận chưa được khám phá
                        double h_cost = calculateHCost(neighborNode, goalNode); // Tính h(t) cho node lân cận
                        neighborNodeInfo = new NodeInfo(neighborNode, tentative_g_cost, h_cost, currentNode);
                        nodeInfoMap.put(neighborNode.getId(), neighborNodeInfo);
                        openSet.add(neighborNodeInfo); // Thêm vào open set
                        Log.d(TAG, "  Neighbor " + neighborNode.getId() + " added to open set. " + neighborNodeInfo);
                    } else {
                        // Tìm thấy đường đi tốt hơn đến node lân cận
                        Log.d(TAG, "  Found better path to neighbor " + neighborNode.getId() + ". Updating G-cost and parent.");
                        neighborNodeInfo.g_cost = tentative_g_cost; // Cập nhật G(t)
                        neighborNodeInfo.f_cost = neighborNodeInfo.g_cost + neighborNodeInfo.h_cost; // Cập nhật F(t)
                        neighborNodeInfo.parent = currentNode; // Cập nhật parent
                        // Cần xóa và thêm lại vào PriorityQueue để nó sắp xếp lại
                        openSet.remove(neighborNodeInfo); // Xóa node cũ
                        openSet.add(neighborNodeInfo); // Thêm node đã cập nhật
                        Log.d(TAG, "  Neighbor " + neighborNode.getId() + " updated in open set. " + neighborNodeInfo);
                    }
                } else {
                    Log.d(TAG, "  Neighbor " + neighborNode.getId() + " already in open set with better or equal G-cost. Skipping.");
                }
            }
        }

        // Nếu vòng lặp kết thúc mà không tìm thấy đường đi đến node đích
        Log.w(TAG, "A* search finished, but no path found to goal node: " + goalNode.getId());
        return null; // Không tìm thấy đường đi
    }
}
