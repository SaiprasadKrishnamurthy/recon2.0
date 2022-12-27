import json

from rich.console import Console

import config

console = Console()

url = f'{config.api_base_url}/login'


def login_command():
    jsonStr = '{ "clientId":"6c3063e1-60bd-4b08-934f-b66d47685544", "apiKey":"d877770c-41f8-46db-bcef-78d06c5ec0e1"}'
    x = json.loads(jsonStr)
    token_file = open(".token.txt", "w")
    token_file.write(x['clientId'] + "," + x['apiKey'])
    token_file.close()
    console.print(f"[bold green]Logged in succesfully![/bold green]")
