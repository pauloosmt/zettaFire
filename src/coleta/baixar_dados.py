import math
import requests
import pandas as pd
from pathlib import Path
from datetime import datetime, timezone
from urllib.parse import urlencode

BASE_URL = "https://terrabrasilis.dpi.inpe.br/queimadas/geoserver/ows"

# Troque aqui depois de rodar o script de listagem
LAYER_NAME = "dados_abertos:focos_48h_br_todosats"
# Janela opcional. Se a camada suportar filtro por data, você pode usar depois.
# Exemplo de formato: "2026-03-30"
DATA_INICIO = None
DATA_FIM = None

# Paginação
PAGE_SIZE = 1000
SORT_BY = "gid"   # pode precisar trocar se a camada não tiver esse campo

PASTA_SAIDA = Path("dados_inpe")
PASTA_SAIDA.mkdir(exist_ok=True)


def montar_cql_filter(data_inicio=None, data_fim=None):
    """
    Monte aqui o filtro temporal se a camada tiver um campo de data compatível.
    Como o nome do atributo de data pode variar por camada, deixei neutro.
    Depois que você baixar um lote inicial, ajustamos isso com segurança.
    """
    if not data_inicio or not data_fim:
        return None

    # Exemplo genérico. Talvez você precise trocar 'data_hora' pelo nome real do campo.
    return f"data_hora BETWEEN '{data_inicio}' AND '{data_fim}'"


def requisitar_pagina(start_index, cql_filter=None):
    params = {
        "service": "WFS",
        "version": "2.0.0",
        "request": "GetFeature",
        "typeName": LAYER_NAME,
        "outputFormat": "application/json",
        "srsName": "EPSG:4326",
        "count": PAGE_SIZE,
        "startIndex": start_index,
        "sortBy": SORT_BY,
    }

    if cql_filter:
        params["CQL_FILTER"] = cql_filter

    url = f"{BASE_URL}?{urlencode(params)}"
    resp = requests.get(url, timeout=120)
    resp.raise_for_status()
    return resp.json()


def extrair_registros(geojson):
    registros = []

    for feature in geojson.get("features", []):
        props = feature.get("properties", {}) or {}
        geom = feature.get("geometry", {}) or {}

        lon = None
        lat = None

        if geom.get("type") == "Point":
            coords = geom.get("coordinates", [])
            if len(coords) >= 2:
                lon, lat = coords[0], coords[1]

        registro = {
            **props,
            "longitude": lon,
            "latitude": lat,
        }
        registros.append(registro)

    return registros


def baixar_tudo():
    cql_filter = montar_cql_filter(DATA_INICIO, DATA_FIM)

    todos_registros = []
    todas_features = []
    start_index = 0

    while True:
        geojson = requisitar_pagina(start_index=start_index, cql_filter=cql_filter)
        features = geojson.get("features", [])

        if not features:
            break

        todas_features.extend(features)
        todos_registros.extend(extrair_registros(geojson))

        print(f"Baixados {len(features)} registros nesta página. startIndex={start_index}")

        if len(features) < PAGE_SIZE:
            break

        start_index += PAGE_SIZE

    return {
        "type": "FeatureCollection",
        "features": todas_features
    }, pd.DataFrame(todos_registros)


def tratar_dataframe(df):
    if df.empty:
        return df

    # Converte coordenadas
    if "latitude" in df.columns:
        df["latitude"] = pd.to_numeric(df["latitude"], errors="coerce")
    if "longitude" in df.columns:
        df["longitude"] = pd.to_numeric(df["longitude"], errors="coerce")

    # Remove coordenadas inválidas
    if {"latitude", "longitude"}.issubset(df.columns):
        df = df.dropna(subset=["latitude", "longitude"])
        df = df[df["latitude"].between(-90, 90)]
        df = df[df["longitude"].between(-180, 180)]

    # Tenta converter colunas que parecem ser data
    for col in df.columns:
        nome = col.lower()
        if "data" in nome or "date" in nome or "hora" in nome or "time" in nome:
            try:
                df[col] = pd.to_datetime(df[col], errors="ignore", utc=False)
            except Exception:
                pass

    # Remove duplicados exatos
    df = df.drop_duplicates()

    return df


def salvar_resultados(geojson, df):
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S")

    caminho_geojson = PASTA_SAIDA / f"focos_raw_{timestamp}.geojson"
    caminho_csv = PASTA_SAIDA / f"focos_flat_{timestamp}.csv"

    with open(caminho_geojson, "w", encoding="utf-8") as f:
        import json
        json.dump(geojson, f, ensure_ascii=False)

    df.to_csv(caminho_csv, index=False, encoding="utf-8")

    print(f"\nArquivos salvos:")
    print(f"- GeoJSON bruto: {caminho_geojson}")
    print(f"- CSV para análise: {caminho_csv}")


def main():
    if "SUBSTITUA_AQUI" in LAYER_NAME:
        raise ValueError(
            "Defina LAYER_NAME com o nome da camada retornada pelo script listar_camadas_inpe.py"
        )

    geojson, df = baixar_tudo()
    df = tratar_dataframe(df)

    print("\nResumo:")
    print(f"Total de linhas após tratamento: {len(df)}")
    print(f"Total de colunas: {len(df.columns)}")
    print("\nColunas:")
    print(list(df.columns))
    print("\nAmostra:")
    print(df.head(10))

    salvar_resultados(geojson, df)


if __name__ == "__main__":
    main()