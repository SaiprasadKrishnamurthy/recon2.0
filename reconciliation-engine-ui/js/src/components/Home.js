import React, { Component } from 'react';
import "./Documentation.scss";
import axiosClient from '../constants'
import { Messages } from 'primereact/messages';
import Terminal from 'react-console-emulator';
import { Client } from '@stomp/stompjs';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import ReactJson from 'react-json-view'



export class Home extends Component {

    constructor() {
        let uniqueId = Date.now().toString(36) + Math.random().toString(36).substring(2);

        super();
        this.showProgress = this.showProgress.bind(this);
        this.showResult = this.showResult.bind(this);
        this.dataTemplate = this.dataTemplate.bind(this);
        this.lineItemTemplate = this.lineItemTemplate.bind(this);
        this.state = {
            job: { jobId: uniqueId },
            progress: { expectedCount: '', processedCount: '' },
            matches: [],
            settings: null
        };
    }

    webSocketBaseUrl() {
        var l = window.location;
        return ((l.protocol === "https:") ? "wss://" : "ws://") + l.hostname + (((l.port !== 80) && (l.port !== 443)) ? ":" + l.port : "") + "/reconciliation-engine-ui";
    }

    showProgress(msg) {
        let json = JSON.parse(msg.body);
        this.setState({ progress: json });
    }

    showResult(msg) {
        let json = JSON.parse(msg.body);
        this.setState({ matches: [...this.state.matches, json] });
    }

    lineItemTemplate(rowData) {
        return <ul>{rowData.rows.map(r => <li>{r}</li>)}</ul>;
    }

    dataTemplate(rowData) {
        return <DataTable value={rowData.records}>
            <Column field="datasource" header="Datasource name" sortable />
            <Column header="Line items" field={this.lineItemTemplate} />
        </DataTable>;
    }

    componentDidMount() {
        this.client = new Client();
        let wsBrokerBaseUrl = process.env.REACT_APP_WS_BASE_URL
        if (process.env.REACT_APP_ENV === 'prod') {
            wsBrokerBaseUrl = this.webSocketBaseUrl()
        }
        this.client.configure({
            brokerURL: wsBrokerBaseUrl + "/stomp",
            onConnect: () => {
                console.log('onConnect');
                this.client.subscribe('/topic/progress/' + this.state.job.jobId, message => {
                    this.showProgress(message);
                });

                this.client.subscribe('/topic/result/' + this.state.job.jobId, message => {
                    this.showResult(message);
                });
            },
        });
        this.client.activate();
    }

    render() {
        let progress = <></>;
        if (this.state.progress.processedCount !== '') {
            progress = <div className="p-col-12">
                <b><span className="p-float-label">
                    Session {this.state.job.jobId} processed {this.state.progress.processedCount} of {this.state.progress.expectedCount}
                </span></b>
            </div>;
        }
        let results = <></>;
        let settings = <></>;
        if (this.state.matches.length > 0) {
            results = <DataTable value={this.state.matches}>
                <Column style={{ width: '10%' }} field="rulesetType" header="RuleSet" sortable filter filterMatchMode='contains' />
                <Column style={{ width: '5%' }} field="bucketValue" header="Bucket" sortable filter filterMatchMode='contains' />
                <Column field="tagsDisplay" header="Tags" sortable filter filterMatchMode='contains' />
                <Column header="Data" body={this.dataTemplate} />
            </DataTable>;
        }
        if (this.state.settings) {
            settings = <ReactJson src={this.state.settings}
                theme={"bright"}
                iconStyle={"square"}
                collapsed={3}
                name={"ReconSettings"}
                display
            />
        }
        return (
            <React.Fragment>
                <div className="card">
                    <h1>Trigger Recon</h1>
                    <br />
                    <Messages ref={(el) => this.msgs = el} />
                    {progress}
                    <div className="p-grid">
                        <div className='p-col-12'>
                            <Terminal
                                commands={{
                                    start: {
                                        description: 'Start a job',
                                        usage: 'start',
                                        fn: (...args) => {
                                            let checksTypes = "";
                                            if (args[0]) {
                                                checksTypes = args[0];
                                            }
                                            this.setState({ progress: { expectedCount: '', processedCount: '' }, matches: [], settings: null });
                                            axiosClient.post('/recon/trigger/' + this.state.job.jobId + "?checksTypes=" + checksTypes)
                                                .then(res => {
                                                    this.setState({ job: res.data });
                                                }).catch(err => {
                                                    this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Unable to trigger recon due to system error.', sticky: false });
                                                });
                                        }
                                    },
                                    settings: {
                                        description: 'View Recon Settings',
                                        usage: 'settings',
                                        fn: (...args) => {
                                            this.setState({ progress: { expectedCount: '', processedCount: '' }, matches: [], setings: {} });
                                            axiosClient.get('/recon/setting')
                                                .then(res => {
                                                    this.setState({ settings: res.data });
                                                }).catch(err => {
                                                    this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Unable to fetch settings due to system error.', sticky: false });
                                                });
                                        }
                                    }
                                }}
                                welcomeMessage={'Welcome to Recon 2.0!\nYour session Id is: ' + this.state.job.jobId + " \n start - command to trigger recon 2.0\nsettings - View Recon settings in the system \n Author: Sai (sai@taxreco.com)\n\n\n---------------------------------------------\n"}
                                promptLabel={'$ '}
                            />
                        </div>
                    </div>
                    <hr />
                    <div className="card">
                        <div className="p-grid">
                            <div className='p-col-12'>
                                {results}
                                {settings}
                            </div>
                        </div>
                    </div>
                </div>
            </React.Fragment>
        );
    }
}