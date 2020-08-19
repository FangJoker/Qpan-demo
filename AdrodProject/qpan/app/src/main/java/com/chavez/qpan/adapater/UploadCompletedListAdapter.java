package com.chavez.qpan.adapater;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chavez.qpan.R;
import com.chavez.qpan.model.UploadInfo;
import com.chavez.qpan.util.support.file.FileConst;
import java.util.List;

public class UploadCompletedListAdapter extends RecyclerView.Adapter<UploadCompletedListAdapter.UploadCompetedListHolder> {

    private List<UploadInfo> mUploadInfoList;
    private Context mContext;


    public UploadCompletedListAdapter(Context context, List<UploadInfo> infoList) {
        this.mContext = context;
        this.mUploadInfoList = infoList;
    }

    public void updateData(List<UploadInfo> data) {
        if (data != null) {
            this.mUploadInfoList = data;
            notifyDataSetChanged();
        }
    }


    @NonNull
    @Override
    public UploadCompetedListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  ItemView = LayoutInflater.from(mContext).inflate(R.layout.upload_completed_list_item, parent, false);
        return new UploadCompetedListHolder(ItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadCompetedListHolder holder, int position) {
        bindingUploadHolder(holder, position);
    }

    void bindingUploadHolder(UploadCompetedListHolder holder, int position) {
        UploadInfo uploadInfo = mUploadInfoList.get(position);
        holder.fileTitleTv.setText(uploadInfo.getTitle());
        holder.fileSizeTv.setText(uploadInfo.getFormatSize());
        holder.fileSizeTv.setText(uploadInfo.getFormatSize());
        holder.fileCreateTimeTv.setText(uploadInfo.getCreateTime());
        Drawable icon;
        switch (uploadInfo.getType()) {
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
    }

    @Override
    public int getItemCount() {
        return mUploadInfoList != null ? mUploadInfoList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mUploadInfoList != null ? mUploadInfoList.get(position).getType() : -1;
    }

    class UploadCompetedListHolder extends RecyclerView.ViewHolder {
        private ImageView fileIcon;
        private TextView fileTitleTv;
        private TextView fileCreateTimeTv;
        private TextView fileSizeTv;


        public UploadCompetedListHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.file_icon);
            fileTitleTv = itemView.findViewById(R.id.file_title);
            fileCreateTimeTv = itemView.findViewById(R.id.file_create_time);
            fileSizeTv = itemView.findViewById(R.id.file_size);
        }
    }
}
