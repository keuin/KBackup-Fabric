package com.keuin.kbackupfabric.backup.incremental.identifier;

import com.keuin.kbackupfabric.util.backup.incremental.identifier.SingleHashIdentifier;

import java.lang.reflect.Field;

public class Sha256IdentifierConverter {
    public static Sha256Identifier convert(com.keuin.kbackupfabric.util.backup.incremental.identifier.Sha256Identifier old) throws NoSuchFieldException, IllegalAccessException {
        Field field = ((SingleHashIdentifier) old).getClass().getSuperclass().getDeclaredField("hash");
        field.setAccessible(true);
        byte[] hash = (byte[]) field.get(old);
        return new Sha256Identifier(hash);
    }
}
