package com.keuin.kbackupfabric.operation.backup;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.keuin.kbackupfabric.util.FilesystemUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;

public class IncrementalBackupUtil {
    /**
     * Generate a json object representing a directory and its all sub files and directories.
     * @param path path to the directory.
     * @return a json object.
     */
    public static JsonObject generateDirectoryJsonObject(String path) throws IOException {
        JsonObject json = new JsonObject();
        File directory = new File(path);
        if (!(directory.isDirectory() && directory.exists()))
            throw new IOException(String.format("Path %s is not a valid directory.", path));

        // Iterate all sub files using BFS.
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(path))) {
            for (Path sub : directoryStream) {
                if (sub.toFile().isFile()) {
                    // A sub file
                    // Just hash and add it as a string
                    try (InputStream is = Files.newInputStream(sub)) {
                        String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
                        json.addProperty(sub.getFileName().toString(), md5);
                    }
                } else {
                    // A sub directory
                    // Search into
                    json.addProperty(String.valueOf(sub.getFileName()), sub.toString());
                }
            }
        }

        return json;
    }

    /**
     * Save new (or modified) files to target path, based on hash json.
     * @param targetSavePath where we should save new files.
     * @param sourcePath where new files come from. This path must be the base directory of given hash json.
     * @param hashJson the json object obtained by calling generateDirectoryJsonObject method.
     * @return total size of new files. If failed, will return -1.
     */
    public static long saveNewFiles(String targetSavePath, String sourcePath, JsonObject hashJson) throws IOException {
        long bytesCopied = 0;
        for (Map.Entry<String, JsonElement> entry : hashJson.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                // A sub file
                // key is file name
                // value is file md5
                String md5 = value.getAsJsonPrimitive().getAsString();
                File saveTarget = new File(targetSavePath, md5);
                if (!saveTarget.exists()) {
                    // Target file does not exist. We have to copy this to the target.
                    File sourceFile = new File(sourcePath, key);
                    Files.copy(sourceFile.toPath(), saveTarget.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                    try {
                        bytesCopied += sourceFile.length();
                    } catch (SecurityException ignored) {
                        // failed to get the file size. Just ignore this.
                    }
                }
            } else if (value.isJsonObject()) {
                // A sub directory
                // key is directory name
                // value is directory json object
                // Go into
                if(!value.isJsonObject())
                    throw new IllegalArgumentException(String.format("Hash json contains illegal argument of a directory item: %s -> %s.", key, value));
                Path pathSource = Paths.get(sourcePath, key);
                bytesCopied += saveNewFiles(targetSavePath, pathSource.toString(), value.getAsJsonObject());
            } else {
                throw new IllegalArgumentException(String.format("Hash json contains illegal element: %s -> %s.", key, value));
            }
        }
        return bytesCopied;
    }
}
