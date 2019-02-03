package com.example.yura.shoplist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.*
import android.view.MotionEvent
import android.view.GestureDetector.SimpleOnGestureListener
import android.support.v4.view.GestureDetectorCompat
import android.widget.Toast
import android.util.Log
import android.view.View.OnTouchListener




class ItemListActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    lateinit var listView: ListView
    private var disposableWriter: Disposable? = null
    private var category: String? = null

    private lateinit var saveText: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var shopModeCheckBox: CheckBox
    private lateinit var allCategoriesCheckBox: CheckBox
    private lateinit var hideEmptyCheckBox: CheckBox
    private lateinit var aOnlyCheckBox: CheckBox
    private lateinit var yuOnlyCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)

        category = ShopListManager.manager.currentCategory // intent.getStringExtra(getString(R.string.category_name)) ?: savedInstanceState?.getString(getString(R.string.category_name), category)
        savedInstanceState?.putString(getString(R.string.category_name), category)


        saveText = findViewById(R.id.save_button)
        progressBar = findViewById(R.id.sync_indicator)
        shopModeCheckBox = findViewById(R.id.shop_mode)
        allCategoriesCheckBox = findViewById(R.id.all_categories)
        hideEmptyCheckBox = findViewById(R.id.hide_empty)
        aOnlyCheckBox = findViewById(R.id.a_only)
        yuOnlyCheckBox = findViewById(R.id.yu_only)
        listView = this.findViewById(R.id.list_view)

        shopModeCheckBox.setOnCheckedChangeListener(this)
        saveText.setOnClickListener { startWritingList() }
        shopModeCheckBox.isChecked = ShopListManager.manager.buyMode

        allCategoriesCheckBox.setOnCheckedChangeListener (this)
        hideEmptyCheckBox.setOnCheckedChangeListener (this)
        aOnlyCheckBox.setOnCheckedChangeListener (this)
        yuOnlyCheckBox.setOnCheckedChangeListener (this)

        this.title = category?: getString(R.string.category_all_name)

        updateView()

    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putString(getString(R.string.category_name), category)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        ShopListManager.manager.buyMode = shopModeCheckBox.isChecked
        if (ShopListManager.manager.buyMode && isChecked)
            hideEmptyCheckBox.isChecked = true
        if (aOnlyCheckBox.isChecked && yuOnlyCheckBox.isChecked)
            aOnlyCheckBox.isChecked = false
        updateView()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (disposableWriter != null)
            disposableWriter!!.dispose()
    }

    private fun startWritingList(){
        progressBar.visibility = View.VISIBLE
        disposableWriter =
                Single.fromCallable{ ShopListManager.manager.save() }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError { showError(it.message!!) }
                        .subscribe(
                                Consumer{
                                    progressBar.visibility = View.INVISIBLE
                                    setEdited(false)
                                }
                        )
    }


    private fun updateView(){
        val c = if (allCategoriesCheckBox.isChecked) null else category
        val includeEmpty = !hideEmptyCheckBox.isChecked
        var buyer: String? = null
        if (aOnlyCheckBox.isChecked)
            buyer = "A"
        else if (yuOnlyCheckBox.isChecked)
            buyer = ""
        listView.adapter = ShopListAdapter(this, ShopListManager.manager.getItemsInCategory(c, includeEmpty, buyer))
    }

    // View related implementations
    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

    fun setEdited(edited: Boolean){
        val colorId = if (edited) R.color.colorAccent else R.color.colorPrimary
        saveText.setColorFilter(resources.getColor(colorId))
    }

    fun isInBuyMode():Boolean{
        return ShopListManager.manager.buyMode//modeSwitcher.isChecked
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updateView()
    }


}

class ShopListAdapter(val context: ItemListActivity, private val items: ArrayList<ShopItem>): BaseAdapter() {
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Get view for row item

        val layoutId = if (context.isInBuyMode()) R.layout.item_buy_fragment else R.layout.item_fill_in_fragment
        val rowView = convertView?: inflater.inflate(layoutId, parent, false)

