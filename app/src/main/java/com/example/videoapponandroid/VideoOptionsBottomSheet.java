package com.example.videoapponandroid;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.PendingIntent;
import android.content.IntentSender;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class VideoOptionsBottomSheet extends BottomSheetDialogFragment {

    // Thêm một TAG cho BottomSheet này để Fragment cha có thể tìm và đóng nó
    public static final String TAG = "VideoOptionsBottomSheetTag";

    private static final int DELETE_PERMISSION_REQUEST_CODE = 101;
    private static final String ARG_VIDEO_TITLE = "video_title";
    private static final String ARG_VIDEO_URI = "video_uri";

    private String videoTitle;
    private Uri videoUri;

    // Interface để giao tiếp với Fragment cha (OneFragment)
    public interface VideoOptionListener {
        void onVideoDeleted();
    }

    private VideoOptionListener listener;

    // Phương thức factory để tạo instance của BottomSheetDialogFragment
    public static VideoOptionsBottomSheet newInstance(String videoTitle, Uri videoUri) {
        VideoOptionsBottomSheet fragment = new VideoOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_TITLE, videoTitle);
        args.putParcelable(ARG_VIDEO_URI, videoUri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Đảm bảo Fragment cha đã implement interface VideoOptionListener
        if (getParentFragment() instanceof VideoOptionListener) {
            listener = (VideoOptionListener) getParentFragment();
        } else {
            // Ném lỗi nếu Fragment cha không implement interface
            throw new RuntimeException(context.toString() + " must implement VideoOptionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Giải phóng listener khi Fragment bị tách
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoTitle = getArguments().getString(ARG_VIDEO_TITLE);
            videoUri = getArguments().getParcelable(ARG_VIDEO_URI);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_video_options, container, false);

        TextView bottomSheetTitle = view.findViewById(R.id.bottomSheetTitle);
        if (videoTitle != null) {
            bottomSheetTitle.setText(videoTitle);
        } else {
            bottomSheetTitle.setText("1 Đã chọn");
        }

        LinearLayout optionShare = view.findViewById(R.id.optionShare);
        LinearLayout optionAddTo = view.findViewById(R.id.optionAddTo);
        LinearLayout optionDelete = view.findViewById(R.id.optionDelete);

        optionShare.setOnClickListener(v -> {
            shareVideo();
            dismiss(); // Đóng dialog sau khi chọn
        });

        optionAddTo.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng 'Thêm vào danh sách phát' đang được phát triển.", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        optionDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog(); // Hiển thị dialog xác nhận trước khi xóa
        });

        return view;
    }

    /**
     * Phương thức để chia sẻ video.
     */
    private void shareVideo() {
        if (videoUri == null || getContext() == null) {
            Toast.makeText(getContext(), "Không thể chia sẻ video.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*"); // Chỉ định loại nội dung là video
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri); // Đính kèm URI của video
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Cấp quyền đọc URI tạm thời cho ứng dụng nhận

        // Kiểm tra xem có ứng dụng nào có thể xử lý Intent chia sẻ không
        if (shareIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ video bằng"));
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ứng dụng nào để chia sẻ video.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị dialog xác nhận xóa video.
     * Sử dụng AlertDialog để đảm bảo người dùng xác nhận hành động quan trọng này.
     */
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa video này khỏi thiết bị?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteVideo(); // Gọi hàm xóa khi người dùng xác nhận
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss(); // Đóng dialog hủy
                    dismiss(); // Đóng BottomSheet
                })
                .show();
    }

    /**
     * Xóa video khỏi thiết bị.
     * Cần quyền WRITE_EXTERNAL_STORAGE (cho API < 29) hoặc xử lý MediaStore.createDeleteRequest() (cho API >= 29).
     */
    private void deleteVideo() {
        if (videoUri == null || getContext() == null) {
            Toast.makeText(getContext(), "Không thể xóa video: URI không hợp lệ.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        ContentResolver contentResolver = getContext().getContentResolver();
        Cursor cursor = null; // Khai báo cursor ngoài try-finally để đảm bảo nó luôn được đóng

        try {
            // Kiểm tra xem URI có hợp lệ và tồn tại trong MediaStore không
            cursor = contentResolver.query(videoUri, new String[]{MediaStore.Video.Media._ID}, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                Toast.makeText(getContext(), "Video không tồn tại hoặc không thể truy cập.", Toast.LENGTH_SHORT).show();
                dismiss();
                return;
            }
            // Không đóng cursor ở đây, nó sẽ được đóng trong khối finally

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Đối với Android 10 (API 29) trở lên
                List<Uri> urisToModify = new ArrayList<>();
                urisToModify.add(videoUri);
                // Tạo một PendingIntent để yêu cầu người dùng cấp quyền xóa
                PendingIntent editPendingIntent = MediaStore.createDeleteRequest(getContext().getContentResolver(), urisToModify);
                try {
                    // SỬA LỖI Ở ĐÂY: Thêm tham số 'Bundle options' cuối cùng (null)
                    getParentFragment().startIntentSenderForResult(editPendingIntent.getIntentSender(), DELETE_PERMISSION_REQUEST_CODE, null, 0, 0, 0, null);
                    // KHÔNG dismiss() ở đây. OneFragment sẽ chịu trách nhiệm đóng BottomSheet sau khi nhận kết quả từ onActivityResult.
                } catch (IntentSender.SendIntentException ex) {
                    Log.e("VideoOptionsBottomSheet", "Lỗi khi gửi yêu cầu xóa: " + ex.getMessage());
                    Toast.makeText(getContext(), "Lỗi khi gửi yêu cầu xóa.", Toast.LENGTH_LONG).show();
                    dismiss(); // Dismiss nếu có lỗi khi gửi yêu cầu
                }
            } else {
                // Đối với Android 9 (API 28) trở xuống
                int rowsDeleted = getContext().getContentResolver().delete(videoUri, null, null);
                if (rowsDeleted > 0) {
                    Toast.makeText(getContext(), "Video đã được xóa thành công.", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onVideoDeleted(); // Gọi callback để OneFragment cập nhật UI
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể xóa video. Có thể bạn không có quyền hoặc tệp không tồn tại.", Toast.LENGTH_LONG).show();
                }
                dismiss(); // Dismiss ở đây cho Android < 10 vì không có hộp thoại hệ thống bổ sung
            }
        } catch (SecurityException e) {
            Log.e("VideoOptionsBottomSheet", "Lỗi quyền khi xóa video (trước Android 10): " + e.getMessage());
            Toast.makeText(getContext(), "Lỗi quyền: Không thể xóa video. Vui lòng cấp quyền hoặc thử lại.", Toast.LENGTH_LONG).show();
            dismiss(); // Dismiss nếu có lỗi quyền
        } catch (Exception e) { // Bắt tất cả các Exception khác
            Log.e("VideoOptionsBottomSheet", "Lỗi chung khi xóa video: " + e.getMessage());
            Toast.makeText(getContext(), "Lỗi khi xóa video: " + e.getMessage(), Toast.LENGTH_LONG).show();
            dismiss(); // Dismiss nếu có lỗi chung
        } finally {
            if (cursor != null) {
                cursor.close(); // Đảm bảo cursor luôn được đóng
            }
        }
    }
}
