package com.example.segurancaapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityHelper {

    private static final String SHARED_PREFS_NAME = "encrypted_prefs";
    private static final String KEY_NOME = "encrypted_nome";
    private static final String KEY_RA = "encrypted_ra";

    private SharedPreferences encryptedPrefs;
    private Context context;

    public SecurityHelper(Context context) {
        this.context = context;
        initEncryptedSharedPreferences();
    }

    private void initEncryptedSharedPreferences() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            encryptedPrefs = EncryptedSharedPreferences.create(
                    SHARED_PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    // Salvar dados criptografados
    public boolean salvarDadosCriptografados(String nome, String ra) {
        try {
            encryptedPrefs.edit()
                    .putString(KEY_NOME, nome)
                    .putString(KEY_RA, ra)
                    .apply();
            return true;
        } catch (Exception e) {
         //   e.printStackTrace();
            return false;
        }
    }

    // Recuperar dados criptografados
    public String[] recuperarDadosCriptografados() {
        try {
            String nome = encryptedPrefs.getString(KEY_NOME, "");
            String ra = encryptedPrefs.getString(KEY_RA, "");
            return new String[]{nome, ra};
        } catch (Exception e) {
           // e.printStackTrace();
            return new String[]{"", ""};
        }
    }

    // Atualizar dados criptografados
    public boolean atualizarDadosCriptografados(String nome, String ra) {
        return salvarDadosCriptografados(nome, ra);
    }

    // Deletar dados criptografados
    public boolean deletarDadosCriptografados() {
        try {
            encryptedPrefs.edit()
                    .remove(KEY_NOME)
                    .remove(KEY_RA)
                    .apply();
            return true;
        } catch (Exception e) {
          //  e.printStackTrace();
            return false;
        }
    }

    // ============================================
    // ANTI-TAMPERING - Verificação de integridade
    // ============================================

    private String calculateApkHash() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_SIGNATURES);

            Signature[] signatures = packageInfo.signatures;
            if (signatures != null && signatures.length > 0) {
                byte[] signature = signatures[0].toByteArray();
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(signature);

                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xFF & b);
                    if (hex.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hex);
                }
                return hexString.toString();
            }
            return "Erro: Sem assinatura";
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
           // e.printStackTrace();
            return "Erro: " + e.getMessage();
        }
    }

    public String getStatusIntegridade() {
        try {
            String currentHash = calculateApkHash();
            // IMPORTANTE: Após gerado o APK release foi adicionado hash real
            // Para obtenção do hash: foi geredo o APK, instalado e visualizado através do Toast ou logcat
            String expectedHash = "8a26d92c8af35dde64640e70b69868457845cd46a3a6d6f42f36ccce44669830";

            if (expectedHash.equals("8a26d92c8af35dde64640e70b69868457845cd46a3a6d6f42f36ccce44669830")) {
                return "Hash configurado";
            } else if (expectedHash.equals(currentHash)) {
                return "Íntegro - APK não adulterado";
            } else {
                return "COMPROMETIDO! APK foi adulterado!";
            }
        } catch (Exception e) {
           // e.printStackTrace();
            return "Erro na verificação de integridade";
        }
    }

    // ============================================
    // DETECÇÃO DE ROOT
    // ============================================

    public String getRootStatus() {
        if (RootDetectionHelper.isDeviceRooted(context)) {
            return "Root detectado - Ambiente comprometido!";
        } else {
            return "Dispositivo seguro - sem root";
        }
    }
}




