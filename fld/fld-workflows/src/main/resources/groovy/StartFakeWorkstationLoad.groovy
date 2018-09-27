def nodeName = 'node1';
def javaOptions = '-Xms96m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError';
def agentListLive = '(.*)Agent_1,(.*)Agent_2,(.*)Agent_3,(.*)Agent_4,(.*)Agent_5,(.*)Agent_6,(.*)Agent_7,(.*)Agent_8,(.*)Agent_9,(.*)Agent_10';
def agentListHistorical = '(.*)Agent_2';
def momHost = 'tas-czt-ne0';
def port = 5001;
def userLive = 'cemadmin';
def passwordLive = 'quality';
def userHistorical = 'cemadmin';
def passwordHistorical = 'quality';
def resolution = 15;
def sleepBetween = 15000;
def metric = 'Servlets\\|Servlet_(.*):Average Response Time \\(ms\\)';


def proxy = agentProxyFactory.createProxy(nodeName);
def fakeWorkstationPlugin = proxy.plugins.fakeWorkstation;
def repoType = com.ca.apm.systemtest.fld.plugin.downloader.ArtifactManager.RepositoryType.ARTIFACTORYLITE;
def downloadDir = 'C:/fakeWS';
def fileName = 'fakeWorkstation';
def extension = '.jar';


def fakeWorkstationJarPath = fakeWorkstationPlugin.downloadFakeWorkstation('99.99.sys-SNAPSHOT', repoType, downloadDir, fileName, extension);




def jvmOptsArray = javaOptions.split("\\s+")

def agentsLive = agentListLive.split(',')


def liveMap = [:]
for (agent in agentsLive) {
    def liveProcId = fakeWorkstationPlugin.runQueriesAgainstMOM(fakeWorkstationJarPath, jvmOptsArray, momHost, port, userLive,
                                                                                                              passwordLive, resolution, sleepBetween, '', metric, agent)
    liveMap.put(agent, liveProcId)
}

//----------------------

def agentsHistorical = agentListHistorical.split(',')

def historicalMap = [:]
for (agent in agentsHistorical) {
    def historicalProcId = fakeWorkstationPlugin.runQueriesAgainstMOM(fakeWorkstationJarPath, jvmOptsArray, momHost, port, userHistorical,
                                                                                                                       passwordHistorical, resolution, sleepBetween, '-historical', metric, agent)
    historicalMap.put(agent, historicalProcId)
}


