package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.dto.CategoriaServicioDto
import com.example.a3df_studio_app.data.remote.dto.ProductoDto
import com.example.a3df_studio_app.data.remote.dto.PromocionDto
import com.example.a3df_studio_app.data.remote.dto.ServicioDto
import com.example.a3df_studio_app.data.remote.supabase.Database
import com.example.a3df_studio_app.data.remote.supabase.Tables
import com.example.a3df_studio_app.data.remote.supabase.supabaseCall
import com.example.a3df_studio_app.domain.mappers.toDomain
import com.example.a3df_studio_app.domain.models.CategoriaServicio
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.models.Promocion
import com.example.a3df_studio_app.domain.models.Servicio
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant

class CatalogRepository {

    private val columnasServicio = Columns.raw("*, categorias_servicios(nombre)")

    suspend fun obtenerProductos(busqueda: String? = null): List<Producto> = supabaseCall {
        val termino = busqueda?.trim()?.takeIf { it.isNotEmpty() }
        Database.postgrest.from(Tables.PRODUCTOS)
            .select {
                filter {
                    eq("activo", true)
                    if (termino != null) ilike("nombre", "%$termino%")
                }
                order("nombre", Order.ASCENDING)
            }
            .decodeList<ProductoDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerProducto(id: Long): Producto? = supabaseCall {
        Database.postgrest.from(Tables.PRODUCTOS)
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<ProductoDto>()
            ?.toDomain()
    }

    suspend fun obtenerCategorias(): List<CategoriaServicio> = supabaseCall {
        Database.postgrest.from(Tables.CATEGORIAS_SERVICIOS)
            .select {
                filter { eq("activo", true) }
                order("nombre", Order.ASCENDING)
            }
            .decodeList<CategoriaServicioDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerServicios(idCategoria: Long? = null): List<Servicio> = supabaseCall {
        Database.postgrest.from(Tables.SERVICIOS)
            .select(columnasServicio) {
                filter {
                    eq("activo", true)
                    if (idCategoria != null) eq("id_categoria", idCategoria)
                }
                order("nombre", Order.ASCENDING)
            }
            .decodeList<ServicioDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerServicio(id: Long): Servicio? = supabaseCall {
        Database.postgrest.from(Tables.SERVICIOS)
            .select(columnasServicio) { filter { eq("id", id) } }
            .decodeSingleOrNull<ServicioDto>()
            ?.toDomain()
    }

    suspend fun obtenerPromocionesActivas(): List<Promocion> = supabaseCall {
        val ahora = Instant.now().toString()
        Database.postgrest.from(Tables.PROMOCIONES)
            .select {
                filter {
                    eq("activo", true)
                    lte("fecha_inicio", ahora)
                    gte("fecha_fin", ahora)
                }
                order("fecha_fin", Order.ASCENDING)
            }
            .decodeList<PromocionDto>()
            .map { it.toDomain() }
    }
}
