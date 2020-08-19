package com.chavez.qpan.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.chavez.qpan.R;
import com.chavez.qpan.adapater.SystemFileListAdapter;
import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.util.support.file.FileManagerSupport;

import java.util.List;

public class SystemFileActivity extends AppCompatActivity {

    private RecyclerView mFileList;
    private String token;
    private FileManagerSupport fileManagerSupport = new FileManagerSupport();
    private SystemFileListAdapter systemFileListAdapter;
    private final static String  SYSTEM_PATH = "storage/emulated/0/";
    private List<BaseFileVO> fileVOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.token = getIntent().getExtras().getString("token");
        setContentView(R.layout.activity_system_file);
    }

    void findView(){
        mFileList = findViewById( android.R.id.list);
    }

    void initView(){
        fileVOS = fileManagerSupport.getFileList(SYSTEM_PATH);
        systemFileListAdapter = new SystemFileListAdapter(this,fileVOS);
    }

}
