package com.dashlane.util.keychain;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.dashlane.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import androidx.annotation.Nullable;

public class KeyChainManager {
    private KeyStore mKeyStore;

    private KeyChainManager() {
    }

    @Nullable
    public static KeyChainManager createInstance() {
        KeyChainManager m = new KeyChainManager();
        boolean initialized = m.initKeyStore();
        if (initialized) {
            return m;
        } else {
            return null;
        }
    }

    @Nullable
    public String encryptBytes(String alias, byte[] bytes) throws KeyChainException {
        try {
            if (bytes == null || bytes.length == 0 || !StringUtils.isNotSemanticallyNull(alias)) {
                return null;
            }
            loadKeyForUser(alias);
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(alias, null);
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();

            Cipher inCipher = getCipher();
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(
                    outputStream, inCipher);
            cipherOutputStream.write(bytes);
            cipherOutputStream.close();

            byte[] vals = outputStream.toByteArray();
            return Base64.encodeToString(vals, Base64.DEFAULT);

        } catch (Exception e) {
            throw new KeyChainException(e);
        }

    }

    @Nullable
    public byte[] decryptString(String alias, String encrypted) throws KeyChainException {
        if (!StringUtils.isNotSemanticallyNull(encrypted) || !StringUtils.isNotSemanticallyNull(alias)) {
            return null;
        }
        try {
            loadKeyForUser(alias);

            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(alias, null);
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();

            Cipher output = getCipher();
            output.init(Cipher.DECRYPT_MODE, privateKey);

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(encrypted, Base64.DEFAULT)),
                    output);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i);
            }

            return bytes;

        } catch (Exception e) {
            throw new KeyChainException(e);
        }

    }

    private boolean initKeyStore() {
        
        
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void loadKeyForUser(String userAlias) throws KeyChainException {
        try {
            
            boolean containsAlias = mKeyStore.containsAlias(userAlias);
            if (containsAlias && !isValidPrivatePublicKey(userAlias)) {
                mKeyStore.deleteEntry(userAlias);
            }
            if (!containsAlias) {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");

                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(userAlias,
                                                                           KeyProperties.PURPOSE_ENCRYPT |
                                                                           KeyProperties.PURPOSE_DECRYPT)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .build();
                generator.initialize(spec);

                
                generator.generateKeyPair();
            }
        } catch (Exception e) {
            throw new KeyChainException(e);
        }
    }

    
    
    @SuppressWarnings("squid:S4787")
    private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException {
        return Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround");
    }

    private boolean isValidPrivatePublicKey(String userAlias) {
        try {
            if (!mKeyStore.containsAlias(userAlias)) {
                return false;
            }
            PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) mKeyStore.getEntry(userAlias, null)).getPrivateKey();
            Cipher output = getCipher();
            output.init(Cipher.DECRYPT_MODE, privateKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