        val viewName = rowView.findViewById(R.id.item_name) as TextView
        var buyerString = items[position].buyer?: ""
        if (buyerString != "") buyerString = "($buyerString)"
        viewName.text = "${items[position].name} $buyerString"

        val viewQuantity = rowView.findViewById(R.id.item_quantity) as TextView
        viewQuantity.text = items[position].quantity.toString()

        if (context.isInBuyMode()) {
            val switch = rowView.findViewById(R.id.bought) as Switch
            /*
            val l = OnSwipeTouchListener(context) {
                items[position].bought = it
                switch.isChecked = it
            }


            viewName.setOnTouchListener(l)
            */
            switch.setOnCheckedChangeListener(null)
            switch.isChecked = items[position].bought
            switch.setOnCheckedChangeListener{_, isChecked ->
                items[position].bought = isChecked
            }
        } else {
            val plusButton = rowView.findViewById(R.id.plus_button) as Button
            plusButton.setOnClickListener {
                items[position].quantity++
                viewQuantity.text = items[position].quantity.toString()
                context.setEdited(true)
            }

            val minusButton = rowView.findViewById(R.id.minus_button) as Button
            minusButton.setOnClickListener {
                items[position].less()
                viewQuantity.text = items[position].quantity.toString()
                context.setEdited(true)
            }

            viewName.setOnClickListener {
                val intent = Intent(context, EditItemActivity::class.java)
                intent.putExtra(context.getString(R.string.item_name), items[position].name)
                context.startActivityForResult(intent, 0)
            }
        }
        return rowView
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return items.size
    }



}

class OnSwipeTouchListener(ctx: ItemListActivity, val onSwipe: (Boolean) -> Unit) : OnTouchListener {
    companion object {
        private val SWIPE_MIN_DISTANCE = 100
        private val SWIPE_MAX_OFF_PATH = 200
        private val SWIPE_THRESHOLD_VELOCITY = 100
    }

    private val gestureDetector: GestureDetectorCompat

    init {
        gestureDetector = GestureDetectorCompat(ctx, GestureListener())
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
//        v.performClick()
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        if (diffX > 0) {
                            onSwipeRight()
                            onSwipe(true)
                        } else {
                            onSwipeLeft()
                            onSwipe(false)
                        }
                        result = true
                    }
                } else if (Math.abs(diffY) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            return result
        }

    }

    fun onSwipeRight() { Log.e("swipe", "right")}

    fun onSwipeLeft() {Log.e("swipe", "left")}

    fun onSwipeTop() {Log.e("swipe", "top")}

    fun onSwipeBottom() {Log.e("swipe", "bottom")}
}


//TODO: положить под GIT
//TODO: заменить в обычном списке ListView на RecyclerView
//TODO: в списке категорий заменить ListView на RecyclerView
//TODO: отрефакторить MVC
//TODO: добавление фотографии товара
//TODO: добваление штрих-кода товара
//TODO: сканирование штрих-кода товара
//TODO: придумать иерархию "пункт списка"/"товар" и т.п.
//TODO: сохранение в базу данных
//TODO: реализовать обновление/синхронихацию списка (с проверкой изменений) - низкий приоритет
//TODO: отправлять совершенные покупки вниз
//TODO: импортировать чеки
//TODO: импорртировать чеки прямо из
//TODO: для товара показывать предыдущие покупки из чеков

//TODO: убрать лишний код


//DONE!: реализовать сохранение списка целиком
//DONE!: отрефакторить работу с Google Sheets
//DONE!: добавить кнопку для перехода на экран покупок
//DONE!: экран "купить по списку" без пересортировки
//NO: автоматическое сохранение списка через 10 секунд после последнего изменения
//DONE!: индикатор синхронизации списка, возможность запустить вручную



//TODO: ГОЛОСОВОЕ УПРАВЛЕНИЕ
//TODO: выяснить как в принципе это делается
//TODO: выбор категории при составлении списка
//TODO: указание количества для конкретного товара
//TODO: переименование пунктов чтобы их было легко распознать
//TODO: выбор категории в магазине
//TODO: команда "что осталось"
//TODO: команда "куплено"
