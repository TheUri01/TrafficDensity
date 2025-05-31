package com.example.trafficdensity.data.model; // Đảm bảo đúng package của bạn

import com.google.android.gms.maps.model.LatLng; // Import LatLng
import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // Import Objects cho hashCode và equals

// --- Lớp biểu diễn một Node (điểm) trong đồ thị ---
// Node có thể là một giao lộ, điểm đặt camera, hoặc điểm trung gian trên đường.
public class Node {

    private String id; // ID duy nhất của node (có thể là ID nội bộ hoặc ID giao lộ ước lượng)
    private String name; // Tên của node (ví dụ: tên giao lộ, tên camera)
    private double latitude; // Vĩ độ
    private double longitude; // Kinh độ
    private float trafficDensity; // Mật độ giao thông ước tính tại node này (có thể cập nhật)
    private boolean isCameraNode; // True nếu node này có liên kết với ít nhất một camera
    private List<String> cameraIds; // Danh sách các ID camera liên kết với node này (nếu có nhiều camera tại cùng vị trí)

    // Constructor
    public Node(String id, String name, double latitude, double longitude, float trafficDensity, boolean isCameraNode) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.trafficDensity = trafficDensity;
        this.isCameraNode = isCameraNode;
        this.cameraIds = new ArrayList<>(); // Khởi tạo danh sách camera IDs
    }

    // Constructor đơn giản hơn, mật độ ban đầu là 0.0f
    public Node(String id, String name, double latitude, double longitude, boolean isCameraNode) {
        this(id, name, latitude, longitude, 0.0f, isCameraNode);
    }

    public Float getDensity(){return trafficDensity;}


    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    public float getTrafficDensity() {
        return trafficDensity;
    }

    public boolean isCameraNode() {
        return isCameraNode;
    }

    public List<String> getCameraIds() {
        return cameraIds;
    }

    // Setters (nếu cần cập nhật thông tin sau khi tạo)
    public void setName(String name) {
        this.name = name;
    }

    public void setTrafficDensity(float trafficDensity) {
        this.trafficDensity = trafficDensity;
    }

    public void setCameraNode(boolean cameraNode) {
        isCameraNode = cameraNode;
    }

    // Phương thức để thêm ID camera vào danh sách
    public void addCameraId(String cameraId) {
        if (cameraId != null && !cameraId.isEmpty() && !this.cameraIds.contains(cameraId)) {
            this.cameraIds.add(cameraId);
            this.isCameraNode = true; // Đảm bảo cờ isCameraNode được đặt khi thêm ID camera
        }
    }

    // Phương thức để kiểm tra xem node có chứa một camera ID cụ thể không
    public boolean hasCameraId(String cameraId) {
        return this.cameraIds.contains(cameraId);
    }


    // Override equals và hashCode dựa trên ID để so sánh các Node
    // (Mặc dù GraphData mới dùng vị trí để quản lý node duy nhất,
    // việc override này hữu ích khi so sánh Node objects trực tiếp ở nơi khác)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id); // So sánh dựa trên ID duy nhất
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Hash dựa trên ID duy nhất
    }

    // Phương thức toString để dễ dàng debug
    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lat=" + latitude +
                ", lng=" + longitude +
                ", isCamera=" + isCameraNode +
                ", cameraIds=" + cameraIds +
                '}';
    }

    public void setDensity(Float aFloat) {
        this.trafficDensity = aFloat;
    }

    public boolean hasCamera() {
        return this.isCameraNode;
    }

    public void setHasCamera(boolean b) {
        this.isCameraNode = b;
    }

    public void setId(String newCameraNodeId) {
        this.id = newCameraNodeId;
    }
}
