package com.simon.yoga_statica.interfaces

interface OnRecyclerItemClickListener {
    fun onItemClicked(position: Int, asuna: String = "")

    fun onItemLongClicked(position: Int)
}