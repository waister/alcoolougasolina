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
import br.com.gazoza.alcoolougasolina.util.getBooleanVal
import br.com.gazoza.alcoolougasolina.util.getJSONArrayVal
import br.com.gazoza.alcoolougasolina.util.getJSONObjectVal
import br.com.gazoza.alcoolougasolina.util.getStringVal
import br.com.gazoza.alcoolougasolina.util.getValidJSONObject
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<NotificationItem>>
    suspend fun getNotificationDetail(id: String): Result<NotificationItem>
    suspend fun identifyUser(token: String): Result<JSONObject>
}

class NotificationRepositoryImpl(
    private val preferencesRepository: PreferencesRepository,
) : NotificationRepository {

    override suspend fun getNotifications(): Result<List<NotificationItem>> = withContext(Dispatchers.IO) {
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
                                        type = itemObj.getStringVal(API_TYPE),
                                    ),
                                )
                            }
                        }
                        Result.success(items)
                    } else {
                        val msg = apiObj?.getStringVal(API_MESSAGE) ?: "Erro ao carregar notificações"
                        Result.failure(Exception(msg))
                    }
                },
                failure = { error ->
                    Result.failure(error)
                },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotificationDetail(id: String): Result<NotificationItem> = withContext(Dispatchers.IO) {
        try {
            val cachedJson = preferencesRepository.getNotificationJson()
            if (!cachedJson.isNullOrEmpty() && cachedJson.getValidJSONObject()?.getStringVal(API_ID) == id) {
                val obj = cachedJson.getValidJSONObject()!!
                return@withContext Result.success(
                    NotificationItem(
                        id = obj.getStringVal(API_ID),
                        title = obj.getStringVal(API_TITLE),
                        body = obj.getStringVal(API_BODY),
                        date = obj.getStringVal(API_DATE),
                        image = obj.getStringVal(API_IMAGE),
                        link = obj.getStringVal(API_LINK),
                        type = obj.getStringVal(API_TYPE),
                    ),
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
                        Result.success(
                            NotificationItem(
                                id = notifObj.getStringVal(API_ID),
                                title = notifObj.getStringVal(API_TITLE),
                                body = notifObj.getStringVal(API_BODY),
                                date = notifObj.getStringVal(API_DATE),
                                image = notifObj.getStringVal(API_IMAGE),
                                link = notifObj.getStringVal(API_LINK),
                                type = notifObj.getStringVal(API_TYPE),
                            ),
                        )
                    } else {
                        val msg = apiObj?.getStringVal(API_MESSAGE) ?: "Notificação não encontrada"
                        Result.failure(Exception(msg))
                    }
                },
                failure = { error ->
                    Result.failure(error)
                },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun identifyUser(token: String): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val params = listOf(API_TOKEN to token)
            val (_, _, result) = API_ROUTE_IDENTIFY.httpGet(params).awaitStringResponseResult()
            result.fold(
                success = { jsonString ->
                    val apiObj = jsonString.getValidJSONObject()
                    if (apiObj != null) {
                        Result.success(apiObj)
                    } else {
                        Result.failure(Exception("Resposta inválida"))
                    }
                },
                failure = { error ->
                    Result.failure(error)
                },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
