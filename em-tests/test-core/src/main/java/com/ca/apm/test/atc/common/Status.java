package com.ca.apm.test.atc.common;

public enum Status {
    OK("OK"), CAUTION("Caution"), DANGER("Danger");

    private final String statusName;

    private Status(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }

    public static Status getByStatusName(String name) {
        for (Status status : Status.values()) {
            if (status.getStatusName().equals(name)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("Unknown status name: " + name);
    }

    public static boolean contains(String name) {
        try {
            getByStatusName(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}