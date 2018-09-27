var Button = ReactBootstrap.Button;
var ButtonToolbar = ReactBootstrap.ButtonToolbar;
var Navbar = ReactBootstrap.Navbar;
var Nav = ReactBootstrap.Nav;
var NavItem = ReactBootstrap.NavItem;
var Table = ReactBootstrap.Table;
var ProgressBar = ReactBootstrap.ProgressBar;
var Glyphicon = ReactBootstrap.Glyphicon;
var Alert = ReactBootstrap.Alert;
var PageHeader = ReactBootstrap.PageHeader;
var Form = ReactBootstrap.Form;
var FormGroup = ReactBootstrap.FormGroup;
var ControlLabel = ReactBootstrap.ControlLabel;
var FormControl = ReactBootstrap.FormControl;
var Grid = ReactBootstrap.Grid;
var Row = ReactBootstrap.Row;
var Col = ReactBootstrap.Col;

var GET_STATUS_URL = "api/loads/status";
var GET_EMAIL_RECIPIENTS_URL = "api/reports/email-recipients";
var REMOVE_EMAIL_RECIPIENT_URL = "api/reports/remove-email-recipient";
var ADD_NEW_RECIPIENT_URL = "api/reports/add-email-recipient";
var REPORT_DAILY_TEST_URL = "api/reports/report-daily-test";
var REPORT_LOAD_LAUNCH_URL = "api/reports/report-load-launch";


function sendRequest(reqType, url, data, successHandler, failureHandler) {
	$.ajax({
        type: reqType,
		url: url,
		data: data
    }).then(
    		function (data) {
    			if (successHandler) {
    				successHandler(data);
    			}
    		},
    		function(jqXHR, textStatus, errorThrown) {
    			if (failureHandler) {
    				failureHandler(jqXHR, textStatus, errorThrown);
    			}
    		}
    );
}

function sendPOSTRequest(url, data, successHandler, failureHandler) {
	sendRequest("POST", url, data, successHandler, failureHandler);
}

function sendGETRequest(url, data, successHandler, failureHandler) {
	sendRequest("GET", url, data, successHandler, failureHandler);
}

function prepareErrorMessage(jqXHR, textStatus, errorThrown) {
	var msg = jqXHR.responseJSON ? jqXHR.responseJSON.message : "";
	var result = jqXHR.responseJSON ? jqXHR.responseJSON.result : "";
	if (!result) {
		msg = "Server might be down. Status: " + textStatus;
	} else {
		msg = "Status: " + textStatus + ". " + errorThrown + ": " + msg;
	}
	return msg;
}

class LoadStatusesTable extends React.Component {

	constructor(props) {
		super(props);
		this.state = { 
				isAlertVisible: false, 
				alertClassName: "", 
				alertMessage: "",
				statuses: []
		};

		this.handleAlertDismiss = this.handleAlertDismiss.bind(this);
		this.startLoad = this.startLoad.bind(this);
		this.stopLoad = this.stopLoad.bind(this);
		this.forceStartLoad = this.forceStartLoad.bind(this);
		this.forceStopLoad = this.forceStopLoad.bind(this);
		this.getStatuses = this.getStatuses.bind(this);

		this.getStatuses();
	}

