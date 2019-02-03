package com.example.yura.shoplist

import android.icu.util.CurrencyAmount
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.internal.Primitives
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileReader
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
/*
class Receipt {

    var total: Amount = Amount(0)

    companion object {
        fun fromJson(json: String): String {
            val obj = JSONObject(json)
            val doc = obj.getJSONObject("document")
            return json
        }
    }


}*/
data class Amount (val rub: Int, val kop: Int = 0)
data class ReceiptLine(val name:String, val price: Int, val quantity: Float, val sum: Int)
data class Receipt(val totalSum: Int,
                   val dateTime: String,
                   val ecashTotalSum: Int,
                   val fiscalDocumentNumber: String,
                   val fiscalDriveNumber: String,
                   val fiscalSign: String,
                   val user: String?,
                   val userInn: String,
                   val items: ArrayList<ReceiptLine>)
data class Document(val receipt: Receipt)
data class ReportItem(val document: Document)

class Purchase(receipt: Receipt, receiptLine: ReceiptLine){
    val pointOfSale = receipt.user?: "ИНН: " + receipt.userInn
    val dateTime = receipt.dateTime
    val receiptId = receipt.fiscalDriveNumber
    val name = receiptLine.name
    val price = receiptLine.price / 100
    val quantity = receiptLine.quantity
    val sum = receiptLine.sum / 100

    fun toRow(): List<String> {
        return arrayOf(pointOfSale, dateTime, receiptId, name, price.toString(), quantity.toString(), sum.toString()).asList()
    }

    override fun toString(): String {
        return toRow().toString()
    }
}

object test {
    @JvmStatic
    fun main(args: Array<String>) {

//        fileToJson("item.json", ReceiptLine::class.java)
//        val receipt = fileToJson("receipt.json", Receipt::class.java)
//        fileToJson("document.json", Document::class.java)
//        fileToJson("reportitem.json", ReportItem::class.java)
        val receipts = fileToJson("report.json", Array<ReportItem>::class.java)
        val m = HashMap<String, Receipt>()
        for (d in receipts){
            val r = d.document.receipt
            m.put(r.fiscalDriveNumber, r)
        }
        val rs = ArrayList<Receipt>()
        m.values.toCollection(rs)
        val items = rs.flatMap { receipt -> receipt.items.map { Purchase (receipt, it) } }
        println("items.size = ${items.size}")
        println("items[0] = ${items[0]}")
        val ds = SheetsDataSource.getForConsoleApp()
        ds.saveValues("1FNKIJKw32EyRI357qBIsG2Dvuko9eoamn-xdrAXf-7E", "Sheet2!A1", items.map{it.toRow()})
    }

    fun <T> fileToJson(filename: String, classOfT : Class<T>): T{
        val reader = BufferedReader(FileReader("C:\\Yura\\AndroidStudioProjects\\ShopList\\app\\src\\test\\res\\" + filename))
        val gson = Gson()
        val ret = gson.fromJson(reader, classOfT)
        println("ret = ${ret}")
        return ret
    }

}