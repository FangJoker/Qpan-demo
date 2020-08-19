package com.chavez.qpan.adapater;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.chavez.qpan.R;
import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.util.support.file.FileConst;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @Author Chavez Qiu
 * @Date 19-12-30.
 * Email：qiuhao1@meizu.com
 * Description：文件列表RecyclerView 适配器
 */
public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileViewHolder> {
    public final static String TAG = "FileListAdapter";

    public final String SET_CHECKBOX_VISIBLE = "checkbox_is_visible";
    public final String SET_CHECKBOX_GONE = "checkbox_is_gone";
    public final String SET_CHECKED = "checkbox_is_checked";

    private List<BaseFileVO> mFileList;
    FileListAdapter.FileViewHolder holder;


    private Context mContext;
    private OnItemOnClickListener onItemOnClickListener;

    public FileListAdapter(Context context, List<BaseFileVO> fileList) {
        this.mContext = context;
        this.mFileList = fileList;
    }

    public void updateData(List<BaseFileVO> fileList) {
        this.mFileList = fileList;
        notifyDataSetChanged();
    }

    public interface OnItemOnClickListener {
        void itemOnClick(View item, int position);

        void itemOnLongClick(View item, int position);
    }

    public void setOnItemOnClickListener(OnItemOnClickListener onItemOnClickListener) {
        this.onItemOnClickListener = onItemOnClickListener;
    }


    @NonNull
    @Override
    public FileListAdapter.FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View fileView = LayoutInflater.from(mContext).inflate(R.layout.file_item, parent, false);
        holder = new FileViewHolder(fileView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull FileListAdapter.FileViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            BaseFileVO item = mFileList.get(position);
            for (Object p : payloads) {
                if (SET_CHECKBOX_VISIBLE.equals(p)) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                }
                if (SET_CHECKBOX_GONE.equals(p)) {
                    holder.checkBox.setVisibility(View.GONE);
                }
                if (SET_CHECKED.equals(p)) {
                    System.out.println("is check " + item.getChecked());
                    holder.checkBox.setChecked(item.getChecked());
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FileListAdapter.FileViewHolder holder, int position) {
        bindFileViewHolder(holder, position);
    }


    /**
     * An onBindViewHolder is called when the itemCount number changes
     * itemCount数目改变后会调用onBindViewHolder
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return mFileList == null ? 0 : mFileList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mFileList == null ? -1 : mFileList.get(position).getType();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView fileTitle;
        TextView fileCreateTime;
        TextView fileSize;
        CheckBox checkBox;
        View emptyView;
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
        BaseFileVO item = mFileList.get(position);
        holder.fileTitle.setText(item.getName());
        holder.fileSize.setText(String.valueOf(item.getFormatSize()));
        holder.fileCreateTime.setText(item.getLastModified());
        holder.root.setOnClickListener(v -> {
            Log.i(TAG, position + ": onClick");
            if (onItemOnClickListener != null) {
                onItemOnClickListener.itemOnClick(v, position);
            }
        });

        holder.root.setOnLongClickListener(v -> {
            Log.i(TAG, position + ": onLongClick");
            if (onItemOnClickListener != null) {
                onItemOnClickListener.itemOnLongClick(v, position);
            }
            return true;
        });

        Drawable icon;
        switch (item.getType()) {
            case FileConst.IS_FOLDER:
                icon = mContext.getResources().getDrawable(R.drawable.folder_icon);
                break;
            case FileConst.IS_AUDIO_FILE:
                icon = mContext.getResources().getDrawable(R.drawable.mp3_icon);
                break;
            case FileConst.IS_VIDEO_FILE:
                icon = mContext.getResources().getDrawable(R.drawable.mp4_icon);
                break;
            case FileConst.IS_APK_FILE:
                icon = mContext.getResources().getDrawable(R.drawable.apk_icon);
                break;
            case FileConst.IS_IMAGE_FILE:
                icon = mContext.getResources().getDrawable(R.drawable.image_icon);
                break;
            default:
                icon = mContext.getResources().getDrawable(R.drawable.file_iocn);
                break;
        }
        holder.fileIcon.setImageDrawable(icon);

        if (item.getChecked()) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }
    }

    public void setItemChecked(boolean checked, int position) {
        mFileList.get(position).setChecked(checked);
        // If you send multiple payload parameters, you end up with a list
        // 如果多次传payload参数，最后都会合并成一个list
        notifyItemChanged(position, SET_CHECKED);
        if (checked) {
            notifyItemChanged(position, SET_CHECKBOX_VISIBLE);
        } else {
            setItemCheckBoxGone(position);
        }
    }

    public void setItemCheckBoxGone(int position) {
        notifyItemChanged(position, SET_CHECKBOX_GONE);
    }

    public void setCancelSelectAll() {
        for (BaseFileVO item : mFileList) {
            item.setChecked(false);
        }
        notifyDataSetChanged();
    }

    public void setSelectAll() {
        for (BaseFileVO item : mFileList) {
            item.setChecked(true);
        }
        notifyDataSetChanged();
    }

}
