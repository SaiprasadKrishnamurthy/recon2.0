import requests
from rich.console import Console

import config
from util import common_login
from util import credentials

console = Console()


def data_load_command(tags, name, zip_file_path, mapping_file_path):
    token = credentials.get_credentials()
    if len(token) > 0:
        call_api(token, tags, name, zip_file_path, mapping_file_path)


def call_api(token, tags, name, zip_file_path, mapping_file_path):
    apiurl = f'{config.api_base_url}/data-load/{name}?tags={tags}'
    files = {
        'file': open(f'{zip_file_path}', 'rb'),
        'dataDefinition': open(f'{mapping_file_path}', 'rb'),
    }
    split = str(token).split(",")
    jobs = requests.post(apiurl, files=files, headers={'X-CLIENT-ID': split[0], 'X-API-KEY': split[1]})
    if jobs.status_code == 200:
        json = jobs.json()
        console.print(f"[bold green]Job submitted to ingest {json['name']}: {json['jobId']}[/bold green]")
    elif jobs.status_code == 403:
        console.print(f"[bold red]Invalid Credentials[/bold red]")
        common_login.common_login()
