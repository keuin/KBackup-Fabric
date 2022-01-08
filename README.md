# 0. README in another language

| Language     | File                               |
|--------------|------------------------------------|
| English (US) | [README.md](README.md)             |
| 简体中文         | [README_zh_CN.md](README_zh_CN.md) |

# 1. Inactive Development Notice

## TL;DR

- I have no time to keep active development in this project.
- Only bug reporting issues will be accepted.
- So if you want some new features, just clone and work on the code. Note that this project is licensed under GPLv3.

**Any PR is welcomed. Feel free to add your reasonable features, if you have written test cases for them.**

## A longer version

I decided to discontinue my development in this project. There are some major reasons:

1. As one of my toy projects, its code quality is not satisfying and refactoring takes too much effort. The git history
   is bad too.
2. The Mod is proven to be working well (at least the latest version on my server, with Minecraft 1.16.5) as a basic
   backup and rollback tool for the game. All important features for a backup Mod are implemented.
3. I need a tool which backs up the save to the filesystem on another computer, via network.

To solve these problems, I'm working on a new backup and rollback tool for Minecraft, which is based
on [rdiff-backup](https://github.com/rdiff-backup/rdiff-backup).

As a general incremental backup tool, it tends to be more solid and well-designed. Moreover, it is able to transfer
backup files over network. Thus, I believe it is a good start point to reimplement an incremental backup Mod for
Minecraft.

However, this tool is still a good choice for any Minecraft server (from 1.14.4 to 1.17.1) who wants to back up its
world locally.

# 2. Usages

A server-only backup mod for **fabric** Minecraft server, which makes **normal `.zip` backup** of your world, or
self-implemented **incremental backup**, with slower increasing disk usage.

Supported Minecraft version: 1.14.4, 1.15.2, 1.16.4/1.16.5, 1.17.1, 1.18.1

[Fabric API](https://minecraft.curseforge.com/projects/fabric/files) is required!

## 2.1 Commands

(In English)

- **/kb**  or **/kb help**: show command list
- **/kb list**: show existing backups
- **/kb backup \[backup_name\]**: make a backup with given name or with the current system time by default
- **/kb incbak \[backup_name\]**: make an incremental backup which will be saved in `incremental` folder. (Incremental
  backup will create an index file which has an ext name of `.kbi`, and it will be saved in `backups` folder, which is
  the same with where `.zip` resides)
- **/kb restore \<backup_name\>**: restore to a certain backup. This command needs a confirmation to execute.
- **/kb confirm**: confirm executing restore operation. The operation is irreversible.
- **/kb delete**: delete an existing backup.
- **/kb prev**: Find and select the most recent backup file. After executing this command, you can use `/kb restore 1`
  to restore to this backup.

Only OPs can make backups and restore by default.

However, you can use permission management mods like [LuckPerms](https://luckperms.net/) to configure exactly what
permissions normal players can use. Permission nodes of each command are listed below:

| Command    | Permission Required |
|------------|---------------------|
| /kb        | kb.root             |
| /kb help   | kb.help             |
| /kb list   | kb.list             |
| /kb backup | kb.backup           |
| /kb incbak | kb.incbak           |
| /kb restore | kb.restore          |
| /kb delete | kb.delete           |
| /kb confirm | kb.confirm          |
| /kb cancel | kb.cancel           |
| /kb prev   | kb.prev             |

## 2.2 Script for auto-restart after restoring

Due to the nature of JVM: the Java language's running environment, there is no elegant way to restart Minecraft server
in a server plugin. In order to auto restart after restoring, an outer system-based script is required, i.e. a batch or
a shell script.

KBackup exit JVM with a special code `111` after restoring the level successfully. The startup script just check the
exit code and restart Minecraft server if the code is `111`.

I will give examples for some popular operating systems. To use these scripts, you should replace your start.bat or
start.sh script with given code lines.

### 2.2.1 Script for Windows

```batch
@echo off
title Keuin's personal Minecraft server
:loop
java -Xms4G -Xmx4G -jar fabric-server-launch.jar nogui
if %errorlevel%==111 goto loop
rem kbackup restore auto restart
pause
```

### 2.2.2 Script for Linux or U\*ix using shell (Not tested, I use Windows for the most time, test it on your own)

```shell
#!/bin/sh
STATUS=111
while [ $STATUS -eq 111 ]
do
    java -Xms4G -Xmx4G -jar fabric-server-launch.jar nogui
    STATUS=$?
done
```

# 3. To-Do List

- A more friendly help menu (colored command help menu)
- New version checker
- Code refactor for maintainability
- Decoupling of plugin core and fabric api (preparing for Bukkit version)