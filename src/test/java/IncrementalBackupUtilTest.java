import com.keuin.kbackupfabric.operation.backup.IncrementalBackupUtil;

import java.io.IOException;

public class IncrementalBackupUtilTest {

    @org.junit.Test
    public void generateDirectoryJsonObject() {
        try {
            System.out.println(IncrementalBackupUtil.generateDirectoryJsonObject("D:\\1"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }
}