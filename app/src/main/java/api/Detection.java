package api; // Đảm bảo đúng package của bạn

import com.google.gson.annotations.SerializedName; // Import cho Gson

import java.io.Serializable; // <-- Import Serializable
import java.util.List; // Import cho List

// --- Lớp biểu diễn một phát hiện (Bounding Box + Class) ---
// --- THÊM implements Serializable ---
public class Detection implements Serializable { // <-- Đã thêm implements Serializable
    // -------------------------------

    // Gson cần một serialVersionUID khi triển khai Serializable
    // Bạn có thể tạo tự động bằng cách Alt+Enter trên tên lớp trong Android Studio
    private static final long serialVersionUID = 1L; // Ví dụ serialVersionUID


    @SerializedName("box")
    private List<Integer> box; // Tọa độ box: [x1, y1, x2, y2]

    @SerializedName("class_id")
    private int classId; // ID của lớp (phương tiện)

    public Detection() {
    }

    // Constructor với tham số (nếu bạn cần tạo đối tượng Detection thủ công)
    public Detection(List<Integer> box, int classId) {
        this.box = box;
        this.classId = classId;
    }

    // Getters
    public List<Integer> getBox() {
        return box;
    }

    public int getClassId() {
        return classId;
    }



    // Setters (tùy chọn, nếu bạn cần sửa đổi dữ liệu sau khi tạo)
    public void setBox(List<Integer> box) {
        this.box = box;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }




    @Override
    public String toString() {
        return "Detection{" +
                "box=" + box +
                ", classId=" + classId +
                '}';
    }
}

