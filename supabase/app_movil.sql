-- ============================================================
-- 3DForge Studio - Extensión para la app móvil (Supabase)
-- Ejecutar DESPUÉS de 3DForge_Studio_inicializacion_postgresql_final.sql
-- ============================================================

ALTER TABLE clientes ADD COLUMN IF NOT EXISTS auth_uid UUID UNIQUE REFERENCES auth.users(id) ON DELETE SET NULL;
ALTER TABLE clientes ADD COLUMN IF NOT EXISTS foto_url TEXT;
CREATE INDEX IF NOT EXISTS idx_clientes_auth_uid ON clientes (auth_uid);

-- ------------------------------------------------------------
-- FUNCIONES AUXILIARES
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_cliente_actual()
RETURNS BIGINT
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public AS $$
    SELECT id FROM clientes WHERE auth_uid = auth.uid() LIMIT 1;
$$;

CREATE OR REPLACE FUNCTION public.app_usuario_sistema()
RETURNS BIGINT
LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_id BIGINT;
BEGIN
    SELECT id INTO v_id
    FROM usuarios
    WHERE is_active AND (is_superuser OR is_staff)
    ORDER BY is_superuser DESC, id
    LIMIT 1;
    IF v_id IS NULL THEN
        RAISE EXCEPTION 'No existe un usuario del sistema (is_staff) para asignar solicitudes de la app';
    END IF;
    RETURN v_id;
END;
$$;

-- ------------------------------------------------------------
-- CLIENTE (perfil CRM vinculado a Supabase Auth)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_asegurar_cliente(
    p_nombres TEXT DEFAULT NULL,
    p_apellidos TEXT DEFAULT NULL,
    p_telefono TEXT DEFAULT NULL,
    p_foto_url TEXT DEFAULT NULL
)
RETURNS clientes
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_uid UUID := auth.uid();
    v_correo TEXT := lower(coalesce(auth.jwt() ->> 'email', ''));
    v_cliente clientes;
BEGIN
    IF v_uid IS NULL THEN
        RAISE EXCEPTION 'AUTH_REQUIRED';
    END IF;

    SELECT * INTO v_cliente FROM clientes WHERE auth_uid = v_uid;

    IF NOT FOUND AND v_correo <> '' THEN
        UPDATE clientes
        SET auth_uid = v_uid, actualizado_en = now()
        WHERE id = (
            SELECT id FROM clientes
            WHERE auth_uid IS NULL AND lower(correo) = v_correo
            ORDER BY id LIMIT 1
        )
        RETURNING * INTO v_cliente;
    END IF;

    IF v_cliente.id IS NULL THEN
        INSERT INTO clientes (auth_uid, tipo_cliente, nombres, apellidos, correo, telefono, foto_url, observaciones)
        VALUES (
            v_uid, 'PERSONA',
            nullif(trim(p_nombres), ''),
            nullif(trim(p_apellidos), ''),
            v_correo,
            nullif(trim(p_telefono), ''),
            nullif(p_foto_url, ''),
            'Registrado desde la app móvil'
        )
        RETURNING * INTO v_cliente;
    ELSE
        UPDATE clientes SET
            nombres = coalesce(nombres, nullif(trim(p_nombres), '')),
            apellidos = coalesce(apellidos, nullif(trim(p_apellidos), '')),
            telefono = coalesce(telefono, nullif(trim(p_telefono), '')),
            foto_url = coalesce(nullif(p_foto_url, ''), foto_url),
            actualizado_en = now()
        WHERE id = v_cliente.id
        RETURNING * INTO v_cliente;
    END IF;

    RETURN v_cliente;
END;
$$;

