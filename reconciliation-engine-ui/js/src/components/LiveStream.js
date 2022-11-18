import { Client } from '@stomp/stompjs';
import React, { Component } from 'react';
import "./Documentation.scss";


export class LiveStream extends Component {

    constructor() {
        super();
        this.state = {
            logs: []
        };
        this.showMessage = this.showMessage.bind(this);
    }

    webSocketBaseUrl() {
        var l = window.location;
        return ((l.protocol === "https:") ? "wss://" : "ws://") + l.hostname + (((l.port !== 80) && (l.port !== 443)) ? ":" + l.port : "") + "/data-generator-service";
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
                this.client.subscribe('/topic/logs', message => {
                    this.showMessage(message);
                });
            },
        });

        this.client.activate();
    }

    showMessage(_msg) {
        if (_msg) {
            let msg = JSON.parse(_msg.body);
            console.log(msg);
            this.setState({ logs: [...this.state.logs, msg] });
        }
    }
    render() {
        return (
            <div className="card">
                <h1>Live Stream</h1>
                <br />
                <div className="p-grid">
                    {this.state.logs.map(l => {
                        return <div className="p-col-12">
                            <div className="card docs">
                                <h2>[{l.dateTime}]&nbsp;&nbsp;&nbsp;&nbsp;{l.templateName}</h2>
                                <pre>{JSON.stringify(l.data)}</pre>
                            </div>
                        </div>
                    })}

                </div>
            </div>
        );
    }
}