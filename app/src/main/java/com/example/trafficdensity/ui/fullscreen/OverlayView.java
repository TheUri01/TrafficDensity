package com.example.trafficdensity.ui.fullscreen; // Đảm bảo đúng package của bạn

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF; // Import RectF cho bounding box
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;

import api.Detection; // Import lớp Detection

import java.util.List;
import java.util.ArrayList; // Sử dụng ArrayList để khởi tạo danh sách rỗng
import java.util.HashMap; // Import HashMap
import java.util.Map; // Import Map
import androidx.annotation.Nullable; // Import Nullable

public class OverlayView extends View {

    private static final String TAG = "OverlayView";

    // Danh sách các detections cần vẽ
    private List<Detection> detections = new ArrayList<>();

    // Paint cho bounding box và text
    private Paint boxPaint;
    private Paint textPaint;

    // Kích thước ảnh gốc (từ backend)
    // Cần biết kích thước ảnh gốc để scale tọa độ box
    // --- CẬP NHẬT KÍCH THƯỚC ẢNH GỐC ---
    private int originalImageWidth = 512; // <-- CẬP NHẬT THÀNH 512
    private int originalImageHeight = 288; // <-- CẬP NHẬT THÀNH 288
    // -----------------------------------

    // Ánh xạ ID lớp sang tên phương tiện (cần khớp với backend)
    private static final String[] VEHICLE_NAMES = {"Xe mô tô", "Xe ô tô", "Xe tải", "Xe bus (khách)"};

