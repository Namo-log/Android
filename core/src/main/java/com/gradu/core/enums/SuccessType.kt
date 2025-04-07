package com.gradu.core.enums

enum class SuccessType {
    ADD,
    EDIT,
    DELETE
}

class SuccessState(val type: SuccessType, val isSuccess: Boolean)