-- Demo fleet for local runs. Tests create their own data and do not rely on these rows.
INSERT INTO bike (code, type, state, battery_level, hub_id)
SELECT b.code, b.type, b.state, b.battery_level, h.id
FROM (VALUES
    ('PP-E001', 'E_BIKE',       'AVAILABLE',   92, 'Residências'),
    ('PP-E002', 'E_BIKE',       'AVAILABLE',   64, 'Residências'),
    ('PP-E003', 'E_BIKE',       'CHARGING',    12, 'Residências'),
    ('PP-C001', 'CARGO_E_BIKE', 'AVAILABLE',   80, 'Residências'),
    ('PP-E004', 'E_BIKE',       'AVAILABLE',   45, 'Biblioteca'),
    ('PP-E005', 'E_BIKE',       'MAINTENANCE', 70, 'Biblioteca'),
    ('PP-E006', 'E_BIKE',       'AVAILABLE',   88, 'Departamento de Informática'),
    ('PP-C002', 'CARGO_E_BIKE', 'AVAILABLE',   55, 'Departamento de Informática')
) AS b (code, type, state, battery_level, hub_name)
JOIN hub h ON h.name = b.hub_name;
