package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["barcode"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String,
    val barcode: String = "",
    val categoryId: Long? = null,
    val categoryName: String = "",
    val brandId: Long? = null,
    val brandName: String = "",
    val unit: String = "Pcs",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val mrp: Double = 0.0,
    val minStock: Double = 5.0,
    val openingStock: Double = 0.0,
    val currentStock: Double = 0.0,
    val supplier: String = "",
    val gstRate: Double = 0.0,
    val imageUri: String? = null,
    val isActive: Boolean = true,
    val allowSellingAboveMrp: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
