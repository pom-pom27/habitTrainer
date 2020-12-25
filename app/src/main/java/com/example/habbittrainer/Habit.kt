package com.example.habbittrainer

import android.graphics.Bitmap

data class Habit(val title: String, val desc: String, val img: Bitmap)

//fun getSampleHabits() =
//    listOf(
//        Habit(
//            "Go for a walk",
//            "A Nice walk in the sun get you ready",
//            R.drawable.walk
//        ),
//        Habit(
//            "Drink a glass of water",
//            "A refreshing glass wof water gets you hydrated",
//            R.drawable.water
//        )
//    )