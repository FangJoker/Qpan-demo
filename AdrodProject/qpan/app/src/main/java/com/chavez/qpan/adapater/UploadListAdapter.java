package com.chavez.qpan.adapater;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chavez.qpan.R;
import com.chavez.qpan.model.UploadInfo;
import com.chavez.qpan.util.support.date.DateSupport;
import com.chavez.qpan.util.support.file.FileConst;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.UploadListHolder> {
    private List<UploadInfo> mUploadInfoList;
    private Context mContext;
    DecimalFormat df = new DecimalFormat("0.00");

    public UploadListAdapter(List<UploadInfo> uploadInfos, Context context) {
        mUploadInfoList = uploadInfos;
        mContext = context;
    }

    @NonNull
    @Override
    public UploadListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View uploadItemView = LayoutInflater.from(mContext).inflate(R.layout.upload_list_item, parent, false);
        return new UploadListHolder(uploadItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadListHolder holder, int position) {
        bindingUploadHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return mUploadInfoList != null ? mUploadInfoList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return mUploadInfoList != null ? mUploadInfoList.get(position).getType() : -1;
    }

    void bindingUploadHolder(UploadListHolder holder, int position) {
        UploadInfo uploadInfo = mUploadInfoList.get(position);
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
        holder.title.setText(uploadInfo.getTitle());
        holder.fileSizeTv.setText(uploadInfo.getFormatSize());
        holder.transitionActionIcon.setImageResource(R.drawable.stop);
        holder.progressBar.setMax(100);
        if (uploadInfo.getTotalBytes() - uploadInfo.getCurrentBytes() >= 0) {
            holder.fileCreateTimeTv.setVisibility(View.GONE);
            // 上传进度展示
            String persent = df.format(uploadInfo.getCurrentBytes().doubleValue() / uploadInfo.getTotalBytes().doubleValue());
            int progress = (int) (Float.valueOf(persent) * 100);
            holder.progressBar.setProgress(progress);
            holder.progressTv.setText(progress + "%");
        }
        if (uploadInfo.getStatus() == UploadInfo.SUCCESS)  {
            holder.fileCreateTimeTv.setText(uploadInfo.getCreateTime());
            holder.progressBar.setVisibility(View.GONE);
            holder.transitionActionIcon.setVisibility(View.GONE);
            holder.progressTv.setVisibility(View.GONE);
        }
    }

    class UploadListHolder extends RecyclerView.ViewHolder {
        ImageView fileIcon;
        TextView title;
        TextView progressTv;
        TextView fileSizeTv;
        TextView fileCreateTimeTv;
        ProgressBar progressBar;
        ImageView transitionActionIcon;

        public UploadListHolder(View v) {
            super(v);
            fileIcon = v.findViewById(R.id.file_icon);
            title = v.findViewById(R.id.file_title);
            progressBar = v.findViewById(R.id.transition_progress);
            transitionActionIcon = v.findViewById(R.id.transition_action_icon);
            progressTv = v.findViewById(R.id.progress_present);
            fileSizeTv = v.findViewById(R.id.file_size);
            fileCreateTimeTv = v.findViewById(R.id.file_create_time);
        }
    }

    public void updateData(List<UploadInfo> data) {
        this.mUploadInfoList = data;
        notifyDataSetChanged();
    }
}
