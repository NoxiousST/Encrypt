package net.stiller.encrypt.utils

data class StateAuth<T>(
    val status: AuthStatus,
    val message: String?
) {
    companion object {
        fun <T> success(): StateAuth<T> {
            return StateAuth(AuthStatus.SUCCESS, null)
        }

        fun <T> error(msg: String): StateAuth<T> {
            return StateAuth(AuthStatus.ERROR, msg)
        }

        fun <T> loading(): StateAuth<T> {
            return StateAuth(AuthStatus.LOADING, null)
        }
    }
}

enum class AuthStatus {
    SUCCESS,
    ERROR,
    LOADING
}