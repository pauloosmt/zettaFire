from __future__ import annotations

import math
import uuid
from pathlib import Path

import pandas as pd
from psycopg2.extras import execute_values

from banco.conexao import criar_conexao


COLUNAS_ESPERADAS = [
    "id_foco_bdq",
    "foco_id",
    "longitude",
    "latitude",
    "data_hora_gmt",
    "municipio",
    "risco_fogo",
    "frp",
]
RAIO_RISCO_PADRAO_METROS = 10000
STATUS_PADRAO = "ACTIVE"


def validar_colunas(df):
    colunas_ausentes = [coluna for coluna in COLUNAS_ESPERADAS if coluna not in df.columns]
    if colunas_ausentes:
        raise ValueError(
            "O dataframe precisa conter as colunas obrigatorias para insercao no banco: "
            + ", ".join(colunas_ausentes)
        )


def normalizar_valor(valor):
    if pd.isna(valor):
        return None
    if isinstance(valor, float) and math.isnan(valor):
        return None
    return valor


def normalizar_foco_id(valor):
    valor = normalizar_valor(valor)
    if valor is None:
        return None
    return str(valor)


def normalizar_data_hora(valor):
    valor = normalizar_valor(valor)
    if valor is None:
        return None
    timestamp = pd.to_datetime(valor, utc=True, errors="coerce")
    if pd.isna(timestamp):
        return None
    return timestamp.to_pydatetime()


def carregar_dataframe(caminho_csv):
    caminho_csv = Path(caminho_csv)
    return pd.read_csv(caminho_csv)


def garantir_tabela_fire_event(conexao):
    with conexao.cursor() as cursor:
        cursor.execute("CREATE EXTENSION IF NOT EXISTS postgis;")
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS fire_event (
                id_fire_event UUID PRIMARY KEY,
                id_foco_bdq BIGINT NOT NULL UNIQUE,
                foco_id UUID NULL,
                latitude DOUBLE PRECISION NOT NULL,
                longitude DOUBLE PRECISION NOT NULL,
                city VARCHAR(255) NOT NULL,
                radius_of_risk BIGINT NOT NULL,
                start_time TIMESTAMPTZ NOT NULL,
                fire_risk DOUBLE PRECISION NULL,
                frp DOUBLE PRECISION NULL,
                status_fire VARCHAR(32) NOT NULL,
                geom geography(Point, 4326) NOT NULL
            );
            """
        )
    conexao.commit()


def buscar_ids_existentes(conexao, ids_focos):
    if not ids_focos:
        return set()

    with conexao.cursor() as cursor:
        cursor.execute(
            """
            SELECT id_foco_bdq
            FROM fire_event
            WHERE id_foco_bdq = ANY(%s);
            """,
            (ids_focos,),
        )
        return {linha[0] for linha in cursor.fetchall()}


def montar_registros_para_insercao(df, radius_of_risk):
    registros = []
    for registro in df.to_dict(orient="records"):
        id_foco_bdq = normalizar_valor(registro["id_foco_bdq"])
        latitude = normalizar_valor(registro["latitude"])
        longitude = normalizar_valor(registro["longitude"])
        data_hora = normalizar_data_hora(registro["data_hora_gmt"])

        if id_foco_bdq is None or latitude is None or longitude is None or data_hora is None:
            continue

        registros.append(
            (
                str(uuid.uuid4()),
                int(id_foco_bdq),
                normalizar_foco_id(registro["foco_id"]),
                float(latitude),
                float(longitude),
                str(normalizar_valor(registro["municipio"]) or "MUNICIPIO NAO INFORMADO"),
                int(radius_of_risk),
                data_hora,
                normalizar_valor(registro["risco_fogo"]),
                normalizar_valor(registro["frp"]),
                STATUS_PADRAO,
                float(longitude),
                float(latitude),
            )
        )
    return registros


def inserir_registros(conexao, registros):
    if not registros:
        return 0

    with conexao.cursor() as cursor:
        execute_values(
            cursor,
            """
            INSERT INTO fire_event (
                id_fire_event,
                id_foco_bdq,
                foco_id,
                latitude,
                longitude,
                city,
                radius_of_risk,
                start_time,
                fire_risk,
                frp,
                status_fire,
                geom
            )
            VALUES %s
            ON CONFLICT (id_foco_bdq) DO NOTHING;
            """,
            [
                (
                    id_fire_event,
                    id_foco_bdq,
                    foco_id,
                    latitude,
                    longitude,
                    city,
                    radius_of_risk,
                    start_time,
                    fire_risk,
                    frp,
                    status_fire,
                    f"SRID=4326;POINT({longitude} {latitude})",
                )
                for (
                    id_fire_event,
                    id_foco_bdq,
                    foco_id,
                    latitude,
                    longitude,
                    city,
                    radius_of_risk,
                    start_time,
                    fire_risk,
                    frp,
                    status_fire,
                    _longitude_geom,
                    _latitude_geom,
                ) in registros
            ],
            template="""
            (
                %s, %s, %s::uuid, %s, %s, %s, %s, %s, %s, %s, %s,
                ST_GeogFromText(%s)
            )
            """,
        )
        linhas_inseridas = cursor.rowcount

    conexao.commit()
    return linhas_inseridas


def inserir_focos_dataframe(df, radius_of_risk=RAIO_RISCO_PADRAO_METROS):
    validar_colunas(df)

    with criar_conexao() as conexao:
        garantir_tabela_fire_event(conexao)

        ids_focos = [
            int(id_foco)
            for id_foco in df["id_foco_bdq"].dropna().tolist()
        ]
        ids_existentes = buscar_ids_existentes(conexao, ids_focos)

        df_novos = df[~df["id_foco_bdq"].isin(ids_existentes)].copy()
        registros = montar_registros_para_insercao(df_novos, radius_of_risk)
        linhas_inseridas = inserir_registros(conexao, registros)

    return {
        "linhas_recebidas": len(df),
        "linhas_novas": len(df_novos),
        "linhas_inseridas": linhas_inseridas,
        "linhas_ignoradas": len(df) - len(df_novos),
    }


def inserir_focos_csv(caminho_csv, radius_of_risk=RAIO_RISCO_PADRAO_METROS):
    df = carregar_dataframe(caminho_csv)
    return inserir_focos_dataframe(df, radius_of_risk=radius_of_risk)
