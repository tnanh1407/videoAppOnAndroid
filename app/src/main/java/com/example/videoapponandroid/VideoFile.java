package com.example.videoapponandroid;

import android.net.Uri;

public class VideoFile {
    private long id;
    private String title;
    private String path;
    private long duration; // in milliseconds
    private Uri uri; // URI để phát video
    private String creationTime; // Thêm trường này để lưu thời gian tạo

    public VideoFile(long id, String title, String path, long duration, Uri uri, String creationTime) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.duration = duration;
        this.uri = uri;
        this.creationTime = creationTime; // Gán giá trị thời gian tạo
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public Uri getUri() {
        return uri;
    }

    public String getCreationTime() { // Thêm getter cho thời gian tạo
        return creationTime;
    }

    // Setters (nếu cần thay đổi dữ liệu sau khi tạo)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
}