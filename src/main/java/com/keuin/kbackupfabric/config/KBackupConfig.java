package com.keuin.kbackupfabric.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.keuin.kbackupfabric.util.PrintUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class KBackupConfig {

    private static KBackupConfig instance = getDefault();
    private static final String CONFIG_FILE = "kbackup.json";

    @JsonProperty("incbak_cow")
    private Boolean incbakCow;

    public static KBackupConfig getInstance() {
        return instance;
    }

    private static KBackupConfig getDefault() {
        return new KBackupConfig(false);
    }

    public static void load(File file) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.enable(JsonParser.Feature.ALLOW_COMMENTS);
        try {
            instance = om.readValue(file, KBackupConfig.class);
        } catch (FileNotFoundException ignored) {
            // generate default config file
            PrintUtil.info("Config file does not exist. Creating default config: " + file.getAbsolutePath());
            instance = getDefault();
            ObjectWriter w = om.writerWithDefaultPrettyPrinter();
            w.writeValue(file, instance);
        }
    }

    public static void load() throws IOException {
        load(new File(CONFIG_FILE));
    }

    public KBackupConfig() {
    }

    public KBackupConfig(Boolean incbakCow) {
        this.incbakCow = incbakCow;
    }

    public Boolean getIncbakCow() {
        return this.incbakCow;
    }

    public void setIncbakCow(Boolean incbakCow) {
        this.incbakCow = incbakCow;
    }
}