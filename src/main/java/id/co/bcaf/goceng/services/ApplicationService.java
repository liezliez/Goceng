package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.ApplicationRequest;
import id.co.bcaf.goceng.dto.ApplicationResponse;
import id.co.bcaf.goceng.enums.ApplicationStatus;
import id.co.bcaf.goceng.enums.ApprovalRole;
import id.co.bcaf.goceng.exceptions.*;
import id.co.bcaf.goceng.models.*;
import id.co.bcaf.goceng.repositories.*;
import id.co.bcaf.goceng.securities.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepo;
    private final CustomerRepository customerRepo;
    private final UserRepository userRepo;
    private final EmployeeRepository employeeRepo;
    private final BranchRepository branchRepo;
    private final ApplicationLogRepository applicationLogRepo;
    private final LoanService loanService;
    private final PlafonRepository plafonRepo;

    private final NotificationService notificationService;
    private final EmailService emailService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            logger.warn("Authentication is null or not authenticated");
            throw new UserNotAuthenticatedException("User is not authenticated");
        }
        String email = auth.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("No user found for email: {}", email);
                    return new UserNotAuthenticatedException("User not found: " + email);
                });
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequest req) {
        Customer customer = customerRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        if (customer.getCreditLimit() == null) {
            throw new LoanAmountExceededException("Customer credit limit is not set.");
        }

        // Use customer's credit limit as maximum allowed amount
        if (req.getAmount().compareTo(customer.getCreditLimit()) > 0) {
            throw new LoanAmountExceededException("Requested loan amount exceeds customer's credit limit.");
        }

        checkForPendingApplications(customer);
        validateCustomerDataCompleted(customer);

        Branch branch = branchRepo.findById(req.getBranchId())
                .orElseThrow(() -> new BranchNotFoundException("Branch not found"));

        Plafon plafon = plafonRepo.findFirstByOrderByPlafonAmountAsc()
                .orElseThrow(() -> new RuntimeException("No loan limit available"));

        // Removed old validateLoanAmount call here

        logger.info("Creating application with branchId: {}", req.getBranchId());

        Application app = Application.builder()
                .customer(customer)
                .amount(req.getAmount())
                .purpose(req.getPurpose())
                .tenor(req.getTenor())
                .branch(branch)
                .status(ApplicationStatus.PENDING_MARKETING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .plafon(plafon)
                .interestRate(plafon.getInterestRate())
                .plafonType(plafon.getPlafonType())
                .plafonLimit(plafon.getPlafonAmount())
                .build();

        return convertToResponse(applicationRepo.save(app));
    }

    private void validateCustomerDataCompleted(Customer customer) {
        boolean incomplete = Stream.of(
                customer.getName(), customer.getNik(), customer.getPlaceOfBirth(),
                customer.getTelpNo(), customer.getAddress(), customer.getMotherMaidenName(),
                customer.getOccupation(), customer.getHomeOwnershipStatus(),
                customer.getEmergencyCall(), customer.getAccountNo(),
                customer.getUrlKtp(), customer.getUrlSelfie()
        ).anyMatch(this::isEmpty) || customer.getDateOfBirth() == null || customer.getSalary() == null || customer.getCreditLimit() == null;

        if (incomplete) {
            throw new IncompleteCustomerDataException("Customer data is incomplete. Please complete all required fields before applying.");
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void checkForPendingApplications(Customer customer) {
        boolean hasPending = applicationRepo.existsByCustomerAndStatusIn(customer, List.of(
                ApplicationStatus.PENDING_MARKETING,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApplicationStatus.PENDING_BACK_OFFICE
        ));
        if (hasPending) {
            throw new ApplicationAlreadyActiveException("Customer already has an active application");
        }
    }

    // Removed old validateLoanAmount method entirely

    public List<ApplicationResponse> getAllApplications() {
        return applicationRepo.findAll().stream()
                .map(this::convertToResponse)
                .toList();
    }

    public ApplicationResponse getApplicationById(UUID id) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        return convertToResponse(app);
    }

    public List<Application> getApplicationsByCurrentUserBranch() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof CustomUserDetails userDetails) {
                User user = userRepo.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (user.getBranch() == null) {
                    throw new RuntimeException("User branch not assigned");
                }

                UUID branchId = user.getBranch().getId();
                return applicationRepo.findByBranch_Id(branchId);
            }
        }
        return List.of();
    }

    public List<Application> getApplicationsByCustomer(UUID id) {
        return Stream.of(
                        applicationRepo.findByCustomer_Id(id)
                ).flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Application> getApplicationsByCustomerOrUserId(UUID id) {
        return Stream.of(
                        applicationRepo.findByCustomer_Id(id),
                        applicationRepo.findByCustomer_User_idUser(id)
                ).flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse approveApplication(UUID id, boolean isApproved, ApplicationStatus currentStatus,
                                                  ApplicationStatus nextStatus, ApprovalRole role, String note) {
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        validateCurrentStatus(app, currentStatus);

        User approver = getCurrentUser();
        validateRolePermission(role, approver);
        validateBranch(approver, app);

        ApplicationStatus beforeStatus = app.getStatus();
        setApprovalFields(app, approver, role, note);
        ApplicationStatus newStatus = isApproved ? nextStatus : getRejectedStatus(role);
        app.setStatus(newStatus);
        app.setUpdatedAt(LocalDateTime.now());

        // Subtract customer's credit limit if application is finally approved
        if (isApproved && newStatus == ApplicationStatus.APPROVED) {
            processLoanCreation(app);
            subtractCustomerCreditLimit(app.getCustomer(), app.getAmount());

            // Notifications
            sendApprovalNotification(app);
        }

        logApplicationChange(app, approver, isApproved ? "APPROVE" : "REJECT", isApproved, beforeStatus, newStatus);
        return convertToResponse(applicationRepo.save(app));
    }

    private void subtractCustomerCreditLimit(Customer customer, BigDecimal amount) {
        BigDecimal currentLimit = Optional.ofNullable(customer.getCreditLimit()).orElse(BigDecimal.ZERO);
        BigDecimal newLimit = currentLimit.subtract(amount);
        if (newLimit.compareTo(BigDecimal.ZERO) < 0) {
            logger.warn("Customer credit limit would become negative for customerId={}", customer.getId());
            throw new LoanAmountExceededException("Customer credit limit cannot be negative.");
        }
        customer.setCreditLimit(newLimit);
        customerRepo.save(customer);
        logger.info("Subtracted {} from customer {} credit limit. New limit: {}", amount, customer.getId(), newLimit);
    }

    @Transactional
    public ApplicationResponse rejectApplication(UUID id, ApprovalRole role, String note) {
        ApplicationStatus currentStatus = switch (role) {
            case MARKETING -> ApplicationStatus.PENDING_MARKETING;
            case BRANCH_MANAGER -> ApplicationStatus.PENDING_BRANCH_MANAGER;
            case BACK_OFFICE -> ApplicationStatus.PENDING_BACK_OFFICE;
        };
        return approveApplication(id, false, currentStatus, null, role, note);
    }

    private void validateCurrentStatus(Application app, ApplicationStatus expectedStatus) {
        if (expectedStatus != null && app.getStatus() != expectedStatus) {
            throw new InvalidApplicationStatusException("Expected status: " + expectedStatus);
        }
    }

    private void validateRolePermission(ApprovalRole role, User approver) {
        String requiredRole = "ROLE_" + role.name();
        if (!requiredRole.equals(approver.getRole().getRoleName())) {
            throw new InsufficientPermissionsException("Required role: " + requiredRole);
        }
    }

    private void validateBranch(User approver, Application app) {
        Employee emp = employeeRepo.findByUser_IdUser(approver.getIdUser())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        if (!emp.getBranch().equals(app.getBranch())) {
            throw new UnauthorizedBranchException("Unauthorized to process application from another branch");
        }
    }

    private void setApprovalFields(Application app, User approver, ApprovalRole role, String note) {
        LocalDateTime now = LocalDateTime.now();
        String nip = getNipFromApprover(approver);
        switch (role) {
            case MARKETING -> {
                app.setMarketingAssigned(approver);
                app.setMarketingAssignedTime(now);
                app.setNipMarketing(nip);
                app.setMarketingNote(note);
            }
            case BRANCH_MANAGER -> {
                app.setBranchManagerAssigned(approver);
                app.setBranchManagerAssignedTime(now);
                app.setNipBranchManager(nip);
                app.setBranchManagerNote(note);
            }
            case BACK_OFFICE -> {
                app.setBackOfficeAssigned(approver);
                app.setBackOfficeAssignedTime(now);
                app.setNipBackOffice(nip);
                app.setBackOfficeNote(note);
            }
        }
    }

    private ApplicationStatus getRejectedStatus(ApprovalRole role) {
        return ApplicationStatus.valueOf("REJECTED_" + role.name());
    }

    private void processLoanCreation(Application app) {
        try {
            loanService.createLoanFromApprovedApplication(app, app.getCustomer(), app.getInterestRate(), app.getTenor());
            logger.info("Loan created for application ID: {}", app.getId());
        } catch (Exception e) {
            logger.error("Failed to create loan for application ID {}: {}", app.getId(), e.getMessage(), e);
            throw new RuntimeException("Loan creation failed: " + e.getMessage());
        }
    }

    private void logApplicationChange(Application app, User approver, String action, boolean isApproved,
                                      ApplicationStatus beforeStatus, ApplicationStatus afterStatus) {
        applicationLogRepo.save(ApplicationLog.builder()
                .applicationId(app.getId())
                .action(action)
                .changedBy(approver.getUsername())
                .timestamp(LocalDateTime.now())
                .beforeStatus(beforeStatus.name())
                .afterStatus(afterStatus.name())
                .details(isApproved ? "Application approved" : "Application rejected")
                .build());
    }

    private ApplicationResponse convertToResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .customerId(app.getCustomer().getId())
                .customerName(app.getCustomer().getUser().getName())
                .amount(app.getAmount())
                .purpose(app.getPurpose())
                .status(app.getStatus().name())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .marketingAssignedName(getName(app.getMarketingAssigned()))
                .branchManagerAssignedName(getName(app.getBranchManagerAssigned()))
                .backOfficeAssignedName(getName(app.getBackOfficeAssigned()))
                .marketingAssignedTime(app.getMarketingAssignedTime())
                .branchManagerAssignedTime(app.getBranchManagerAssignedTime())
                .backOfficeAssignedTime(app.getBackOfficeAssignedTime())
                .tenor(app.getTenor())
                .limitType(app.getPlafonType())
                .limitAmount(app.getPlafonLimit())
                .interestRate(app.getInterestRate())
                .noteMarketing(app.getMarketingNote())
                .noteBranchManager(app.getBranchManagerNote())
                .noteBackOffice(app.getBackOfficeNote())
                .nipMarketing(app.getNipMarketing())
                .nipBranchManager(app.getNipBranchManager())
                .nipBackOffice(app.getNipBackOffice())
                .build();
    }

    private String getName(User user) {
        return user != null ? user.getName() : null;
    }

    private String getNipFromApprover(User approver) {
        return employeeRepo.findByUser_IdUser(approver.getIdUser())
                .map(Employee::getNIP)
                .orElse("");
    }

    public ApplicationResponse marketingApprove(UUID id, boolean isApproved, String note) {
        return approveApplication(id, isApproved,
                ApplicationStatus.PENDING_MARKETING,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApprovalRole.MARKETING,
                note);
    }

    public ApplicationResponse branchManagerApprove(UUID id, boolean isApproved, String note) {
        return approveApplication(id, isApproved,
                ApplicationStatus.PENDING_BRANCH_MANAGER,
                ApplicationStatus.PENDING_BACK_OFFICE,
                ApprovalRole.BRANCH_MANAGER,
                note);
    }

    public ApplicationResponse backOfficeApprove(UUID id, boolean isApproved, String note) {
        return approveApplication(id, isApproved,
                ApplicationStatus.PENDING_BACK_OFFICE,
                ApplicationStatus.APPROVED,
                ApprovalRole.BACK_OFFICE,
                note);
    }

    private void sendApprovalNotification(Application app) {
        try {
            User customerUser = app.getCustomer().getUser();

            String notificationTitle = "Application Approved";
            String notificationMessage = "Your loan application has been approved.";

            notificationService.sendNotification(customerUser.getFcmToken(), notificationTitle, notificationMessage);

            String emailSubject = "Loan Application Approved";
            String emailBody = "Dear " + customerUser.getName() + ",\n\n" +
                    "Congratulations! Your loan application with ID " + app.getId() + " has been approved.\n\n" +
                    "Thank you for choosing us.\n\nBest regards,\nGoceng App";

            emailService.sendEmail(customerUser.getEmail(), emailSubject, emailBody);
        } catch (Exception e) {
            logger.error("Failed to send approval notifications: {}", e.getMessage(), e);
        }
    }

    // Testing purpose

    @Transactional
    public ApplicationResponse autoApproveApplication(UUID applicationId, String note) {
        Application app = applicationRepo.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));

        if (app.getStatus() != ApplicationStatus.PENDING_MARKETING) {
            throw new InvalidApplicationStatusException("Application must start at PENDING_MARKETING");
        }

        // Fake current user as system/test admin for audit logs
        User systemUser = getCurrentUser();

        // Marketing approval
        setApprovalFields(app, systemUser, ApprovalRole.MARKETING, note);
        app.setStatus(ApplicationStatus.PENDING_BRANCH_MANAGER);

        // Branch manager approval
        setApprovalFields(app, systemUser, ApprovalRole.BRANCH_MANAGER, note);
        app.setStatus(ApplicationStatus.PENDING_BACK_OFFICE);

        // Back office approval and final approval
        setApprovalFields(app, systemUser, ApprovalRole.BACK_OFFICE, note);
        app.setStatus(ApplicationStatus.APPROVED);
        app.setUpdatedAt(LocalDateTime.now());

        // Create loan and send notifications
        processLoanCreation(app);
        sendApprovalNotification(app);

        logApplicationChange(app, systemUser, "AUTO_APPROVE", true,
                ApplicationStatus.PENDING_MARKETING, ApplicationStatus.APPROVED);

        return convertToResponse(applicationRepo.save(app));
    }

}
