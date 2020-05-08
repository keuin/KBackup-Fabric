package com.keuin.kbackupfabric.util;

public class ServerRestartUtil {

    public static void forkAndRestart() {
//        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // Here we restart the minecraft server
//                StringBuilder cmd = new StringBuilder();
//                cmd.append(System.getProperty("java.home")).append(File.separator).append("bin").append(File.separator).append("java ");
//                for (String jvmArg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
//                    cmd.append(jvmArg + " ");
//                }
//                cmd.append("-cp ").append(ManagementFactory.getRuntimeMXBean().getClassPath()).append(" ");
//                cmd.append(MinecraftServer.class.getName()).append(" ");
//                for (String arg : args) {
//                    cmd.append(arg).append(" ");
//                }
//                Runtime.getRuntime().exec(cmd.toString());
//                System.exit(0);
//            }
//        }));
    }

    private static void startRestartThread() {
//        (new Thread(() -> {
//
//
//            // kill threads
//            Set<Thread> threads = Thread.getAllStackTraces().keySet();
//            Thread currentThread = Thread.currentThread();
//            for (Thread t : threads) {
//                if(t != currentThread && t.isAlive()) {
//                    t.setUncaughtExceptionHandler((t1, e) -> {
//                        // set empty handler
//                    });
//                    //t.interrupt();
//                    try {
//                        t.setDaemon(true);
//                    } catch (Exception ignored) {
//                    }
//
//                    t.stop();
//                }
//            }
//
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException ignored) {
//            }
//
//            // restart Minecraft server
//            String[] args = new String[]{};
//            MinecraftServer.main(args);
//        })).start();
    }
}
