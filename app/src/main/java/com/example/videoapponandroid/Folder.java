//package com.example.videoapponandroid;
//
//// Folder.java
//public class Folder {
//    private String name;
//    private String path;
//    private int videoCount; // Thêm số lượng video
//
//    public Folder(String name, String path, int videoCount) {
//        this.name = name;
//        this.path = path;
//        this.videoCount = videoCount;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public String getPath() {
//        return path;
//    }
//
//    public int getVideoCount() {
//        return videoCount;
//    }
//
//    public void setVideoCount(int videoCount) {
//        this.videoCount = videoCount;
//    }
//}


// Đặt file này trong thư mục: com.example.videoapponandroid
package com.example.videoapponandroid;

public class Folder {
    private String name;
    private String path;
    private int videoCount;
    private String firstVideoThumbnailPath; // Thêm trường này để lưu đường dẫn ảnh thumbnail

    public Folder(String name, String path, int videoCount) {
        this.name = name;
        this.path = path;
        this.videoCount = videoCount;
        this.firstVideoThumbnailPath = null; // Khởi tạo ban đầu là null
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    // Getter và Setter cho firstVideoThumbnailPath
    public String getFirstVideoThumbnailPath() {
        return firstVideoThumbnailPath;
    }

    public void setFirstVideoThumbnailPath(String firstVideoThumbnailPath) {
        this.firstVideoThumbnailPath = firstVideoThumbnailPath;
    }
}