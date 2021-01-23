package com.keuin.kbackupfabric.backup.incremental;

import com.keuin.kbackupfabric.backup.incremental.identifier.Sha256IdentifierConverter;
import com.keuin.kbackupfabric.util.backup.incremental.identifier.Sha256Identifier;

public class ObjectElementConverter {
    public static ObjectElement convert(com.keuin.kbackupfabric.util.backup.incremental.ObjectElement oldObjectElement) {
        try {
            return new ObjectElement(
                    oldObjectElement.getName(),
                    // in real world case, Sha256Identifier is the only used identifier in KBackup. So the cast is surely safe
                    Sha256IdentifierConverter.convert((Sha256Identifier) oldObjectElement.getIdentifier())
            );
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
