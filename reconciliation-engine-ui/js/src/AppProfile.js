import React, { Component } from "react";


export class AppProfile extends Component {
  constructor() {
    super();
    this.state = {
      expanded: false
    };
    this.onClick = this.onClick.bind(this);
  }

  onClick(event) {
    this.setState({ expanded: !this.state.expanded });
    event.preventDefault();
  }

  render() {
    return (
      <div>
        <img
          src="assets/layout/images/logo.png"
          alt=""
          width="140"
        />
        <br /><br />
        <div>
          <h1 style={{ color: "white" }}>Recon 2.0 client</h1>
        </div>
        <br />
        <hr />
        <br /><br /><br />


      </div>
    );
  }
}