    // --- Ánh xạ Class ID sang Màu sắc ---
    // Định nghĩa màu sắc cho từng loại phương tiện
    private static final Map<Integer, Integer> CLASS_COLORS = new HashMap<>();
    static {
        CLASS_COLORS.put(0, Color.RED);     // Ví dụ: Class 0 (Xe mô tô) - Màu Đỏ
        CLASS_COLORS.put(1, Color.GREEN);   // Ví dụ: Class 1 (Xe ô tô) - Màu Xanh Lá
        CLASS_COLORS.put(2, Color.BLUE);    // Ví dụ: Class 2 (Xe tải) - Màu Xanh Dương
        CLASS_COLORS.put(3, Color.YELLOW);  // Ví dụ: Class 3 (Xe bus) - Màu Vàng
        // Thêm các class ID khác nếu model của bạn có nhiều lớp hơn
    }
    // -----------------------------------


    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Khởi tạo Paint cho bounding box
        boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE); // Chỉ vẽ viền
        boxPaint.setStrokeWidth(4f); // Độ dày viền
        boxPaint.setAntiAlias(true); // Làm mịn đường viền
        // Màu mặc định sẽ được thiết lập trong onDraw dựa trên class ID

        // Khởi tạo Paint cho text (tên lớp)
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE); // Màu trắng cho text (thường dễ đọc trên nền màu bất kỳ)
        textPaint.setTextSize(30f); // Kích thước text
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.LEFT); // Căn lề text
    }

    // Phương thức để cập nhật danh sách detections và kích thước ảnh gốc
    public void setDetections(List<Detection> detections, int originalImageWidth, int originalImageHeight) {
        this.detections = detections != null ? detections : new ArrayList<>(); // Tránh null
        this.originalImageWidth = originalImageWidth;
        this.originalImageHeight = originalImageHeight;
        Log.d(TAG, "Detections updated. Count: " + this.detections.size() +
                ", Original Image Size: " + originalImageWidth + "x" + originalImageHeight);
        invalidate(); // Yêu cầu View vẽ lại khi dữ liệu thay đổi
    }

    // Phương thức để cập nhật chỉ danh sách detections (giữ nguyên kích thước ảnh gốc đã set)
    public void setDetections(List<Detection> detections) {
        this.detections = detections != null ? detections : new ArrayList<>(); // Tránh null
        Log.d(TAG, "Detections updated (size only). Count: " + this.detections.size());
        invalidate(); // Yêu cầu View vẽ lại khi dữ liệu thay đổi
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw called. Drawing detections: " + detections.size()); // Log này có thể quá nhiều

        if (detections.isEmpty()) {
            Log.d(TAG, "No detections to draw.");
            return; // Không có gì để vẽ nếu danh sách rỗng
        }

        // Lấy kích thước hiện tại của OverlayView (kích thước hiển thị trên màn hình)
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        Log.d(TAG, "OverlayView dimensions: " + viewWidth + "x" + viewHeight);
        Log.d(TAG, "Original image dimensions: " + originalImageWidth + "x" + originalImageHeight);


        if (viewWidth <= 0 || viewHeight <= 0 || originalImageWidth <= 0 || originalImageHeight <= 0) {
            Log.w(TAG, "View dimensions or original image dimensions are invalid. Cannot draw.");
            return; // Không vẽ nếu kích thước không hợp lệ
        }

        // Tính toán tỷ lệ scaling
        // Cần tính tỷ lệ dựa trên cách ảnh được hiển thị trong ImageView (scaleType="fitCenter")
        // fitCenter sẽ scale ảnh để vừa với View, giữ nguyên tỷ lệ khung hình, và căn giữa.
        // Tỷ lệ scaling sẽ là min(viewWidth / originalImageWidth, viewHeight / originalImageHeight)
        float scaleX = (float) viewWidth / originalImageWidth;
        float scaleY = (float) viewHeight / originalImageHeight;
        float scale = Math.min(scaleX, scaleY);

        // Tính toán offset do căn giữa (fitCenter)
        // Nếu tỷ lệ chiều rộng lớn hơn tỷ lệ chiều cao, ảnh sẽ được căn giữa theo chiều ngang
        float offsetX = 0;
        float offsetY = 0;
        if (scaleX > scaleY) {
            // Ảnh được căn giữa theo chiều ngang, có khoảng trống ở hai bên
            offsetX = (viewWidth - originalImageWidth * scale) / 2f;
        } else {
            // Ảnh được căn giữa theo chiều dọc, có khoảng trống ở trên và dưới
            offsetY = (viewHeight - originalImageHeight * scale) / 2f;
        }

        // Log tỷ lệ và offset để debug
        Log.d(TAG, "Calculated Scale: " + scale + ", OffsetX: " + offsetX + ", OffsetY: " + offsetY);


        // Lặp qua từng detection và vẽ bounding box
        for (int i = 0; i < detections.size(); i++) { // Sử dụng index để log chi tiết từng box
            Detection detection = detections.get(i);
            List<Integer> box = detection.getBox();
            int classId = detection.getClassId();
            // float score = detection.getScore(); // Tùy chọn sử dụng score

            if (box != null && box.size() == 4) {
                try {
                    // Lấy tọa độ box gốc
                    float x1_original = box.get(0);
                    float y1_original = box.get(1);
                    float x2_original = box.get(2);
                    float y2_original = box.get(3);

                    // Log tọa độ gốc của box hiện tại
                    Log.d(TAG, "Box " + i + " - Original: [" + x1_original + ", " + y1_original + ", " + x2_original + ", " + y2_original + "]");


                    // Scale và dịch chuyển tọa độ box
                    float x1_scaled = x1_original * scale + offsetX;
                    float y1_scaled = y1_original * scale + offsetY;
                    float x2_scaled = x2_original * scale + offsetX;
                    float y2_scaled = y2_original * scale + offsetY;

                    // Log tọa độ đã scale và dịch chuyển
                    Log.d(TAG, "Box " + i + " - Scaled: [" + x1_scaled + ", " + y1_scaled + ", " + x2_scaled + ", " + y2_scaled + "]");


                    // Tạo đối tượng RectF cho bounding box đã scale
                    RectF rect = new RectF(x1_scaled, y1_scaled, x2_scaled, y2_scaled);

                    // --- Chọn màu cho bounding box dựa trên Class ID ---
                    Integer color = CLASS_COLORS.get(classId);
                    if (color != null) {
                        boxPaint.setColor(color); // Đặt màu cho boxPaint
                    } else {
                        boxPaint.setColor(Color.GRAY); // Màu mặc định nếu class ID không có trong map
                        Log.w(TAG, "No color defined for class ID: " + classId + ". Using GRAY.");
                    }
                    // -------------------------------------------------

                    // Vẽ bounding box
                    canvas.drawRect(rect, boxPaint);


                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "Invalid box coordinates received: " + box + ". Expected 4 integers.", e);
                } catch (Exception e) {
                    Log.e(TAG, "Error drawing detection: " + detection, e);
                }
            } else {
                Log.w(TAG, "Skipping drawing invalid detection: " + detection);
            }
        }
    }
}
