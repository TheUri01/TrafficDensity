package api;
import com.example.trafficdensity.CameraInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface TrafficApiService {

    // Định nghĩa endpoint GET để lấy dữ liệu mật độ
    // Endpoint Flask: /api/traffic/latest_densities
    // Tham số query: camera_ids (chứa ID của camera bạn muốn lấy dữ liệu)
    @GET("/api/traffic/latest_densities") // <-- Đường dẫn endpoint
    Call<List<TrafficDensityReading>> getLatestTrafficDensities(@Query("camera_ids") String cameraIds);


    // Bạn có thể thêm các phương thức API khác ở đây nếu cần
}