	getStatuses() {
		sendGETRequest(
				GET_STATUS_URL,
				{}, 

				(data) => 
				{
					this.setState({ statuses : data.statuses });
	    		},

	    		(jqXHR, textStatus, errorThrown) => 
	    		{
	    			this.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: prepareErrorMessage(jqXHR, textStatus, errorThrown) });
	    		}
		);
	}

	componentDidMount() {
		this.interval = setInterval(() => this.getStatuses(), 5000);
	}

	componentWillUnmount() {
		clearInterval(this.interval);
	}

	handleAlertDismiss() {
		this.setState({ isAlertVisible: false, alertClassName: "", alertMessage: "" });
	}

	startLoad(loadName) {
		sendPOSTRequest(
				"api/loads/start?loadId=" + encodeURIComponent(loadName), 
				{},
	    		function (data) {
	    		},
	    		(jqXHR, textStatus, errorThrown) => 
	    		{
	    			
	    			this.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: prepareErrorMessage(jqXHR, textStatus, errorThrown) });
	    		}
		);
	}

	forceStartLoad(loadName) {
		sendPOSTRequest(
				"api/loads/start?loadId=" + encodeURIComponent(loadName) + "&force=true", 
				{},
	    		function (data) {
	    		},
	    		(jqXHR, textStatus, errorThrown) => 
	    		{
	    			
	    			this.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: prepareErrorMessage(jqXHR, textStatus, errorThrown) });
	    		}
		);
	}

	stopLoad(loadName) {
		sendPOSTRequest(
				"api/loads/stop?loadId=" + encodeURIComponent(loadName), 
				{},
	    		function (data) {
	    		},
	    		(jqXHR, textStatus, errorThrown) => 
	    		{
	    			this.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: prepareErrorMessage(jqXHR, textStatus, errorThrown) });
	    		}
		);
	}

	forceStopLoad(loadName) {
		console.log("forceStopLoad: loadName = " + loadName);
		sendPOSTRequest(
				"api/loads/stop?loadId=" + encodeURIComponent(loadName) + "&force=true", 
				{},
	    		function (data) {
	    		},
	    		(jqXHR, textStatus, errorThrown) => 
	    		{
	    			var msg = errorThrown + ": " + (jqXHR.responseJSON ? jqXHR.responseJSON.message : "");
	    			this.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: msg });
	    		}
		);
	}

	render() {
	    var rows = [];
	    if (this.state.statuses) {
	        rows = this.state.statuses.map((load, index) => {
	        	var buttonLabel;
	    	    var buttonFunction;
	    	    if (load.status === "STARTED" || load.status === "FAILEDSTART" || load.status === "ISSTARTING") {
	    	        buttonLabel = "Stop";
	    	        buttonFunction = this.stopLoad;
	    	    } else {
	    	        buttonLabel = "Start";
	    	        buttonFunction = this.startLoad;
	    	    }

	    	    var loadProgressBar;
	    	    if (load.status === "STARTED") {
	    	        loadProgressBar = <ProgressBar striped style={{ verticalAlign : "middle" }} bsStyle="success" now={100} label={'Started'} active />;
	    	    } else if (load.status === "ISSTOPPING") {
	    	        loadProgressBar = <ProgressBar striped style={{ verticalAlign : "middle" }} bsStyle="danger" now={100} label={'Stopping'} active />;
	    	    } else if (load.status === "ISSTARTING") {
	    	        loadProgressBar = <ProgressBar striped style={{ verticalAlign : "middle" }} bsStyle="warning" now={100} label={'Starting'} active />;
	    	    } else {
	    	        loadProgressBar = load.status;
	    	    }

	    	    return (
		    	    <tr key={load.name + "_" + (index + 1)}>
		    	    	<td style={{ verticalAlign : "middle" }}>{load.name}</td>
		    	    	<td style={{ verticalAlign : "middle" }}>{loadProgressBar}</td>
		    	    	<td style={{ verticalAlign : "middle", textAlign: "center" }}>
		    	    		<ButtonToolbar>
		    	    			<Button bsStyle="primary" bsSize="small" onClick={() => buttonFunction(load.name)}>
		    	    				{buttonLabel}
		    	    			</Button>
		    	    			<Button bsStyle="danger" bsSize="small"  onClick={() => this.forceStartLoad(load.name)}>
		    	    				Force Start
		    	    			</Button>
		    	    			<Button bsStyle="danger" bsSize="small"  onClick={() => this.forceStopLoad(load.name)}>
		    	    				Force Stop
		    	    			</Button>
		    	    		</ButtonToolbar>
		    	    	</td>
		    	    </tr>
	    	    );
	        });
	    }
		
		return (
				<div className="contentContainer">
				{ 
					this.state.isAlertVisible && 
					<Alert bsStyle={this.state.alertClassName} onDismiss={this.handleAlertDismiss}>
						{ this.state.alertMessage }
					</Alert>
				}

				<Table striped hover bordered responsive id="statuses-table" >
					<thead style={{ backgroundColor: "rgba(51, 122, 183, 0.48)" }}>
						<tr>
							<th style={{ width: "65%" }}>Load Name</th>
							<th style={{ textAlign : "center" }}>Load Status</th>
							<th style={{ textAlign : "center" }}>Operation</th>
						</tr>
					</thead>
					<tbody>{rows}</tbody>
				</Table>
				
				</div>
		);
	}
}

