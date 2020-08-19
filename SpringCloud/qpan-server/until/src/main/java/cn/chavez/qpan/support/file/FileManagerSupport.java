package cn.chavez.qpan.support.file;


import cn.chavez.qpan.support.date.DateSupport;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/2/8 20:22
 */
public class FileManagerSupport {
    public static final int IS_FOLDER = 0;
    public static final int IS_NORMAL_FILE = 1;
    public static final int IS_AUDIO_FILE = 2;
    public static final int IS_VIDEO_FILE = 3;
    public static final int IS_APK_FILE = 4;

    public static List<BaseFileTo> getFileList(String path) {
        List<BaseFileTo> dirList = getDirList(path);
        return dirList;
    }

    /**
     * 返回指定目录下内容
     *
     * @param dirPath 路径
     */
    private static List<BaseFileTo> getDirList(String dirPath) {
        List<BaseFileTo> mDirList = new ArrayList<>();
        File dest = new File(dirPath);
        File[] files = dest.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.exists()) {
                    String name = file.getName();
                    int fileType = getFileType(name);
                    String path = file.getAbsolutePath();
                    String lastModify = DateSupport.getSimpleDateString(new Date(file.lastModified()));
                    long size = file.length();
                    int fileNameLength = name.length();
                    if (fileNameLength > 15) {
                        name = name.substring(0, 13) + "..." + name.substring(fileNameLength - 5, fileNameLength);
                    }
                    BaseFileTo fileItemVO = new BaseFileTo();
                    fileItemVO.setName(name);
                    fileItemVO.setPath(path);
                    fileItemVO.setSize(size);
                    fileItemVO.setLastModified(lastModify);
                    if (file.isDirectory()) {
                        fileItemVO.setType(FileConst.IS_FOLDER);
                    } else {
                        fileItemVO.setType(fileType);
                    }
                    mDirList.add(fileItemVO);
                }
            }
        }
        return mDirList;
    }


    private class FileConst {
        public static final int IS_FOLDER = 0;
        public static final int IS_NORMAL_FILE = 1;
        public static final int IS_AUDIO_FILE = 2;
        public static final int IS_VIDEO_FILE = 3;
        public static final int IS_APK_FILE = 4;
        public static final int IS_IMAGE_FILE = 5;
    }

    /**
     * 判断root目录下是否存在文件Path
     *
     * @param path
     * @param root
     * @return
     */
    public static Boolean isBelongToRoot(String path, String root) {
        if (path != null && root != null) {
            File file = new File(path);
            if (file.exists()) {
                String parentPath = file.getParent();
                System.out.println("parentPath: " + file.getParent());
                if (!root.equals(parentPath)) {
                    return isBelongToRoot(parentPath, root);
                } else {
                    return true;
                }
            } else {
                System.out.println("parentPath no exist");
                return false;
            }
        }
        return false;
    }

    /**
     * 某根目录下搜索文件
     *
     * @param rootDir
     * @param targetName
     * @param result     用于接收搜索结果的List
     */
    public static List<BaseFileTo> searchFileInRoot(String rootDir, String targetName, List<BaseFileTo> result) {
        File root = new File(rootDir);
        if (root.exists()) {
            if (root.isDirectory()) {
                File[] files = root.listFiles();
                for (File item : files) {
                    if (!item.isDirectory()) {
                        if (targetName.equals(item.getName()) || item.getName().contains(targetName)) {
                            BaseFileTo baseFileVO = new BaseFileTo();
                            baseFileVO.setName(item.getName());
                            baseFileVO.setSize(item.length());
                            baseFileVO.setLastModified(DateSupport.getSimpleDateString(new Date(item.lastModified())));
                            baseFileVO.setPath(item.getAbsolutePath());
                            int fileType = getFileType(item.getName());
                            baseFileVO.setType(fileType);
                            result.add(baseFileVO);
                        }
                    } else {
                        searchFileInRoot(item.getAbsolutePath(), targetName, result);
                    }
                }
                return result;
            }
        }
        return null;
    }

    public static int copyStream(InputStream in, OutputStream out, long copySize) {
        byte[] buffer = new byte[1024 * 4];
        int pos;
        int count = 0;
        try {
            while ((pos = (in.read(buffer))) != -1) {
                out.write(buffer);
                count += pos;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    public static int getFileType(String originalFilename) {
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        int fileType = FileConst.IS_NORMAL_FILE;
        if ("mp3".equals(suffix) || "wav".equals(suffix)) {
            fileType = FileConst.IS_AUDIO_FILE;
        } else if ("mp4".equals(suffix) || "avi".equals(suffix) || "wmv".equals(suffix)) {
            fileType = FileConst.IS_VIDEO_FILE;
        } else if ("apk".equals(suffix)) {
            fileType = FileConst.IS_APK_FILE;
        } else if ("jpg".equals(suffix) || "jpge".equals(suffix) || "png".equals(suffix) || "svg".equals(suffix)) {
            fileType = FileConst.IS_IMAGE_FILE;
        }
        return fileType;
    }

    public static void main(String[] args) {

    }
}
