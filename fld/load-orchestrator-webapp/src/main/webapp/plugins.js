/* plugins.html javascript file
 * author: Pavel Zuna <pavel.zuna@contractor.ca.com> */

var NODE_SELECT_ID = 'nodeName';
var PLUGIN_SELECT_ID = 'pluginName';
var METHOD_SELECT_ID = 'methodName';
var METHOD_PARAMETERS_TABLE = 'methodParameters';

var node_plugins_data = [];

String.prototype.rsplit = function (delimiter, limit) {
	delimiter = this.split(delimiter || /s+/);
	return (limit ? delimiter.splice (-limit) : delimiter);
} 

function fill_method_params_form(plugin_name, method_name) {
	for (var i = 0; i < node_plugins_data.plugins.length; ++i) {
		var p = node_plugins_data.plugins[i];
		if (p.name == plugin_name) {
			for (var j = 0; j < p.operations.length; ++j) {
				var o = p.operations[j];
				if (o.name == method_name) {
					var table = document.getElementById(METHOD_PARAMETERS_TABLE);
					var tbody = table.getElementsByTagName('tbody')[0];
					while (tbody.firstChild) {
						tbody.removeChild(tbody.firstChild);
					}
					for (var k = 0; k < o.parameters.length; ++k) {
						var prop = o.parameters[k];
						var tr = document.createElement('tr');
						var tdName = document.createElement('td');
						var inputName = document.createTextNode(prop.javaType);
						tdName.appendChild(inputName);
						tr.appendChild(tdName);
						var tdVal = document.createElement('td');
						tdVal.setAttribute('style', 'width: 600px;');
						var inputVal = document.createElement('input');
						inputVal.setAttribute('name', 'methodParams');
						inputVal.setAttribute('type', 'text');
						if (prop.value) {
							inputVal.setAttribute('value', prop.value);
						}
						tdVal.appendChild(inputVal);
						tr.appendChild(tdVal);
						tbody.appendChild(tr);
					}
					break;
				}
			}
			break;
		}
	}
}

function fill_method_select(plugin_name) {
	var select = document.getElementById(METHOD_SELECT_ID);
	while (select.firstChild) {
		select.removeChild(select.firstChild);
	}
	for (var i = 0; i < node_plugins_data.plugins.length; ++i) {
		var p = node_plugins_data.plugins[i];
		if (p.name == plugin_name) {
			for (var j = 0; j < p.operations.length; ++j) {
				var o = p.operations[j];
				var node = document.createElement('option');
				node.setAttribute('value', o.name);
				var textNode = document.createTextNode(o.name);
				node.appendChild(textNode);
				select.appendChild(node);
			}
			select.plugin_name = plugin_name;
			break;
		}
	}
	select.onchange = function () {
		fill_method_params_form(this.plugin_name, this.options[this.selectedIndex].value);
	}
	select.onchange();
}

function fill_plugin_select(json) {
	var select = document.getElementById(PLUGIN_SELECT_ID);
	while (select.firstChild) {
		select.removeChild(select.firstChild);
	}
	for (var i = 0; i < json.plugins.length; ++i) {
		var p = json.plugins[i];
		var node = document.createElement('option');
		node.setAttribute('value', p.name);
		var textNode = document.createTextNode(p.name);
		node.appendChild(textNode);
		select.appendChild(node);
	}
	select.onchange = function () {
		fill_method_select(this.options[this.selectedIndex].value);
	}
	select.onchange();
}

function fill_node_select(json) {
	var select = document.getElementById(NODE_SELECT_ID);
	while (select.firstChild) {
		select.removeChild(select.firstChild);
	}
	for (var i = 0; i < json.nodes.length; ++i) {
		var n = json.nodes[i];
		var node = document.createElement('option');
		node.setAttribute('value', n.name);
		var textNode = document.createTextNode(n.name);
		node.appendChild(textNode);
		select.appendChild(node);
	}
	select.onchange = function () {
		var xhr = new XMLHttpRequest();
		xhr.open('GET', 'nodePluginList?nodeName=' + this.options[this.selectedIndex].value, true);
		xhr.onreadystatechange = function () {
			xhr.onreadystatechange = function () {
				if (xhr.readyState == 4) {
					if (xhr.status == 200) {
						node_plugins_data = JSON.parse(xhr.responseText);
						if (node_plugins_data.status != '200') {
							alert('the server returned an error response');
						} else {
							fill_plugin_select(node_plugins_data);
						}
					} else {
						alert(xhr.responseText);
					}
				}
			}
		}
		xhr.send();
	}
	select.onchange();
}

function list_nodes() {
	var xhr = new XMLHttpRequest();
	xhr.open('GET', 'nodeList', true);
	xhr.onreadystatechange = function () {
		xhr.onreadystatechange = function () {
			if (xhr.readyState == 4) {
				if (xhr.status == 200) {
					var upload_response_data = JSON.parse(xhr.responseText);
					if (upload_response_data.status != '200') {
						alert('the server returned an error response');
					} else {
						fill_node_select(upload_response_data);
					}
				} else {
					alert(xhr.responseText);
				}
			}
		}
	}
	xhr.send();
	return (false);
}

function load_stuff() {
	list_nodes();
}

/* end of file */