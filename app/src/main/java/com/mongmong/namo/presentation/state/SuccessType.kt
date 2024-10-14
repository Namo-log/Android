package com.mongmong.namo.presentation.state

enum class SuccessType {
    ADD,
    EDIT,
    DELETE
}

class SuccessState(val type: SuccessType, val isSuccess: Boolean)