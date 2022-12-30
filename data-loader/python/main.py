"""
    Main cli app for data loader
"""

__author__ = 'Sai Kris'

import typer

from commands.data_load import data_load_command
from commands.jobs import list_jobs_command, get_job_detail_command
from commands.data_load_errors import data_load_error_summary_command, data_load_error_detail_command

app = typer.Typer()


@app.command(short_help='List all the jobs')
def list_jobs():
    list_jobs_command()


@app.command(short_help='Get Errors Summary')
def errors_summary(job_id: str):
    data_load_error_summary_command(job_id)


@app.command(short_help='Get Errors')
def errors(job_id: str):
    data_load_error_detail_command(job_id)


@app.command(short_help='Get Job Detail')
def get_job_detail(job_id: str):
    get_job_detail_command(job_id)


@app.command(
    short_help='Start Data Load')
def data_load(
        tags: str,
        name: str,
        zip_file_path: str,
        mapping_file_path: str):
    data_load_command(tags, name, zip_file_path, mapping_file_path)


if __name__ == "__main__":
    app()
