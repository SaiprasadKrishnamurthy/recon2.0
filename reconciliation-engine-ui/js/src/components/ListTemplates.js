import React, { Component } from 'react';
import "./Documentation.scss";
import { DataService } from "../service/DataService";
import { Button } from 'primereact/button';
import axiosClient from '../constants'
import { Messages } from 'primereact/messages';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Toolbar } from 'primereact/toolbar';



export class ListTemplates extends Component {

    constructor() {
        super();
        this.dataService = new DataService();
        this.state = {
            templates: []
        };
        this.list = this.list.bind(this);
        this.actionBodyTemplate = this.actionBodyTemplate.bind(this);
    }

    componentDidMount() {
        this.list();
    }

    list() {
        axiosClient.get('/templates')
            .then(res => {
                this.setState({ templates: res.data });
            }).catch(err => {
                this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Unable to get the templates due to a system error.', sticky: false });
            });
    }


    disable(templateName) {
        axiosClient.put('/template/' + templateName + "/_disable")
            .then(res => {
                this.list();
                this.msgs.show({ severity: 'success', summary: 'Template Disabled', detail: 'successfully', sticky: false });
            }).catch(err => {
                this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Unable to disable the template due to a system error.', sticky: false });
            });
    }

    enable(templateName) {
        axiosClient.put('/template/' + templateName + "/_enable")
            .then(res => {
                this.list();
                this.msgs.show({ severity: 'success', summary: 'Template Enabled', detail: 'successfully', sticky: false });
            }).catch(err => {
                this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Unable to disable the template due to a system error.', sticky: false });
            });
    }

    generate(templateName) {
        axiosClient.put('/template/' + templateName + "/_generate")
            .then(res => {
                this.list();
                this.msgs.show({ severity: 'success', summary: 'Generated data for ' + templateName, detail: 'successfully', sticky: false });
            }).catch(err => {
                this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Unable to generate data for ' + templateName + ' due to a system error.', sticky: false });
            });
    }

    delete(templateName) {
        axiosClient.delete('/template/' + templateName + "/_delete")
            .then(res => {
                this.list();
                this.msgs.show({ severity: 'success', summary: 'Deleted  template ' + templateName, detail: 'successfully', sticky: false });
            }).catch(err => {
                this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Unable to delete template ' + templateName + ' due to a system error.', sticky: false });
            });
    }


    actionBodyTemplate(rowData) {

        return (
            <React.Fragment>
                <Button icon="pi pi-pencil" className="p-button-rounded p-button-information p-mr-2" onClick={() => {
                    this.props.history.push('/update-template?name=' + rowData.name);
                }} tooltip={"Edit template"} />
                &nbsp;
                <Button icon="pi pi-ban" className="p-button-rounded p-button-danger p-mr-2"
                    onClick={() => {
                        this.disable(rowData.name);
                    }}
                    tooltip={"Disable template"}
                />
                &nbsp;
                <Button icon="pi pi-check" className="p-button-rounded p-button-success p-mr-2"
                    onClick={() => {
                        this.enable(rowData.name);
                    }}
                    tooltip={"Enable template"}
                />
                &nbsp;
                <Button icon="pi pi-chevron-circle-right" className="p-button-rounded p-button-success p-mr-2"
                    onClick={() => {
                        this.generate(rowData.name);
                    }}
                    tooltip={"Generate data instantly"}
                />

                &nbsp;
                <Button icon="pi pi-trash" className="p-button-rounded p-button-danger p-mr-2"
                    onClick={() => {
                        this.delete(rowData.name);
                    }}
                    tooltip={"Delete template instantly"}
                />
            </React.Fragment>
        )
    }

    render() {
        return (
            <div className="card">
                <h1>All Templates</h1>
                <br />
                <Toolbar>
                    <div className="p-toolbar-group-right">
                        <Button icon="pi pi-refresh" className="p-button-rounded p-button-success p-mr-2"
                            onClick={this.list} alt={'Refresh results'}
                            tooltip={'Refresh Results'}
                        />
                    </div>
                </Toolbar>
                <Messages ref={(el) => this.msgs = el} />
                <DataTable value={this.state.templates} paginator responsiveLayout="scroll"
                    paginatorTemplate="CurrentPageReport FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
                    rows={100} rowsPerPageOptions={[100, 150, 100]}
                    filterDisplay="menu"
                    header={"Available templates in the system"}
                    stripedRows>
                    <Column sortable={true} filter={true} filterPlaceholder="Search" field="name" header="Name"></Column>
                    <Column sortable={true} filter={true} filterPlaceholder="Search" field="description" header="Description"></Column>
                    <Column sortable={true} filter={true} filterPlaceholder="Search" field="cronLabel" header="Scheduled Cron"></Column>
                    <Column sortable={true} filter={true} filterPlaceholder="Search" field="outputTopic" header="Output Topic"></Column>
                    <Column sortable={true} filter={true} filterPlaceholder="Search" field="disabledDescription" header="Disabled"></Column>
                    <Column body={this.actionBodyTemplate} header="Actions" />
                </DataTable>
            </div>
        );
    }
}