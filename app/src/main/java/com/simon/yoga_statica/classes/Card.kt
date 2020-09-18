package com.simon.yoga_statica.classes

class Card {
    lateinit var id: String
    var allCardCount: Int = 0
    var currentCardNum: Int = 0
    var likesCount: Int = 0
    var commentsCount: Int = 0
    lateinit var title: String
    lateinit var thumbPath: String
}