1. Prerequisites:
-----------------
    * CTG Server
    * CICS Region
    * Specific test definitions can have additional requirements

2. Setup:
---------
    * Unpack the CICSTestDriver package somewhere (<ctd-path>)

    * Deploy an Agent installation,
      e.g. IntroscopeAgentFiles-NoInstaller<version>default.<os-sufix>

    * In Agent installation copy wily\examples\Cross-Enterprise_APM\ext\*.jar
      to wily\core\ext

    * Enable the following PBDs in the Agent:
        * default-typical.pbl             (part of the Agent distribution)
        * CTG_ECI_Tracer_For_SYSVIEW.pbd  (part of the Agent distribution)
        * CICSTestDriver.pbd              (part of CICSTestDriver)

    * Set the following Agent configuration options:
        * com.wily.introscope.agent.httpheaderinsertion.enabled=true
        * introscope.ctg.tracer.inject.guid=true
        * introscope.agent.transactiontracer.tailfilterPropagate.enable=true
        * ceapm.ipic.supported=true
        * ceapm.commarea.sampling.supported=true
        * ceapm.commarea.program.name.regex=.*
        * ceapm.nodata.program.name.regex=.*
        * ceapm.channel.program.name.regex=.*

    * Update the CICSTestDriver configuration at the start of either
      <ctd-path>/run.sh or <ctd-path>/run.bat depending on which operating
      system is being used.

3. Running tests:
-----------------
    * Execute either the <ctd-path>/run.sh or <ctd-path>/run.bat script, passing
      it the test definition you wish to use.
    * You can examine the set of pre-packaged test definitions by looking
      through the files in the <ctd-path>/xml directory.
