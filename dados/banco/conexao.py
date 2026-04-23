import os

import psycopg2


def obter_configuracao_banco():
    return {
        "host": os.getenv("DB_HOST", "localhost"),
        "port": int(os.getenv("DB_PORT", "5432")),
        "dbname": os.getenv("DB_NAME", "zettaFire"),
        "user": os.getenv("DB_USER", "postZetta"),
        "password": os.getenv("DB_PASSWORD", "squadA"),
    }


def criar_conexao():
    return psycopg2.connect(**obter_configuracao_banco())
