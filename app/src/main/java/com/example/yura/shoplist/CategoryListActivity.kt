package com.example.yura.shoplist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import java.util.*


class CategoryListActivity : ShopListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)
        updateUI()
    }

    override fun updateUI(){
        val backgroundText = findViewById<TextView>(R.id.background_text)
        backgroundText.text = ""
        val listView = findViewById<ListView>(R.id.category_list_view)
        listView.adapter = CategoryListAdapter(this, ShopListManager.manager.getCategories())
    }

}

class CategoryListAdapter(val context: CategoryListActivity, private val categories:ArrayList<String>):BaseAdapter() {
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val rowView = convertView?: inflater.inflate(R.layout.category_fragment, parent, false)

        val catName = rowView.findViewById(R.id.category_name) as TextView

        val categoryId = if (position < categories.size) categories[position] else null
        catName.text = categoryId?: context.getString(R.string.category_all_name)
        catName.setOnClickListener {
            ShopListManager.manager.currentCategory = categoryId
            val intent = Intent(context, ItemListActivity::class.java)
            intent.putExtra(context.getString(R.string.category_name), categoryId)
            context.startActivity(intent)
        }

        return rowView
    }

    override fun getItem(position: Int): Any {
        return categories[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return categories.size + 1
    }

}
