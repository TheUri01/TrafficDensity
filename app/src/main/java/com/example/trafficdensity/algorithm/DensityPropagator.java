package com.example.trafficdensity.algorithm;

import android.location.Location; // Import Location để tính khoảng cách
import android.util.Log;

import com.example.trafficdensity.data.model.Node;
import com.example.trafficdensity.data.model.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class DensityPropagator {

    private static final String TAG = "DensityPropagator";
    private static final float POWER = 2.0f; // Sức mạnh của ảnh hưởng khoảng cách (1/distance^POWER)
    private static final int NUM_CLOSEST_CAMERAS = 2; // Số lượng camera gần nhất để tính toán

    /**
     * Tính toán và lan truyền density cho các node không phải camera,
     * dựa trên density của các camera gần nhất.
     *
     * @param graphNodes Map chứa tất cả các Node trong đồ thị.
     * @param graphEdgesMap Map từ Node ID nguồn đến danh sách cạnh (không dùng trực tiếp để lan truyền,
     * nhưng cần để xác định các node).
     */
    public void propagateDensity(Map<String, Node> graphNodes, Map<String, List<Edge>> graphEdgesMap) {
        if (graphNodes == null || graphNodes.isEmpty()) {
            Log.e(TAG, "Graph Nodes is empty. Cannot propagate density.");
            return;
        }

        Log.d(TAG, "Starting density propagation with 'closest cameras' logic for " + graphNodes.size() + " nodes.");

        // 1. Lấy danh sách các node camera
        List<Node> cameraNodes = new ArrayList<>();
        for (Node node : graphNodes.values()) {
            if (node.isCameraNode()) {
                cameraNodes.add(node);
            }
        }

        if (cameraNodes.isEmpty()) {
            Log.w(TAG, "No camera nodes found. All non-camera nodes will have 0.0 density after reset.");
            // Reset tất cả các node không phải camera về 0.0f nếu không có camera nào
            for (Node node : graphNodes.values()) {
                if (!node.isCameraNode()) {
                    node.setDensity(0.0f);
                }
            }
            return;
        }

        // 2. Tính toán density cho mỗi node giao lộ (non-camera node)
        // Chúng ta sẽ tính toán một lần duy nhất cho mỗi node giao lộ
        // vì nó phụ thuộc vào các camera cố định, không phải node lân cận đã được lan truyền.
        for (Node targetNode : graphNodes.values()) {
            if (targetNode.isCameraNode()) {
                // Node camera đã có density từ API, không cần tính toán lại
                Log.d(TAG, String.format(Locale.US, "Camera Node %s (ID: %s): Density %.2f (from API).",
                        targetNode.getName(), targetNode.getId(), targetNode.getDensity()));
                continue;
            }

            // Tìm NUM_CLOSEST_CAMERAS gần nhất đến targetNode
            List<CameraDistance> distances = new ArrayList<>();
            for (Node cameraNode : cameraNodes) {
                float[] results = new float[1];
                Location.distanceBetween(targetNode.getLatitude(), targetNode.getLongitude(),
                        cameraNode.getLatitude(), cameraNode.getLongitude(), results);
                distances.add(new CameraDistance(cameraNode, results[0]));
            }

            // Sắp xếp theo khoảng cách tăng dần
            Collections.sort(distances, new Comparator<CameraDistance>() {
                @Override
                public int compare(CameraDistance d1, CameraDistance d2) {
                    return Float.compare(d1.distance, d2.distance);
                }
            });

            float weightedSum = 0.0f;
            float totalInverseWeight = 0.0f;
            int camerasConsidered = 0;

            // Lấy NUM_CLOSEST_CAMERAS và tính toán density
            for (int i = 0; i < Math.min(NUM_CLOSEST_CAMERAS, distances.size()); i++) {
                CameraDistance cd = distances.get(i);
                Node closestCamera = cd.cameraNode;
                float distance = cd.distance;
                Log.d(TAG, "Target Node: " + targetNode.getName() + " Closest: " + closestCamera.getName() + " Distance: " + distance);


                        // Tránh chia cho 0 hoặc khoảng cách rất nhỏ (dưới 1m)
                float effectiveDistance = Math.max(1.0f, distance);
                float inverseWeight = (float) (1.0f / Math.pow(effectiveDistance, POWER));

                weightedSum += closestCamera.getDensity() * inverseWeight;
                totalInverseWeight += inverseWeight;
                camerasConsidered++;
            }

            float propagatedDensity = 0.0f;
            if (totalInverseWeight > 0 && camerasConsidered > 0) {
                propagatedDensity = weightedSum / totalInverseWeight;
            } else {
                // Nếu không tìm thấy đủ camera hoặc tổng trọng số bằng 0, gán density mặc định
                propagatedDensity = 0.0f; // Hoặc một giá trị mặc định khác nếu bạn muốn
            }
            targetNode.setDensity(propagatedDensity);
            Log.d(TAG, String.format(Locale.US, "Intersection Node %s (ID: %s): Propagated density: %.2f from %d closest cameras.",
                    targetNode.getName(), targetNode.getId(), propagatedDensity, camerasConsidered));
        }

        Log.d(TAG, "Finished density propagation using closest cameras logic.");
    }

    // Lớp nội bộ để lưu trữ Node camera và khoảng cách của nó
    private static class CameraDistance {
        Node cameraNode;
        float distance;

        CameraDistance(Node cameraNode, float distance) {
            this.cameraNode = cameraNode;
            this.distance = distance;
        }
    }
}