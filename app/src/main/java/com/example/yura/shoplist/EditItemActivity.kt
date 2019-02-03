package com.example.yura.shoplist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.AutoCompleteTextView
import android.widget.EditText
import kotlinx.android.synthetic.main.item_buy_fragment.*
import android.widget.ArrayAdapter
import android.widget.TextView


class EditItemActivity : AppCompatActivity() {

    lateinit var item: ShopItem;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        val itemName = intent.getStringExtra(getString(R.string.item_name))
        for (i in ShopListManager.manager.items) {
            if (i.name == itemName)
                item = i
        }

        findViewById<EditText>(R.id.edit_display_name).setText(item.name)
        findViewById<AutoCompleteTextView>(R.id.edit_category).setText(item.category)
        findViewById<AutoCompleteTextView>(R.id.edit_buyer_name).setText(item.buyer)
        findViewById<TextView>(R.id.goods_ru_link).setText(item.urlGoodsRu?: "Нет")


        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line, ShopListManager.manager.getCategories()
        )
        findViewById<AutoCompleteTextView>(R.id.edit_category).setAdapter(adapter)
    }

    override fun onPause() {
        super.onPause()
        item.name = findViewById<EditText>(R.id.edit_display_name).text.toString()
        item.category = findViewById<AutoCompleteTextView>(R.id.edit_category).text.toString()
        item.buyer = findViewById<AutoCompleteTextView>(R.id.edit_buyer_name).text.toString()
    }
}
