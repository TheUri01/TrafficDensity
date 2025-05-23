package com.example.trafficdensity; // Đảm bảo package đúng

import com.google.android.gms.maps.model.LatLng;

public class CameraInfo {
    public String id;
    public String name;
    public LatLng location;
    public String imageUrl;
    // Các thông tin khác như mật độ giao thông có thể thêm vào đây

    public CameraInfo(String id, String name, LatLng location, String imageUrl) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
    }
    // Getter methods (có thể cần hoặc không tùy cách bạn sử dụng)
    public String getId() { return id; }
    public String getName() { return name; }
    public LatLng getLocation() { return location; }
    public String getVideoUrl() { return imageUrl; }


    public double getLatitude() {
        return this.location.latitude;
    }

    public double getLongitude() {
        return this.location.longitude;
    }
}