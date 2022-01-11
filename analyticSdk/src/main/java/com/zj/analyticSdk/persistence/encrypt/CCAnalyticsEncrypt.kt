package com.zj.analyticSdk.persistence.encrypt

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.zj.analyticSdk.CAConfigs
import com.zj.analyticSdk.CALogs
import com.zj.analyticSdk.UTL
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.Key
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Suppress("unused")
class CCAnalyticsEncrypt(private val context: Context, private val persistentSecretKey: IPersistentSecretKey?) {

    companion object {
        private const val SP_SECRET_KEY = "secret_key"
        private const val KEY_VERSION_DEFAULT = 0
        private const val TAG = "CCA.CCAnalyticsEncrypt"
    }

    private var aesKeyValue: ByteArray? = null
    private var mSecreteKey: SecreteKey? = null

    /**
     * RSA encrypted value after AES key
     */
    private var mEKey: String? = null

    /**
     * Encrypt data
     * @param jsonObject, data to be encrypted
     * @return encrypted data
     */
    fun encryptTrackData(jsonObject: JSONObject): JSONObject {
        try {
            if (isSecretKeyNull(mSecreteKey)) {
                mSecreteKey = loadSecretKey()
                if (isSecretKeyNull(mSecreteKey)) {
                    return jsonObject
                }
            }
            generateAESKey(mSecreteKey)
            if (TextUtils.isEmpty(mEKey)) {
                return jsonObject
            }
            val encryptData = aesEncrypt(aesKeyValue, jsonObject.toString())
            val dataJson = JSONObject()
            dataJson.put("eKey", mEKey)
            dataJson.put("pkv", mSecreteKey?.version)
            dataJson.put("payloads", encryptData)
            return dataJson
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
        }
        return jsonObject
    }

    fun saveSecretKey(secreteKey: SecreteKey) {
        try {
            if (persistentSecretKey != null) {
                persistentSecretKey.saveSecretKey(secreteKey)

                // Also delete the local key
                saveLocalSecretKey("")
            } else {
                saveLocalSecretKey(secreteKey.toString())
            }
        } catch (e: Exception) {
            CALogs.printStackTrace(e)
        }
    }

    val isRSASecretKeyNull: Boolean
        get() {
            try {
                val secreteKey: SecreteKey = loadSecretKey()
                return TextUtils.isEmpty(secreteKey.key)
            } catch (e: Exception) {
                CALogs.printStackTrace(e)
            }
            return true
        }

    /**
     * Check whether the RSA key information is the same as the local one
     * @param version version number
     * @param key key information
     * @return -1 means the local key information is empty, -2 means the same, others are different
     */
    fun checkRSASecretKey(version: String, key: String): String {
        val tip = ""
        try {
            val secreteKey: SecreteKey = loadSecretKey()
            return if (TextUtils.isEmpty(secreteKey.key)) {
                "Key verification failed, App-side key is empty"
            } else if (version == secreteKey.version.toString() + "" && key == secreteKey.key) {
                "Key verification passed, the selected key is the same as the App-side key"
            } else {
                "Key verification failed, the selected key is not the same as the app-side key. Selected key version: " + version + ", App-side key version: " + secreteKey.version
            }
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
        }
        return tip
    }

    /**
     * AES encryption
     * @param key AES encryption key
     * @param content encrypted content
     * @return AES encrypted data
     */
    private fun aesEncrypt(key: ByteArray?, content: String): String? {
        try {
            val random = Random()
            val ivBytes = ByteArray(16)
            random.nextBytes(ivBytes)
            val contentBytes = gzipEventData(content)
            val secretKeySpec = SecretKeySpec(key, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(ivBytes))
            val encryptedBytes = cipher.doFinal(contentBytes)
            val byteBuffer = ByteBuffer.allocate(ivBytes.size + encryptedBytes.size)
            byteBuffer.put(ivBytes)
            byteBuffer.put(encryptedBytes)
            val cipherMessage = byteBuffer.array()
            return String(Base64Coder.encode(cipherMessage))
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
        }
        return null
    }

