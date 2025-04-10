package com.example.lifetracker.data.model

enum class PhotoCategory(val displayName: String) {
    FRONT("Front"),
    BACK("Back"),
    SIDE("Side"),
    BICEPS("Biceps"),
    CHEST("Chest"),
    LEGS("Legs"),
    FULL_BODY("Full Body"),
    OTHER("Other");
    
    companion object {
        fun fromString(value: String): PhotoCategory {
            return values().find { it.name == value } ?: OTHER
        }
    }
} 