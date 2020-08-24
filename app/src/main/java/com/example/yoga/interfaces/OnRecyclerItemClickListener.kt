package com.example.yoga.interfaces

import android.view.View

interface OnRecyclerItemClickListener {
    fun onItemClicked(asuna: String, position: Int)

    fun onItemLongClicked(position: Int)
}