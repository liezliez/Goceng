    package id.co.bcaf.goceng.dto;

    import lombok.Getter;
    import lombok.Setter;

    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.util.UUID;

    @Getter
    @Setter
    public class CustomerResponse {
        private UUID idCustomer;
        private UUID idUser;
        private String name;
        private String nik;
        private LocalDate dateOfBirth;
        private String placeOfBirth;
        private String telpNo;
        private String address;
        private String motherMaidenName;
        private String occupation;
        private BigDecimal salary;
        private String homeOwnershipStatus;
        private String emergencyCall;
        private BigDecimal creditLimit;
        private String accountNo;
        private String urlKtp;
        private String urlSelfie;
        private UUID plafonId;
        private String plafonType;
        private BigDecimal plafonAmount;
        private BigDecimal interestRate;



    }
