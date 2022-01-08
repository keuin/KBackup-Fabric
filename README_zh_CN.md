# 0. README in another language

| Language     | File                               |
|--------------|------------------------------------|
| English (US) | [README.md](README.md)             |
| 简体中文         | [README_zh_CN.md](README_zh_CN.md) |

# 1. 不活跃开发声明

## 太长不看版

- 我没有时间来保持活跃维护这个项目
- Issues里，只有反馈BUG的Issue会被我处理（欢迎其他贡献者来维护）
- 如果你想要新功能，请自己实现（注意遵守GPLv3协议）

**欢迎任何人的PR，放心大胆地添加你的feature，只要它是合理有用的。不过，请给你的新代码写测试。 :)**

## 长文阅读版

由于我想实现一个可以将存档备份到网络上另一台计算机上的备份Mod，而如果要给这个项目增加此功能且保证可靠，需要做大量的修改和测试，这个工作量是我无法承受的。
因此，我打算废弃这个项目，开始基于[rdiff-backup](https://github.com/rdiff-backup/rdiff-backup)开发一个新的备份Mod。

在新Mod完善之前，这个Mod仍会继续更新，不过只是在修复Bug方面。这个Mod已经持续开发了一年半，基本功能是十分可靠的，因此，如果你只是需要一个将存档 备份到本地的Mod，那么这仍然是一个可靠的选择。

# 2. 使用说明

一个仅服务端的Fabric备份Mod，支持普通备份（将存档整体压缩为 `.zip` 文件，保存在 `backups` 目录下）和增量备份（按需保存到 `incremental` 目录下，并将目录树结构保存在 `backups` 目录下）

支持的Minecraft版本：1.14.4、1.15.2、1.16.4/1.16.5、1.17.1、1.18.1

需要安装[Fabric API](https://minecraft.curseforge.com/projects/fabric/files)模组才可使用！

## 2.1 指令列表

- **/kb**  or **/kb help**: 显示命令列表
- **/kb list**: 显示所有已有的备份
- **/kb backup \[backup_name\]**: 以给定名字创建一个新备份，缺省的名字是“noname”
- **/kb incbak \[backup_name\]**: 创建一个增量备份，保存在 `incremental` 目录下。 (增量备份会创建扩展名为`.kbi`的一个索引文件，该文件仍被保存在 `backups`
  目录下，与`.zip`文件的保存位置相同)
- **/kb restore \<backup_name\>**: 还原到指定的备份。该命令需要二次确认才会真正被执行
- **/kb confirm**: 二次确认，一旦确认，等待确认的命令会立刻被执行。这个命令是不可逆的
- **/kb delete**: 删除一个现有的备份
- **/kb prev**: 显示并且选中最近的一个备份，执行这个命令后，可以直接使用 `/kb restore 1` 进行还原

默认情况下，只有OP才能备份和回档。

如果要详细配置你的玩家可以使用哪些命令，可以用像 [LuckPerms](https://luckperms.net/) 之类的权限管理插件来配置。KBackup的指令权限节点如下表所示：

| 指令          | 需要的权限      |
|-------------|------------|
| /kb         | kb.root    |
| /kb help    | kb.help    |
| /kb list    | kb.list    |
| /kb backup  | kb.backup  |
| /kb incbak  | kb.incbak  |
| /kb restore | kb.restore |
| /kb delete  | kb.delete  |
| /kb confirm | kb.confirm |
| /kb cancel  | kb.cancel  |
| /kb prev    | kb.prev    |

## 2.2 如何在回档后自动重启服务端

受限于JVM自身，MC的模组和插件无法优雅地重启自己。KBackup采取的自动重启方案是：

- MC服务端被用特殊的启动脚本启动。
- 回档完成后，MC服务端进程退出，退出代码为特殊值`111`。
- 启动脚本在MC服务端进程结束后检查返回值。如果是`111`，则重新启动服务端，否则结束脚本。

在Windows和Linux下都是可用的，而且POSIX兼容的操作系统应该都可以用。下文将给出Windows和Linux系统的示例脚本（分别为`.bat`批处理脚本和`.sh`外壳脚本）。

### 2.2.1 适用于Windows的自动重启脚本

```batch
@echo off
title Keuin's personal Minecraft server
:loop
java -Xms4G -Xmx4G -jar fabric-server-launch.jar nogui
if %errorlevel%==111 goto loop
rem kbackup restore auto restart
pause
```

### 2.2.2 适用于Linux的自动重启脚本

```shell
#!/bin/sh
STATUS=111
while [ $STATUS -eq 111 ]
do
    java -Xms4G -Xmx4G -jar fabric-server-launch.jar nogui
    STATUS=$?
done
```
