package com.example.signinregister.domain.repository

import android.content.Context
import android.util.Log
import com.example.signinregister.R
import com.example.signinregister.domain.repository.DirectionsApiService
import com.example.signinregister.domain.repository.DirectionsResponse
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException

private const val API_ERROR_TAG = "DIRECTIONS_API_ERROR"
private const val NETWORK_ERROR_TAG = "NETWORK_FAILURE"

class RouteRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RouteRepository {

    private val api: DirectionsApiService
    private val mapsApiKey: String = context.getString(R.string.google_maps_key)

    init {
        Log.d("RUTA_INIT_DEBUG", "Clave leída: ${if (mapsApiKey.isBlank()) "Vacía/No Encontrada" else "OK"}")

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(DirectionsApiService::class.java)
        Log.d("RUTA_INIT_DEBUG", "Retrofit de Rutas inicializado.")
    }

    override suspend fun getRoutePolyline(origin: LatLng, destination: LatLng): String = withContext(Dispatchers.IO) {

        val originStr = "${origin.latitude},${origin.longitude}"
        val destStr = "${destination.latitude},${destination.longitude}"

        return@withContext try {

            val response = api.getDirections(originStr, destStr, apiKey = mapsApiKey)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e(API_ERROR_TAG, "Fallo HTTP: Código ${response.code()}")
                Log.e(API_ERROR_TAG, "Mensaje: ${response.message()}")
                Log.e(API_ERROR_TAG, "Cuerpo de error (JSON de Google): $errorBody")

                throw Exception("Error de servidor (${response.code()}). Revisar Logcat (API_ERROR_TAG) para el detalle.")
            }

            val responseBody = response.body() ?: throw Exception("Cuerpo de respuesta nulo de la API.")

            if (responseBody.routes.isEmpty()) {
                Log.e(API_ERROR_TAG, "API devolvió 0 rutas (ZERO_RESULTS). Origen: $originStr, Destino: $destStr")
                throw Exception("No se encontró una ruta válida entre los puntos.")
            }

            responseBody.routes.first().overviewPolyline.points

        } catch (e: IOException) {
            Log.e(NETWORK_ERROR_TAG, "FALLO DE CONEXIÓN DE RED. Mensaje: ${e.message}", e)
            throw RuntimeException("Fallo de red al conectar con Google: ${e.message}")
        } catch (e: Exception) {
            Log.e("RUTA_API_ERROR_FINAL", "Fallo final de ruta. Mensaje: ${e.message}", e)
            throw e
        }
    }
}
