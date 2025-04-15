package id.co.bcaf.goceng.enums;

public enum ApplicationStatus {
    PENDING_MARKETING,
    PENDING_BRANCH_MANAGER,
    PENDING_BACK_OFFICE,
    APPROVED,
    REJECTED,
    REJECTED_MARKETING,   // Added detailed rejection status
    REJECTED_BRANCH_MANAGER,   // Added detailed rejection status
    REJECTED_BACK_OFFICE   // Added detailed rejection status
}
