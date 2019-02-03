package com.example.yura.shoplist

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ShopListManager() {

    var source: SheetsDataSource? = null
    var buyMode: Boolean = false
    var currentCategory: String? = null

//    var modified: Boolean = false
//    var syncInProgress: Boolean = false

    val items: ArrayList<ShopItem> = ArrayList()

    fun addItem(item: ShopItem){
        items.add(item)
    }

    fun getCategories():ArrayList<String> {
        val categories = ArrayList<String>()
        for (i in items) {
            if (categories.indexOf(i.category) == -1)
                categories.add(i.category)
        }
        categories.sortBy { it }
        return categories
    }

    fun getItemsInCategory (category: String?, includeEmpty: Boolean = true, buyer: String? = null):ArrayList<ShopItem>{
        val itemsInCategory = ArrayList<ShopItem>()
        for (i in items) {
            if (i.buyer == null)
                i.buyer = ""
            if ((category == null || category == i.category) && (includeEmpty || i.quantity > 0) && (buyer == null || buyer == i.buyer))
                itemsInCategory.add(i)
        }
        return itemsInCategory
    }

    fun save() {
        source?.saveList(this)
    }

    companion object {

        var manager: ShopListManager = createSampleList()

        fun connectToGoogleSheets(context: AppCompatActivity) {
            if (manager.source == null) { // We do not re-load the list if it is already loaded
                val ds = SheetsDataSource.getForMobileApp(context)
                manager = ds.readList()
            }
        }

        private fun createSampleList(): ShopListManager {
            val m = ShopListManager()
            var i = ShopItem("Молоко")
            i.category = "Молочные продукты"
            m.addItem(i)

            i = ShopItem("Сметана")
            i.category = "Молочные продукты"
            m.addItem(i)

            i = ShopItem("Творог")
            i.category = "Молочные продукты"
            m.addItem(i)

            i = ShopItem("Кефир")
            i.category = "Молочные продукты"
            m.addItem(i)

            i = ShopItem("Туалетная бумага")
            i.category = "Товары для дома"
            m.addItem(i)

            i = ShopItem("Сухие салфетки")
            i.category = "Товары для дома"
            m.addItem(i)

            i = ShopItem("Влажные салфетки (большие)")
            i.category = "Товары для дома"
            m.addItem(i)

            i = ShopItem("Зубная паста взрослая")
            i.category = "Косметика"
            m.addItem(i)

            i = ShopItem("Зубная паста детская")
            i.category = "Косметика"
            m.addItem(i)

            return m
        }
    }


}