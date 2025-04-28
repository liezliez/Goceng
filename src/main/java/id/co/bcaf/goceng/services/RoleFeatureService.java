package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.Role;
import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.repositories.RoleFeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleFeatureService {

    private final RoleFeatureRepository roleFeatureRepository;

    public List<RoleFeature> getFeaturesByRole(Role role) {
        return roleFeatureRepository.findAllByRole(role);
    }
}
