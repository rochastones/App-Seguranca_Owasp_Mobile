package com.example.segurancaapp;

import android.content.Context;
import android.os.Build;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RootDetectionHelper {

    private static final String[] ROOT_PATHS = {
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
    };

    private static final String[] ROOT_PACKAGES = {
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.topjohnwu.magisk"
    };

    private static final String[] ROOT_COMMANDS = {
            "su",
            "busybox",
            "magisk"
    };

    public static boolean isDeviceRooted(Context context) {
        return checkRootPaths() || checkRootPackages(context) ||
                checkRootCommands() || checkBuildTags() || checkSuExists();
    }

    private static boolean checkRootPaths() {
        for (String path : ROOT_PATHS) {
            File file = new File(path);
            if (file.exists()) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkRootPackages(Context context) {
        for (String packageName : ROOT_PACKAGES) {
            try {
                context.getPackageManager().getPackageInfo(packageName, 0);
                return true;
            } catch (Exception e) {
                // Pacote não encontrado - epp de root não instalado
            }
        }
        return false;
    }

    private static boolean checkRootCommands() {
        for (String command : ROOT_COMMANDS) {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"which", command});
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                if (reader.readLine() != null) {
                    return true;
                }
            } catch (Exception e) {
                // Comando 'su' ou 'busybox' de root não encontrados no sistema
            }
        }
        return false;
    }

    private static boolean checkBuildTags() {
        String buildTags = Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkSuExists() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.getOutputStream().close();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getRootStatus(Context context) {
        if (isDeviceRooted(context)) {
            return "Dispositivo ROOT detectado - Ambiente comprometido!";
        } else {
            return "Dispositivo não está rooteado";
        }
    }
}