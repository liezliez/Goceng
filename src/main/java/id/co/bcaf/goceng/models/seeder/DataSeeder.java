//package id.co.bcaf.goceng.models.seeder;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import id.co.bcaf.goceng.models.Branch;
//import id.co.bcaf.goceng.models.Role;
//import id.co.bcaf.goceng.models.User;
//import id.co.bcaf.goceng.repositories.BranchRepository;
//import id.co.bcaf.goceng.repositories.RoleRepository;
//import id.co.bcaf.goceng.repositories.UserRepository;
//
//import jakarta.transaction.Transactional;
//
//import java.util.List;
//import java.util.UUID;
//
//@Component
//public class DataSeeder implements CommandLineRunner {
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private BranchRepository branchRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Transactional
//    @Override
//    public void run(String... args) {
//        seedRoles();
//        seedBranches();
//        seedUsers();
//    }
//
//    private void seedRoles() {
//        if (roleRepository.count() == 0) {
//            roleRepository.saveAll(List.of(
//                    new Role("ROLE_SUPERADMIN"),
//                    new Role("ROLE_CUSTOMER"),
//                    new Role("ROLE_MARKETING"),
//                    new Role("ROLE_BRANCH_MANAGER"),
//                    new Role("ROLE_BACK_OFFICE")
//            ));
//        }
//    }
//
//    private void seedBranches() {
//        if (branchRepository.count() == 0) {
//            branchRepository.saveAll(List.of(
//                    new Branch(UUID.fromString("42F47C49-01B9-423A-AA56-D160F8196641"), "Jakarta Pusat", "Jl. Merdeka No.1", "Jakarta", "DKI Jakarta", -6.2088, 106.8456),
//                    new Branch(UUID.fromString("B43A94D7-4C5E-4F2D-8A7B-02477F36D65F"), "Bandung", "Jl. Asia Afrika No.22", "Bandung", "Jawa Barat", -6.9175, 107.6191),
//                    new Branch(UUID.fromString("31C93F27-70A9-44A0-A922-D0BC906A47A1"), "Surabaya", "Jl. Pemuda No.17", "Surabaya", "Jawa Timur", -7.2504, 112.7688),
//                    new Branch(UUID.fromString("9B9A08D0-67C7-4783-B5F0-35E0E1A3D0D0"), "Medan", "Jl. Gatot Subroto No.5", "Medan", "Sumatera Utara", 3.5952, 98.6722),
//                    new Branch(UUID.fromString("F4A8DA3E-0458-4694-B8F3-1D17E7B26937"), "Yogyakarta", "Jl. Malioboro No.11", "Yogyakarta", "DI Yogyakarta", -7.7956, 110.3695),
//                    new Branch(UUID.fromString("84936B2B-960F-41A0-BE4B-B3C76D8A1705"), "Makassar", "Jl. Sudirman No.9", "Makassar", "Sulawesi Selatan", -5.1477, 119.4328),
//                    new Branch(UUID.fromString("A4C56E9D-07F2-4D6D-B6F1-5A4F6DC5975D"), "Denpasar", "Jl. Teuku Umar No.6", "Denpasar", "Bali", -8.4095, 115.1889),
//                    new Branch(UUID.fromString("D699DB1D-F8E4-42B9-974E-F256E5F05339"), "Semarang", "Jl. Pandanaran No.3", "Semarang", "Jawa Tengah", -6.9669, 110.4194),
//                    new Branch(UUID.fromString("FD7B1328-60D9-44B3-9E93-0C2D126BC6EF"), "Balikpapan", "Jl. Jendral Sudirman No.2", "Balikpapan", "Kalimantan Timur", -1.2654, 116.9026),
//                    new Branch(UUID.fromString("B8828AE4-2266-4634-BB2A-4B8C870B32F5"), "Padang", "Jl. Ahmad Yani No.7", "Padang", "Sumatera Barat", -0.9470, 100.4170)
//            ));
//        }
//    }
//
//    private void seedUsers() {
//        if (userRepository.count() == 0) {
//            Branch bandung = branchRepository.findById(UUID.fromString("B43A94D7-4C5E-4F2D-8A7B-02477F36D65F")).orElseThrow();
//            userRepository.saveAll(List.of(
//                    new User(UUID.fromString("7E008E63-4B4F-4CF7-8E41-813593066A7A"), "ACTIVE", "superadmin@example.org", "Wellington5", "$2a$10$lWw4y4z9aYLIcAMfJzSaeOKc2D8euLhTo5Jub6fOyOMTnum8PkIRy", bandung, roleRepository.findByRoleName("ROLE_SUPERADMIN").orElseThrow()),
//                    new User(UUID.fromString("2EBDCEF1-8455-4C01-8FDC-869F6F84C43B"), "ACTIVE", "customer@example.net", "Raphaelle.Heidenreich", "$2a$10$5WDG0VyLF3hC48ETeDJxo.1eBkRe6juebB9Lw4ZbkwJR5ihuBRgBi", bandung, roleRepository.findByRoleName("ROLE_CUSTOMER").orElseThrow()),
//                    new User(UUID.fromString("2A733039-184A-4B75-A92E-9ED920481C12"), "ACTIVE", "marketing@example.com", "Blaze.Brekke31", "$2a$10$PULiXRDwgBxIRvePMqMfcu3chjEodn4evV7S2PzMictKiEGciAu2e", bandung, roleRepository.findByRoleName("ROLE_MARKETING").orElseThrow()),
//                    new User(UUID.fromString("6E8A1D43-BDA8-4E45-8655-D276AD0CF0C0"), "ACTIVE", "bm@example.org", "Serena.Gutkowski", "$2a$10$Nj/izbUcFCXiUzxWf2FFL.oQoU7kpx.hdOBGyna8eNhWXGJdkCJiO", bandung, roleRepository.findByRoleName("ROLE_BRANCH_MANAGER").orElseThrow()),
//                    new User(UUID.fromString("2871926D-1E9C-4E3A-9915-F5A66C2EBC25"), "ACTIVE", "bo@example.com", "Taurean.Wyman91", "$2a$10$db8Q10H2o5NgYoMFWI4/SeRYz2Y/DYcPOZOS6dZ4XbSkX2TjQdtTe", bandung, roleRepository.findByRoleName("ROLE_BACK_OFFICE").orElseThrow())
//            ));
//        }
//    }
//}
