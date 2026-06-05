package com.gymcats.util

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordSecurity {
    private const val PREFIX = "pbkdf2"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_SIZE = 16

    fun hash(password: String): String {
        val salt = ByteArray(SALT_SIZE)
        SecureRandom().nextBytes(salt)
        val hash = pbkdf2(password, salt, ITERATIONS, KEY_LENGTH)
        return buildString {
            append(PREFIX)
            append("$")
            append(ITERATIONS)
            append("$")
            append(Base64.getEncoder().encodeToString(salt))
            append("$")
            append(Base64.getEncoder().encodeToString(hash))
        }
    }

    fun verify(password: String, storedValue: String): Boolean {
        if (!isHashed(storedValue)) return password == storedValue

        val parts = storedValue.split("$")
        if (parts.size != 4) return false

        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = runCatching { Base64.getDecoder().decode(parts[2]) }.getOrNull() ?: return false
        val expectedHash = runCatching { Base64.getDecoder().decode(parts[3]) }.getOrNull() ?: return false
        val actualHash = pbkdf2(password, salt, iterations, expectedHash.size * 8)
        return actualHash.contentEquals(expectedHash)
    }

    fun isHashed(value: String): Boolean = value.startsWith("$PREFIX$")

    fun validateStrength(password: String): String? {
        if (password.length < 8) return "A senha precisa ter ao menos 8 caracteres."
        if (password.none { it.isUpperCase() }) return "A senha precisa ter pelo menos uma letra maiúscula."
        if (password.none { it.isLowerCase() }) return "A senha precisa ter pelo menos uma letra minúscula."
        if (password.none { it.isDigit() }) return "A senha precisa ter pelo menos um número."
        return null
    }

    private fun pbkdf2(password: String, salt: ByteArray, iterations: Int, keyLength: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, keyLength)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
