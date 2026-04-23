import argparse
import time
from datetime import datetime

from banco.inserir_focos import inserir_focos_csv
from coleta.baixar_dados import DATA_FIM, DATA_INICIO, INTERVALO_PADRAO_SEGUNDOS, executar_coleta
from tratamento.limpar_dados import executar_limpeza


def executar_pipeline(inserir_no_banco=True):
    resultado_coleta = executar_coleta(data_inicio=DATA_INICIO, data_fim=DATA_FIM)
    resultado_limpeza = executar_limpeza(caminho_entrada=resultado_coleta["csv_path"])
    resultado_banco = None

    if inserir_no_banco:
        resultado_banco = inserir_focos_csv(caminho_csv=resultado_limpeza["output_path"])

    print("\nPipeline concluido com sucesso.")
    print(f"CSV bruto: {resultado_coleta['csv_path']}")
    print(f"CSV limpo: {resultado_limpeza['output_path']}")
    if resultado_banco is not None:
        print(f"Novos focos inseridos no banco: {resultado_banco['linhas_inseridas']}")

    return {
        "coleta": resultado_coleta,
        "limpeza": resultado_limpeza,
        "banco": resultado_banco,
    }


def executar_pipeline_periodico(intervalo_segundos, max_execucoes=None, inserir_no_banco=True):
    execucoes = 0

    while True:
        print(f"\n[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] Iniciando pipeline...")
        try:
            executar_pipeline(inserir_no_banco=inserir_no_banco)
        except Exception as exc:
            print(f"Falha no pipeline: {exc}")

        execucoes += 1
        if max_execucoes is not None and execucoes >= max_execucoes:
            print("Limite de execucoes atingido. Encerrando.")
            break

        print(f"Aguardando {intervalo_segundos} segundos para a proxima execucao.")
        time.sleep(intervalo_segundos)


def main():
    parser = argparse.ArgumentParser(
        description="Executa o pipeline de coleta e limpeza dos focos do INPE."
    )
    parser.add_argument(
        "--interval-seconds",
        type=int,
        default=INTERVALO_PADRAO_SEGUNDOS,
        help="Intervalo entre execucoes do pipeline em segundos.",
    )
    parser.add_argument(
        "--max-runs",
        type=int,
        default=None,
        help="Limita a quantidade de execucoes quando usar o modo continuo.",
    )
    parser.add_argument(
        "--once",
        action="store_true",
        help="Executa o pipeline apenas uma vez e encerra.",
    )
    parser.add_argument(
        "--skip-db",
        action="store_true",
        help="Executa a coleta e a limpeza sem inserir os focos no banco de dados.",
    )
    args = parser.parse_args()

    if args.once:
        executar_pipeline(inserir_no_banco=not args.skip_db)
        return

    executar_pipeline_periodico(
        intervalo_segundos=args.interval_seconds,
        max_execucoes=args.max_runs,
        inserir_no_banco=not args.skip_db,
    )


if __name__ == "__main__":
    main()
