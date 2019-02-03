package com.example.yura.shoplist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

class SendJSONActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_json)

        Log.e("mytag",  intent.type)
        Toast.makeText(this, intent.type, Toast.LENGTH_SHORT).show()

    }
}
