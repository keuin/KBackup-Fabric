# Depreciation Notice (废弃声明)

**This project is DISCONTINUED. Only bug reporting issues will be accepted.**

**However, it is still a good choice for single-machine backup.**

I decide to discontinue my development in this project. There are some major reasons:

1. As one of my toy projects, its code quality is not satisfying and refactoring takes too much effort. The git history is bad too.
2. The Mod is proven to be working well (at least the latest version on my server, with Minecraft 1.16.5) as a basic backup and rollback tool for the game. All important features for a backup Mod are implemented.
3. I need a tool which backs up the save to the filesystem on another computer, via network.

To solve these problems, I'm working on a new backup and rollback tool for Minecraft, which is based on [rdiff-backup](https://github.com/rdiff-backup/rdiff-backup).

As a general incremental backup tool, it tends to be more solid and well-designed. Moreover, it is able to transfer backup files over network.
Thus, I believe it is a good start point to reimplement an incremental backup Mod for Minecraft.

However, this tool is still a good choice for any Minecraft server (from 1.14.4 to 1.17.1) who wants to back up its world locally.

(in simplified Chinese)

由于我想实现一个可以将存档备份到网络上另一台计算机上的备份Mod，而如果要给这个项目增加此功能且保证可靠，需要做大量的修改和测试，这个工作量是我无法承受的。
因此，我打算废弃这个项目，开始基于[rdiff-backup](https://github.com/rdiff-backup/rdiff-backup)开发一个新的备份Mod。

在新Mod完善之前，这个Mod仍会继续更新，不过只是在修复Bug方面。这个Mod已经持续开发了一年半，基本功能是十分可靠的，因此，如果你只是需要一个将存档
备份到本地的Mod，那么这仍然是一个可靠的选择。

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

- A more friendly help menu (colored command help menu)
- New version checker
- Code refactor for maintainability
- Decoupling of plugin core and fabric api (preparing for Bukkit version)