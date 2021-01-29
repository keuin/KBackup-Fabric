# KBackup-Fabric

A simple backup mod for **fabric** Minecraft server, which makes **normal `.zip` backup** of your world, or self-implemented **incremental backup**, with slower increasing disk usage.

一个简单的Fabric备份Mod，支持普通备份（将存档整体压缩为 `.zip` 文件，保存在 `backups` 目录下）和增量备份（按需保存到 `incremental` 目录下，并将目录树结构保存在 `backups` 目录下）

Supported Minecraft version: 1.14.4, 1.15.2, 1.16.4/1.16.5

[Fabric API](https://minecraft.curseforge.com/projects/fabric/files) is required!

## 1. Commands

(In English)

- **/kb**  or **/kb help**: show command list
- **/kb list**: show existing backups
- **/kb backup \[backup_name\]**: make a backup with given name or with the current system time by default
- **/kb incbak \[backup_name\]**: make an incremental backup which will be saved in `incremental` folder. (Incremental backup will create an index file which has an ext name of `.kbi`, and it will be saved in `backups` folder, which is the same with where `.zip` resides)
- **/kb restore \<backup_name\>**: restore to a certain backup. This command needs a confirmation to execute.
- **/kb confirm**: confirm executing restore operation. The operation is irreversible.
- **/kb delete**: delete an existing backup.
- **/kb prev**: Find and select the most recent backup file. After executing this command, you can use `/kb restore 1` to restore to this backup.

(In simplified Chinese, 简体中文版本)

- **/kb**  or **/kb help**: 显示命令列表
- **/kb list**: 显示所有已有的备份
- **/kb backup \[backup_name\]**: 以给定名字创建一个新备份，缺省的名字是“noname”
- **/kb incbak \[backup_name\]**: 创建一个增量备份，保存在 `incremental` 目录下。 (增量备份会创建扩展名为`.kbi`的一个索引文件，该文件仍被保存在 `backups` 目录下，与`.zip`文件的保存位置相同)
- **/kb restore \<backup_name\>**: 还原到指定的备份。该命令需要二次确认才会真正被执行
- **/kb confirm**: 二次确认，一旦确认，等待确认的命令会立刻被执行。这个命令是不可逆的
- **/kb delete**: 删除一个现有的备份
- **/kb prev**: 显示并且选中最近的一个备份，执行这个命令后，可以直接使用 `/kb restore 1` 进行还原

## 2. Script for auto-restart after restoring

Due to the nature of JVM: the Java language's running environment, there is no elegant way to restart Minecraft server in a server plugin. In order to auto restart after restoring, an outer system-based script is required, i.e. a batch or a shell script.

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

### 2.2 Script for Linux or U\*ix using shell (Not tested, I use Windows for the most time, test it on your own)

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
- Code refactor for maintainability
- Decoupling of plugin core and fabric api (preparing for Bukkit version)