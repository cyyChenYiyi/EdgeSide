package com.edgeside.app.data.entity

enum class PanelEdge(val key: String) {
    LEFT("LEFT"),
    RIGHT("RIGHT");

    companion object {
        fun fromKey(key: String?): PanelEdge {
            return values().find { it.key == key } ?: RIGHT
        }
    }
}
