import argparse
from pathlib import Path

import geopandas as gpd
import pandas as pd


COLUNAS_PARA_REMOVER = [
    "data_pas",
    "continente_id",
    "id_0",
    "pais",
    "id_1",
    "id_2",
]
COLUNA_ID_UNICO = "id_foco_bdq"
NOME_ARQUIVO_FINAL = "focos_limpo.csv"

PASTA_COLETA = Path(__file__).resolve().parents[1] / "coleta" / "dados_inpe"
PASTA_SAIDA = Path(__file__).resolve().parent / "dados_tratados"
PASTA_BUFFER = Path(__file__).resolve().parent
ARQUIVOS_BUFFER_PADRAO = [
    PASTA_BUFFER / "Lavras_buffer10km.geojson",
    PASTA_BUFFER / "Lavras_buffer_10km.shp",
]


def encontrar_csv_mais_recente():
    arquivos = sorted(PASTA_COLETA.glob("focos_flat_*.csv"), key=lambda caminho: caminho.stat().st_mtime)
    if not arquivos:
        raise FileNotFoundError(
            f"Nenhum arquivo focos_flat_*.csv foi encontrado em {PASTA_COLETA}"
        )
    return arquivos[-1]


def encontrar_buffer_padrao():
    for caminho in ARQUIVOS_BUFFER_PADRAO:
        if caminho.exists():
            return caminho
    raise FileNotFoundError(
        "Nenhum arquivo de buffer foi encontrado. Esperado um destes caminhos: "
        + ", ".join(str(caminho) for caminho in ARQUIVOS_BUFFER_PADRAO)
    )


def ajustar_crs_buffer(gdf_buffer):
    minx, miny, maxx, maxy = gdf_buffer.total_bounds
    parece_graus = (
        -180 <= minx <= 180
        and -180 <= maxx <= 180
        and -90 <= miny <= 90
        and -90 <= maxy <= 90
    )

    if parece_graus and gdf_buffer.crs != "EPSG:4326":
        return gdf_buffer.set_crs("EPSG:4326", allow_override=True)
    return gdf_buffer


def preparar_coordenadas(df):
    if not {"latitude", "longitude"}.issubset(df.columns):
        raise ValueError(
            "O CSV precisa conter as colunas 'latitude' e 'longitude' para aplicar o filtro espacial."
        )

    df_preparado = df.copy()
    df_preparado["latitude"] = pd.to_numeric(df_preparado["latitude"], errors="coerce")
    df_preparado["longitude"] = pd.to_numeric(df_preparado["longitude"], errors="coerce")
    df_preparado = df_preparado.dropna(subset=["latitude", "longitude"])
    df_preparado = df_preparado[df_preparado["latitude"].between(-90, 90)]
    df_preparado = df_preparado[df_preparado["longitude"].between(-180, 180)]
    return df_preparado


def filtrar_pontos_no_buffer(df, caminho_buffer):
    df_preparado = preparar_coordenadas(df)
    if df_preparado.empty:
        return df_preparado

    gdf_pontos = gpd.GeoDataFrame(
        df_preparado,
        geometry=gpd.points_from_xy(df_preparado["longitude"], df_preparado["latitude"]),
        crs="EPSG:4326",
    )

    gdf_buffer = gpd.read_file(caminho_buffer)
    if gdf_buffer.empty:
        raise ValueError(f"O buffer {caminho_buffer} nao contem geometrias.")
    if gdf_buffer.crs is None:
        raise ValueError(f"O buffer {caminho_buffer} nao possui CRS definido.")

    gdf_buffer = ajustar_crs_buffer(gdf_buffer).to_crs(gdf_pontos.crs)
    geometria_buffer = gdf_buffer.union_all()
    return gdf_pontos[gdf_pontos.within(geometria_buffer)].drop(columns="geometry")


def limpar_dataframe(df, caminho_buffer):
    df_filtrado = filtrar_pontos_no_buffer(df, caminho_buffer)
    colunas_existentes = [coluna for coluna in COLUNAS_PARA_REMOVER if coluna in df_filtrado.columns]
    df_limpo = df_filtrado.drop(columns=colunas_existentes)
    if COLUNA_ID_UNICO not in df_limpo.columns:
        raise ValueError(
            f"O CSV precisa conter a coluna '{COLUNA_ID_UNICO}' para controlar duplicidade."
        )
    return df_limpo.drop_duplicates(subset=[COLUNA_ID_UNICO])


