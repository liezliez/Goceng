package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.LoanApplicationDTO;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Loan;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoanApplicationService {

    private final ApplicationRepository applicationRepository;
    private final LoanRepository loanRepository;

    public LoanApplicationService(ApplicationRepository applicationRepository, LoanRepository loanRepository) {
        this.applicationRepository = applicationRepository;
        this.loanRepository = loanRepository;
    }

    public List<LoanApplicationDTO> getLoanApplication(UUID customerId) {
        // Fetch all applications by customer
        List<Application> applications = applicationRepository.findByCustomer_Id(customerId);

        // Fetch all loans linked to these applications
        List<Loan> loans = loanRepository.findByApplication_Customer_Id(customerId);

        // Map loans by application ID
        Map<UUID, Loan> loanMap = loans.stream()
                .filter(loan -> loan.getApplication() != null)
                .collect(Collectors.toMap(
                        loan -> loan.getApplication().getId(),
                        loan -> loan
                ));

        List<LoanApplicationDTO> dtos = new ArrayList<>();

        for (Application app : applications) {
            LoanApplicationDTO dto = new LoanApplicationDTO();

            // Set application fields
            dto.setApplicationId(app.getId());
            dto.setApplicationCreatedAt(app.getCreatedAt());
            dto.setApplicationAmount(app.getAmount());
            dto.setApplicationStatus(app.getStatus().name());
            dto.setApplicationTenor(app.getTenor());
            dto.setPurpose(app.getPurpose());

            // Get the loan associated with this application (if any)
            Loan matchedLoan = loanMap.get(app.getId());

            if (matchedLoan != null) {
                dto.setLoanId(matchedLoan.getId());
                dto.setLoanAmount(matchedLoan.getLoanAmount());
                dto.setLoanTenor(matchedLoan.getTenor());
                dto.setLoanInstallment(matchedLoan.getInstallment());
                dto.setLoanInterestRate(matchedLoan.getInterestRate());
                dto.setLoanStatus(matchedLoan.getStatus().name());
                dto.setLoanDisbursedAt(matchedLoan.getDisbursedAt());
            }

            dtos.add(dto);
        }

        return dtos;
    }
}
