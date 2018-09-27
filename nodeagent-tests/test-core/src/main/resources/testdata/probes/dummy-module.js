var util = require('util');
var agent = require('../agent');
var proxy = require('../proxy');

module.exports = function(dummyModule) {

	proxy.before(dummyModule, 'logProcessId', function caHTTPBeforeHook(obj,
			args) {
		// cause exception
		obj.nonExisting();
	});

	proxy.after(dummyModule, [ 'logDate' ], function caHTTAfterHook(obj, args) {
		
		// throw custom exception
		throw "erroneous probe logic";
	});

	proxy.before(dummyModule, [ 'readProcessId' ], function caHTTPBeforeHook(
			obj, args) {

		proxy.callback(args, -1, function caHTTPCallbackHook(obj, args, _, _,
				_, storage) {
			console.log(obj.headers.user);
		});
	});
};