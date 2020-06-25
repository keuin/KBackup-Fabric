# KBackup-Fabric

A simple backup mod for fabric Minecraft server.

Minecraft version: 1.15.2

[Fabric API](https://minecraft.curseforge.com/projects/fabric/files) is required!

commands:

- **/kb**  or **/kb help**: show command list
- **/kb list**: show existing backups
- **/kb backup \[backup_name\]**: make a backup with a given name, or the current system time will be used by default
- **/kb restore \<backup_name\>**: restore to a certain backup. This command needs a confirmation to execute.
- **/kb confirm**: confirm executing restore operation. The operation is irreversible.
- **/kb delete**: delete an existing backup.
- **/kb prev**: Find and select the most recent backup file.


To-Do List:

- New version checker.
- Refactor code.
- More thorough test.
- Implement incremental backup.
    + Restore: trace-back (recursively, then generate file dependence tree)
        - Implement unZipRecursively (unzip a .zip.inc file recursively until reaches the root (i.e. the last full backup).)
    + Backup: base-diff (select most recently backup as the base, then diff)
        - Implement zipDiff (make a new zip with the latest backup as the base, store diff-table in zip comment (A:Add, M:Modification, D:Deletion))
- Optimize help menu. (colored command help menu)
- Add op login hint in the next start after restoring.
- Implement incremental backup.
