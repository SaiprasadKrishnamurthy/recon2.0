import pandas as pd
import requests
from rich.console import Console
from rich.table import Table
from tabulate import tabulate

import config
from util import common_login
from util import credentials

console = Console()


def data_load_error_summary_command(job_id):
    token = credentials.get_credentials()
    if len(token) > 0:
        call_summary_api(token, job_id)


def data_load_error_detail_command(job_id):
    token = credentials.get_credentials()
    if len(token) > 0:
        call_detail_api(token, job_id)


def call_summary_api(token, job_id):
    apiurl = f'{config.api_base_url}/error-summary/{job_id}'
    split = str(token).split(",")
    jobs = requests.get(apiurl, headers={'X-CLIENT-ID': split[0], 'X-API-KEY': split[1]})

    if jobs.status_code == 200:
        json = jobs.json()
        table = Table(show_header=True, header_style='bold blue')
        table.add_column("JobId", justify="left")
        table.add_column("Data Name", justify="left")
        table.add_column("Total Errors", justify="left")
        table.add_row(json["jobId"], str(json["name"]), str(json["errorsCount"]))
        console.print(table)
    elif jobs.status_code == 403:
        console.print(f"[bold red]Invalid Credentials[/bold red]")
        common_login.common_login()
    elif jobs.status_code == 404:
        console.print(f"[bold red]{jobs.json()}[/bold red]")
        common_login.common_login()


def call_detail_api(token, job_id):
    apiurl = f'{config.api_base_url}/error-detail/{job_id}'
    split = str(token).split(",")
    jobs = requests.get(apiurl, headers={'X-CLIENT-ID': split[0], 'X-API-KEY': split[1]})

    if jobs.status_code == 200:
        json = jobs.json()
        df = pd.json_normalize(json)
        df.to_csv(f'errors_{job_id}.csv', encoding='utf-8', index=False)
        print(tabulate(df, headers='keys', tablefmt='psql'))
        print(f" \n\n File errors_{job_id} has been generated. \n\n")
    elif jobs.status_code == 403:
        console.print(f"[bold red]Invalid Credentials[/bold red]")
        common_login.common_login()
    elif jobs.status_code == 404:
        console.print(f"[bold red]{jobs.json()}[/bold red]")
        common_login.common_login()
