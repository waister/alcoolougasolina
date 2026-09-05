package br.com.gazoza.alcoolougasolina.data.repository

import br.com.gazoza.alcoolougasolina.domain.NotificationItem
import br.com.gazoza.alcoolougasolina.util.API_BODY
import br.com.gazoza.alcoolougasolina.util.API_DATE
import br.com.gazoza.alcoolougasolina.util.API_ID
import br.com.gazoza.alcoolougasolina.util.API_IMAGE
import br.com.gazoza.alcoolougasolina.util.API_LINK
import br.com.gazoza.alcoolougasolina.util.API_MESSAGE
import br.com.gazoza.alcoolougasolina.util.API_NOTIFICATION
import br.com.gazoza.alcoolougasolina.util.API_NOTIFICATIONS
import br.com.gazoza.alcoolougasolina.util.API_ROUTE_IDENTIFY
import br.com.gazoza.alcoolougasolina.util.API_ROUTE_NOTIFICATION
import br.com.gazoza.alcoolougasolina.util.API_ROUTE_NOTIFICATIONS
import br.com.gazoza.alcoolougasolina.util.API_SUCCESS
import br.com.gazoza.alcoolougasolina.util.API_TITLE
import br.com.gazoza.alcoolougasolina.util.API_TOKEN
import br.com.gazoza.alcoolougasolina.util.API_TYPE
import br.com.gazoza.alcoolougasolina.util.CONNECTION_ERROR
import br.com.gazoza.alcoolougasolina.util.UNKNOWN_ERROR
import br.com.gazoza.alcoolougasolina.util.getBooleanVal
import br.com.gazoza.alcoolougasolina.util.getJSONArrayVal
import br.com.gazoza.alcoolougasolina.util.getJSONObjectVal
import br.com.gazoza.alcoolougasolina.util.getStringVal
import br.com.gazoza.alcoolougasolina.util.getValidJSONObject
import br.com.gazoza.alcoolougasolina.util.printOrReport
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()

    data class Error(val message: String, val throwable: Throwable? = null) : DataResult<Nothing>()
}

interface NotificationRepository {
    suspend fun getNotifications(): DataResult<List<NotificationItem>>

    suspend fun getNotificationDetail(id: String): DataResult<NotificationItem>

    suspend fun identifyUser(token: String): DataResult<JSONObject>
}

class NotificationRepositoryImpl(private val preferencesRepository: PreferencesRepository) : NotificationRepository {
    override suspend fun getNotifications(): DataResult<List<NotificationItem>> = withContext(Dispatchers.IO) {
        try {
            val (_, _, result) = API_ROUTE_NOTIFICATIONS.httpGet().awaitStringResponseResult()
            result.fold(
                success = { jsonString ->
                    val apiObj = jsonString.getValidJSONObject()
                    if (apiObj != null && apiObj.getBooleanVal(API_SUCCESS)) {
                        val jsonArray = apiObj.getJSONArrayVal(API_NOTIFICATIONS)
                        val items = mutableListOf<NotificationItem>()
                        if (jsonArray != null) {
                            for (i in 0 until jsonArray.length()) {
                                val itemObj = jsonArray.optJSONObject(i) ?: continue
                                items.add(
                                    NotificationItem(
                                        id = itemObj.getStringVal(API_ID),
                                        title = itemObj.getStringVal(API_TITLE),
                                        body = itemObj.getStringVal(API_BODY),
                                        date = itemObj.getStringVal(API_DATE),
                                        image = itemObj.getStringVal(API_IMAGE),
                                        link = itemObj.getStringVal(API_LINK),
                                        type = itemObj.getStringVal(API_TYPE)
                                    )
                                )
                            }
                        }
                        DataResult.Success(items)
                    } else {
                        val msg =
                            apiObj?.getStringVal(API_MESSAGE) ?: "Erro ao carregar notificações"
                        DataResult.Error(msg)
                    }
                },
                failure = { error ->
                    DataResult.Error(error.message ?: CONNECTION_ERROR, error)
                }
            )
        } catch (e: Exception) {
            e.printOrReport()
            DataResult.Error(e.message ?: UNKNOWN_ERROR, e)
        }
    }

    override suspend fun getNotificationDetail(id: String): DataResult<NotificationItem> = withContext(Dispatchers.IO) {
        try {
            val cachedJson = preferencesRepository.getNotificationJson()
            if (!cachedJson.isNullOrEmpty() &&
                cachedJson
                    .getValidJSONObject()
                    ?.getStringVal(API_ID) == id
            ) {
                val obj = cachedJson.getValidJSONObject()!!
                return@withContext DataResult.Success(
                    NotificationItem(
                        id = obj.getStringVal(API_ID),
                        title = obj.getStringVal(API_TITLE),
                        body = obj.getStringVal(API_BODY),
                        date = obj.getStringVal(API_DATE),
                        image = obj.getStringVal(API_IMAGE),
                        link = obj.getStringVal(API_LINK),
                        type = obj.getStringVal(API_TYPE)
                    )
                )
            }

            val route = API_ROUTE_NOTIFICATION + id
            val (_, _, result) = route.httpGet().awaitStringResponseResult()
            result.fold(
                success = { jsonString ->
                    val apiObj = jsonString.getValidJSONObject()
                    val notifObj = apiObj?.getJSONObjectVal(API_NOTIFICATION)
                    if (notifObj != null) {
                        preferencesRepository.setNotificationJson(notifObj.toString())
                        DataResult.Success(
                            NotificationItem(
                                id = notifObj.getStringVal(API_ID),
                                title = notifObj.getStringVal(API_TITLE),
                                body = notifObj.getStringVal(API_BODY),
                                date = notifObj.getStringVal(API_DATE),
                                image = notifObj.getStringVal(API_IMAGE),
                                link = notifObj.getStringVal(API_LINK),
                                type = notifObj.getStringVal(API_TYPE)
                            )
                        )
                    } else {
                        val msg =
                            apiObj?.getStringVal(API_MESSAGE) ?: "Notificação não encontrada"
                        DataResult.Error(msg)
                    }
                },
                failure = { error ->
                    DataResult.Error(error.message ?: CONNECTION_ERROR, error)
                }
            )
        } catch (e: Exception) {
            e.printOrReport()
            DataResult.Error(e.message ?: UNKNOWN_ERROR, e)
        }
    }

    override suspend fun identifyUser(token: String): DataResult<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val params = listOf(API_TOKEN to token)
            val (_, _, result) = API_ROUTE_IDENTIFY.httpGet(params).awaitStringResponseResult()
            result.fold(
                success = { jsonString ->
                    val apiObj = jsonString.getValidJSONObject()
                    if (apiObj != null) {
                        DataResult.Success(apiObj)
                    } else {
                        DataResult.Error("Resposta inválida")
                    }
                },
                failure = { error ->
                    DataResult.Error(error.message ?: CONNECTION_ERROR, error)
                }
            )
        } catch (e: Exception) {
            e.printOrReport()
            DataResult.Error(e.message ?: UNKNOWN_ERROR, e)
        }
    }
}
