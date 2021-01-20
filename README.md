# KBackup-Fabric

A simple backup mod for fabric Minecraft server.

Minecraft version: 1.14.4, 1.15.2, 1.16.4

[Fabric API](https://minecraft.curseforge.com/projects/fabric/files) is required!

## 1. Commands

- **/kb**  or **/kb help**: show command list
- **/kb list**: show existing backups
- **/kb backup \[backup_name\]**: make a backup with given name or the current system time by default
- **/kb incbak \[backup_name\]**: make an incremental backup which will be saved in `incremental` folder. (However, the index file will be saved in `backups` folder)
- **/kb restore \<backup_name\>**: restore to a certain backup. This command needs a confirm to execute.
- **/kb confirm**: confirm executing restore operation. The operation is irreversible.
- **/kb delete**: delete an existing backup.
- **/kb prev**: Find and select the most recent backup file.

## 2. Script for auto-restart after restoring

Due to the nature of JVM: the Java language's running environment, there is no elegant way to restart Minecraft server in a server plugin. In order to achieve auto restarting, the outer system-based script is required, i.e the script is a batch or a shell script.

KBackup exit JVM with a special code `111` after restoring the level successfully. The startup script just check the exit code and restart Minecraft server if the code is `111`.

I will give examples for some popular operating systems. To use these scripts, you should replace your start.bat or start.sh script with given code lines.

### 2.1 Script for Windows

```batch
@echo off
title Keuin's personal Minecraft server
:loop
java -Xms4G -Xmx4G -jar fabric-server-launch.jar nogui
if %errorlevel%==111 goto loop
rem kbackup restore auto restart
pause
```

### 2.2 Script for Linux or U\*ix using shell

```shell
#!/bin/sh
STATUS=111
while [ $STATUS -eq 111 ]
do
    java -Xms4G -Xmx4G -jar fabric-server-launch.jar nogui
    STATUS=$?
done
```


## 3. To-Do List:

- Op login hint in the next start after restoring
- A more friendly help menu (colored command help menu)
- New version checker
- Code refactor
