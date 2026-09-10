package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

sealed class ProductValidationResult {
    object Success : ProductValidationResult()
    data class Error(val message: String) : ProductValidationResult()
}

class ProductRepository(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val brandDao: BrandDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = productDao.getActiveProducts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allBrands: Flow<List<BrandEntity>> = brandDao.getAllBrands()

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return if (query.isBlank()) {
            productDao.getAllProducts()
        } else {
            productDao.searchProducts(query.trim())
        }
    }

    suspend fun getProductById(id: Long): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductById(id)
    }

    suspend fun validateProduct(product: ProductEntity, isEdit: Boolean): ProductValidationResult = withContext(Dispatchers.IO) {
        val trimmedName = product.name.trim()
        val trimmedSku = product.sku.trim()
        val trimmedBarcode = product.barcode.trim()

        if (trimmedName.isEmpty()) {
            return@withContext ProductValidationResult.Error("Product name cannot be empty")
        }
        if (trimmedSku.isEmpty()) {
            return@withContext ProductValidationResult.Error("SKU code cannot be empty")
        }

        // Duplicate SKU check
        val excludeId = if (isEdit) product.id else 0L
        val skuCount = productDao.countProductsWithSku(trimmedSku, excludeId)
        if (skuCount > 0) {
            return@withContext ProductValidationResult.Error("SKU '$trimmedSku' is already assigned to another product")
        }

        // Duplicate Barcode check (only if barcode is provided)
        if (trimmedBarcode.isNotEmpty()) {
            val barcodeCount = productDao.countProductsWithBarcode(trimmedBarcode, excludeId)
            if (barcodeCount > 0) {
                return@withContext ProductValidationResult.Error("Barcode '$trimmedBarcode' is already in use by another product")
            }
        }

        // Selling Price vs MRP validation
        if (product.mrp > 0 && product.sellingPrice > product.mrp && !product.allowSellingAboveMrp) {
            return@withContext ProductValidationResult.Error("Selling price (₹${product.sellingPrice}) cannot exceed MRP (₹${product.mrp}) unless explicitly allowed")
        }

        ProductValidationResult.Success
    }

    suspend fun saveProduct(product: ProductEntity, isEdit: Boolean): Result<Long> = withContext(Dispatchers.IO) {
        val validation = validateProduct(product, isEdit)
        if (validation is ProductValidationResult.Error) {
            return@withContext Result.failure(IllegalArgumentException(validation.message))
        }

        try {
            val cleanProduct = product.copy(
                name = product.name.trim(),
                sku = product.sku.trim().uppercase(),
                barcode = product.barcode.trim(),
                updatedAt = System.currentTimeMillis()
            )
            if (isEdit) {
                productDao.updateProduct(cleanProduct)
                Result.success(cleanProduct.id)
            } else {
                val newId = productDao.insertProduct(cleanProduct)
                Result.success(newId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleProductActive(product: ProductEntity): Unit = withContext(Dispatchers.IO) {
        productDao.updateActiveStatus(product.id, !product.isActive)
    }

    suspend fun deleteProduct(product: ProductEntity): Unit = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteProductById(id: Long): Unit = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
    }

    // Category operations
    suspend fun saveCategory(category: CategoryEntity, isEdit: Boolean): Result<Long> = withContext(Dispatchers.IO) {
        val trimmed = category.name.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Category name cannot be empty"))
        }
        try {
            val clean = category.copy(name = trimmed)
            if (isEdit) {
                categoryDao.updateCategory(clean)
                Result.success(clean.id)
            } else {
                val id = categoryDao.insertCategory(clean)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(category: CategoryEntity): Unit = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
    }

    // Brand operations
    suspend fun saveBrand(brand: BrandEntity, isEdit: Boolean): Result<Long> = withContext(Dispatchers.IO) {
        val trimmed = brand.name.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Brand name cannot be empty"))
        }
        try {
            val clean = brand.copy(name = trimmed)
            if (isEdit) {
                brandDao.updateBrand(clean)
                Result.success(clean.id)
            } else {
                val id = brandDao.insertBrand(clean)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteBrand(brand: BrandEntity): Unit = withContext(Dispatchers.IO) {
        brandDao.deleteBrand(brand)
    }
}