-- ------------------------------------------------------------
-- COTIZACIONES (precios calculados en el servidor)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_crear_cotizacion(
    p_items JSONB,
    p_observaciones TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_cliente BIGINT := app_cliente_actual();
    v_iva NUMERIC := 0.13;
    v_cot_id BIGINT;
    v_codigo TEXT;
    v_item JSONB;
    v_servicio servicios;
    v_cantidad NUMERIC(10,2);
    v_sub NUMERIC(12,2);
    v_subtotal NUMERIC(12,2) := 0;
    v_impuesto NUMERIC(12,2);
BEGIN
    IF v_cliente IS NULL THEN RAISE EXCEPTION 'CLIENTE_NO_REGISTRADO'; END IF;
    IF p_items IS NULL OR jsonb_typeof(p_items) <> 'array' OR jsonb_array_length(p_items) = 0 THEN
        RAISE EXCEPTION 'SIN_ITEMS';
    END IF;

    v_codigo := 'COT-APP-' || to_char(now(), 'YYMMDDHH24MISS') || '-' || lpad((floor(random() * 1000))::INT::TEXT, 3, '0');

    INSERT INTO cotizaciones (id_cliente, id_usuario_responsable, codigo, fecha_vencimiento, estado, observaciones)
    VALUES (v_cliente, app_usuario_sistema(), v_codigo, now() + INTERVAL '15 days', 'BORRADOR', p_observaciones)
    RETURNING id INTO v_cot_id;

    FOR v_item IN SELECT value FROM jsonb_array_elements(p_items) LOOP
        SELECT * INTO v_servicio FROM servicios
        WHERE id = (v_item ->> 'id_servicio')::BIGINT AND activo;
        IF NOT FOUND THEN RAISE EXCEPTION 'SERVICIO_NO_DISPONIBLE'; END IF;

        v_cantidad := coalesce((v_item ->> 'cantidad')::NUMERIC, 1);
        IF v_cantidad <= 0 THEN RAISE EXCEPTION 'CANTIDAD_INVALIDA'; END IF;

        v_sub := round(v_servicio.precio_base * v_cantidad, 2);
        INSERT INTO cotizaciones_detalles (id_cotizacion, id_servicio, descripcion, cantidad, precio_unitario, impuesto, subtotal)
        VALUES (
            v_cot_id, v_servicio.id,
            left(coalesce(nullif(trim(v_item ->> 'descripcion'), ''), v_servicio.nombre), 250),
            v_cantidad, v_servicio.precio_base, round(v_sub * v_iva, 2), v_sub
        );
        v_subtotal := v_subtotal + v_sub;
    END LOOP;

    v_impuesto := round(v_subtotal * v_iva, 2);
    UPDATE cotizaciones
    SET subtotal = v_subtotal, impuesto = v_impuesto, total = v_subtotal + v_impuesto
    WHERE id = v_cot_id;

    RETURN jsonb_build_object('id', v_cot_id, 'codigo', v_codigo, 'total', v_subtotal + v_impuesto);
END;
$$;

-- ------------------------------------------------------------
-- PEDIDOS DE PRODUCTOS (encargo + factura pendiente)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_crear_pedido(
    p_items JSONB,
    p_observaciones TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_cliente_id BIGINT := app_cliente_actual();
    v_cliente clientes;
    v_iva NUMERIC := 0.13;
    v_enc_id BIGINT;
    v_fac_id BIGINT;
    v_codigo TEXT;
    v_numero TEXT;
    v_sufijo TEXT := to_char(now(), 'YYMMDDHH24MISS') || '-' || lpad((floor(random() * 1000))::INT::TEXT, 3, '0');
    v_item JSONB;
    v_producto productos;
    v_cantidad INTEGER;
    v_sub NUMERIC(12,2);
    v_subtotal NUMERIC(12,2) := 0;
    v_impuesto NUMERIC(12,2);
BEGIN
    IF v_cliente_id IS NULL THEN RAISE EXCEPTION 'CLIENTE_NO_REGISTRADO'; END IF;
    IF p_items IS NULL OR jsonb_typeof(p_items) <> 'array' OR jsonb_array_length(p_items) = 0 THEN
        RAISE EXCEPTION 'SIN_ITEMS';
    END IF;

    SELECT * INTO v_cliente FROM clientes WHERE id = v_cliente_id;
    v_codigo := 'ENC-APP-' || v_sufijo;
    v_numero := 'FAC-APP-' || v_sufijo;

    INSERT INTO encargos (id_cliente, codigo, descripcion, estado, observaciones)
    VALUES (v_cliente_id, v_codigo, 'Pedido de productos desde la app móvil', 'PENDIENTE', p_observaciones)
    RETURNING id INTO v_enc_id;

    FOR v_item IN SELECT value FROM jsonb_array_elements(p_items) LOOP
        SELECT * INTO v_producto FROM productos
        WHERE id = (v_item ->> 'id_producto')::BIGINT AND activo;
        IF NOT FOUND THEN RAISE EXCEPTION 'PRODUCTO_NO_DISPONIBLE'; END IF;

        v_cantidad := coalesce((v_item ->> 'cantidad')::INTEGER, 1);
        IF v_cantidad <= 0 THEN RAISE EXCEPTION 'CANTIDAD_INVALIDA'; END IF;
        IF v_producto.stock < v_cantidad THEN
            RAISE EXCEPTION 'STOCK_INSUFICIENTE:%', v_producto.nombre;
        END IF;

        v_sub := round(v_producto.precio * v_cantidad, 2);
        INSERT INTO encargos_detalles (id_encargo, id_producto, descripcion, cantidad, precio_unitario, impuesto, subtotal)
        VALUES (v_enc_id, v_producto.id, v_producto.nombre, v_cantidad, v_producto.precio, round(v_sub * v_iva, 2), v_sub);
        v_subtotal := v_subtotal + v_sub;
    END LOOP;

    v_impuesto := round(v_subtotal * v_iva, 2);
    UPDATE encargos
    SET subtotal = v_subtotal, impuesto = v_impuesto, total = v_subtotal + v_impuesto
    WHERE id = v_enc_id;

    INSERT INTO historial_encargos (id_encargo, id_usuario, estado_anterior, estado_nuevo, comentario)
    VALUES (v_enc_id, app_usuario_sistema(), NULL, 'PENDIENTE', 'Pedido creado desde la app móvil');

    INSERT INTO facturas (
        id_cliente, id_encargo, numero_factura, fecha_vencimiento, nombre_facturacion,
        identificacion_fiscal, direccion_facturacion, subtotal, impuesto, total, saldo_pendiente, estado
    )
    VALUES (
        v_cliente_id, v_enc_id, v_numero, now() + INTERVAL '7 days',
        left(coalesce(nullif(v_cliente.nombre_empresa, ''),
                      nullif(trim(coalesce(v_cliente.nombres, '') || ' ' || coalesce(v_cliente.apellidos, '')), ''),
                      v_cliente.correo), 150),
        v_cliente.identificacion_fiscal, v_cliente.direccion,
        v_subtotal, v_impuesto, v_subtotal + v_impuesto, v_subtotal + v_impuesto, 'PENDIENTE'
    )
    RETURNING id INTO v_fac_id;

    INSERT INTO facturas_detalles (id_factura, id_producto, descripcion, cantidad, precio_unitario, impuesto, subtotal)
    SELECT v_fac_id, id_producto, left(descripcion, 250), cantidad, precio_unitario, impuesto, subtotal
    FROM encargos_detalles
    WHERE id_encargo = v_enc_id;

    RETURN jsonb_build_object(
        'id_encargo', v_enc_id,
        'codigo', v_codigo,
        'id_factura', v_fac_id,
        'numero_factura', v_numero,
        'total', v_subtotal + v_impuesto
    );
END;
$$;

-- ------------------------------------------------------------
-- PAGO SIMULADO CON TARJETA (solo últimos 4 dígitos)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_registrar_pago_simulado(
    p_id_factura BIGINT,
    p_monto NUMERIC,
    p_marca TEXT,
    p_ultimos4 TEXT,
    p_titular TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_cliente BIGINT := app_cliente_actual();
    v_factura facturas;
    v_metodo SMALLINT;
    v_pago_id BIGINT;
    v_ref TEXT;
    v_monto NUMERIC(12,2) := round(p_monto, 2);
    v_saldo NUMERIC(12,2);
    v_estado TEXT;
BEGIN
    IF v_cliente IS NULL THEN RAISE EXCEPTION 'CLIENTE_NO_REGISTRADO'; END IF;

    SELECT * INTO v_factura FROM facturas
    WHERE id = p_id_factura AND id_cliente = v_cliente
    FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'FACTURA_NO_ENCONTRADA'; END IF;
    IF v_factura.estado NOT IN ('PENDIENTE', 'PARCIALMENTE_PAGADA', 'VENCIDA') THEN
        RAISE EXCEPTION 'FACTURA_NO_PAGABLE';
    END IF;
    IF v_monto IS NULL OR v_monto <= 0 OR v_monto > v_factura.saldo_pendiente THEN
        RAISE EXCEPTION 'MONTO_INVALIDO';
    END IF;
    IF p_ultimos4 IS NULL OR p_ultimos4 !~ '^[0-9]{4}$' THEN
        RAISE EXCEPTION 'TARJETA_INVALIDA';
    END IF;

    SELECT id INTO v_metodo FROM metodos_pago WHERE upper(nombre) = 'TARJETA' LIMIT 1;
    IF v_metodo IS NULL THEN
        INSERT INTO metodos_pago (nombre, descripcion)
        VALUES ('TARJETA', 'Tarjeta de crédito o débito')
        RETURNING id INTO v_metodo;
    END IF;

    v_ref := 'SIM-' || upper(substr(md5(random()::TEXT || clock_timestamp()::TEXT), 1, 12));

    INSERT INTO pagos (id_factura, id_metodo_pago, referencia, monto, estado, observaciones)
    VALUES (
        v_factura.id, v_metodo, v_ref, v_monto, 'CONFIRMADO',
        format('Pago simulado desde app: %s ****%s', upper(coalesce(p_marca, 'TARJETA')), p_ultimos4)
    )
    RETURNING id INTO v_pago_id;

    INSERT INTO transacciones_pasarela (id_pago, proveedor, referencia_externa, estado_respuesta, respuesta, fecha_respuesta)
    VALUES (
        v_pago_id, 'SIMULADO', v_ref, 'APPROVED',
        jsonb_build_object('marca', p_marca, 'ultimos4', p_ultimos4, 'titular', p_titular, 'simulado', TRUE),
        now()
    );

    v_saldo := greatest(v_factura.saldo_pendiente - v_monto, 0);
    v_estado := CASE WHEN v_saldo = 0 THEN 'PAGADA' ELSE 'PARCIALMENTE_PAGADA' END;

    UPDATE facturas SET saldo_pendiente = v_saldo, estado = v_estado WHERE id = v_factura.id;

    IF v_estado = 'PAGADA' AND v_factura.id_encargo IS NOT NULL THEN
        UPDATE encargos SET estado = 'EN_REVISION'
        WHERE id = v_factura.id_encargo AND estado = 'PENDIENTE';
        IF FOUND THEN
            INSERT INTO historial_encargos (id_encargo, id_usuario, estado_anterior, estado_nuevo, comentario)
            VALUES (v_factura.id_encargo, app_usuario_sistema(), 'PENDIENTE', 'EN_REVISION', 'Pago confirmado desde la app móvil');
        END IF;
    END IF;

    RETURN jsonb_build_object(
        'id_pago', v_pago_id,
        'referencia', v_ref,
        'estado', 'CONFIRMADO',
        'saldo_pendiente', v_saldo,
        'estado_factura', v_estado
    );
END;
$$;

-- ------------------------------------------------------------
-- CRM: INTERACCIONES (asesorías, seguimientos)
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION public.app_registrar_interaccion(
    p_tipo TEXT,
    p_asunto TEXT,
    p_descripcion TEXT DEFAULT NULL
)
RETURNS BIGINT
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_cliente BIGINT := app_cliente_actual();
    v_id BIGINT;
BEGIN
    IF v_cliente IS NULL THEN RAISE EXCEPTION 'CLIENTE_NO_REGISTRADO'; END IF;
    IF upper(p_tipo) NOT IN ('LLAMADA', 'CORREO', 'ASESORIA', 'REUNION', 'SEGUIMIENTO', 'RECLAMO') THEN
        RAISE EXCEPTION 'TIPO_INVALIDO';
    END IF;

    INSERT INTO interacciones_clientes (id_cliente, id_usuario_responsable, tipo_interaccion, asunto, descripcion)
    VALUES (v_cliente, app_usuario_sistema(), upper(p_tipo), left(p_asunto, 150), p_descripcion)
    RETURNING id INTO v_id;

    RETURN v_id;
END;
$$;

-- ------------------------------------------------------------
-- PERMISOS DE FUNCIONES
-- ------------------------------------------------------------

REVOKE ALL ON FUNCTION public.app_cliente_actual() FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_usuario_sistema() FROM PUBLIC, anon, authenticated;
REVOKE ALL ON FUNCTION public.app_asegurar_cliente(TEXT, TEXT, TEXT, TEXT) FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_crear_cotizacion(JSONB, TEXT) FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_crear_pedido(JSONB, TEXT) FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_registrar_pago_simulado(BIGINT, NUMERIC, TEXT, TEXT, TEXT) FROM PUBLIC, anon;
REVOKE ALL ON FUNCTION public.app_registrar_interaccion(TEXT, TEXT, TEXT) FROM PUBLIC, anon;

GRANT EXECUTE ON FUNCTION public.app_cliente_actual() TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_asegurar_cliente(TEXT, TEXT, TEXT, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_crear_cotizacion(JSONB, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_crear_pedido(JSONB, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_registrar_pago_simulado(BIGINT, NUMERIC, TEXT, TEXT, TEXT) TO authenticated;
GRANT EXECUTE ON FUNCTION public.app_registrar_interaccion(TEXT, TEXT, TEXT) TO authenticated;

-- ------------------------------------------------------------
-- ROW LEVEL SECURITY
-- Todas las tablas quedan cerradas por defecto; solo se abre
-- lo que la app necesita. Django (dueño de las tablas) no se ve afectado.
-- ------------------------------------------------------------

ALTER TABLE estados_usuarios        ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuarios                ENABLE ROW LEVEL SECURITY;
ALTER TABLE clientes                ENABLE ROW LEVEL SECURITY;
ALTER TABLE empleados               ENABLE ROW LEVEL SECURITY;
ALTER TABLE categorias_servicios    ENABLE ROW LEVEL SECURITY;
ALTER TABLE servicios               ENABLE ROW LEVEL SECURITY;
ALTER TABLE productos               ENABLE ROW LEVEL SECURITY;
ALTER TABLE cotizaciones            ENABLE ROW LEVEL SECURITY;
ALTER TABLE cotizaciones_detalles   ENABLE ROW LEVEL SECURITY;
ALTER TABLE encargos                ENABLE ROW LEVEL SECURITY;
ALTER TABLE encargos_detalles       ENABLE ROW LEVEL SECURITY;
ALTER TABLE archivos_encargos       ENABLE ROW LEVEL SECURITY;
ALTER TABLE historial_encargos      ENABLE ROW LEVEL SECURITY;
ALTER TABLE facturas                ENABLE ROW LEVEL SECURITY;
ALTER TABLE facturas_detalles       ENABLE ROW LEVEL SECURITY;
ALTER TABLE metodos_pago            ENABLE ROW LEVEL SECURITY;
ALTER TABLE pagos                   ENABLE ROW LEVEL SECURITY;
ALTER TABLE transacciones_pasarela  ENABLE ROW LEVEL SECURITY;
ALTER TABLE interacciones_clientes  ENABLE ROW LEVEL SECURITY;
ALTER TABLE notas_clientes          ENABLE ROW LEVEL SECURITY;
ALTER TABLE solicitudes_soporte     ENABLE ROW LEVEL SECURITY;
ALTER TABLE notificaciones          ENABLE ROW LEVEL SECURITY;
ALTER TABLE promociones             ENABLE ROW LEVEL SECURITY;
ALTER TABLE resenas                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE configuracion_negocio   ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS app_categorias_lectura ON categorias_servicios;
CREATE POLICY app_categorias_lectura ON categorias_servicios FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_servicios_lectura ON servicios;
CREATE POLICY app_servicios_lectura ON servicios FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_productos_lectura ON productos;
CREATE POLICY app_productos_lectura ON productos FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_promociones_lectura ON promociones;
CREATE POLICY app_promociones_lectura ON promociones FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_metodos_pago_lectura ON metodos_pago;
CREATE POLICY app_metodos_pago_lectura ON metodos_pago FOR SELECT TO anon, authenticated USING (activo);

DROP POLICY IF EXISTS app_negocio_lectura ON configuracion_negocio;
CREATE POLICY app_negocio_lectura ON configuracion_negocio FOR SELECT TO anon, authenticated USING (TRUE);

DROP POLICY IF EXISTS app_clientes_lectura ON clientes;
CREATE POLICY app_clientes_lectura ON clientes FOR SELECT TO authenticated
    USING (auth_uid = auth.uid());

DROP POLICY IF EXISTS app_clientes_actualizacion ON clientes;
CREATE POLICY app_clientes_actualizacion ON clientes FOR UPDATE TO authenticated
    USING (auth_uid = auth.uid())
    WITH CHECK (auth_uid = auth.uid());

REVOKE UPDATE ON clientes FROM anon, authenticated;
GRANT UPDATE (tipo_cliente, nombres, apellidos, nombre_empresa, telefono,
              identificacion_fiscal, direccion, foto_url, actualizado_en)
    ON clientes TO authenticated;

DROP POLICY IF EXISTS app_cotizaciones_lectura ON cotizaciones;
CREATE POLICY app_cotizaciones_lectura ON cotizaciones FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_cot_detalles_lectura ON cotizaciones_detalles;
CREATE POLICY app_cot_detalles_lectura ON cotizaciones_detalles FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM cotizaciones c WHERE c.id = id_cotizacion AND c.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_encargos_lectura ON encargos;
CREATE POLICY app_encargos_lectura ON encargos FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_enc_detalles_lectura ON encargos_detalles;
CREATE POLICY app_enc_detalles_lectura ON encargos_detalles FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM encargos e WHERE e.id = id_encargo AND e.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_historial_lectura ON historial_encargos;
CREATE POLICY app_historial_lectura ON historial_encargos FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM encargos e WHERE e.id = id_encargo AND e.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_facturas_lectura ON facturas;
CREATE POLICY app_facturas_lectura ON facturas FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_fact_detalles_lectura ON facturas_detalles;
CREATE POLICY app_fact_detalles_lectura ON facturas_detalles FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM facturas f WHERE f.id = id_factura AND f.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_pagos_lectura ON pagos;
CREATE POLICY app_pagos_lectura ON pagos FOR SELECT TO authenticated
    USING (EXISTS (SELECT 1 FROM facturas f WHERE f.id = id_factura AND f.id_cliente = app_cliente_actual()));

DROP POLICY IF EXISTS app_resenas_lectura ON resenas;
CREATE POLICY app_resenas_lectura ON resenas FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_resenas_insercion ON resenas;
CREATE POLICY app_resenas_insercion ON resenas FOR INSERT TO authenticated
    WITH CHECK (
        id_cliente = app_cliente_actual()
        AND (id_encargo IS NULL OR EXISTS (
            SELECT 1 FROM encargos e WHERE e.id = id_encargo AND e.id_cliente = app_cliente_actual()
        ))
    );

DROP POLICY IF EXISTS app_soporte_lectura ON solicitudes_soporte;
CREATE POLICY app_soporte_lectura ON solicitudes_soporte FOR SELECT TO authenticated
    USING (id_cliente = app_cliente_actual());

DROP POLICY IF EXISTS app_soporte_insercion ON solicitudes_soporte;
CREATE POLICY app_soporte_insercion ON solicitudes_soporte FOR INSERT TO authenticated
    WITH CHECK (id_cliente = app_cliente_actual() AND estado = 'ABIERTA' AND id_usuario_responsable IS NULL);
