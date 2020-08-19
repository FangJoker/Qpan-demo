package com.chavez.qpan.util.support.file;

import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.util.common.LinkStack;

import java.io.File;
import java.util.List;

public class DirManager implements DirStackAble<BaseFileVO> {

    private LinkStack<List<BaseFileVO>> linkStack = new LinkStack();
//    private static DirManager instance;
//
//    private DirManager() {
//
//    }
//
//    public static DirManager getInstance() {
//        if (instance == null) {
//            synchronized (DirManager.class) {
//                if (instance == null) {
//                    return new DirManager();
//                }
//            }
//        }
//        return instance;
//    }

   public void deleteFile(BaseFileVO body){
        File file = new File(body.getPath());
        if (file.exists()){
            file.delete();
        }
    }

    @Override
    public void dirStackPush(List<BaseFileVO> baseFileVOList) {
        linkStack.push(baseFileVOList);
    }

    @Override
    public List<BaseFileVO> dirStackPull() {
        return linkStack.pull();
    }

    @Override
    public List<BaseFileVO> getCurDir() {
        return linkStack.getTopValue();
    }

    @Override
    public int getDirDepth() {
        return linkStack.getStackSize();
    }
}
