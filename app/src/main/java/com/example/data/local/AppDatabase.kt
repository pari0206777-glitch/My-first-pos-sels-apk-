package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ProductEntity::class, CategoryEntity::class, BrandEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun brandDao(): BrandDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vyapar_pos_database"
                )
                    .addCallback(DatabaseSeedCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseSeedCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                populateInitialData(database)
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val categoryDao = db.categoryDao()
            val brandDao = db.brandDao()
            val productDao = db.productDao()

            // Pre-populate standard retail categories
            val categories = listOf(
                CategoryEntity(name = "Grocery & Staples", description = "Grains, flours, oils, pulses", colorHex = "#2E7D32"),
                CategoryEntity(name = "Snacks & Packaged Food", description = "Biscuits, chips, noodles", colorHex = "#E65100"),
                CategoryEntity(name = "Dairy & Bakery", description = "Milk, butter, bread, cheese", colorHex = "#1565C0"),
                CategoryEntity(name = "Beverages & Cold Drinks", description = "Tea, coffee, juices, sodas", colorHex = "#00838F"),
                CategoryEntity(name = "Personal Care", description = "Soaps, shampoos, oral care", colorHex = "#7B1FA2"),
                CategoryEntity(name = "Household & Cleaning", description = "Detergents, surface cleaners", colorHex = "#C2185B")
            )
            for (cat in categories) {
                categoryDao.insertCategory(cat)
            }

            // Pre-populate popular brands
            val brands = listOf(
                BrandEntity(name = "Generic / Local", description = "Unbranded or local farm produce"),
                BrandEntity(name = "Amul", description = "The Taste of India dairy products"),
                BrandEntity(name = "Tata", description = "Tata Sampann, Tata Tea, Tata Salt"),
                BrandEntity(name = "Nestle", description = "Maggi, KitKat, Nescafe"),
                BrandEntity(name = "Britannia", description = "Good Day, Marie Gold, Milk Bikis"),
                BrandEntity(name = "Parle", description = "Parle-G, Monaco, Hide & Seek")
            )
            for (brand in brands) {
                brandDao.insertBrand(brand)
            }

            // Pre-populate 3 sample starter products
            productDao.insertProduct(
                ProductEntity(
                    name = "Tata Salt Iodized 1kg",
                    sku = "SKU-SALT-1KG",
                    barcode = "8901030383181",
                    categoryName = "Grocery & Staples",
                    brandName = "Tata",
                    unit = "Packet",
                    purchasePrice = 22.0,
                    sellingPrice = 27.0,
                    mrp = 28.0,
                    minStock = 10.0,
                    openingStock = 50.0,
                    currentStock = 50.0,
                    supplier = "Tata Consumer Products",
                    gstRate = 0.0,
                    isActive = true,
                    allowSellingAboveMrp = false
                )
            )

            productDao.insertProduct(
                ProductEntity(
                    name = "Amul Taaza Milk 500ml",
                    sku = "SKU-MILK-500ML",
                    barcode = "8901262010058",
                    categoryName = "Dairy & Bakery",
                    brandName = "Amul",
                    unit = "Packet",
                    purchasePrice = 25.5,
                    sellingPrice = 28.0,
                    mrp = 28.0,
                    minStock = 15.0,
                    openingStock = 30.0,
                    currentStock = 30.0,
                    supplier = "Amul Dairy Dist",
                    gstRate = 0.0,
                    isActive = true,
                    allowSellingAboveMrp = false
                )
            )

            productDao.insertProduct(
                ProductEntity(
                    name = "Britannia Good Day Butter 200g",
                    sku = "SKU-GD-200G",
                    barcode = "8901063012430",
                    categoryName = "Snacks & Packaged Food",
                    brandName = "Britannia",
                    unit = "Packet",
                    purchasePrice = 38.0,
                    sellingPrice = 45.0,
                    mrp = 50.0,
                    minStock = 8.0,
                    openingStock = 40.0,
                    currentStock = 40.0,
                    supplier = "City FMCG Distributors",
                    gstRate = 18.0,
                    isActive = true,
                    allowSellingAboveMrp = false
                )
            )
        }
    }
}
