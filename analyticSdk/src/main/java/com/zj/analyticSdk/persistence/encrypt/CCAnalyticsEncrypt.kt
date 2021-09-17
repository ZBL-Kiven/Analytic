package com.zj.analyticSdk.persistence.encrypt

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
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
     * RSA 加密 AES 密钥后的值
     */
    private var mEKey: String? = null

    /**
     * 针对数据进行加密
     *
     * @param jsonObject，需要加密的数据
     * @return 加密后的数据
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

    /**
     * 保存密钥
     *
     * @param secreteKey SecreteKey
     */
    fun saveSecretKey(secreteKey: SecreteKey) {
        try {
            CALogs.i(TAG, "[saveSecretKey] key = " + secreteKey.key.toString() + " ,v = " + secreteKey.version)
            if (persistentSecretKey != null) {
                persistentSecretKey.saveSecretKey(secreteKey) // 同时删除本地的密钥
                saveLocalSecretKey("")
            } else {
                saveLocalSecretKey(secreteKey.toString())
            }
        } catch (e: Exception) {
            CALogs.printStackTrace(e)
        }
    }

    /**
     * 公钥是否为空
     *
     * @return true，为空。false，不为空
     */
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
     * 检查 RSA 密钥信息是否和本地一致
     *
     * @param version 版本号
     * @param key 密钥信息
     * @return -1 是本地密钥信息为空，-2 是相同，其它是不相同
     */
    fun checkRSASecretKey(version: String, key: String): String {
        val tip = ""
        try {
            val secreteKey: SecreteKey = loadSecretKey()
            return if (TextUtils.isEmpty(secreteKey.key)) {
                "密钥验证不通过，App 端密钥为空"
            } else if (version == secreteKey.version.toString() + "" && key == secreteKey.key) {
                "密钥验证通过，所选密钥与 App 端密钥相同"
            } else {
                "密钥验证不通过，所选密钥与 App 端密钥不相同。所选密钥版本:" + version + "，App 端密钥版本:" + secreteKey.version
            }
        } catch (ex: Exception) {
            CALogs.printStackTrace(ex)
        }
        return tip
    }

    /**
     * AES 加密
     *
     * @param key AES 加密秘钥
     * @param content 加密内容
     * @return AES 加密后的数据
     */
    private fun aesEncrypt(key: ByteArray?, content: String): String? {
        try {
            val random = Random() // 随机生成初始化向量
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
     * RSA 加密
     *
     * @param rsaPublicKey，公钥秘钥
     * @param content，加密内容
     * @return 加密后的数据
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

            /** RSA 最大加密明文大小：1024 位公钥：117，2048 为公钥：245*/

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

    /**
     * 压缩事件
     *
     * @param record 压缩
     * @return 压缩后事件
     */
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
     * 随机生成 AES 加密秘钥
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

    /**
     * 存储密钥
     *
     * @param key 密钥
     */
    private fun saveLocalSecretKey(key: String) {
        val preferences: SharedPreferences = UTL.getSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(SP_SECRET_KEY, key)
        editor.apply()
    }

    /**
     * 加载密钥
     *
     * @throws JSONException 异常
     */
    @Throws(JSONException::class)
    private fun loadSecretKey(): SecreteKey {
        return if (persistentSecretKey != null) {
            readAppKey()
        } else {
            readLocalKey()
        }
    }

    /**
     * 从 App 端读取密钥
     */
    private fun readAppKey(): SecreteKey {
        var rsaPublicKey: String? = null
        var rsaVersion = 0
        val rsaPublicKeyVersion = persistentSecretKey?.loadSecretKey()
        if (rsaPublicKeyVersion != null) {
            rsaPublicKey = rsaPublicKeyVersion.key
            rsaVersion = rsaPublicKeyVersion.version
        }
        CALogs.i(TAG, "readAppKey [key = $rsaPublicKey ,v = $rsaVersion]")
        return SecreteKey(rsaPublicKey, rsaVersion)
    }

    /**
     * 从 SDK 端读取密钥
     *
     * @throws JSONException 异常
     */
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
        CALogs.i(TAG, "readLocalKey [key = $rsaPublicKey ,v = $rsaVersion]")
        return SecreteKey(rsaPublicKey, rsaVersion)
    }

    private fun isSecretKeyNull(secreteKey: SecreteKey?): Boolean {
        return secreteKey == null || TextUtils.isEmpty(secreteKey.key) || secreteKey.version == KEY_VERSION_DEFAULT
    }
}