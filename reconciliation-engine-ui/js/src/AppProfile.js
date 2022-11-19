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
          <h1 style={{ color: "white" , marginLeft: "5px"}}>Recon 2.0 client</h1>
          <h3 style={{ color: "gold", marginLeft: "20px" }}>Project Aurum</h3>
        </div>
        <br />
        <hr />
        <br /><br /><br />


      </div>
    );
  }
}
