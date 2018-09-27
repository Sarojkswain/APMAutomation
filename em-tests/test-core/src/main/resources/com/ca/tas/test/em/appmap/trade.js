var baseURL = "http://${machine.tradeServiceApp.hostname}:${role.tomcat60.tomcat.port}/";
var localHrefs = [ "TradeService", "AuthenticationService/ServletA6", "TradeService/PlaceOrder", "TradeService/TradeOptions", "TradeService/ViewOrders", "ReportingService/ServletA6" ];
var count = 0;
var loading = 0;

function loadPage(url) {
    console.log(url + ' ...');
    ++loading;
    var page = require('webpage').create();
    page.open(url, function(status) {
        if (status !== 'success') {
            console.log('FAIL to load the address');
            phantom.exit(1);
        } else {
            console.log(url + ' loaded');
            --loading;
        }
    });
};

function loadPages() {
    if (loading == 0) {
        if (count >= 20) {
            phantom.exit();
        }
        for (i in localHrefs) {
            loadPage(baseURL + localHrefs[i]);
        }
        ++count;
    }
};

setInterval(loadPages, 500);
