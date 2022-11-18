import React, { Component } from 'react';
import { InputText } from 'primereact/inputtext';
import { Fieldset } from 'primereact/fieldset';
import { ReCron, Tab } from '@sbzen/re-cron';
import JSONInput from 'react-json-ide';
import locale from 'react-json-ide/locale/en';
import "./Documentation.scss";
import { DataService } from "../service/DataService";
import ReactHtmlParser from 'react-html-parser';
import { Button } from 'primereact/button';
import axiosClient from '../constants'
import { Messages } from 'primereact/messages';
import { Message } from 'primereact/message';
import queryString from 'query-string';


export class UpdateTemplate extends Component {

    constructor() {
        super();
        this.dataService = new DataService();
        this.state = {
            name: '',
            description: '',
            schedule: '*/5 * * * * *',
            outputTopic: '',
            outputKey: '',
            howMany: 1,
            placeholder: {},
            body: {},
            result: {},
            functionReference: this.dataService.getFunctionReference()
        };
        this.findById = this.findById.bind(this);
        this.save = this.save.bind(this);
        this.test = this.test.bind(this);

    }

    componentDidMount() {
        this.findById();
    }

    findById() {
        let params = queryString.parse(this.props.location.search)
        let templateName = params['name'];
        axiosClient.get('/template/?name=' + templateName)
            .then(res => {
                let tmp = res.data;
                this.setState({
                    name: tmp.name,
                    description: tmp.description,
                    schedule: tmp.cron, outputTopic:
                        tmp.outputTopic,
                    outputKey: tmp.outputKey,
                    placeholder: tmp.template,
                    body: tmp.template,
                    howMany: tmp.howMany
                });
            }).catch(err => {
                this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Unable to get template to a system error.', sticky: false });
            });
    }

    test() {
        axiosClient.post('/generate', { template: this.state.body })
            .then(res => {
                this.setState({ result: res.data });
            }).catch(err => {
                this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Unable to generate due to a system error.', sticky: false });
            });
    }

    save() {
        axiosClient.put('/template',
            {
                name: this.state.name,
                description: this.state.description,
                cron: this.state.schedule,
                outputTopic: this.state.outputTopic,
                outputKey: this.state.outputKey,
                howMany: this.state.howMany,
                template: this.state.body
            }).then(res => {
                this.msgs.show({ severity: 'success', summary: 'Success', detail: 'Template Saved and Deployed Successfully!', sticky: false });
            }).catch(err => {
                this.msgs.show({ severity: 'error', summary: 'Error', detail: 'Error occurred. Template Not Saved!', sticky: false });
            });
    }

    render() {
        return (
            <div className="card">
                <h1>Update Template</h1>
                <br />
                <Messages ref={(el) => this.msgs = el} />

                <Fieldset legend="General Details" toggleable>
                    <br />
                    <div className="card">
                        <div className="p-grid">
                            <div className="p-col-2">
                                <span className="p-float-label">
                                    <InputText readOnly id="in" value={this.state.name} onChange={(e) => this.setState({ name: e.target.value })} />
                                    <label htmlFor="in">Template Name</label>
                                </span>
                            </div>
                            <div className="p-col-2">
                                <span className="p-float-label">
                                    <InputText id="in1" value={this.state.description} onChange={(e) => this.setState({ description: e.target.value })} />
                                    <label htmlFor="in1">Template Description</label>
                                </span>
                            </div>
                            <div className="p-col-2">
                                <span className="p-float-label">
                                    <InputText id="in3" value={this.state.outputTopic} onChange={(e) => this.setState({ outputTopic: e.target.value })} />
                                    <label htmlFor="in3">Output Topic</label>
                                </span>
                            </div>
                            <div className="p-col-2">
                                <span className="p-float-label">
                                    <InputText id="in3" value={this.state.outputKey} onChange={(e) => this.setState({ outputKey: e.target.value })} />
                                    <label htmlFor="in3">Output Key</label>
                                </span>
                            </div>
                            <div className="p-col-2">
                                <span className="p-float-label">
                                    <InputText id="in4" value={this.state.howMany} onChange={(e) => this.setState({ howMany: e.target.value })} />
                                    <label htmlFor="in4">How many messages?</label>
                                </span>
                            </div>
                        </div>
                    </div>
                </Fieldset>
                <br />
                <Fieldset legend={"Template Schedule - (" + this.state.schedule + ") "} toggleable collapsed>
                    <br />
                    <div>
                        <ReCron
                            tabs={[Tab.SECONDS, Tab.MINUTES, Tab.HOURS]}
                            value={this.state.schedule}
                            onChange={e => this.setState({ schedule: e })}
                        />
                    </div>
                </Fieldset>
                <br />
                <Fieldset legend="Template Body" toggleable collapsed>
                    <div className="card">
                        <div className="p-grid">
                            <div className="p-col-6">
                                <h3>Input</h3>
                                <JSONInput
                                    id='jsoninput'
                                    value={this.state.body}
                                    placeholder={this.state.placeholder}
                                    locale={locale}
                                    onChange={e => {
                                        this.setState({ body: JSON.parse(e.json) });
                                    }}
                                    height='350px'
                                />
                            </div>
                            <div className="p-col-6">
                                <h3>Result</h3>
                                <JSONInput
                                    id='jsoninput'
                                    value={this.state.result}
                                    placeholder={this.state.result}
                                    locale={locale}
                                    height='350px'
                                />
                            </div>
                        </div>
                        <div className="card">
                            <Button style={{ float: 'centre', width: "100%" }} label="TEST THIS TEMPLATE" className="p-button-success" onClick={this.test} />
                        </div>
                    </div>
                </Fieldset>
                <br />
                <Fieldset legend="Function Reference" toggleable collapsed>
                    <div className="card docs">
                        <h1>Template Function Reference</h1>
                        <span><a href='https://github.com/vincentrussell/json-data-generator' target={"_blank"}>Original Documentation (opens in a new tab)</a></span>
                        <br />
                        <br />
                        {ReactHtmlParser(this.state.functionReference)}
                    </div>
                </Fieldset>
                <div className="card">
                    <Button style={{ float: 'centre', width: "100%" }} label="UPDATE THIS TEMPLATE" className="p-button-information" onClick={this.save} />
                </div>
            </div>
        );
    }
}