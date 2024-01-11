package net.stiller.encrypt.utils

data class StateInput<T>(
    val inputStatus: InputStatus,
) {
    companion object {
        fun <T> delete(): StateInput<T> {
            return StateInput(InputStatus.DELETE)
        }

        fun <T> password(): StateInput<T> {
            return StateInput(InputStatus.PASSWORD)
        }

        fun <T> phone(): StateInput<T> {
            return StateInput(InputStatus.PHONE)
        }

        fun <T> username(): StateInput<T> {
            return StateInput(InputStatus.USERNAME)
        }
    }
}

enum class InputStatus {
    DELETE,
    PASSWORD,
    PHONE,
    USERNAME
}