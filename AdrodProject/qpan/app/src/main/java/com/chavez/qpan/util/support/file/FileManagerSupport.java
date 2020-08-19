package com.chavez.qpan.util.support.file;

import com.chavez.qpan.model.BaseFileVO;
import com.chavez.qpan.model.FileItemVO;
import com.chavez.qpan.model.FolderVO;
import com.chavez.qpan.util.support.date.DateSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * @Author Chavez Qiu
 * @Date 19-12-27.
 * Email：qiuhao1@meizu.com
 * Description：文件遍历  Document traversal
 */
public class FileManagerSupport {

    private static List<BaseFileVO> mFileList = new ArrayList<>();
    private DirManager dirManager = new DirManager();


    public List<BaseFileVO> getFileList(String path) {
        List<BaseFileVO> dirList = getDirList(path);
        dirManager.dirStackPush(dirList);
        return dirList;
    }

    public DirManager getDirManager() {
        return dirManager;
    }

    /**
     * 返回指定目录下内容
     *
     * @param dirPath
     * @return
     */
    public static List<BaseFileVO> getDirList(String dirPath) {
        List<BaseFileVO> mDirList = new ArrayList<>();
        File dest = new File(dirPath);
        File[] files = dest.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.exists()) {
                    String name = file.getName();
                    String suffix = name.substring(name.lastIndexOf(".") + 1);
                    String path = file.getAbsolutePath();
                    String lastModify = DateSupport.getSimpleDateString(new Date(file.lastModified()));
                    long size = file.length();

                    int fileNameLength = name.length();
                    if (fileNameLength > 15) {
                        name = name.substring(0, 13) + "..." + name.substring(fileNameLength - 5, fileNameLength);
                    }
                    int fileType = FileConst.IS_NORMAL_FILE;
                    if ("mp3".equals(suffix) || "wav".equals(suffix)) {
                        fileType = FileConst.IS_AUDIO_FILE;
                    } else if ("mp4".equals(suffix) || "avi".equals(suffix) || "wmv".equals(suffix)) {
                        fileType = FileConst.IS_VIDEO_FILE;
                    } else if ("apk".equals(suffix)) {
                        fileType = FileConst.IS_APK_FILE;
                    }else if ("jpg".equals(suffix) || "jpge".equals(suffix) || "png".equals(suffix) || "svg".equals(suffix)) {
                        fileType = FileConst.IS_IMAGE_FILE;
                    }
                    FileItemVO fileItemVO = new FileItemVO();
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
        } else {
            System.out.println("path is invalid");
        }
        return mDirList;
    }


    /**
     * Recursively traverse the folder
     * 递归遍历文件夹
     *
     * @param filePath 路径
     * @param dir
     * @param data
     */
    private static void listFile(String filePath, FolderVO dir, List<BaseFileVO> data) {
        File dest = new File(filePath);
        File[] files = dest.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.exists()) {
                    String name = file.getName();
                    String suffix = name.substring(name.lastIndexOf(".") + 1);
                    String path = file.getAbsolutePath();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
                    String lastModify = sdf.format(new Date(file.lastModified()));
                    long size = file.length();

                    int fileNameLength = name.length();
                    if (fileNameLength > 15) {
                        name = name.substring(0, 13) + "..." + name.substring(fileNameLength - 5, fileNameLength);
                    }
                    int fileType = FileConst.IS_NORMAL_FILE;
                    if ("mp3".equals(suffix) || "wav".equals(suffix)) {
                        fileType = FileConst.IS_AUDIO_FILE;
                    } else if ("mp4".equals(suffix) || "avi".equals(suffix) || "wmv".equals(suffix)) {
                        fileType = FileConst.IS_VIDEO_FILE;
                    } else if ("apk".equals(suffix)) {
                        fileType = FileConst.IS_APK_FILE;
                    } else if ("jpg".equals(suffix) || "png".equals(suffix) || "jpge".equals(suffix)) {
                        fileType = FileConst.IS_IMAGE_FILE;
                    }
                    // file
                    // 文件
                    if (file.isFile()) {
                        FileItemVO fileItemVO = new FileItemVO();
                        fileItemVO.setName(name);
                        fileItemVO.setPath(path);
                        fileItemVO.setSize(size);
                        fileItemVO.setLastModified(lastModify);
                        fileItemVO.setType(fileType);
                        // Recursively traverse the folder
                        // 是否一级目录
                        if (dir != null) {
                            data.add(fileItemVO);
                        } else {
                            mFileList.add(fileItemVO);
                        }
                        // folder
                        // 文件夹
                    } else if (file.isDirectory()) {
                        List<BaseFileVO> folderData = new ArrayList<>();
                        FolderVO folderVO = new FolderVO();
                        folderVO.setName(name);
                        folderVO.setPath(path);
                        folderVO.setSize(size);
                        folderVO.setLastModified(lastModify);
                        folderVO.setData(folderData);
                        folderVO.setType(FileConst.IS_FOLDER);
                        //  Recursively traverse the folder
                        // 是否一级目录
                        if (dir != null) {
                            data.add(folderVO);
                        } else {
                            mFileList.add(folderVO);
                        }
                        // Recursive folder
                        // 递归文件夹
                        listFile(file.getAbsolutePath(), folderVO, folderData);
                    }
                }
            }
        }
    }


    public static boolean deleteFileIfExists(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return file.delete();
        } else {
            return false;
        }
    }

    public static void deleteFileIfExists(String[] fileNames) throws FileNotFoundException {
        for (int i = 0; i < fileNames.length; i++) {
            File file = new File(fileNames[i]);
            if (file.exists()) {
                try {
                    file.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                throw new FileNotFoundException("file " + fileNames[i] + " is not found");
            }
        }
    }

    public static int getFileType(File file) {
        String originalFilename = file.getName();
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

    public static String getFormatSize(Long size) {
        DecimalFormat df = new DecimalFormat("0.00");
        long byteToKB = size / 1024;
        long byteToMB = size / (1024 * 1024);
        long byteToGB = size / (1024 * 1024 * 1024);

        if (byteToKB >= 0 && byteToMB <= 0) {
            return df.format((float) size / 1024) + " KB";
        } else if (byteToMB >= 0 && byteToGB <= 0) {
            return df.format((float) size / (1024 * 1024)) + " MB";
        } else {
            return df.format((float) size / (1024 * 1024 * 1024)) + " GB";
        }
    }
}

