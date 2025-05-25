package com.example.videoapponandroid;

// FolderAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.videoapponandroid.R;

import com.example.videoapponandroid.Folder; // Đảm bảo Folder là đúng package của bạn

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<Folder> folderList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Folder folder);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FolderAdapter(List<Folder> folderList) {
        this.folderList = folderList;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folderList.get(position);
        holder.textViewFolderName.setText(folder.getName());
        holder.textViewVideoCount.setText(folder.getVideoCount() + " video");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(folder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFolderName;
        TextView textViewVideoCount;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFolderName = itemView.findViewById(R.id.textViewFolderName);
            textViewVideoCount = itemView.findViewById(R.id.textViewVideoCount);
        }
    }

    // Cập nhật danh sách thư mục và thông báo cho adapter
    public void updateFolders(List<Folder> newFolders) {
        this.folderList.clear();
        this.folderList.addAll(newFolders);
        notifyDataSetChanged();
    }
}

