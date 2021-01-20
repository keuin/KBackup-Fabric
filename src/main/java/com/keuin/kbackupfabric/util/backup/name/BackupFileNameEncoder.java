package com.keuin.kbackupfabric.util.backup.name;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Encode and decode backup file name for a specific backup type.
 */
public interface BackupFileNameEncoder {

    /**
     * Construct full backup file name from custom name and creation time.
     * @param customName the custom name. If the custom name contains invalid chars, an exception will be thrown.
     * @param time the creation time.
     * @return the file name.
     */
    String encode(String customName, LocalDateTime time);

    /**
     * Extract custom and backup time from backup file name.
     *
     * @param fileName the backup file name.
     * @return the information. If the given file name is invalid, return null.
     */
    BackupBasicInformation decode(String fileName);

    default boolean isValidFileName(String fileName) {
        return decode(fileName) != null;
    }

    /**
     * Check if the given string is a valid custom backup name.
     *
     * @param customName the custom backup name.
     * @return if the name is valid.
     */
    default boolean isValidCustomName(String customName) {
        final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};
        for (char c : ILLEGAL_CHARACTERS) {
            if (customName.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    class BackupBasicInformation {

        public final String customName;
        public final LocalDateTime time;

        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm.ss");

        protected BackupBasicInformation(String customName, LocalDateTime time) {
            this.customName = customName;
            this.time = time;
        }

        @Override
        public String toString() {
            return String.format("%s, %s", customName, time.format(formatter));
        }
    }
}
