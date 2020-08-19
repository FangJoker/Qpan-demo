package com.chavez.qpan.adapater;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chavez.qpan.R;
import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.util.support.file.FileConst;

import java.util.List;

public class SystemFileListAdapter extends RecyclerView.Adapter<SystemFileListAdapter.FileViewHolder> {
    static final String TAG = "SystemFileListAdapter";
    private Context mContext;
    private List<BaseFileVO> mFileList;
    ItemOnClickListener itemOnClickListener;
    ItemCheckBoxOnCheckListener itemCheckBoxOnCheckListener;
    FileViewHolder holder;

    public interface ItemOnClickListener {
        void onClick(View view, int position);
    }

    public interface ItemCheckBoxOnCheckListener {
        void onCheck(CompoundButton button, int position);
    }

    public SystemFileListAdapter(Context context, List<BaseFileVO> fileVOList) {
        this.mContext = context;
        this.mFileList = fileVOList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fileView = LayoutInflater.from(mContext).inflate(R.layout.file_item, parent, false);
        holder = new FileViewHolder(fileView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        bindFileViewHolder(holder, position);
    }

    public void setItemOnClickListener(ItemOnClickListener onClickListener) {
        this.itemOnClickListener = onClickListener;
    }

    public void setItemCheckBoxOnCheckListener(ItemCheckBoxOnCheckListener onCheckListener) {
        this.itemCheckBoxOnCheckListener = onCheckListener;
    }

    public void updateData(List<BaseFileVO> data) {
        if (data!=null){
            this.mFileList = data;
            for (BaseFileVO fileVO : mFileList) {
                fileVO.setChecked(false);
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mFileList != null ? mFileList.size() : 0;
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView fileTitle;
        TextView fileCreateTime;
        TextView fileSize;
        CheckBox checkBox;
        View root;

        public FileViewHolder(View v) {
            super(v);
            root = v;
            fileIcon = v.findViewById(R.id.file_icon);
            fileTitle = v.findViewById(R.id.file_title);
            fileCreateTime = v.findViewById(R.id.file_create_time);
            fileSize = v.findViewById(R.id.file_size);
            checkBox = v.findViewById(R.id.file_item_checkbox);
        }
    }

    private void bindFileViewHolder(FileViewHolder holder, int position) {
        System.out.println("===binding position:" + position);
        BaseFileVO item = mFileList.get(position);
        holder.fileTitle.setText(item.getName());
        holder.fileSize.setText(String.valueOf(item.getFormatSize()));
        holder.fileCreateTime.setText(item.getLastModified());
        holder.root.setOnClickListener(v -> {
            Log.i(TAG, position + ": onClick");
            if (itemOnClickListener != null) {
                itemOnClickListener.onClick(v, position);
            }
        });
        Drawable icon;
        switch (item.getType()) {
            case FileConst.IS_FOLDER:
                holder.checkBox.setVisibility(View.GONE);
                icon = mContext.getResources().getDrawable(R.drawable.folder_icon);
                break;
            case FileConst.IS_AUDIO_FILE:
                icon = mContext.getResources().getDrawable(R.drawable.mp3_icon);
                holder.checkBox.setVisibility(View.VISIBLE);
                break;
            case FileConst.IS_VIDEO_FILE:
                icon = mContext.getResources().getDrawable(R.drawable.mp4_icon);
                holder.checkBox.setVisibility(View.VISIBLE);
                break;
            case FileConst.IS_APK_FILE:
                icon = mContext.getResources().getDrawable(R.drawable.apk_icon);
                holder.checkBox.setVisibility(View.VISIBLE);
                break;
            case FileConst.IS_IMAGE_FILE:
                icon = mContext.getResources().getDrawable(R.drawable.image_icon);
                holder.checkBox.setVisibility(View.VISIBLE);
                break;
            default:
                icon = mContext.getResources().getDrawable(R.drawable.file_iocn);
                holder.checkBox.setVisibility(View.VISIBLE);
                break;
        }
        holder.fileIcon.setImageDrawable(icon);
        System.out.println("***position:" + position + "is check ?" + item.getChecked());
        /**
         *
         * Invoked automatically RecycleView rolling onCheckedChanged
         *
         * When the holder is reused, checkBox will call setChecked(false).
         * Performing checkBox setChecked will trigger the OnCheckedChange event
         *
         * RecycleView 滚动时自动调用 onCheckedChanged
         * 当holder 被重用的时候 checkbox 会调用setChecked(false)
         * 执行checkbox setChecked 会触发OnCheckedChange 事件
         */
        // Set to Null to avoid checkbox state confusion
        // 先设为Null 避免checkbox状态混乱
        holder.checkBox.setOnCheckedChangeListener(null);
        if (item.getChecked()) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                System.out.println("***position:" + position + " check");
                item.setChecked(true);
                holder.checkBox.setChecked(true);
            } else {
                System.out.println("***position:" + position + " uncheck");
                item.setChecked(false);
                holder.checkBox.setChecked(false);
            }
            if (itemCheckBoxOnCheckListener != null) {
                itemCheckBoxOnCheckListener.onCheck(buttonView, position);
            }
        });
    }
}
