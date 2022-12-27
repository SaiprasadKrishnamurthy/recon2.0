import json
from datetime import datetime

import requests
from rich.console import Console
from rich.table import Table

import config
from util import credentials

console = Console()


def list_jobs_command():
    token = credentials.get_credentials()
    if len(token) > 0:
        apiurl = f'{config.api_base_url}/jobs'
        split = str(token).split(",")
        jobs = requests.get(apiurl, headers={'X-CLIENT-ID': split[0], 'X-API-KEY': split[1]})
        if jobs.status_code == 200:
            jobs_json = jobs.json()
            table = Table(show_header=True, header_style='bold blue')
            table.add_column("JobId", justify="left")
            table.add_column("Started", justify="left")
            table.add_column("state", justify="left")
            sliced = jobs_json[0:10]
            for job in sliced:
                table.add_row(job["jobId"], str(format_date_time(job["startedMillis"])), job["state"])
            console.print(table)
        else:
            console.print(
                f"[bold red]Error: {jobs.json()} [/bold "
                f"red]")


def format_date_time(epoch):
    try:
        return datetime.fromtimestamp(epoch / 1000)
    except:
        return '-'


def get_job_detail_command(job_id: str):
    token = credentials.get_credentials()
    if len(token) > 0:
        apiurl = f'{config.api_base_url}/job/{job_id}'
        split = str(token).split(",")
        jobs = requests.get(apiurl, headers={'X-CLIENT-ID': split[0], 'X-API-KEY': split[1]})
        if jobs.status_code == 200:
            jobs_json = jobs.json()
            console.print(
                f"[bold green]{json.dumps(jobs_json, indent=4)} [/bold "
                f"green]")
        else:
            console.print(
                f"[bold red]{jobs.json()} [/bold "
                f"red]")
