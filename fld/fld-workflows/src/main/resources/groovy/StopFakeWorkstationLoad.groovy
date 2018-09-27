def nodeName = 'node1';
def proxy = agentProxyFactory.createProxy(nodeName);
def fakeWorkstationPlugin = proxy.plugins.fakeWorkstation;
fakeWorkstationPlugin.stopAllFakeWorkstationProcesses();

