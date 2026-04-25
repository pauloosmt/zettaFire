import argparse
import json
import time
from datetime import datetime, timezone
from pathlib import Path
from urllib.parse import urlencode
from xml.etree import ElementTree as ET

import pandas as pd
import requests


BASE_URL = "https://terrabrasilis.dpi.inpe.br/queimadas/geoserver/ows"
LAYER_NAME = "dados_abertos:focos_48h_br_todosats"
DATA_INICIO = None
DATA_FIM = None
PAGE_SIZE = 1000
SORT_BY = None
INTERVALO_PADRAO_SEGUNDOS = 1800

PASTA_SAIDA = Path(__file__).resolve().parent / "dados_inpe"
PASTA_SAIDA.mkdir(parents=True, exist_ok=True)

AUTO_SORT_BY = None


def criar_sessao():
    sessao = requests.Session()
    sessao.headers.update({"User-Agent": "zettaFire/1.0"})
    return sessao


def montar_cql_filter(data_inicio=None, data_fim=None):
    if not data_inicio or not data_fim:
        return None
    return f"data_hora BETWEEN '{data_inicio}' AND '{data_fim}'"


def extrair_mensagem_erro(resposta):
    try:
        raiz = ET.fromstring(resposta.text)
        namespaces = {"ows": "http://www.opengis.net/ows/1.1"}
        no = raiz.find(".//ows:ExceptionText", namespaces)
        if no is not None and no.text:
            return no.text.strip()
    except ET.ParseError:
        pass
    return resposta.text[:500]


def descrever_camada(sessao):
    params = {
        "service": "WFS",
        "version": "2.0.0",
        "request": "DescribeFeatureType",
        "typeNames": LAYER_NAME,
    }
    resposta = sessao.get(BASE_URL, params=params, timeout=120)
    resposta.raise_for_status()
    return resposta.text


def descobrir_campos_da_camada(sessao):
    xml_texto = descrever_camada(sessao)
    raiz = ET.fromstring(xml_texto)
    namespaces = {"xsd": "http://www.w3.org/2001/XMLSchema"}

    campos = []
    for no in raiz.findall(".//xsd:sequence/xsd:element", namespaces):
        nome = no.attrib.get("name")
        tipo = no.attrib.get("type", "")
        if nome:
            campos.append({"nome": nome, "tipo": tipo})

    return campos


def escolher_sort_by(sessao):
    global AUTO_SORT_BY

    if SORT_BY:
        return SORT_BY
    if AUTO_SORT_BY is not None:
        return AUTO_SORT_BY

    try:
        campos = descobrir_campos_da_camada(sessao)
    except Exception as exc:
        print(f"Aviso: nao foi possivel descobrir campos da camada automaticamente: {exc}")
        return None

    candidatos_preferidos = [
        "data_hora_gmt",
        "datahora_gmt",
        "data_hora",
        "data_pas",
        "data",
        "date",
        "datetime",
        "timestamp",
        "id",
        "gid",
        "objectid",
        "fid",
    ]

    nomes = {campo["nome"].lower(): campo["nome"] for campo in campos}
    for candidato in candidatos_preferidos:
        if candidato in nomes:
            AUTO_SORT_BY = nomes[candidato]
            return AUTO_SORT_BY

    for campo in campos:
        nome = campo["nome"]
        tipo = campo["tipo"].lower()
        if nome.lower() == "geom":
            continue
        if any(chave in tipo for chave in ("date", "time", "int", "long", "short", "decimal", "double")):
            AUTO_SORT_BY = nome
            return AUTO_SORT_BY

    for campo in campos:
        nome = campo["nome"]
        if nome.lower() != "geom":
            AUTO_SORT_BY = nome
            return AUTO_SORT_BY

    return None


def requisitar_pagina(sessao, start_index, cql_filter=None):
    sort_by = escolher_sort_by(sessao)
    params = {
        "service": "WFS",
        "version": "2.0.0",
        "request": "GetFeature",
        "typeNames": LAYER_NAME,
        "outputFormat": "application/json",
        "srsName": "EPSG:4326",
        "count": PAGE_SIZE,
    }

    if start_index:
        params["startIndex"] = start_index
    if sort_by:
        params["sortBy"] = sort_by
    if cql_filter:
        params["CQL_FILTER"] = cql_filter

    url = f"{BASE_URL}?{urlencode(params)}"
    resposta = sessao.get(url, timeout=120)

    if not resposta.ok:
        detalhe = extrair_mensagem_erro(resposta)
        raise requests.HTTPError(
            f"{resposta.status_code} Client Error para {resposta.url}\nDetalhe do servidor: {detalhe}",
            response=resposta,
        )

    return resposta.json()


def extrair_registros(geojson):
    registros = []

    for feature in geojson.get("features", []):
        propriedades = feature.get("properties", {}) or {}
        geometria = feature.get("geometry", {}) or {}

        longitude = None
        latitude = None

        if geometria.get("type") == "Point":
            coordenadas = geometria.get("coordinates", [])
            if len(coordenadas) >= 2:
                longitude, latitude = coordenadas[0], coordenadas[1]

        registros.append(
            {
                **propriedades,
                "longitude": longitude,
                "latitude": latitude,
                "geometry_wkt": (
                    f"POINT({longitude} {latitude})"
                    if longitude is not None and latitude is not None
                    else None
                ),
            }
        )

    return registros


