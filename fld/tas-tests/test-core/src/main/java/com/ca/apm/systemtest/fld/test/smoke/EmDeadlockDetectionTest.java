package com.ca.apm.systemtest.fld.test.smoke;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.flow.DeadlockDetectionFlow;
import com.ca.apm.systemtest.fld.flow.DeadlockDetectionFlowContext;
import com.ca.apm.systemtest.fld.role.DeadlockDetectionRole;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfigurationService;
import com.ca.apm.systemtest.fld.testbed.smoke.EmDeadlockDetectionTestTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class EmDeadlockDetectionTest extends TasTestNgTest {

    private static FLDConfiguration fldConfiguration = FLDConfigurationService.getConfig();

    @Test(groups = {"emDeadlockDetectionTest"})
    @Tas(testBeds = @TestBed(name = EmDeadlockDetectionTestTestbed.class, executeOn = EmDeadlockDetectionTestTestbed.MACHINE_ID_windows), size = SizeType.MEDIUM, owner = "bocto01")
    public void testEmDeadlockDetection_1_windows() {
        DeadlockDetectionFlowContext context =
            (new DeadlockDetectionFlowContext.Builder()).locateRunningEm()
                .fromEmailAddress(DeadlockDetectionRole.DEFAULT_FROM_EMAIL_ADDRESS)
                .smtpHost(fldConfiguration.getFldConfigSmtpHost())
                .addEmailAddress("bocto01@ca.com")
                // .addEmailAddress(fldConfiguration.getReportEmail())
                .build();

        runFlowByMachineId(EmDeadlockDetectionTestTestbed.MACHINE_ID_windows,
            DeadlockDetectionFlow.class, context);
    }

    @Test(groups = {"emDeadlockDetectionTest"})
    @Tas(testBeds = @TestBed(name = EmDeadlockDetectionTestTestbed.class, executeOn = EmDeadlockDetectionTestTestbed.MACHINE_ID_windows), size = SizeType.MEDIUM, owner = "bocto01")
    public void testEmDeadlockDetection_2_windows() {
        DeadlockDetectionFlowContext context =
            deserializeFlowContextFromRole(
                EmDeadlockDetectionTestTestbed.EM_DEADLOCK_DETECTION_ROLE_ID_windows,
                DeadlockDetectionRole.ENV_RUN_EM_DEADLOCK_DETECTION,
                DeadlockDetectionFlowContext.class);

        runFlowByMachineId(EmDeadlockDetectionTestTestbed.MACHINE_ID_windows,
            DeadlockDetectionFlow.class, context);
    }

    @Test(groups = {"emDeadlockDetectionTest"})
    @Tas(testBeds = @TestBed(name = EmDeadlockDetectionTestTestbed.class, executeOn = EmDeadlockDetectionTestTestbed.MACHINE_ID_linux), size = SizeType.MEDIUM, owner = "bocto01")
    public void testEmDeadlockDetection_1_linux() {
        DeadlockDetectionFlowContext context =
            (new DeadlockDetectionFlowContext.Builder()).locateRunningEm()
                .fromEmailAddress(DeadlockDetectionRole.DEFAULT_FROM_EMAIL_ADDRESS)
                .smtpHost(fldConfiguration.getFldConfigSmtpHost())
                .addEmailAddress("bocto01@ca.com")
                // .addEmailAddress(fldConfiguration.getReportEmail())
                .build();

        runFlowByMachineId(EmDeadlockDetectionTestTestbed.MACHINE_ID_linux,
            DeadlockDetectionFlow.class, context);
    }

    @Test(groups = {"emDeadlockDetectionTest"})
    @Tas(testBeds = @TestBed(name = EmDeadlockDetectionTestTestbed.class, executeOn = EmDeadlockDetectionTestTestbed.MACHINE_ID_linux), size = SizeType.MEDIUM, owner = "bocto01")
    public void testEmDeadlockDetection_2_linux() {
        DeadlockDetectionFlowContext context =
            deserializeFlowContextFromRole(
                EmDeadlockDetectionTestTestbed.EM_DEADLOCK_DETECTION_ROLE_ID_linux,
                DeadlockDetectionRole.ENV_RUN_EM_DEADLOCK_DETECTION,
                DeadlockDetectionFlowContext.class);

        runFlowByMachineId(EmDeadlockDetectionTestTestbed.MACHINE_ID_linux,
            DeadlockDetectionFlow.class, context);
    }

}
