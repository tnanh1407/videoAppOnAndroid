package com.example.videoapponandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddVideoOptionsBottomSheet extends BottomSheetDialogFragment {

    // Mã request code cho Camera và Gallery
    private static final int REQUEST_CODE_CAPTURE_VIDEO = 1;
    private static final int REQUEST_CODE_PICK_VIDEO_FROM_GALLERY_OR_FILE_MANAGER = 2;

    public AddVideoOptionsBottomSheet() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_video_options, container, false);

        LinearLayout optionRecordVideo = view.findViewById(R.id.optionRecordVideo);
        LinearLayout optionChooseFromGallery = view.findViewById(R.id.optionChooseFromGallery);

        optionRecordVideo.setOnClickListener(v -> {
            // Xử lý khi chọn "Quay video mới"
            Toast.makeText(getContext(), "Đang mở Camera...", Toast.LENGTH_SHORT).show();
            openCameraForVideo();
            dismiss(); // Đóng dialog sau khi chọn
        });

        optionChooseFromGallery.setOnClickListener(v -> {
            // Xử lý khi chọn "Chọn video từ thư viện"
            Toast.makeText(getContext(), "Đang trình quản lí tệp ...", Toast.LENGTH_SHORT).show();
            openVideoFileManager();
            dismiss(); // Đóng dialog sau khi chọn
        });

        return view;
    }


    private void openCameraForVideo() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Kiểm tra xem có ứng dụng nào có thể xử lý Intent này không
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Sử dụng getParentFragment() để gửi kết quả về OneFragment
            // OneFragment sẽ nhận được kết quả trong onActivityResult của nó.
            getParentFragment().startActivityForResult(takeVideoIntent, REQUEST_CODE_CAPTURE_VIDEO);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng Camera nào có thể quay video.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openVideoFileManager() {
        Intent intent;
        // ACTION_OPEN_DOCUMENT (API 19+) thường tốt hơn để chọn file lâu dài
        // và tích hợp tốt với các ứng dụng Document Provider (bao gồm File Manager).
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE); // Chỉ hiển thị các file có thể mở
        } else {
            // Với các phiên bản Android cũ hơn, ACTION_GET_CONTENT phổ biến hơn.
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }

        intent.setType("video/*"); // Chỉ định chỉ hiển thị file video

        // Kiểm tra xem có ứng dụng nào có thể xử lý Intent này không
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getParentFragment().startActivityForResult(intent, REQUEST_CODE_PICK_VIDEO_FROM_GALLERY_OR_FILE_MANAGER);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng nào để chọn video từ thiết bị.", Toast.LENGTH_SHORT).show();
        }
    }
}