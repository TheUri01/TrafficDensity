package api; // Đảm bảo đúng package của bạn

import com.google.gson.annotations.SerializedName; // Import cho Gson

import java.io.Serializable; // Implement Serializable nếu cần truyền qua Intent (ít dùng)
import java.util.List; // Import cho List

import api.Detection;

// --- Lớp biểu diễn dữ liệu mật độ giao thông cho một camera ---
public class TrafficDensityReading { // Không cần Serializable nếu không truyền trực tiếp qua Intent

    @SerializedName("camera_id")
    private String cameraId;
    @SerializedName("timestamp")
    private long timestamp;
    @SerializedName("density")
    private float density; // Mật độ giao thông (tỷ lệ 0.0 - 1.0)
    @SerializedName("summary")
    private String summary; // Tóm tắt số lượng phương tiện
    @SerializedName("status")
    private String status; // Trạng thái (ví dụ: "ok", "not_found", "error")
    // @SerializedName("maximum") // Trường này thường chỉ dùng ở backend
    @SerializedName("image_path") // Tên file ảnh trên backend
    private String imagePath;

    // --- THÊM TRƯỜNG detections ---
    @SerializedName("detections")
    private List<Detection> detections; // Danh sách các đối tượng Detection
    // ---------------------------

    // Constructor (Gson cần một constructor không tham số)
    public TrafficDensityReading() {
    }

    // Constructor với tham số (tùy chọn)
    public TrafficDensityReading(String cameraId, long timestamp, float density, String summary, String status, String imagePath, List<Detection> detections) {
        this.cameraId = cameraId;
        this.timestamp = timestamp;
        this.density = density;
        this.summary = summary;
        this.status = status;
        this.imagePath = imagePath;
        this.detections = detections;
    }

    // Getters (cần thiết để truy cập dữ liệu)
    public String getCameraId() {
        return cameraId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getDensity() {
        return density;
    }

    public String getSummary() {
        return summary;
    }

    public String getStatus() {
        return status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public List<Detection> getDetections() {
        return detections;
    }

    // Setters (tùy chọn)
    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setDetections(List<Detection> detections) {
        this.detections = detections;
    }


    @Override
    public String toString() {
        return "TrafficDensityReading{" +
                "cameraId='" + cameraId + '\'' +
                ", timestamp=" + timestamp +
                ", density=" + density +
                ", summary='" + summary + '\'' +
                ", status='" + status + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", detections=" + (detections != null ? detections.size() : 0) + " items" +
                '}';
    }
}
