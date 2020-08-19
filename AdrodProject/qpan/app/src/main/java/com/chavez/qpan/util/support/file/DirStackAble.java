package com.chavez.qpan.util.support.file;

import com.chavez.qpan.model.BaseFileVO;

import java.util.List;

public interface DirStackAble<T> {
    void dirStackPush(List<BaseFileVO> baseFileVOList);

    List<T> dirStackPull();

    List<T> getCurDir();

    int getDirDepth();
}
