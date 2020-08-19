package com.chavez.qpan.adapater;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chavez.qpan.R;
import com.chavez.qpan.model.UserMainFunctionVO;

import java.util.ArrayList;
import java.util.List;

public class UserMainFunctionListAdapter extends BaseAdapter {
    List<UserMainFunctionVO> userMainFunctionVOList = new ArrayList<>();
    private Context mContext;

    public UserMainFunctionListAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<UserMainFunctionVO> data) {
        userMainFunctionVOList = data;
    }

    @Override
    public int getCount() {
        return userMainFunctionVOList == null ? 0 : userMainFunctionVOList.size();
    }

    @Override
    public Object getItem(int position) {
        return userMainFunctionVOList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return userMainFunctionVOList == null ? -1 : userMainFunctionVOList.get(position).getIndex();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserMainFunctionViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_main_function_list_item, null);
            holder = new UserMainFunctionViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (UserMainFunctionViewHolder) convertView.getTag();
        }
        bindingHolder(position, holder);
        return convertView;
    }

    class UserMainFunctionViewHolder {
        ImageView icon;
        TextView name;

        public UserMainFunctionViewHolder(View v) {
            icon = v.findViewById(R.id.user_main_function_list_item_icon);
            name = v.findViewById(R.id.user_main_function_list_item_name);
        }
    }

    private void bindingHolder(int position, UserMainFunctionViewHolder holder) {
        UserMainFunctionVO item = userMainFunctionVOList.get(position);
        holder.icon.setImageResource(item.getIconResourceId());
        holder.name.setText(mContext.getResources().getString(item.getIndexNameResourceId()));

    }
}
