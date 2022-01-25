package com.example.instach

class FireBaseDatabase {

    companion object {
        @Volatile
        private var instance: FireBaseDatabase? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: FireBaseDatabase().also {
                    instance = it
                }
            }
    }
}