    /**
     * RSA encryption
     * @param rsaPublicKey, public key
     * @param content, encrypted content
     * @return encrypted data
     */
    private fun rsaEncrypt(rsaPublicKey: String, content: ByteArray?): String? {
        if (TextUtils.isEmpty(rsaPublicKey)) {
            return null
        }
        try {
            val keyBytes: ByteArray = Base64Coder.decode(rsaPublicKey)
            val x509EncodedKeySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey: Key = keyFactory.generatePublic(x509EncodedKeySpec)
            val cipher = Cipher.getInstance("RSA/None/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val contentLen = content!!.size
            val outputStream = ByteArrayOutputStream()
            var offSet = 0
            var cache: ByteArray

            //RSA Maximum encrypted plaintext size: 1024 bits Public key: 117, 2048 is public key: 245

            val maxEncryptSize = 245
            while (contentLen - offSet > 0) {
                cache = if (contentLen - offSet > maxEncryptSize) {
                    cipher.doFinal(content, offSet, maxEncryptSize)
                } else {
                    cipher.doFinal(content, offSet, contentLen - offSet)
                }
                outputStream.write(cache, 0, cache.size)
                offSet += maxEncryptSize
            }
            val encryptedData = outputStream.toByteArray()
            outputStream.close()
            return String(Base64Coder.encode(encryptedData))
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
        }
        return null
    }

    private fun gzipEventData(record: String): ByteArray? {
        var gzipOutputStream: GZIPOutputStream? = null
        return try {
            val buffer = ByteArrayOutputStream()
            gzipOutputStream = GZIPOutputStream(buffer)
            gzipOutputStream.write(record.toByteArray())
            gzipOutputStream.finish()
            buffer.toByteArray()
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
            null
        } finally {
            if (gzipOutputStream != null) {
                try {
                    gzipOutputStream.close()
                } catch (ex: Exception) {
                    CALogs.printStackTrace(ex)
                }
            }
        }
    }

    /**
     * Randomly generated AES encryption key
     */
    @Throws(NoSuchAlgorithmException::class)
    private fun generateAESKey(secreteKey: SecreteKey?) {
        if (TextUtils.isEmpty(mEKey) || aesKeyValue == null) {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(128)
            val secretKey = keyGen.generateKey()
            aesKeyValue = secretKey.encoded
            mEKey = rsaEncrypt(secreteKey?.key ?: "", aesKeyValue)
        }
    }

    private fun saveLocalSecretKey(key: String) {
        val preferences: SharedPreferences = UTL.getSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(SP_SECRET_KEY, key)
        editor.apply()
    }

    @Throws(JSONException::class)
    private fun loadSecretKey(): SecreteKey {
        return if (persistentSecretKey != null) {
            readAppKey()
        } else {
            readLocalKey()
        }
    }

    private fun readAppKey(): SecreteKey {
        var rsaPublicKey: String? = null
        var rsaVersion = 0
        val rsaPublicKeyVersion = persistentSecretKey?.loadSecretKey()
        if (rsaPublicKeyVersion != null) {
            rsaPublicKey = rsaPublicKeyVersion.key
            rsaVersion = rsaPublicKeyVersion.version
        }
        CALogs.i(CAConfigs.LOG_ALL, TAG, "readAppKey [key = $rsaPublicKey ,v = $rsaVersion]")
        return SecreteKey(rsaPublicKey, rsaVersion)
    }

    @Throws(JSONException::class)
    private fun readLocalKey(): SecreteKey {
        var rsaPublicKey: String? = null
        var rsaVersion = 0
        val preferences: SharedPreferences = UTL.getSharedPreferences(context)
        val secretKey = preferences.getString(SP_SECRET_KEY, "")
        if (!TextUtils.isEmpty(secretKey)) {
            val jsonObject = JSONObject(secretKey ?: "")
            rsaPublicKey = jsonObject.optString("key", "")
            rsaVersion = jsonObject.optInt("version", KEY_VERSION_DEFAULT)
        }
        CALogs.i(CAConfigs.LOG_ALL, TAG, "readLocalKey [key = $rsaPublicKey ,v = $rsaVersion]")
        return SecreteKey(rsaPublicKey, rsaVersion)
    }

    private fun isSecretKeyNull(secreteKey: SecreteKey?): Boolean {
        return secreteKey == null || TextUtils.isEmpty(secreteKey.key) || secreteKey.version == KEY_VERSION_DEFAULT
    }
}