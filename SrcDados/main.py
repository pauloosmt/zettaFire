import argparse
import time
from datetime import datetime

from coleta.baixar_dados import DATA_FIM, DATA_INICIO, INTERVALO_PADRAO_SEGUNDOS, executar_coleta
from tratamento.limpar_dados import executar_limpeza


def executar_pipeline():
    resultado_coleta = executar_coleta(data_inicio=DATA_INICIO, data_fim=DATA_FIM)
    resultado_limpeza = executar_limpeza(caminho_entrada=resultado_coleta["csv_path"])

    print("\nPipeline concluido com sucesso.")
    print(f"CSV bruto: {resultado_coleta['csv_path']}")
    print(f"CSV limpo: {resultado_limpeza['output_path']}")

    return {
        "coleta": resultado_coleta,
        "limpeza": resultado_limpeza,
    }


def executar_pipeline_periodico(intervalo_segundos, max_execucoes=None):
    execucoes = 0

    while True:
        print(f"\n[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] Iniciando pipeline...")
        try:
            executar_pipeline()
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
    args = parser.parse_args()

    if args.once:
        executar_pipeline()
        return

    executar_pipeline_periodico(
        intervalo_segundos=args.interval_seconds,
        max_execucoes=args.max_runs,
    )


if __name__ == "__main__":
    main()
