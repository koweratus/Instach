package com.example.instach

interface OnItemClickListener {
    fun onItemClick(position: Int)

    fun onWhatEverClick(position: Int)

    fun onDeleteClick(position: Int)
}