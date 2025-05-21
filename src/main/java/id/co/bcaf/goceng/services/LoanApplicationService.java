package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.LoanApplicationDTO;
import id.co.bcaf.goceng.models.Application;
import id.co.bcaf.goceng.models.Loan;
import id.co.bcaf.goceng.repositories.ApplicationRepository;
import id.co.bcaf.goceng.repositories.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        // Fetch all applications for this customer
        List<Application> applications = applicationRepository.findByCustomer_Id(customerId);

        // Fetch all loans for this customer
        List<Loan> loans = loanRepository.findByCustomer_Id(customerId);

        // Map loans by tenor for quick lookup (if multiple loans with same tenor, keep the latest)
        Map<Integer, Loan> loanMap = loans.stream()
                .collect(Collectors.toMap(
                        Loan::getTenor,
                        loan -> loan,
                        (existing, replacement) -> replacement
                ));

        List<LoanApplicationDTO> dtos = new ArrayList<>();

        // For each application, try to find a matching loan with the same tenor
        for (Application app : applications) {
            LoanApplicationDTO dto = new LoanApplicationDTO();

            // Set application fields
            dto.setApplicationId(app.getId());
            dto.setApplicationCreatedAt(app.getCreatedAt());
            dto.setApplicationAmount(app.getAmount());
            dto.setApplicationStatus(app.getStatus().name());
            dto.setApplicationTenor(app.getTenor());
            dto.setPurpose(app.getPurpose());

            // Match loan by tenor (adjust logic if needed)
            Loan matchedLoan = loanMap.get(app.getTenor());

            if (matchedLoan != null) {
                // Set loan fields if loan found
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
