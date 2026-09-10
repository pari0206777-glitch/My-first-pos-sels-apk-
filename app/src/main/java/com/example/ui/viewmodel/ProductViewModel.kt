package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ProductRepository
import com.example.data.local.ProductValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBrands: StateFlow<List<BrandEntity>> = repository.allBrands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _filterActiveOnly = MutableStateFlow(false)
    val filterActiveOnly = _filterActiveOnly.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage = _successMessage.asStateFlow()

    // Filtered products based on search, category chip, and active status
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        searchQuery,
        selectedCategoryFilter,
        filterActiveOnly
    ) { products, query, catFilter, activeOnly ->
        products.filter { product ->
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.sku.contains(query, ignoreCase = true) ||
                    product.barcode.contains(query, ignoreCase = true) ||
                    product.categoryName.contains(query, ignoreCase = true) ||
                    product.brandName.contains(query, ignoreCase = true)

            val matchesCategory = catFilter == null || product.categoryName.equals(catFilter, ignoreCase = true)
            val matchesActive = !activeOnly || product.isActive

            matchesQuery && matchesCategory && matchesActive
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setFilterActiveOnly(activeOnly: Boolean) {
        _filterActiveOnly.value = activeOnly
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun saveProduct(
        product: ProductEntity,
        isEdit: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.saveProduct(product, isEdit)
            if (result.isSuccess) {
                _successMessage.value = if (isEdit) "Product updated successfully" else "Product added successfully"
                onComplete(true)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to save product"
                onComplete(false)
            }
        }
    }

    fun toggleProductActive(product: ProductEntity) {
        viewModelScope.launch {
            repository.toggleProductActive(product)
            _successMessage.value = "${product.name} is now ${if (product.isActive) "Inactive" else "Active"}"
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _successMessage.value = "Deleted ${product.name}"
        }
    }

    // Category actions
    fun saveCategory(category: CategoryEntity, isEdit: Boolean, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveCategory(category, isEdit)
            if (result.isSuccess) {
                _successMessage.value = if (isEdit) "Category updated" else "Category created"
                onComplete(true)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to save category"
                onComplete(false)
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _successMessage.value = "Category '${category.name}' removed"
        }
    }

    // Brand actions
    fun saveBrand(brand: BrandEntity, isEdit: Boolean, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.saveBrand(brand, isEdit)
            if (result.isSuccess) {
                _successMessage.value = if (isEdit) "Brand updated" else "Brand created"
                onComplete(true)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to save brand"
                onComplete(false)
            }
        }
    }

    fun deleteBrand(brand: BrandEntity) {
        viewModelScope.launch {
            repository.deleteBrand(brand)
            _successMessage.value = "Brand '${brand.name}' removed"
        }
    }

    // Helper: Generate unique internal barcode (in-store retail format prefix 200...)
    fun generateInternalBarcode(): String {
        val num = Random.nextLong(100000000L, 999999999L)
        return "200$num"
    }

    // Helper: Generate auto SKU
    fun generateAutoSku(productName: String = ""): String {
        val prefix = if (productName.length >= 3) {
            productName.filter { it.isLetter() }.take(4).uppercase()
        } else "PRD"
        val suffix = Random.nextInt(1000, 9999)
        return "$prefix-$suffix"
    }
}

class ProductViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