def baixar_tudo(sessao, data_inicio=None, data_fim=None):
    cql_filter = montar_cql_filter(data_inicio, data_fim)
    todos_registros = []
    todas_features = []
    start_index = 0

    while True:
        geojson = requisitar_pagina(sessao, start_index=start_index, cql_filter=cql_filter)
        features = geojson.get("features", [])

        if not features:
            break

        todas_features.extend(features)
        todos_registros.extend(extrair_registros(geojson))
        print(f"Baixados {len(features)} registros nesta pagina. startIndex={start_index}")

        if len(features) < PAGE_SIZE:
            break

        start_index += PAGE_SIZE

    return {"type": "FeatureCollection", "features": todas_features}, pd.DataFrame(todos_registros)


def tratar_dataframe(df):
    if df.empty:
        return df.copy()

    df_tratado = df.copy()

    if "latitude" in df_tratado.columns:
        df_tratado["latitude"] = pd.to_numeric(df_tratado["latitude"], errors="coerce")
    if "longitude" in df_tratado.columns:
        df_tratado["longitude"] = pd.to_numeric(df_tratado["longitude"], errors="coerce")

    if {"latitude", "longitude"}.issubset(df_tratado.columns):
        df_tratado = df_tratado.dropna(subset=["latitude", "longitude"])
        df_tratado = df_tratado[df_tratado["latitude"].between(-90, 90)]
        df_tratado = df_tratado[df_tratado["longitude"].between(-180, 180)]

    for coluna in df_tratado.columns:
        nome = coluna.lower()
        if "data" in nome or "date" in nome or "hora" in nome or "time" in nome:
            try:
                df_tratado[coluna] = pd.to_datetime(df_tratado[coluna], utc=False)
            except Exception:
                pass

    return df_tratado.drop_duplicates()


def salvar_resultados(geojson, df):
    timestamp = datetime.now(timezone.utc).strftime("%Y%m%d_%H%M%S")
    caminho_geojson = PASTA_SAIDA / f"focos_raw_{timestamp}.geojson"
    caminho_csv = PASTA_SAIDA / f"focos_flat_{timestamp}.csv"

    with caminho_geojson.open("w", encoding="utf-8") as arquivo_geojson:
        json.dump(geojson, arquivo_geojson, ensure_ascii=False)

    df.to_csv(caminho_csv, index=False, encoding="utf-8")
    return caminho_geojson, caminho_csv


def resumir_dataframe(df):
    return {
        "linhas": len(df),
        "colunas": len(df.columns),
        "nomes_colunas": list(df.columns),
    }


def executar_coleta(data_inicio=None, data_fim=None, sessao=None):
    if "SUBSTITUA_AQUI" in LAYER_NAME:
        raise ValueError("Defina LAYER_NAME com o nome da camada retornada pelo INPE.")

    sessao_local = sessao or criar_sessao()
    geojson, df_bruto = baixar_tudo(sessao_local, data_inicio=data_inicio, data_fim=data_fim)
    df_tratado = tratar_dataframe(df_bruto)
    caminho_geojson, caminho_csv = salvar_resultados(geojson, df_tratado)
    resumo = resumir_dataframe(df_tratado)

    print("\nResumo da coleta:")
    print(f"Linhas apos tratamento: {resumo['linhas']}")
    print(f"Colunas disponiveis: {resumo['colunas']}")
    print(f"GeoJSON salvo em: {caminho_geojson}")
    print(f"CSV salvo em: {caminho_csv}")

    return {
        "geojson_path": caminho_geojson,
        "csv_path": caminho_csv,
        "dataframe": df_tratado,
        "resumo": resumo,
    }


def executar_coleta_periodica(intervalo_segundos=INTERVALO_PADRAO_SEGUNDOS, max_execucoes=None):
    total_execucoes = 0

    while True:
        inicio = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"\n[{inicio}] Iniciando ciclo de coleta...")

        try:
            executar_coleta(data_inicio=DATA_INICIO, data_fim=DATA_FIM)
        except Exception as exc:
            print(f"Falha durante a coleta: {exc}")

        total_execucoes += 1
        if max_execucoes is not None and total_execucoes >= max_execucoes:
            print("Limite de execucoes atingido. Encerrando timer.")
            break

        proxima_execucao = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(
            f"Ciclo finalizado. Aguardando {intervalo_segundos} segundos ate a proxima coleta "
            f"(base: {proxima_execucao})."
        )
        time.sleep(intervalo_segundos)


def main():
    parser = argparse.ArgumentParser(
        description="Baixa dados de focos do INPE e salva os arquivos brutos e tabulares."
    )
    parser.add_argument(
        "--interval-seconds",
        type=int,
        default=None,
        help="Se informado, executa a coleta continuamente nesse intervalo de segundos.",
    )
    parser.add_argument(
        "--max-runs",
        type=int,
        default=None,
        help="Limita a quantidade de execucoes quando usar o timer.",
    )
    args = parser.parse_args()

    if args.interval_seconds:
        executar_coleta_periodica(
            intervalo_segundos=args.interval_seconds,
            max_execucoes=args.max_runs,
        )
        return

    executar_coleta(data_inicio=DATA_INICIO, data_fim=DATA_FIM)


if __name__ == "__main__":
    main()
