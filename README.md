# KBackup-Fabric

A simple backup mod for fabric Minecraft server.

Minecraft version: 1.14.4

[Fabric API](https://minecraft.curseforge.com/projects/fabric/files) is required!

commands:

- **/kb**  or **/kb help**: show command list
- **/kb list**: show existing backups
- **/kb backup \[backup_name\]**: make a backup with given name or the current system time by default
- **/kb restore \<backup_name\>**: restore to a certain backup. This command needs a confirm to execute.
- **/kb confirm**: confirm executing restore operation. The operation is irreversible.


To-Do List:

- More thorough test.
- Enhance ZipUtil for hashing sub-files and generating incremental diff-table (A:Add, M:Modification, D:Deletion)
- Optimize help menu. (colored command help menu)
- Implement incremental backup.
