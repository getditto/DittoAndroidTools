package live.ditto.presencedegradationreporter.usecase

import live.ditto.DittoPeerKey
import java.math.BigInteger
import java.security.MessageDigest

class HashPeerKeyUseCase {
    private val md5 = MessageDigest.getInstance("md5")

    operator fun invoke(peerKey: DittoPeerKey): String = BigInteger(1, md5.digest(peerKey))
        .toString(16)
        .padStart(32, '0')
}