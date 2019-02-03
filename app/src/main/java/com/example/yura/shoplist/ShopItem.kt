package com.example.yura.shoplist

class ShopItem(var name: String, var quantity: Int = 0, var category: String = "OTHER") {

    var fullName: String? = null
    var barcode: String? = null
    var buyer:String? = null
    var id: Int? = null
    var orderIndex: Int? = null
    var receiptNames: List<String>? = null
    var urlGoodsRu: String? = null
    var location: String? = null
    var bought: Boolean = false

    fun more(){
        quantity++
    }

    fun less(){
        if (quantity > 0)
            quantity--
    }

    fun fromRow(row: MutableList<Any>): ShopItem {
        location = row[0].toString()
        name = row[1].toString()
        quantity = row[2].toString().toIntOrNull()?:0
        buyer = row.getOrNull(3)?.toString()
        category = row.getOrNull(9)?.toString()?: "OTHER"
        fullName = row.getOrNull(10)?.toString()
        orderIndex = row.getOrNull(11)?.toString()?.toIntOrNull()
        id = row.getOrNull(12)?.toString()?.toIntOrNull()
        barcode = row.getOrNull(13)?.toString()
        receiptNames = row.getOrNull(14)?.toString()?.split(";")
        urlGoodsRu = row.getOrNull(15)?.toString()
        return this
    }

    fun toRow(): List<Any?> {
        return arrayOf(
            location,
            name,
            quantity.toString(),
            buyer,
            null,
            null,
            "=INDIRECT(\"R[0]C[-6]\", false)",
            "=INDIRECT(\"R[0]C[-6]\", false)",
            "=INDIRECT(\"R[0]C[-6]\", false)",
            category,
            fullName,
            orderIndex?.toString(),
            id,
            barcode,
            receiptNames?.joinToString(separator = ";"),
            urlGoodsRu
            ).asList().map { it?: " " }
    }
}