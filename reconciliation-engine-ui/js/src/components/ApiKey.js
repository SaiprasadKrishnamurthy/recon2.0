import React, { Component } from 'react';
import { InputTextarea } from 'primereact/inputtextarea';
import { Panel } from 'primereact/panel';
import "./Documentation.scss";


export class ApiKey extends Component {

    constructor() {
        super();
        this.state = {
            apiKey: localStorage.getItem('apiKey')
        };
    }

    render() {
        return (
            <div className="p-grid">
                <div className="p-col-12">
                    <div className="card docs">
                        <h1>Your API Key</h1>
                    </div>
                    <Panel>
                        <InputTextarea placeholder='paste your api key here' value={localStorage.getItem('apiKey')} onChange={(e) => {
                            localStorage.removeItem('apiKey');
                            localStorage.setItem('apiKey', e.target.value);
                            this.setState({ apiKey: e.target.value });
                        }} rows={5} cols={80} autoResize />
                        <p style={{ float: "center" }}>
                            <font color='blue' size='2'><i>Email to: <a href="mailto:digital_twin_core@sita.aero">digital_twin_core@sita.aero</a> to get the API Key if you don't have it.</i></font>
                        </p>
                    </Panel>
                </div>
            </div>
        );
    }
}