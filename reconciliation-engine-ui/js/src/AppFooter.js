import React, { Component } from "react";
import axiosClient from './constants'

export class AppFooter extends Component {
  constructor() {
    super();
    this.state = {
      version: ''
    };
  }

  componentDidMount() {
    axiosClient.get('/version')
      .then(res => {
        this.setState({ version: res.data.version });
      }).catch(err => {

      });
  }

  render() {
    return (
      <div className="layout-footer">
        <span className="footer-text" style={{ marginRight: "5px" }}>
          <font color='blue' size='2'><i>Email to: <a href="mailto:sai@taxreco.com">Sai - sai@taxreco.com</a> for support.</i></font>
        </span>
      </div>
    );
  }
}
