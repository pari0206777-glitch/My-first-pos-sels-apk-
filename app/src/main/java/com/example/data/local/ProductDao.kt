package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE LOWER(sku) = LOWER(:sku) LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND barcode != '' LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("""
        SELECT * FROM products 
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(sku) LIKE '%' || LOWER(:query) || '%'
           OR barcode LIKE '%' || :query || '%'
           OR LOWER(categoryName) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(brandName) LIKE '%' || LOWER(:query) || '%'
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE LOWER(sku) = LOWER(:sku) AND id != :excludeId")
    suspend fun countProductsWithSku(sku: String, excludeId: Long = 0): Int

    @Query("SELECT COUNT(*) FROM products WHERE barcode = :barcode AND barcode != '' AND id != :excludeId")
    suspend fun countProductsWithBarcode(barcode: String, excludeId: Long = 0): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("UPDATE products SET currentStock = :newStock, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET isActive = :isActive, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateActiveStatus(id: Long, isActive: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
