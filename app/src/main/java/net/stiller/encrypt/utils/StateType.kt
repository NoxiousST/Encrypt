package net.stiller.encrypt.utils

data class StateType<T>(
    val inputStatus: TypeStatus,
) {
    companion object {
        fun <T> aes(): StateType<T> {
            return StateType(TypeStatus.AES)
        }

        fun <T> des(): StateType<T> {
            return StateType(TypeStatus.DES)
        }
    }
}

enum class TypeStatus {
    AES,
    DES
}