package com.dashlane.cryptography

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Test

class DecryptionEngineCacheTest {
    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun testCacheUsage() {
        
        val encryptedTransaction1 =
            "JDEkYXJnb24yZCQxNiQzJDMyNzY4JDIkYWVzMjU2JGNiY2htYWMkMTYkS6zSLU2tPsdsSWqKs7352yfGe4uvxIeOQshFs/juoRj1iSTZAGZK3RM1GOdC8WiWXkQT4FrSS2+OxsnuGSvEbA7cP7eKq0t+F9MUFE9tuJnDxV8cJBLCeYWe0k4ZjEy2c/unhhaTzuyAob0hqfwZjqP0Iw128HOzSsjbu6X/fBa/LdlwZC2+aFTEKkELjVMvWupjVfgzo3pqyOyB318XeD6vAM/r2VXuanuDltv6JhdqcvK0HxTFx4Wx0erxezx5Ue+GkvyXqgp0ukuiWqesZh6BDJ+2g9RSYePrAfjLQZaF7CCuTWfPwtccRdPrwId8NuokWMbZvAnwYb1rKgwYBF1rZkLeHTZc/+TTCiP65mCEvZw89U6ubY/9LxBBZvSqYph2AtDSuJpejGMwF3/LrwQcP5CFdU+pYtvb+FQW0FM/fZ2Z/DGAy518ja04/f+tlghebR85WeRC4eVvyogDgYBwVudjmXK3nwLlrFx2CFDRyXUHNzhHgQKgDWP/gNsLubhRs4+rHsoQX9x0J7n6Q2IqDwTnjqU5KBHguBu2dofnuAo3LA8S09zvrsJzhiVNBhzbOhC2hil0pMvOP51yxKvIf8yfydecmsPr+j2Oa5XaLQwW0a9SLBSg8cp0STWQ0/lFtTDqorrAQowmvRgzluXYEAzdw1oWE8jkjv8ZMTt8V02KMUFU
        val encryptedTransaction2 =
            "JDEkYXJnb24yZCQxNiQzJDMyNzY4JDIkYWVzMjU2JGNiY2htYWMkMTYkS6zSLU2tPsdsSWqKs73527mu7I6a5N0MKaHxGH1/YxfhuIoz5YWOGll+3Ic5N2JLqPs7aFYnpxceO0BZmPVb84WddlBCkp1C6suDDYI3Rxis+LbHT52SRAvlkeMLC2jDPPNjaN9wJk7JOCYlnnO5HzSwMm/2fKMCLjZHIRiJn0OFn+MV1eFCqORybbpJFSD6XtalDXgErLrx8RHqwDiycJztk6LKqLr+PRX/lHdQshxxCFSQRdUKcjsUgbqT3UprVk5zGmnu9qYtnvX/8+i7CAGWtkmqQWyb8nj/QrGimEsjH14L8Zd0LQ2C6UfY5rpaGtLjeWs1XzjQeCcQGdM6y7DSHa67F13VT730JJJQEyL9TP1JDHuJlrzRmspshh8lfwv6wpvGbP0ksqbOrkMBh5Ghpo9AmDHZvXSkzRFfdMM6ouxTi6JnEZfBDersmDfR8/GsN5zb+wN+11OivRsOh22iUXOzWJHXn/Pju5VcbaEFG4f8kpts/M+qgPsb/nbRzGk69LKmSq3zmdEE+zfrIErRxf4lG2JxSMvqvLYDJJ7WLz8qqPpfiKA11OgqmrTm99XY/+goTtsR+Bvu6lG2OxHryHViCPlLIZrEHVgFo4QBECfAMznceEpOvHF6kCi33SkOmunVqg1hnsWspdN1UgE4PteAmHr18IhRHyNwNdBRGxWqEsBx2ocYkXA7FQg1SsuWO5aigg6WsF6dpfi6IMQ5et94fka+Q6H3qX23hFMEjxL6tLph4hmdi9/YlZJh".asEncryptedBase64()
        val encryptedTransaction3 =
            "JDEkYXJnb24yZCQxNiQzJDMyNzY4JDIkYWVzMjU2JGNiY2htYWMkMTYkS6zSLU2tPsdsSWqKs73521ZXhROjUc5eGz7vEJG8Xi97QoWQG4HaHlGjtXnJtYJaGRh3sIIQ5K9XDGTs+U1YnGZZJinykkWRVfWM5HNMRVbgP31BYQTf0I810kVk6wx6EFOCnpFYqMHCiofRPyLQhWq1zCQpsKqerQrvpSYIuAFXS5/q7UCk3D11AjN1l3qm4gtSaWmroCFuFLychsYookGMIg4Kozyoqr+S9g53nOgiPKKOEB2eEvS/Kquq8iHSZy6ihZPCJFeA+RkiIdDlthrRZvnwoNJcDgM+wvwKFGxbFqgR+9NS90gfQ3v+ZeBBnfoctULE6MLZemeW9p73sohMo0kgxcEaKO+BTc7aIbvzyGjtKovArDob2zcqet85xo/kkbe1E1bU6VWVi4Gpqxlj9KLAOuLxKxkpZ398BjymqPEcU5zQqmrC3PGxr0AV5FSuBezuyW0SS/Xo2I0QQTz2IDWqK5eGa7fHZlbATImdqZWz0fn4VDxMDKGrerbbUm1PQ5rDsDRCxVGBahl7HMohGWnJV4e2/WlohAyZZ6MjMP002LI/X+uNVSLnRbih8/IRjpm5SYQgZyn7KsPtP9KIKk9PLQDkheKnDh6y5aM8y4ZsQq3u1O7loZrmtaNstcWZasWRaFaIPd9qwMzT8bbNTEmLsNPQVhYn12lD2p1qF7lIzuV0A8bJakEsabFvXh3mxyNd+VoGG5+UI4CIJbcM2QLLKoQoyCUQxPkf1/Q=".asEncryptedBase64()
        val password = "Dashlane12"
        val salt = "S6zSLU2tPsdsSWqKs7352w=="
        val derivedKey =
            "RxpgG7w02xBkrKcCBo2zXFcYxxtFFfTe496gVDfx1dA=".decodeBase64()!!.toByteArray()

        val cipherFactory = CipherFactory()
        val macFactory = MacFactory()
        val keyDerivationEngine = mockk<KeyDerivationEngine> {
            every {
                deriveKeyArgon2d(
                    key = match { it.contentEquals(password.encodeToByteArray()) },
                    salt = match { it.toByteString() == salt.decodeBase64() },
                    timeCost = 3,
                    memoryCost = 32768,
                    parallelism = 2,
                )
            } returns derivedKey
        }
        val decryptionEngineFactory = DecryptionEngineImpl.Factory(
            cipherFactory,
            macFactory,
            keyDerivationEngine,
        )
        val cryptographyKey = CryptographyKey.ofPassword(password)
        val decryptionEngine = decryptionEngineFactory.create(cryptographyKey)

        decryptionEngine.decryptBase64ToUtf8String(encryptedTransaction1)
        decryptionEngine.decryptBase64ToUtf8String(encryptedTransaction2)
        decryptionEngine.decryptBase64ToUtf8String(encryptedTransaction3)

        verify(exactly = 1) {
            keyDerivationEngine.deriveKeyArgon2d(any(), any(), any(), any(), any(), any())
        }
    }
}