class DeleteByIdButton extends React.Component {
	constructor(props) {
		super(props);
		this.byId = props.byId;
		this.handleClick = this.handleClick.bind(this);
	}	

	handleClick(event) {
		this.props.onClick(this.byId);
		event.preventDefault();
	}

	render() {
		return (
				<Button bsStyle="primary" onClick={this.handleClick} >
					<Glyphicon glyph="trash" />
				</Button>
		);
	}
}

class EmailReportConfig extends React.Component {

	constructor(props) {
		super(props);
		this.state = { 
				emailRecipients: [],  
				isAlertVisible: false, 
				alertClassName: "", 
				alertMessage: "",
				newRecipientName: "", 
				newRecipientSurname: "", 
				newRecipientEmail: "", 
				testAlmId: "",
				testDescr: "", 
				testStatus: "",
				loadName: "",
				loadDescr: "", 
				loadStartedAt: "",
				loadFinishesAt: ""
		};
		
		this.handleAlertDismiss = this.handleAlertDismiss.bind(this);
		this.handleSubmitNewRecipient = this.handleSubmitNewRecipient.bind(this);
		this.handleSubmitDailyTestReport = this.handleSubmitDailyTestReport.bind(this);
		this.handleNewRecipientChange = this.handleNewRecipientChange.bind(this);
		this.handleDailyTestReportChange = this.handleDailyTestReportChange.bind(this);
		this.handleLoadLaunchReportChange = this.handleLoadLaunchReportChange.bind(this);
		this.handleSubmitLoadLaunchReport = this.handleSubmitLoadLaunchReport.bind(this);
		this.onDeleteRecipient = this.onDeleteRecipient.bind(this);
	}
	
	componentDidMount() {
		this.getEmailRecipients();
	}
	
