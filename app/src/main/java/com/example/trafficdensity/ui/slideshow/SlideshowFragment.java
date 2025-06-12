package com.example.trafficdensity.ui.slideshow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trafficdensity.R;
import com.google.android.material.button.MaterialButton;

public class SlideshowFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Dòng này sẽ "thổi phồng" (inflate) file layout XML của bạn để biến nó thành giao diện thực tế
        return inflater.inflate(R.layout.fragment_slideshow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tìm nút Facebook bằng ID
        MaterialButton facebookButton = view.findViewById(R.id.btn_facebook);

        // Gán sự kiện click cho nút (đoạn code từ lần trước)
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String facebookUrl = "https://www.facebook.com/con.con.rua.114138/";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Không thể mở liên kết", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}