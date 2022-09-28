package com.keuin.kbackupfabric.util;

import com.keuin.kbackupfabric.exception.ZipUtilException;
import com.keuin.kbackupfabric.metadata.BackupMetadata;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public final class ZipUtil {

    private static final int unzipBufferSize = 1024 * 1024 * 8; // 8MB
    private static final int zipBufferSize = 1024 * 1024 * 8; // 8MB

    /**
     * 递归压缩文件夹
     *
     * @param srcRootDir      压缩文件夹根目录的子路径
     * @param file            当前递归压缩的文件或目录对象
     * @param zipOutputStream 压缩文件存储对象
     * @param filesSkipping   被忽略的文件
     * @throws IOException IO Error
     */
    private static void zip(String srcRootDir, File file, ZipOutputStream zipOutputStream, Set<String> filesSkipping, final byte[] buffer) throws IOException {
        if (file == null) {
            return;
        }

        boolean skipping = Optional.ofNullable(filesSkipping).orElse(Collections.emptySet()).contains(file.getName())
                || file.getName().equals(BackupMetadata.metadataFileName);
        if (skipping)
            return; // Reject

        // 如果是文件，则直接压缩该文件
        if (file.isFile()) {
            int count;

            // 获取文件相对于压缩文件夹根目录的子路径
            String subPath = file.getAbsolutePath();
            int index = subPath.indexOf(srcRootDir);
            if (index != -1) {
                subPath = subPath.substring(srcRootDir.length() + File.separator.length());
            }

            // 写入压缩包
            ZipEntry entry = new ZipEntry(subPath);
            zipOutputStream.putNextEntry(entry);
//            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            try (InputStream inputStream = new FileInputStream(file)) {
                while ((count = inputStream.read(buffer, 0, zipBufferSize)) != -1) {
                    zipOutputStream.write(buffer, 0, count);
                }
            } finally {
                zipOutputStream.closeEntry();
            }
        } else {
            // 如果是目录，则压缩整个目录
            // 压缩目录中的文件或子目录
            File[] childFileList = file.listFiles();
            if (childFileList != null) {
                for (File value : childFileList)
                    zip(srcRootDir, value, zipOutputStream, filesSkipping, buffer);
            }
        }
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param srcPath     要压缩的源文件路径。如果是目录，则将递归压缩这个目录及其所有子文件、子目录树。
     * @param zipPath     压缩文件保存的路径。注意：zipPath不能是srcPath路径下的子文件夹
     * @param zipFileName 压缩文件名
     * @throws IOException      IO Error
     * @throws ZipUtilException General exception, such as loop recursion.
     */
    public static void makeBackupZip(String srcPath, String zipPath, String zipFileName, BackupMetadata backupMetadata, int zipLevel) throws IOException, ZipUtilException {
        Objects.requireNonNull(srcPath);
        Objects.requireNonNull(zipPath);
        Objects.requireNonNull(zipFileName);
        Objects.requireNonNull(backupMetadata);
        Objects.requireNonNull(srcPath);
        if (srcPath.isEmpty()) {
            throw new IllegalArgumentException("srcPath cannot be empty");
        }
        if (zipPath.isEmpty()) {
            throw new IllegalArgumentException("zipPath cannot be empty");
        }
        if (zipFileName.isEmpty()) {
            throw new IllegalArgumentException("zipFileName cannot be empty");
        }

        File srcFile = new File(srcPath);

        //判断压缩文件保存的路径是否为源文件路径的子文件夹，如果是，则抛出异常（防止无限递归压缩的发生）
        if (srcFile.isDirectory() && zipPath.contains(srcPath)) {
            throw new ZipUtilException("Detected loop recursion in directory structure, please check symlink linking to parent directories.");
        }

        //判断压缩文件保存的路径是否存在，如果不存在，则创建目录
        File zipDir = new File(zipPath);
        if (!zipDir.exists() || !zipDir.isDirectory()) {
            if (!zipDir.mkdirs()) {
                throw new IOException(String.format("Failed to make directory tree %s", zipDir.toString()));
            }
        }

        //创建压缩文件保存的文件对象
        String zipFilePath = zipPath + File.separator + zipFileName;
        File zipFile = new File(zipFilePath);
        if (zipFile.exists()) {
            //删除已存在的目标文件
            if (!zipFile.delete()) {
                throw new IOException(String.format("Failed to delete existing file %s", zipFile.toString()));
            }
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
             CheckedOutputStream checkedOutputStream = new CheckedOutputStream(fileOutputStream, new CRC32());
             ZipOutputStream zipOutputStream = new ZipOutputStream(checkedOutputStream)) {
            zipOutputStream.setLevel(zipLevel);

            // If with backup metadata, we serialize it and write it into file "kbackup_metadata"
            ZipEntry metadataEntry = new ZipEntry(BackupMetadata.metadataFileName);

            zipOutputStream.putNextEntry(metadataEntry);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(backupMetadata);
                zipOutputStream.write(baos.toByteArray());
            } finally {
                zipOutputStream.closeEntry();
            }

            //如果只是压缩一个文件，则需要截取该文件的父目录
            String srcRootDir = srcPath;
            if (srcFile.isFile()) { // (Disabled) Hack this stupid setting. We want to keep our least parent folder!
                int index = srcPath.lastIndexOf(File.separator);
                if (index != -1) {
                    srcRootDir = srcPath.substring(0, index);
                }
            }

            //调用递归压缩方法进行目录或文件压缩
            zip(srcRootDir, srcFile, zipOutputStream, Collections.singleton("session.lock"), new byte[zipBufferSize]);
            zipOutputStream.flush();
        }
    }

    public static void makeBackupZip(String srcPath, String zipPath, String zipFileName, BackupMetadata backupMetadata) throws IOException, ZipUtilException {
        makeBackupZip(srcPath, zipPath, zipFileName, backupMetadata, Deflater.BEST_SPEED);
    }

    /**
     * 解压缩zip包
     *
     * @param zipFilePath        zip文件的全路径
     * @param unzipFilePath      解压后的文件保存的路径
     * @param includeZipFileName 解压后的文件保存的路径是否包含压缩文件的文件名。true-包含；false-不包含
     */
    public static void unzip(String zipFilePath, String unzipFilePath, boolean includeZipFileName) throws IOException {
        final byte[] buffer = new byte[unzipBufferSize];
        if (zipFilePath.isEmpty() || unzipFilePath.isEmpty()) {
            throw new IllegalArgumentException("Parameter for unzip() contains null.");
        }
        File zipFile = new File(zipFilePath);
        // 如果解压后的文件保存路径包含压缩文件的文件名，则追加该文件名到解压路径
        if (includeZipFileName) {
            String fileName = zipFile.getName();
            if (!fileName.isEmpty()) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            unzipFilePath = unzipFilePath + File.separator + fileName;
        }
        // 创建解压缩文件保存的路径
        File unzipFileDir = new File(unzipFilePath);
        if (!unzipFileDir.exists() || !unzipFileDir.isDirectory()) {
            if (!unzipFileDir.mkdirs())
                throw new IOException(String.format("Failed to make directory tree %s", unzipFileDir.toString()));
        }

        // 开始解压
        ZipEntry entry;
        String entryFilePath, entryDirPath;
        File entryFile, entryDir;
        int index, count;
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            // 循环对压缩包里的每一个文件进行解压
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                // 构建压缩包中一个文件解压后保存的文件全路径
                entryFilePath = unzipFilePath + File.separator + entry.getName();

                // 构建解压后保存的文件夹路径
                index = entryFilePath.lastIndexOf(File.separator);
                if (index != -1) {
                    entryDirPath = entryFilePath.substring(0, index);
                } else {
                    entryDirPath = "";
                }
                entryDir = new File(entryDirPath);
                // 如果文件夹路径不存在，则创建文件夹
                if (!entryDir.exists() || !entryDir.isDirectory()) {
                    if (!entryDir.mkdirs())
                        throw new IOException(String.format("Failed to make directory tree %s", entryDir.toString()));
                }

                // 创建解压文件
                entryFile = new File(entryFilePath);
                if (entryFile.exists()) {
                    // 删除已存在的目标文件
                    if (!entryFile.delete())
                        throw new IOException(String.format("Failed to delete existing file %s", entryFile.toString()));
                }
                if (entry.isDirectory()) {
                    // If the entry is a directory, we make its corresponding directory.
                    if (!entryFile.mkdir())
                        throw new IOException(String.format("Failed to create directory %s", entryFile.toString()));
                } else {
                    // Is a file, we write the data
                    // 写入文件
                    try (OutputStream outputStream = new FileOutputStream(entryFile);
                         InputStream inputStream = zip.getInputStream(entry)) {
                        while ((count = inputStream.read(buffer, 0, unzipBufferSize)) != -1) {
                            outputStream.write(buffer, 0, count);
                        }
                        outputStream.flush();
                    }
                }
            }
        }
    }
}