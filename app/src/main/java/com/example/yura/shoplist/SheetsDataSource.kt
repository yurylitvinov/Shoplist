package com.example.yura.shoplist

import android.support.v7.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.Drive
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

class SheetsDataSource (val transport: HttpTransport) {
    private val APPLICATION_NAME = "Google Sheets API Java Quickstart"
    private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

//    private val spreadsheetId = "1FNKIJKw32EyRI357qBIsG2Dvuko9eoamn-xdrAXf-7E"
//    private val range = "Sheet1!B2:K300"
    var credential: HttpRequestInitializer? = null

    fun authorizeConsoleApp() {
        val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY, SheetsScopes.SPREADSHEETS)
        // Load client secrets.
        val `in` = FileInputStream(CONSOLE_CREDENTIALS_PATH)
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
                transport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build()
        val app = AuthorizationCodeInstalledApp(flow, LocalServerReceiver())
        credential = app.authorize("user")
    }

    fun readValues(spreadsheetId: String, range: String): MutableList<MutableList<Any>>? {
        // Build a new authorized API client service.
        if (credential == null)
            return null
        val service = Sheets.Builder(transport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build()
        val response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
        return response.getValues()
    }

    fun readItems(): List<ShopItem>? {
        return readValues(spreadsheetId, range)?.map { itemFromRow(it) }
    }

    fun readList(): ShopListManager {
        val manager = ShopListManager()
        readItems()?.toCollection(manager.items)
        manager.source = this
        return manager
    }

    fun saveList(manager: ShopListManager){
        saveValues(SheetsDataSource.spreadsheetId, "Sheet1!B2", manager.items.map { it.toRow() })
    }

    fun saveValues(spreadsheetId: String, range: String, values: List<List<Any?>>){
        val service = Sheets.Builder(transport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build()
        val body = ValueRange().setValues(values)
        service.spreadsheets().values().update(spreadsheetId, range, body)
            .setValueInputOption("USER_ENTERED")
            .execute()
    }

    fun sync(list: ShopListManager) {
        if (list.items.size == 0)
            readItems()?.toCollection(list.items)
        saveList(list)
    }


    companion object {

        const val TOKENS_DIRECTORY_PATH = "tokens"
        const val CONSOLE_CREDENTIALS_PATH = "C:\\yura\\AndroidStudioProjects\\ShopList\\app\\src\\main\\res\\credentials.json"
        const val RQ_GOOGLE_SIGN_IN = 999
        const val range = "Sheet1!B2:Q300"
        const val spreadsheetId = "1FNKIJKw32EyRI357qBIsG2Dvuko9eoamn-xdrAXf-7E"


        fun getForMobileApp(context: AppCompatActivity): SheetsDataSource {
            val transport = AndroidHttp.newCompatibleTransport()
            val ds = SheetsDataSource(transport)
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                val signInOptions : GoogleSignInOptions =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
                        .requestScopes(Drive.SCOPE_FILE)
                        .requestEmail()
                        .build()
                val googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
                context.startActivityForResult(googleSignInClient.signInIntent, RQ_GOOGLE_SIGN_IN)
            } else {
                val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS_READONLY)
                val webCredential = GoogleAccountCredential
                    .usingOAuth2(context, Arrays.asList(*SCOPES))
                    .setBackOff(ExponentialBackOff())
                webCredential.selectedAccount = account?.account
                ds.credential = webCredential
            }
            return ds
        }

        fun getForConsoleApp (): SheetsDataSource {
            val ds = SheetsDataSource(GoogleNetHttpTransport.newTrustedTransport())
            ds.authorizeConsoleApp()
            return ds
        }
    }

    private fun itemFromRow(row: MutableList<Any>): ShopItem {
        val item = ShopItem(row[1].toString())
        return item.fromRow(row)
    }

}