	getEmailRecipients() {
	    var self = this;
		sendGETRequest(
				GET_EMAIL_RECIPIENTS_URL,
				{}, 
	    		function (data) {
	    			self.setState({ emailRecipients : data.emailRecipients });
	    		},

	    		function(jqXHR, textStatus, errorThrown) {
	    			self.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: prepareErrorMessage(jqXHR, textStatus, errorThrown) });
	    		}
		);
	}
	
	handleAlertDismiss() {
		this.setState({ isAlertVisible: false, alertClassName: "", alertMessage: "" });
	}
	
	handleSubmitNewRecipient(event) {
	    var self = this;
		sendPOSTRequest(
				ADD_NEW_RECIPIENT_URL,
				{ name: self.state.newRecipientName, surname: self.state.newRecipientSurname, email: self.state.newRecipientEmail }, 

				function (data) {
					self.setState({ isAlertVisible: true, alertClassName: "success", alertMessage: "New recipient added!" });
					self.getEmailRecipients();
				},
	    		
				function(jqXHR, textStatus, errorThrown) {
					self.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: prepareErrorMessage(jqXHR, textStatus, errorThrown) });
				}
		);

		event.preventDefault();
	}
	
	handleNewRecipientChange(event) {
		const target = event.target;
		const value = target.value;
		const name = target.name;
		
	    this.setState({ [name] : value });
	}

	handleSubmitDailyTestReport(event) {
	    var self = this;
		
		sendPOSTRequest(
				REPORT_DAILY_TEST_URL,
				{ almId: self.state.testAlmId, desc: self.state.testDescr, passed: self.state.testStatus }, 

				function (data) {
					self.setState({ isAlertVisible: true, alertClassName: "success", alertMessage: "Daily test report sent!" });
				},

				function(jqXHR, textStatus, errorThrown) {
					self.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: prepareErrorMessage(jqXHR, textStatus, errorThrown) });
				}
		);

		event.preventDefault();
	}
	
	handleDailyTestReportChange(event) {
		const target = event.target;
		const value = target.value;
		const name = target.name;
		
	    this.setState({ [name] : value });
	}
	
	handleSubmitLoadLaunchReport(event) {
	    var self = this;
		sendPOSTRequest(
				REPORT_LOAD_LAUNCH_URL,
				{ loadName: self.state.loadName, desc: self.state.loadDescr, startedAt: self.state.loadStartedAt, finishesAt: self.state.loadFinishesAt }, 

				function (data) {
					self.setState({ isAlertVisible: true, alertClassName: "success", alertMessage: "Load launch report sent!" });
				},

				function(jqXHR, textStatus, errorThrown) {
					self.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: prepareErrorMessage(jqXHR, textStatus, errorThrown) });
				}
		);

		event.preventDefault();
	}
	
	handleLoadLaunchReportChange(event) {
		const target = event.target;
		const value = target.value;
		const name = target.name;
		
	    this.setState({ [name] : value });
	}
	
	onDeleteRecipient(id) {
		var self = this;
		sendPOSTRequest(REMOVE_EMAIL_RECIPIENT_URL, 
				{ id: id },
				function (data) {
					self.setState({ isAlertVisible: true, alertClassName: "success", alertMessage: "Recipient deleted!" });
					self.getEmailRecipients();
				},
				function(jqXHR, textStatus, errorThrown) {
					self.setState({ isAlertVisible: true, alertClassName: "danger", alertMessage: prepareErrorMessage(jqXHR, textStatus, errorThrown) });
				}
		);
	}
	
	render() {
		return (
				<div className="contentContainer">
					{ 
						this.state.isAlertVisible && 
						<Alert bsStyle={this.state.alertClassName} onDismiss={this.handleAlertDismiss}>
							{ this.state.alertMessage }
						</Alert>
					}
					
					<PageHeader>List of known report email recipients</PageHeader>
					<Table striped hover responsive>
						<thead>
							<tr>
								<th>#</th>
								<th>First Name</th>
								<th>Last Name</th>
								<th>Email</th>
								<th>Action</th>
							</tr>
						</thead>
						<tbody>
						{
							this.state.emailRecipients.map((emailRecipient, index) => 
								<tr key={emailRecipient.email}>
									<td>{index + 1}</td>
									<td>{emailRecipient.name}</td>
									<td>{emailRecipient.surname}</td>
									<td>{emailRecipient.email}</td>
									<td>
										<DeleteByIdButton byId={emailRecipient.id} onClick={this.onDeleteRecipient}>
										</DeleteByIdButton>
									</td>
								</tr>
							)
						}
				       </tbody>
				  </Table>

				  <PageHeader>Add a new recipient</PageHeader>
				  
				  <Grid>
				  	<Row>
					  <Form onSubmit={this.handleSubmitNewRecipient}>
					  	<Col md={3} lg={3}>  	
						  	<FormControl type="text" placeholder="Name" name="newRecipientName" value={this.state.newRecipientName} onChange={this.handleNewRecipientChange} />
						</Col>
				  		<Col md={4} lg={4}>
					  		<FormControl type="text" placeholder="Surname" name="newRecipientSurname" value={this.state.newRecipientSurname} onChange={this.handleNewRecipientChange} />
					  	</Col>
				  		<Col md={4} lg={4}>
					    	<FormControl type="email" placeholder="email@domain.com" name="newRecipientEmail" value={this.state.newRecipientEmail} onChange={this.handleNewRecipientChange} />
						</Col>
					    <Col md={1} lg={1}>
							<Button type="submit">
							   	Add
							</Button>
						</Col>
					  </Form>				  
				  	</Row>
				  </Grid>

				  <PageHeader>Send daily test report manually</PageHeader>
				  
				  <Grid>
				  	<Row>
					  <Form onSubmit={this.handleSubmitDailyTestReport}>
					  	<Col md={3} lg={3}>  	
						  	<FormControl type="text" placeholder="Alm Id" name="testAlmId" value={this.state.testAlmId} onChange={this.handleDailyTestReportChange} />
						</Col>
				  		<Col md={4} lg={4}>
					  		<FormControl type="text" placeholder="Description" name="testDescr" value={this.state.testDescr} onChange={this.handleDailyTestReportChange} />
					  	</Col>
				  		<Col md={4} lg={4}>
					    	<FormControl type="text" placeholder="Status" name="testStatus" value={this.state.testStatus} onChange={this.handleDailyTestReportChange} />
						</Col>
					    <Col md={1} lg={1}>
							<Button type="submit">
							   	Send
							</Button>
						</Col>
					  </Form>				  
				  	</Row>
				  </Grid>

				  <PageHeader>Send load launch report manually</PageHeader>
				  
				  <Grid>
				  	<Row>
					  <Form onSubmit={this.handleSubmitLoadLaunchReport}>
					  	<Col md={3} lg={3}>  	
						  	<FormControl type="text" placeholder="Load name" name="loadName" value={this.state.loadName} onChange={this.handleLoadLaunchReportChange} />
						</Col>
				  		<Col md={4} lg={4}>
					  		<FormControl type="text" placeholder="Description" name="loadDescr" value={this.state.loadDescr} onChange={this.handleLoadLaunchReportChange} />
					  	</Col>
				  		<Col md={2} lg={2}>
					    	<FormControl type="text" placeholder="Started At" name="loadStartedAt" value={this.state.loadStartedAt} onChange={this.handleLoadLaunchReportChange} />
						</Col>
				  		<Col md={2} lg={2}>
					    	<FormControl type="text" placeholder="Finishes At" name="loadFinishesAt" value={this.state.loadFinishesAt} onChange={this.handleLoadLaunchReportChange} />
						</Col>
					    <Col md={1} lg={1}>
							<Button type="submit">
							   	Send
							</Button>
						</Col>
					  </Form>				  
				  	</Row>
				  </Grid>
				  
				  </div>
		);
	}
}

