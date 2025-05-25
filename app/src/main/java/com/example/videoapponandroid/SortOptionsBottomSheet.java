package com.example.videoapponandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast; // Import Toast để hiển thị thông báo tạm thời

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortOptionsBottomSheet extends BottomSheetDialogFragment {

    public interface SortOptionListener {
        void onSortOptionSelected(SortOption sortOption);
    }

    private SortOptionListener sortOptionListener;

    public void setSortOptionListener(SortOptionListener listener) {
        this.sortOptionListener = listener;
    }

    // CẬP NHẬT ENUM SORTOPTION VỚI CÁC TÙY CHỌN CHI TIẾT HƠN
    public enum SortOption {
        DATE_ASC,   // Sắp xếp theo ngày sớm nhất (cũ nhất)
        DATE_DESC,  // Sắp xếp theo ngày muộn nhất (mới nhất)
        NAME_ASC,   // Sắp xếp theo tên A-Z
        NAME_DESC,  // Sắp xếp theo tên Z-A
        DURATION_ASC, // Sắp xếp theo thời lượng tăng dần
        DURATION_DESC // Sắp xếp theo thời lượng giảm dần
    }

    public SortOptionsBottomSheet() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_sort_options, container, false);

        // Ánh xạ các LinearLayout cho từng tùy chọn sắp xếp
        LinearLayout optionSortByDateAsc = view.findViewById(R.id.optionSortByDateAsc);
        LinearLayout optionSortByDateDesc = view.findViewById(R.id.optionSortByDateDesc);
        LinearLayout optionSortByNameAsc = view.findViewById(R.id.optionSortByNameAsc);
        LinearLayout optionSortByNameDesc = view.findViewById(R.id.optionSortByNameDesc);
        LinearLayout optionSortByDurationAsc = view.findViewById(R.id.optionSortByDurationAsc);
        LinearLayout optionSortByDurationDesc = view.findViewById(R.id.optionSortByDurationDesc);


        // Thiết lập OnClickListener cho từng tùy chọn
        optionSortByDateAsc.setOnClickListener(v -> {
            if (sortOptionListener != null) {
                sortOptionListener.onSortOptionSelected(SortOption.DATE_ASC);
            }
            dismiss();
        });

        optionSortByDateDesc.setOnClickListener(v -> {
            if (sortOptionListener != null) {
                sortOptionListener.onSortOptionSelected(SortOption.DATE_DESC);
            }
            dismiss();
        });

        optionSortByNameAsc.setOnClickListener(v -> {
            if (sortOptionListener != null) {
                sortOptionListener.onSortOptionSelected(SortOption.NAME_ASC);
            }
            dismiss();
        });

        optionSortByNameDesc.setOnClickListener(v -> {
            if (sortOptionListener != null) {
                sortOptionListener.onSortOptionSelected(SortOption.NAME_DESC);
            }
            dismiss();
        });

        optionSortByDurationAsc.setOnClickListener(v -> {
            if (sortOptionListener != null) {
                sortOptionListener.onSortOptionSelected(SortOption.DURATION_ASC);
            }
            dismiss();
        });

        optionSortByDurationDesc.setOnClickListener(v -> {
            if (sortOptionListener != null) {
                sortOptionListener.onSortOptionSelected(SortOption.DURATION_DESC);
            }
            dismiss();
        });

        return view;
    }
}