package com.example.trafficdensity.data.model; // Đảm bảo đúng package của bạn

import java.io.Serializable; // Implement Serializable
import java.util.Locale; // Import Locale

// --- Lớp biểu diễn một cạnh (đường nối) giữa hai Node ---
// Lớp này không thay đổi so với trước, chỉ làm việc với lớp Node
public class Edge implements Serializable {

    private static final long serialVersionUID = 1L; // SerialVersionUID

    private Node source; // Node nguồn
    private Node destination; // Node đích
    private double weight; // Trọng số của cạnh (ví dụ: khoảng cách)

    // Constructor
    public Edge(Node source, Node destination, double weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    // Getters
    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source=" + source.getId() +
                ", destination=" + destination.getId() +
                ", weight=" + String.format(Locale.US, "%.2f", weight) +
                '}';
    }
    public void clear(){

    }

    public double getWeightedCost() {
        return  weight;
    }
}
