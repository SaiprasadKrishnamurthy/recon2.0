import React, { Component } from "react";
import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Dropdown } from "primereact/dropdown";
import { Panel } from "primereact/panel";
import { Calendar } from "primereact/calendar";
import { Growl } from "primereact/growl";
import { Button } from "primereact/button";
import { PacmanLoader } from "react-spinners";

export class Jobs extends Component {
  constructor() {
    super();
    this.state = {
      jobs: [],
      outputDirSize: "0",
      configs: [],
      config: "",
      from: null,
      to: null,
      collapseJobsHistory: false
    };

    this.predictiveModelsService = new PredictiveModelService();
    this.csv = this.csv.bind(this);
    this.json = this.json.bind(this);
    this.trigger = this.trigger.bind(this);
    this.status = this.status.bind(this);
  }

  componentDidMount() {
    this.predictiveModelsService.getJobs().then(data => {
      this.setState({ jobs: data });
    });
    this.predictiveModelsService
      .outputDirSize()
      .then(data => this.setState({ outputDirSize: data.size }));
    this.predictiveModelsService.getConfigs().then(c => {
      const cs = c.map(x => {
        return { label: x.id, value: x.id };
      });
      this.setState({ configs: cs });
    });
  }

  status(rowData) {
    console.log("****" + rowData.status);
    if (rowData.status === "InProgress") {
      return <PacmanLoader sizeUnit={"px"} size={18} color={"green"} />;
    } else {
      return <span style={{ fontWeight: "bold" }}>Completed</span>;
    }
  }
  trigger(configId) {
    this.predictiveModelsService
      .trigger(this.state.config, this.state.from, this.state.to)
      .then(res => {
        this.growl.show({
          severity: "info",
          summary: "Info",
          detail: "Job Triggered."
        });
        this.setState({ collapseJobsHistory: false });
        // Refresh jobs.
        this.predictiveModelsService
          .getJobs()
          .then(r => this.setState({ jobs: r }));
      });
  }
  csv(rowData, column) {
    if (rowData.numberOfFiles == 0) {
      return <div />;
    }
    return (
      <div>
        <a
          href={this.predictiveModelsService.csvDownloadLink(rowData.id)}
          target="_blank"
        >
          Download
        </a>
      </div>
    );
  }

  json(rowData, column) {
    if (rowData.numberOfFiles == 0) {
      return <div />;
    }
    return (
      <div>
        {console.log(JSON.stringify(rowData))}

        <a
          href={this.predictiveModelsService.jsonDownloadLink(rowData.id)}
          target="_blank"
        >
          Download
        </a>
      </div>
    );
  }
  render() {
    var header = (
      <div className="p-clearfix" style={{ lineHeight: "1.87em" }}>
        Jobs{" "}
      </div>
    );

    return (
      <React.Fragment>
        <Growl ref={el => (this.growl = el)} />

        <h1>JOBS</h1>
        <hr />
        <br />
        <Panel header="Trigger a New JOB" toggleable={true} collapsed={true}>
          <div className="p-grid">
            <div className="p-col-12">
              <div className="card">
                <div className="p-col-12">
                  <span style={{ fontWeight: "bold" }}>Configs</span>
                </div>
                <div className="p-col-12">
                  <Dropdown
                    value={this.state.config}
                    options={this.state.configs}
                    onChange={e => {
                      this.setState({ config: e.value });
                    }}
                    placeholder="Select Config"
                  />
                </div>
                <div className="p-col-12">
                  <span style={{ fontWeight: "bold" }}>From Date</span>
                </div>
                <div className="p-col-12">
                  <Calendar
                    monthNavigator={true}
                    yearNavigator={true}
                    yearRange="2010:2030"
                    dateFormat="dd-MM-yy"
                    value={this.state.from}
                    onChange={e => this.setState({ from: e.value })}
                    readonlyInput={false}
                  />
                </div>
                <div className="p-col-12">
                  <span style={{ fontWeight: "bold" }}>To Date</span>
                </div>
                <div className="p-col-12">
                  <Calendar
                    dateFormat="dd-MM-yy"
                    monthNavigator={true}
                    yearNavigator={true}
                    yearRange="2010:2030"
                    value={this.state.to}
                    onChange={e => this.setState({ to: e.value })}
                    readonlyInput={false}
                  />
                </div>
              </div>
              <div className="p-col-12">
                <Button
                  label="Trigger"
                  className="p-button-danger"
                  onClick={this.trigger}
                />
              </div>
            </div>
          </div>
        </Panel>
        <br />
        <hr />
        <br />
        <Panel
          header="Jobs history"
          toggleable={true}
          collapsed={this.state.collapseJobsHistory}
        >
          <div className="p-grid">
            <div className="p-col-12">
              <Button
                className="p-button-info"
                icon="pi pi-refresh"
                onClick={e => {
                  this.predictiveModelsService
                    .getJobs()
                    .then(r => this.setState({ jobs: r }));
                  this.predictiveModelsService
                    .outputDirSize()
                    .then(data => this.setState({ outputDirSize: data.size }));
                }}
              />
            </div>
          </div>
          <div className="p-grid">
            <div className="p-col-12">
              <div className="card">
                <h2>
                  Output Directory Size on Server: {this.state.outputDirSize}
                </h2>

                <br />
                <DataTable
                  value={this.state.jobs}
                  paginator={true}
                  header={header}
                  rows={10}
                >
                  <Column
                    field="id"
                    header="Job ID"
                    sortable={true}
                    filter={true}
                    filterMatchMode="contains"
                  />
                  <Column
                    field="configId"
                    header="Config ID"
                    sortable={true}
                    filter={true}
                    filterMatchMode="contains"
                  />
                  <Column
                    field="fromDateTime"
                    header="Query Start Date"
                    sortable={true}
                    filterMatchMode="contains"
                  />
                  <Column
                    field="toDateTime"
                    header="Query End Date"
                    sortable={true}
                    filterMatchMode="contains"
                  />
                  <Column
                    field="startDateTime"
                    header="Start Date Time"
                    sortable={true}
                  />
                  <Column
                    field="endDateTime"
                    header="End Date Time"
                    sortable={true}
                  />
                  <Column
                    field="numberOfFiles"
                    header="Total Samples"
                    sortable={true}
                  />
                  <Column
                    header="CSV Download"
                    body={this.csv}
                    style={{ textAlign: "center", width: "8em" }}
                  />
                  <Column
                    header="JSON Download"
                    body={this.json}
                    style={{ textAlign: "center", width: "8em" }}
                  />
                  <Column
                    header="STATUS"
                    body={this.status}
                    style={{ textAlign: "center", width: "8em" }}
                  />
                </DataTable>
              </div>
            </div>
          </div>
        </Panel>
      </React.Fragment>
    );
  }
}
