package com.personal.biji.android.upload

import com.personal.biji.android.domain.DirectImageUploadSession
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

interface TosSigner {
    fun signedPut(data: ByteArray, contentType: String, session: DirectImageUploadSession, now: Instant = Instant.now()): Request
}

class TosV4Signer : TosSigner {
    override fun signedPut(data: ByteArray, contentType: String, session: DirectImageUploadSession, now: Instant): Request {
        val endpoint = session.endpoint.removePrefix("https://").removePrefix("http://").trimEnd('/')
        val host = "${session.bucket}.$endpoint"
        val path = "/${session.objectKey}"
        val longDate = LONG_DATE.format(now)
        val shortDate = SHORT_DATE.format(now)
        val payloadHash = sha256(data)
        val headers = sortedMapOf(
            "content-disposition" to "inline",
            "content-type" to contentType,
            "host" to host,
            "x-tos-content-sha256" to payloadHash,
            "x-tos-date" to longDate,
            "x-tos-security-token" to session.credentials.securityToken,
        )
        val signedHeaders = headers.keys.joinToString(";")
        val canonicalHeaders = headers.entries.joinToString("\n", postfix = "\n") { "${it.key}:${it.value}" }
        val canonicalRequest = listOf("PUT", encodePath(path), "", canonicalHeaders, signedHeaders, payloadHash).joinToString("\n")
        val scope = "$shortDate/${session.region}/tos/request"
        val stringToSign = listOf("TOS4-HMAC-SHA256", longDate, scope, sha256(canonicalRequest.toByteArray())).joinToString("\n")
        val dateKey = hmac(session.credentials.secretAccessKey.toByteArray(), shortDate)
        val regionKey = hmac(dateKey, session.region)
        val serviceKey = hmac(regionKey, "tos")
        val signingKey = hmac(serviceKey, "request")
        val signature = hex(hmac(signingKey, stringToSign))
        val authorization = "TOS4-HMAC-SHA256 Credential=${session.credentials.accessKeyId}/$scope,SignedHeaders=$signedHeaders,Signature=$signature"
        return Request.Builder()
            .url("https://$host${encodePath(path)}")
            .put(data.toRequestBody())
            .header("Content-Disposition", "inline")
            .header("Content-Type", contentType)
            .header("X-Tos-Content-Sha256", payloadHash)
            .header("X-Tos-Date", longDate)
            .header("X-Tos-Security-Token", session.credentials.securityToken)
            .header("Authorization", authorization)
            .build()
    }

    private fun encodePath(path: String): String = path.split("/").joinToString("/") {
        URLEncoder.encode(it, StandardCharsets.UTF_8.name()).replace("+", "%20")
    }
    private fun sha256(value: ByteArray) = hex(MessageDigest.getInstance("SHA-256").digest(value))
    private fun hmac(key: ByteArray, value: String): ByteArray = Mac.getInstance("HmacSHA256").run {
        init(SecretKeySpec(key, "HmacSHA256"))
        doFinal(value.toByteArray())
    }
    private fun hex(value: ByteArray) = value.joinToString("") { "%02x".format(it) }

    companion object {
        private val LONG_DATE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)
        private val SHORT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC)
    }
}
