package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class ShopDetails(
    val shopName: String,
    val ownerName: String,
    val mobileNumber: String,
    val address: String,
    val city: String,
    val state: String
)

class ShopPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vyapar_pos_shop_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_SETUP_DONE = "is_setup_done"
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_OWNER_NAME = "owner_name"
        private const val KEY_MOBILE_NUMBER = "mobile_number"
        private const val KEY_ADDRESS = "address"
        private const val KEY_CITY = "city"
        private const val KEY_STATE = "state"
    }

    fun isSetupDone(): Boolean {
        return prefs.getBoolean(KEY_IS_SETUP_DONE, false)
    }

    fun saveShopDetails(details: ShopDetails) {
        prefs.edit()
            .putBoolean(KEY_IS_SETUP_DONE, true)
            .putString(KEY_SHOP_NAME, details.shopName.trim())
            .putString(KEY_OWNER_NAME, details.ownerName.trim())
            .putString(KEY_MOBILE_NUMBER, details.mobileNumber.trim())
            .putString(KEY_ADDRESS, details.address.trim())
            .putString(KEY_CITY, details.city.trim())
            .putString(KEY_STATE, details.state.trim())
            .apply()
    }

    fun getShopDetails(): ShopDetails? {
        if (!isSetupDone()) return null
        return ShopDetails(
            shopName = prefs.getString(KEY_SHOP_NAME, "") ?: "",
            ownerName = prefs.getString(KEY_OWNER_NAME, "") ?: "",
            mobileNumber = prefs.getString(KEY_MOBILE_NUMBER, "") ?: "",
            address = prefs.getString(KEY_ADDRESS, "") ?: "",
            city = prefs.getString(KEY_CITY, "") ?: "",
            state = prefs.getString(KEY_STATE, "") ?: ""
        )
    }

    fun clearShopDetails() {
        prefs.edit().clear().apply()
    }
}
