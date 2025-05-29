package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.dto.BranchRequest;
import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.repositories.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public Branch createBranch(BranchRequest request) {
        Branch branch = new Branch();
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setCity(request.getCity());
        branch.setProvince(request.getProvince());
        branch.setLatitude(request.getLatitude());
        branch.setLongitude(request.getLongitude());

        return branchRepository.save(branch);
    }

    public Branch updateBranch(UUID id, BranchRequest request) {
        Branch existing = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        existing.setName(request.getName());
        existing.setAddress(request.getAddress());
        existing.setCity(request.getCity());
        existing.setProvince(request.getProvince());
        existing.setLatitude(request.getLatitude());
        existing.setLongitude(request.getLongitude());

        return branchRepository.save(existing);
    }

    public Branch getBranchById(UUID id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));
    }

    public void deleteBranch(UUID id) {
        if (!branchRepository.existsById(id)) {
            throw new RuntimeException("Branch not found");
        }
        branchRepository.deleteById(id);
    }

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }
}
