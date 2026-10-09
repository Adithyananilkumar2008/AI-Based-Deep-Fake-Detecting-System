package com.example.forensics

import java.io.InputStream
import java.security.MessageDigest

object CryptoUtil {

    fun computeSha256(bytes: ByteArray): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(bytes)
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "HASH_UNAVAILABLE"
        }
    }

    fun computeSha256(inputStream: InputStream): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            val hash = digest.digest()
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "HASH_UNAVAILABLE"
        }
    }

    fun computeMd5(bytes: ByteArray): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(bytes)
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "HASH_UNAVAILABLE"
        }
    }
}
