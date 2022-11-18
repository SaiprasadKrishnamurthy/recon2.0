import React, { Component } from "react";
import "./Documentation.scss";

export class Docs extends Component {
  constructor() {
    super();
    this.state = {};
  }

  render() {
    return (
      <div className="p-grid">
        <div className="p-col-12">
          <div className="card docs">
            <h1>Recon 2.0 Docs</h1>
            <h2>Recon Settings</h2>
            <img src="assets/layout/images/recon_sample_setting.jpg"
              width={"100%"}
              height={"100%"}
              alt="" />
          </div>
        </div>
      </div>
    );
  }
}
