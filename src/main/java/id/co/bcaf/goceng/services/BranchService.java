package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.Branch;
import id.co.bcaf.goceng.repositories.BranchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }
}