class MyNavBar extends React.Component {

	constructor(props) {
		super(props);
		this.handleSelect = this.handleSelect.bind(this);
		this.state = { selectedKey: 1 };
	}
	
	handleSelect (selectedKey) {
	    switch (selectedKey) {
	    default:
	    case 1:
			ReactDOM.render(
		    		<LoadStatusesTable statuses="" />, 
		    		document.getElementById('container')
		    );
	    	break;
	    case 2: 
			ReactDOM.render(
		    		<EmailReportConfig />, 
		    		document.getElementById('container')
		    );
	    	break;
	    }
	    this.setState({ selectedKey: selectedKey });
	}
	
	render () {

		return ( 
		
		<Navbar>
			<Navbar.Header className="navBarHeader">
				<Navbar.Brand className="navBarBrand">
					<span className="navBarLogoSpan">
						<img className="navBarLogoImage" src="img/heavy_load_48x48.png"/> &nbsp;&nbsp;&nbsp;FLD Load Monitor
					</span>
				</Navbar.Brand>
			</Navbar.Header>
			<Navbar.Collapse>
				<Nav pullRight activeKey={this.state.selectedKey} onSelect={this.handleSelect}>
					<NavItem eventKey={1} href="/home" title="Home">
						<Glyphicon glyph="home" className="navGlyphButton" /> 
					</NavItem>
					<NavItem eventKey={2} href="/report-config" title="Email Reporting Configuration">
						<Glyphicon glyph="envelope" className="navGlyphButton" /> 
					</NavItem>
				</Nav>
			</Navbar.Collapse>
		</Navbar>
	
		);
		
	}
	
}

$(document).ready(function() {
    
    ReactDOM.render(
    		<MyNavBar />, 
    		document.getElementById('navigationTopMenuBar')
    );

    ReactDOM.render(
    		<LoadStatusesTable statuses="" />, 
    		document.getElementById('container')
    );
});

