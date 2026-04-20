from __future__ import annotations

import math
from pathlib import Path

import pandas as pd
from psycopg2.extras import execute_values

from banco.conexao import criar_conexao


COLUNAS_ESPERADAS = [
    "city",
    "fire_risk",
    "frp",
    "latitude",
    "longitude",
    "radius_of_risk",
    "start_data",
    "status",
    "geom",
    "id_foco_bdq",
]
RAIO_RISCO_PADRAO_METROS = 3000
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


def normalizar_data_hora(valor):
    valor = normalizar_valor(valor)
    if valor is None:
        return None
    timestamp = pd.to_datetime(valor, utc=True, errors="coerce")
    if pd.isna(timestamp):
        return None
    return timestamp.to_pydatetime()


def normalizar_geometry_wkt(valor, longitude, latitude):
    valor = normalizar_valor(valor)
    if valor is not None and str(valor).strip():
        geometry_wkt = str(valor).strip()
        if geometry_wkt.upper().startswith("SRID="):
            return geometry_wkt
        return f"SRID=4326;{geometry_wkt}"

    if longitude is None or latitude is None:
        return None
    return f"SRID=4326;POINT({float(longitude)} {float(latitude)})"


def carregar_dataframe(caminho_csv):
    caminho_csv = Path(caminho_csv)
    return pd.read_csv(caminho_csv)


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
        data_hora = normalizar_data_hora(registro["start_data"])
        geometry_wkt = normalizar_geometry_wkt(registro.get("geom"), longitude, latitude)
        radius_of_risk = normalizar_valor(registro["radius_of_risk"])
        status = str(normalizar_valor(registro["status"]) or STATUS_PADRAO)

        if (
            id_foco_bdq is None
            or latitude is None
            or longitude is None
            or data_hora is None
            or radius_of_risk is None
            or geometry_wkt is None
        ):
            continue

        registros.append(
            (
                str(normalizar_valor(registro["city"]) or "MUNICIPIO NAO INFORMADO"),
                normalizar_valor(registro["fire_risk"]),
                normalizar_valor(registro["frp"]),
                float(latitude),
                float(longitude),
                int(radius_of_risk),
                data_hora,
                status,
                geometry_wkt,
                int(id_foco_bdq),
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
                city,
                fire_risk,
                frp,
                latitude,
                longitude,
                radius_of_risk,
                start_time,
                status_fire,
                geom,
                id_foco_bdq
            )
            VALUES %s
            ON CONFLICT (id_foco_bdq) DO NOTHING;
            """,
            registros,
            template="""
            (
                %s, %s, %s, %s, %s, %s, %s, %s,
                ST_GeogFromText(%s), %s
            )
            """,
        )
        linhas_inseridas = cursor.rowcount

    conexao.commit()
    return linhas_inseridas


def inserir_focos_dataframe(df, radius_of_risk=RAIO_RISCO_PADRAO_METROS):
    validar_colunas(df)

    with criar_conexao() as conexao:
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
