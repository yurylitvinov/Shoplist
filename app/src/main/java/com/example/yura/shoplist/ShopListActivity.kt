package com.example.yura.shoplist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


abstract class ShopListActivity: AppCompatActivity() {

    private var disposableReader: Disposable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        startReadingList()
    }

    private fun startReadingList() {
        if (ShopListManager.manager.items.size < 50) {

            disposableReader =
                    Single.fromCallable{ ShopListManager.connectToGoogleSheets(this) }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError { showError(it.message!!) }
                        .subscribe(Consumer { updateUI() })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SheetsDataSource.RQ_GOOGLE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                updateUI()
            } else {
                showError("Login failed")
            }
        }
    }


    abstract fun updateUI()

    private fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }

}