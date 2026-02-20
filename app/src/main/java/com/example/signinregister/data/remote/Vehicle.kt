package com.example.signinregister.data.remote

data class Vehicle(
    val id: String,
    val driverName: String,
    val plate: String,
    val brand: String,
    val model: String,
    val color: String,
    val currentLat: Double,
    val currentLng: Double,
    val rating: Double,
    val imageUrl: String
)