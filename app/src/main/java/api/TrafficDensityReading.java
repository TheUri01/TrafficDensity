package api;

import com.google.gson.annotations.SerializedName; // Import này nếu dùng Gson

public class TrafficDensityReading {

    @SerializedName("camera_id")
    private String cameraId;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("density")
    private float density;

    @SerializedName("summary")
    private String summary;
    @SerializedName("image_path")
    private String image_path;
    // ---------------------------


    // Getter methods
    public String getCameraId() {
        return cameraId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getDensity() {
        return density;
    }


    // --- Thêm getter cho summary ---
    public String getSummary() {
        return summary;
    }

    public String getImagePath(){return image_path;}
}
// -----------------------------}