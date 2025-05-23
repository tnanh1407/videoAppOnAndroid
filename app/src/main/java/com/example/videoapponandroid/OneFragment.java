package com.example.videoapponandroid;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections; // Import Collections
import java.util.Comparator; // Import Comparator
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OneFragment extends Fragment implements VideoOptionsBottomSheet.VideoOptionListener {

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<VideoFile> videoList;
    private List<VideoFile> originalVideoList; // Danh sách gốc để lọc và sắp xếp lại
    private FloatingActionButton fabAddVideo;
    private TextView noVideosFoundText; // Khai báo TextView mới

    private static final int REQUEST_CODE_CAPTURE_VIDEO = 1;
    private static final int REQUEST_CODE_PICK_VIDEO_FROM_GALLERY_OR_FILE_MANAGER = 2; // Đổi tên từ REQUEST_CODE_PICK_VIDEO
    private static final int PERMISSION_REQUEST_CODE = 100; // Mã request cho quyền đọc chung
    private static final int DELETE_PERMISSION_REQUEST_CODE = 101; // Mã request cho quyền xóa

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoList = new ArrayList<>();
        originalVideoList = new ArrayList<>(); // Khởi tạo danh sách gốc
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Ánh xạ TextView hiển thị thông báo
        noVideosFoundText = view.findViewById(R.id.noVideosFoundText);

        FragmentManager childFragmentManager = getChildFragmentManager();
        videoAdapter = new VideoAdapter(getContext(), videoList, childFragmentManager);
        recyclerView.setAdapter(videoAdapter);

        fabAddVideo = view.findViewById(R.id.fabAddVideo);
        fabAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddVideoOptionsBottomSheet();
            }
        });

        // Tải video khi Fragment được tạo và quyền đã được cấp
        checkAndLoadVideos();

        return view;
    }

    private void checkAndLoadVideos() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_VIDEO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            loadVideos();
        } else {
            Log.d("OneFragment", "Quyền đọc bộ nhớ chưa được cấp.");
            // Hiển thị thông báo khi quyền chưa cấp
            noVideosFoundText.setText("Vui lòng cấp quyền truy cập bộ nhớ để xem video.");
            noVideosFoundText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE); // Ẩn RecyclerView
        }
    }


    private void loadVideos() {
        Log.d("OneFragment", "Bắt đầu tải video...");
        videoList.clear();
        originalVideoList.clear(); // Xóa cả danh sách gốc

        Context context = getContext();
        if (context == null) {
            Log.e("OneFragment", "Context is null, cannot load videos.");
            updateNoVideosFoundVisibility(); // Cập nhật visibility nếu context null
            return;
        }

        ContentResolver contentResolver = context.getContentResolver();
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_ADDED
        };

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    collection,
                    projection,
                    null,
                    null,
                    sortOrder
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

                do {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String path = cursor.getString(dataColumn);
                    long duration = cursor.getLong(durationColumn);
                    long dateAddedSeconds = cursor.getLong(dateAddedColumn);

                    Uri contentUri = Uri.withAppendedPath(collection, String.valueOf(id));

                    String creationTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(new Date(dateAddedSeconds * 1000L));

                    VideoFile videoFile = new VideoFile(id, title, path, duration, contentUri, creationTime);
                    videoList.add(videoFile);
                    originalVideoList.add(videoFile); // Thêm vào danh sách gốc
                } while (cursor.moveToNext());
            } else {
                Log.d("OneFragment", "Không tìm thấy video nào trong MediaStore.");
            }
        } catch (Exception e) {
            Log.e("OneFragment", "Lỗi khi truy xuất video: " + e.getMessage());
            Toast.makeText(getContext(), "Lỗi khi tải video: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        videoAdapter.notifyDataSetChanged();
        Log.d("OneFragment", "Số lượng video sau khi tải lại: " + videoList.size());
        updateNoVideosFoundVisibility(); // Cập nhật visibility sau khi tải
    }

    // Phương thức trợ giúp để điều khiển hiển thị của TextView và RecyclerView
    private void updateNoVideosFoundVisibility() {
        if (videoList.isEmpty()) {
            noVideosFoundText.setText(R.string.no_videos_found); // Đặt lại chuỗi mặc định
            noVideosFoundText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noVideosFoundText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Phương thức hiển thị BottomSheet để thêm video
    private void showAddVideoOptionsBottomSheet() {
        AddVideoOptionsBottomSheet bottomSheet = new AddVideoOptionsBottomSheet();
        bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CAPTURE_VIDEO:
                case REQUEST_CODE_PICK_VIDEO_FROM_GALLERY_OR_FILE_MANAGER:
                    Toast.makeText(getContext(), "Video đã được thêm!", Toast.LENGTH_SHORT).show();
                    loadVideos(); // Tải lại danh sách video để hiển thị video mới
                    break;
                case DELETE_PERMISSION_REQUEST_CODE:
                    Toast.makeText(getContext(), "Video đã được xóa thành công.", Toast.LENGTH_SHORT).show();
                    loadVideos(); // Tải lại danh sách sau khi xóa
                    // Tìm và đóng VideoOptionsBottomSheet nếu nó vẫn đang hiển thị
                    BottomSheetDialogFragment bottomSheet = (BottomSheetDialogFragment) getChildFragmentManager().findFragmentByTag(VideoOptionsBottomSheet.TAG);
                    if (bottomSheet != null) {
                        bottomSheet.dismiss();
                    }
                    break;
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == DELETE_PERMISSION_REQUEST_CODE) {
                Toast.makeText(getContext(), "Thao tác xóa đã bị hủy bởi người dùng.", Toast.LENGTH_SHORT).show();
                BottomSheetDialogFragment bottomSheet = (BottomSheetDialogFragment) getChildFragmentManager().findFragmentByTag(VideoOptionsBottomSheet.TAG);
                if (bottomSheet != null) {
                    bottomSheet.dismiss();
                }
            } else {
                Toast.makeText(getContext(), "Thao tác đã bị hủy.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Lỗi không xác định: resultCode = " + resultCode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Quyền truy cập bộ nhớ đã được cấp.", Toast.LENGTH_SHORT).show();
                loadVideos(); // Tải lại video sau khi có quyền
            } else {
                Toast.makeText(getContext(), "Quyền truy cập bộ nhớ bị từ chối. Không thể tải video.", Toast.LENGTH_LONG).show();
                noVideosFoundText.setText("Quyền truy cập bộ nhớ bị từ chối. Không thể hiển thị video.");
                noVideosFoundText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
    }

    // Triển khai phương thức từ interface VideoOptionListener
    @Override
    public void onVideoDeleted() {
        // Phương thức này chỉ được gọi khi xóa thành công trên Android < 10
        // hoặc nếu bạn muốn gọi nó sau khi MediaStore.createDeleteRequest() hoàn tất
        // Với Android 10+, việc tải lại danh sách sẽ được thực hiện trong onActivityResult sau DELETE_PERMISSION_REQUEST_CODE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            loadVideos();
        }
        // Trên Android 10+, onActivityResult đã xử lý việc tải lại và đóng BottomSheet.
    }

    // Phương thức lọc video theo từ khóa tìm kiếm
    public void filterVideos(String query) {
        videoList.clear();
        if (query.isEmpty()) {
            videoList.addAll(originalVideoList); // Hiển thị lại tất cả video nếu query rỗng
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (VideoFile video : originalVideoList) {
                if (video.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    videoList.add(video);
                }
            }
        }
        videoAdapter.notifyDataSetChanged();
        // Cập nhật TextView hiển thị thông báo sau khi lọc
        if (videoList.isEmpty()) {
            noVideosFoundText.setText("Không tìm thấy video nào khớp với \"" + query + "\".");
            noVideosFoundText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noVideosFoundText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // Phương thức sắp xếp video
    public void sortVideos(SortOptionsBottomSheet.SortOption sortOption) {
        // Luôn sắp xếp trên danh sách `videoList` hiện tại (đã được lọc nếu có)
        // và sau đó cập nhật adapter.
        switch (sortOption) {
            case DATE_ASC:
                Collections.sort(videoList, Comparator.comparing(VideoFile::getCreationTime));
                break;
            case DATE_DESC:
                Collections.sort(videoList, (v1, v2) -> v2.getCreationTime().compareTo(v1.getCreationTime()));
                break;
            case NAME_ASC:
                Collections.sort(videoList, Comparator.comparing(VideoFile::getTitle));
                break;
            case NAME_DESC:
                Collections.sort(videoList, (v1, v2) -> v2.getTitle().compareTo(v1.getTitle()));
                break;
            case DURATION_ASC:
                Collections.sort(videoList, Comparator.comparingLong(VideoFile::getDuration));
                break;
            case DURATION_DESC:
                Collections.sort(videoList, (v1, v2) -> Long.compare(v2.getDuration(), v1.getDuration()));
                break;
        }
        videoAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Danh sách video đã được sắp xếp.", Toast.LENGTH_SHORT).show();
        // Không cần cập nhật noVideosFoundText ở đây vì việc sắp xếp không thay đổi số lượng video.
    }
}