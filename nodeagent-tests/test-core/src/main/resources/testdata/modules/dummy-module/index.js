var logProcessId = function() {
	console.log('current process id: ' + process.pid);
}

var readProcessId = function(callback) {
	callback(process.pid);
}

var logDate = function() {
	console.log("today's date: " + new Date());
}

var readDate = function(callback) {
	callback(new Date());
}

module.exports = {
	logProcessId: logProcessId,
	readProcessId: readProcessId,
	logDate: logDate,
	readDate: readDate
}