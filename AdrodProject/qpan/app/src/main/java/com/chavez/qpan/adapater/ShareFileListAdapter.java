package com.chavez.qpan.adapater;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chavez.qpan.R;
import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.model.ShareFileVo;
import com.chavez.qpan.util.support.file.FileConst;
import com.chavez.qpan.util.support.file.FileManagerSupport;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @Author Chavez Qiu
 * @Date 20-5-14.
 * Email：qiuhao1@meizu.com
 * Description：
 */
public class ShareFileListAdapter extends RecyclerView.Adapter<ShareFileListAdapter.ShareFileViewHolder> {
    private List<ShareFileVo> mFileVOList;
    private Context mContext;
    private ItemOnClickListener itemOnClickListener;

    public interface  ItemOnClickListener{
        void onClick(View itemView,int position);
    }

    public ShareFileListAdapter(Context context, List<ShareFileVo> baseFileVOS) {
        this.mContext = context;
        this.mFileVOList = baseFileVOS;
    }

    public void setItemOnClickListener(ItemOnClickListener listener){
        this.itemOnClickListener = listener;
    }

    @NonNull
    @Override
    public ShareFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.share_file_item, parent, false);
        return new ShareFileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareFileViewHolder holder, int position) {
        bindingHolder(holder,position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mFileVOList != null) {
            return mFileVOList.get(position) != null ? mFileVOList.get(position).getType() : -1;
        } else {
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return mFileVOList != null ? mFileVOList.size() : 0;
    }

    private void bindingHolder(ShareFileViewHolder holder, int position){
        ShareFileVo item = mFileVOList.get(position);
        Drawable icon;
        System.out.println("TYPE:"+getItemViewType(position));
        switch (getItemViewType(position)) {
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
        holder.icon.setImageDrawable(icon);
        holder.title.setText(item.getFileName());
        holder.size.setText(FileManagerSupport.getFormatSize(item.getTotalBytes()));
        holder.author.setText(item.getAuthorName());
        if (itemOnClickListener!=null){
            holder.itemLayout.setOnClickListener(v -> itemOnClickListener.onClick(v,position));
        }
    }

    public void  updateData(List<ShareFileVo> data){
        if (data!=null){
            this.mFileVOList = data;
            notifyDataSetChanged();
        }
    }
    class ShareFileViewHolder extends RecyclerView.ViewHolder {
        View itemLayout;
        ImageView icon;
        TextView title;
        TextView size;
        TextView author;

        public ShareFileViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView;
            icon = itemView.findViewById(R.id.file_icon);
            title = itemView.findViewById(R.id.file_title);
            size = itemView.findViewById(R.id.file_size);
            author = itemView.findViewById(R.id.file_author);
        }
    }
}
