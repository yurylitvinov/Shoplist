package com.example.yura.shoplist

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import io.reactivex.Observable
import org.junit.Test
import io.reactivex.functions.Consumer
import org.junit.Assert.*
import java.util.*

class SheetsDataSourceTest {

    @Test
    fun readList() {
        val ds = SheetsDataSource.getForConsoleApp()
        val list = ds.readList()
        testReadList(list)
    }

    private fun testReadList(list: ShopListManager) {
        assert(list.items.size > 15)

        var nullQuantity: Int = 0
        var notNullQuantity: Int = 0
        for (item in list.items)
            if (item.quantity == 0)
                nullQuantity++
            else
                notNullQuantity++

        assert(nullQuantity > 5)
        assert(notNullQuantity > 5)


        assert(list.getCategories().size > 1)
    }

    @Test
    fun readWrite(){
        val ds = SheetsDataSource.getForConsoleApp()
        val values = ds.readValues(SheetsDataSource.spreadsheetId, "Sheet3!B2:Q300")
        val shopItems = values?.map { ShopItem("").fromRow(it) }
        assertNotNull(shopItems)
        assert(shopItems!!.size > 100)
        assert(shopItems[0].name.startsWith("Сыр"))
        ds.saveValues(SheetsDataSource.spreadsheetId, "Sheet4!B2", shopItems.map { it.toRow() })
        val values2 = ds.readValues(SheetsDataSource.spreadsheetId, "Sheet4!B2:Q300")
        assertEquals(values, values2)
        assert(shopItems[116].urlGoodsRu?.length ?:0 > 5)
    }


}
