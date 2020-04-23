# KBackup-Fabric

A simple backup mod for fabric Minecraft server.

Minecraft version: 1.14.4

[Fabric API](https://minecraft.curseforge.com/projects/fabric/files) is required!

commands:

- */kb*  or */kb help*: show command list
- */kb list*: show existing backups
- */kb backup \[backup_name\]*: make a backup with given name or the current system time by default
- */kb restore \<backup_name\>*: restore to a certain backup


To-Do List:
- Optimize ZipUtil
- Optimize lag during the backup process (use async I/O)
- Optimize output format
- Implement incremental backup
