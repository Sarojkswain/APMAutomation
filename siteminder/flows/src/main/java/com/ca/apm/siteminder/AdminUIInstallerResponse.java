package com.ca.apm.siteminder;

public enum AdminUIInstallerResponse {

    SUCCESS(0),
    SUCCESS_WITH_RESULT_1(1),
    PARTIAL_SUCCESS(2),
    ERROR_IN_RESPONSE_FILE(403);

    private int exitStatus;

    AdminUIInstallerResponse(int exitStatus) {
        this.exitStatus = exitStatus;
    }

    public int exitStatus() {
        return exitStatus;
    }

    public static AdminUIInstallerResponse fromExitStatus(int exitStatus) {
        for (AdminUIInstallerResponse response : values()) {
            if (response.exitStatus == exitStatus) {
                return response;
            }
        }

        return null;
    }

}
