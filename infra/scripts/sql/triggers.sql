CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- 1. CRIANDO OS ESQUELETOS DAS TABELAS
-- Para o banco existir antes do Spring Boot adicionar o restante das colunas
CREATE TABLE IF NOT EXISTS fire_event (id_fire_event UUID PRIMARY KEY DEFAULT uuid_generate_v4());
CREATE TABLE IF NOT EXISTS address (id_address UUID PRIMARY KEY DEFAULT uuid_generate_v4());
CREATE TABLE IF NOT EXISTS users (id_user UUID PRIMARY KEY DEFAULT uuid_generate_v4());
CREATE TABLE IF NOT EXISTS alert (id_alert UUID PRIMARY KEY);
CREATE TABLE IF NOT EXISTS user_alert (id_alert UUID, id_user UUID);

-- 2. GARANTIR COLUNAS DE GEOMETRIA
ALTER TABLE fire_event ADD COLUMN IF NOT EXISTS geom geometry(Point, 4326);
ALTER TABLE address ADD COLUMN IF NOT EXISTS geom geometry(Point, 4326);

-- 3. TRIGGERS DE PREENCHIMENTO DE GEOMETRIA
CREATE OR REPLACE FUNCTION preencher_geom_address()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.latitude IS NOT NULL AND NEW.longitude IS NOT NULL THEN
        NEW.geom := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_preencher_geom_address
BEFORE INSERT OR UPDATE ON address
FOR EACH ROW EXECUTE FUNCTION preencher_geom_address();

CREATE OR REPLACE FUNCTION preencher_geom_incendio()
RETURNS TRIGGER AS $$
BEGIN
    NEW.geom := ST_SetSRID(ST_MakePoint(NEW.longitude, NEW.latitude), 4326);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_preencher_geom_fire
BEFORE INSERT ON fire_event
FOR EACH ROW EXECUTE FUNCTION preencher_geom_incendio();

-- 4. TRIGGER DE ALERTA
CREATE OR REPLACE FUNCTION monitorar_e_alertar_usuarios()
RETURNS TRIGGER AS $$
DECLARE
    novo_alerta_id UUID := gen_random_uuid();
    contagem_usuarios INTEGER;
BEGIN
    -- 1. Conta quantos usuários únicos estão na área de risco
    SELECT COUNT(DISTINCT u.id_user) INTO contagem_usuarios
    FROM users u
    JOIN address a ON u.id_address = a.id_address
    WHERE ST_DWithin(a.geom::geography, NEW.geom::geography, NEW.radius_of_risk)
       OR a.city = NEW.city; -- Failsafe: se a geom falhar, alerta pela cidade

    -- 2. Se houver público-alvo, gera o alerta principal
    IF contagem_usuarios > 0 THEN
        INSERT INTO alert (id_alert, date, status, id_fire_event)
        VALUES (novo_alerta_id, CURRENT_DATE, 'PENDING', NEW.id_fire_event);

        -- 3. Vincula todos os usuários afetados (Linkagem múltipla)
        INSERT INTO user_alert (id_alert, id_user)
        SELECT DISTINCT novo_alerta_id, u.id_user
        FROM users u
        JOIN address a ON u.id_address = a.id_address
        WHERE ST_DWithin(a.geom::geography, NEW.geom::geography, NEW.radius_of_risk)
           OR a.city = NEW.city;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_novo_incendio_alerta
AFTER INSERT ON fire_event
FOR EACH ROW EXECUTE FUNCTION monitorar_e_alertar_usuarios();