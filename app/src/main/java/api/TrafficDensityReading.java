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
}
// -----------------------------}