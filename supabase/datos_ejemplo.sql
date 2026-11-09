-- ============================================================
-- 3DForge Studio - Datos de ejemplo para probar la app (opcional)
-- ============================================================

INSERT INTO estados_usuarios (nombre, descripcion)
VALUES ('ACTIVO', 'Usuario activo')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO usuarios (password, is_superuser, nombres, apellidos, correo, is_staff, is_active, id_estado)
SELECT '!', TRUE, 'Sistema', 'App Móvil', 'sistema@3dforge.local', TRUE, TRUE, id
FROM estados_usuarios WHERE nombre = 'ACTIVO'
ON CONFLICT (correo) DO NOTHING;

INSERT INTO categorias_servicios (nombre, descripcion) VALUES
    ('Impresión FDM', 'Piezas funcionales en PLA, PETG y ABS'),
    ('Impresión en resina', 'Alta definición para miniaturas y joyería'),
    ('Modelado 3D', 'Diseño de piezas a partir de bocetos o planos'),
    ('Postprocesado', 'Lijado, pintura y acabados profesionales')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO servicios (id_categoria, nombre, descripcion, precio_base, requiere_archivo)
SELECT c.id, s.nombre, s.descripcion, s.precio, s.archivo
FROM (VALUES
    ('Impresión FDM', 'Impresión FDM por pieza', 'Impresión de piezas medianas en PLA o PETG.', 8.50, TRUE),
    ('Impresión FDM', 'Prototipado rápido', 'Prototipos funcionales con entrega en 48 horas.', 25.00, TRUE),
    ('Impresión en resina', 'Miniaturas en resina', 'Figuras de alta definición hasta 10 cm.', 15.00, TRUE),
    ('Modelado 3D', 'Modelado de pieza simple', 'Diseño CAD de una pieza a partir de medidas.', 30.00, FALSE),
    ('Postprocesado', 'Pintura y acabado', 'Lijado, imprimación y pintura a mano.', 12.00, FALSE)
) AS s(categoria, nombre, descripcion, precio, archivo)
JOIN categorias_servicios c ON c.nombre = s.categoria
WHERE NOT EXISTS (SELECT 1 FROM servicios x WHERE x.nombre = s.nombre);

INSERT INTO productos (sku, nombre, descripcion, precio, stock, stock_minimo) VALUES
    ('PRD-LLAV-001', 'Llavero personalizado', 'Llavero en PLA con nombre o logo.', 3.50, 120, 20),
    ('PRD-MACE-002', 'Maceta geométrica', 'Maceta low-poly de 12 cm con drenaje.', 9.99, 40, 5),
    ('PRD-SOPO-003', 'Soporte para celular', 'Soporte ajustable compatible con cualquier teléfono.', 6.75, 60, 10),
    ('PRD-FIGU-004', 'Figura articulada dragón', 'Dragón articulado impreso en una sola pieza.', 18.00, 25, 5),
    ('PRD-ORGA-005', 'Organizador de escritorio', 'Organizador modular para lápices y accesorios.', 12.50, 30, 5)
ON CONFLICT (sku) DO NOTHING;

INSERT INTO metodos_pago (nombre, descripcion)
VALUES ('TARJETA', 'Tarjeta de crédito o débito')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO promociones (nombre, descripcion, tipo_descuento, valor_descuento, fecha_inicio, fecha_fin)
SELECT 'Bienvenida 3DForge', '10% en tu primera impresión', 'PORCENTAJE', 10, now() - INTERVAL '1 day', now() + INTERVAL '60 days'
WHERE NOT EXISTS (SELECT 1 FROM promociones WHERE nombre = 'Bienvenida 3DForge');

INSERT INTO configuracion_negocio (nombre_negocio, correo, telefono, direccion, sitio_web)
SELECT '3DForge Studio', 'contacto@3dforge.studio', '50370000000', 'Santa Ana, El Salvador', 'https://3dforge.studio'
WHERE NOT EXISTS (SELECT 1 FROM configuracion_negocio);
