package com.example.trafficdensity.api;

import com.google.gson.annotations.SerializedName;

public class PathfindingCameraDensity {
    @SerializedName("camera_id")
    private String cameraId;
    private float density;

    // Constructor (tùy chọn, nếu bạn cần tạo đối tượng này thủ công)
    public PathfindingCameraDensity(String cameraId, float density) {
        this.cameraId = cameraId;
        this.density = density;
    }

    // Getters
    public String getCameraId() {
        return cameraId;
    }

    public float getDensity() {
        return density;
    }

    // Setters (tùy chọn, thường không cần nếu chỉ dùng để nhận dữ liệu từ API)
    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    @Override
    public String toString() {
        return "PathfindingCameraDensity{" +
                "cameraId='" + cameraId + '\'' +
                ", density=" + density +
                '}';
    }
}