def montar_caminho_saida(arquivo_entrada):
    PASTA_SAIDA.mkdir(exist_ok=True)
    return PASTA_SAIDA / NOME_ARQUIVO_FINAL


def carregar_csv_final(caminho_saida):
    if not caminho_saida.exists():
        return pd.DataFrame()
    return pd.read_csv(caminho_saida)


def acumular_registros_novos(df_novo, caminho_saida):
    df_existente = carregar_csv_final(caminho_saida)

    if df_existente.empty:
        df_final = df_novo.copy()
        linhas_novas = len(df_final)
        linhas_repetidas = 0
    else:
        if COLUNA_ID_UNICO not in df_existente.columns:
            raise ValueError(
                f"O CSV final precisa conter a coluna '{COLUNA_ID_UNICO}' para controlar duplicidade."
            )

        ids_existentes = set(df_existente[COLUNA_ID_UNICO].astype(str))
        df_novo_normalizado = df_novo.copy()
        df_novo_normalizado[COLUNA_ID_UNICO] = df_novo_normalizado[COLUNA_ID_UNICO].astype(str)
        mask_novos = ~df_novo_normalizado[COLUNA_ID_UNICO].isin(ids_existentes)
        df_apenas_novos = df_novo_normalizado.loc[mask_novos]

        linhas_novas = len(df_apenas_novos)
        linhas_repetidas = len(df_novo_normalizado) - linhas_novas
        df_final = pd.concat([df_existente, df_apenas_novos], ignore_index=True)
        df_final = df_final.drop_duplicates(subset=[COLUNA_ID_UNICO], keep="first")

    df_final.to_csv(caminho_saida, index=False, encoding="utf-8")
    return df_final, linhas_novas, linhas_repetidas


def executar_limpeza(caminho_entrada=None, caminho_saida=None):
    caminho_entrada = Path(caminho_entrada) if caminho_entrada else encontrar_csv_mais_recente()
    caminho_saida = Path(caminho_saida) if caminho_saida else montar_caminho_saida(caminho_entrada)
    caminho_buffer = encontrar_buffer_padrao()

    df = pd.read_csv(caminho_entrada)
    df_limpo = limpar_dataframe(df, caminho_buffer)
    df_final, linhas_novas, linhas_repetidas = acumular_registros_novos(df_limpo, caminho_saida)

    print("\nResumo da limpeza:")
    print(f"Arquivo de entrada: {caminho_entrada}")
    print(f"Buffer utilizado: {caminho_buffer}")
    print(f"Arquivo de saida: {caminho_saida}")
    print(f"Linhas no lote filtrado: {len(df_limpo)}")
    print(f"Linhas novas adicionadas: {linhas_novas}")
    print(f"Linhas repetidas ignoradas: {linhas_repetidas}")
    print(f"Total acumulado no CSV final: {len(df_final)}")
    print(f"Colunas antes: {len(df.columns)}")
    print(f"Colunas depois: {len(df_final.columns)}")
    print("Colunas removidas:", ", ".join(COLUNAS_PARA_REMOVER))

    return {
        "input_path": caminho_entrada,
        "output_path": caminho_saida,
        "buffer_path": caminho_buffer,
        "linhas_lote": len(df_limpo),
        "linhas_novas": linhas_novas,
        "linhas_repetidas": linhas_repetidas,
        "linhas_total": len(df_final),
        "colunas_antes": len(df.columns),
        "colunas_depois": len(df_final.columns),
    }


def main():
    parser = argparse.ArgumentParser(
        description="Filtra o CSV de focos pelo buffer do projeto e remove colunas redundantes."
    )
    parser.add_argument(
        "--input",
        type=Path,
        help="Caminho do CSV de entrada. Se omitido, usa o arquivo mais recente em src/coleta/dados_inpe.",
    )
    parser.add_argument(
        "--output",
        type=Path,
        help="Caminho do CSV de saida. Se omitido, salva em src/tratamento/dados_tratados.",
    )
    args = parser.parse_args()

    executar_limpeza(caminho_entrada=args.input, caminho_saida=args.output)


if __name__ == "__main__":
    main()
