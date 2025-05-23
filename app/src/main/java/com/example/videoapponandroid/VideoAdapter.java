package com.example.videoapponandroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final Context context;
    private final List<VideoFile> videoList; // Danh sách này sẽ là filteredVideoList từ OneFragment
    private final FragmentManager fragmentManager;

    public VideoAdapter(Context context, List<VideoFile> videoList, FragmentManager fragmentManager) {
        this.context = context;
        this.videoList = videoList;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoFile video = videoList.get(position);

        holder.videoTitle.setText(video.getTitle());
        holder.videoDuration.setText(formatDuration(video.getDuration()));
        holder.videoCreationTime.setText(video.getCreationTime()); // Hiển thị thời gian tạo

        // Load thumbnail video bằng Glide
        Glide.with(context)
                .load(video.getUri())
                .into(holder.videoThumbnail);

        holder.itemView.setOnClickListener(v -> {
            // Mở VideoPlayerActivity khi click vào item
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.setData(video.getUri()); // Truyền URI của video
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            // Hiển thị BottomSheet Options khi long click
            VideoOptionsBottomSheet bottomSheet = VideoOptionsBottomSheet.newInstance(video.getTitle(), video.getUri());
            if (fragmentManager != null) {
                bottomSheet.show(fragmentManager, VideoOptionsBottomSheet.TAG);
            } else {
                Toast.makeText(context, "Lỗi: FragmentManager không khả dụng.", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView videoThumbnail;
        TextView videoTitle;
        TextView videoDuration;
        TextView videoCreationTime; // <-- Thêm TextView cho thời gian tạo

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
            videoTitle = itemView.findViewById(R.id.videoTitle);
            videoDuration = itemView.findViewById(R.id.videoDuration);
            videoCreationTime = itemView.findViewById(R.id.videoCreationTime); // <-- Ánh xạ ID của TextView mới trong item_video.xml
        }
    }

    // Hàm tiện ích để định dạng thời lượng từ mili giây sang HH:MM:SS
    private String formatDuration(long milliseconds) {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }
}