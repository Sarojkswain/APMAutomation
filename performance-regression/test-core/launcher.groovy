@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.2')
import groovyx.net.http.RESTClient
import groovy.net.http.ContentType
import org.apache.http.entity.ContentType

import static java.lang.Integer.parseInt

def buildStatus(def buildName) {
    def client = new RESTClient('http://fld-prg-res-man.ca.com:8080/resman/api/build/')
    client.setHeaders(Accept: 'application/json')
    def resp = client.get(path: buildName)

    if (resp.status == 200) {
        def data = resp.getData()
        if (data != null && !data.isEmpty()) {
            def result = data.get(0).get("result")
            if (result != null && result.containsKey("status")) {
                return result.get("status")
            }
        }
    }

    return ""
}

def startMaven(def testName) {
    println "Starting maven run for test ${testName}"
    def buildId = ""
    def process = "cmd /c mvn tas:run -Dresman.host=fld-prg-res-man.ca.com -Dtas.customJettyHost=jirji01w7a.ca.com -Dtas.skipLocalRepo=true -Dtas.tests=com.ca.apm.tests.test.ClusterRegressionTest.${testName}".execute()
//    def process = "cmd /c mvn tas:run -Dresman.host=fld-prg-res-man.ca.com -Dtas.customJettyHost=jirji01w7a.ca.com -Dtas.tests=com.ca.apm.tests.test.devel.StopLoadTest.${testName}".execute()
    process.in.eachLine { line ->
        if (line.contains("Test status tracking:"))
            buildId = line.substring(line.lastIndexOf('/') + 1)
        println line
    }
    process.err.eachLine { line -> println line }
    process.waitFor()
    return buildId
}

def startTest(def testName) {
    def buildId = startMaven(testName)
    switch (buildStatus(buildId)) {
        case "ERROR_DEPLOYMENT":
        case "FINISHED_FAILED":
        case "":
            startMaven(testName)
            break
    }
}

def availableBuilds(def va, def vb, def vc, def vd) {
    def client = new RESTClient("http://artifactory-emea-cz.ca.com:8081/artifactory/repo/com/ca/apm/delivery/introscope-installer-windows/")
    def resp = client.get(contentType: ContentType.TEXT_HTML)
    def a = 0, b = 0, c = 0, d = 0
    if (resp.status == 200) {
        def data = resp.getData().toString()
        if (data != null) {
            data.eachLine { line ->
                //matcher = (line =~ /.*href="(\d+)\.(\d+)\.(\d+)\.(\d+)\/".*/)
                matcher = (line =~ /(\d+)\.(\d+)\.(\d+)\.(\d+)\/.*/)
                if (matcher.matches()) {
                    def g1 = parseInt(matcher.group(1))
                    def g2 = parseInt(matcher.group(2))
                    def g3 = parseInt(matcher.group(3))
                    def g4 = parseInt(matcher.group(4))
                    if ((va == null || va == g1) && (vb == null || vb == g2) && (vc == null || vc == g3) && (vd == null || vd == g4)) {
                        a = Math.max(g1, a)
                        b = Math.max(g2, b)
                        c = Math.max(g3, c)
                        d = Math.max(g4, d)
                    }
                }
            }
        }
    }
    return a + '.' + b + '.' + c + '.' + d
}

println "Available builds: "
println availableBuilds(10, 2, null, null)
println availableBuilds(10, 3, null, null)

//startTest("testJMeter")
//startTest("testFakeWorkstation")
//startTest("testWurlitzer")
//startTest("testWebView")

//startTest("currentRegressionTest")
//startTest("baseRegressionTest")
//startTest("currentRegressionTest")
