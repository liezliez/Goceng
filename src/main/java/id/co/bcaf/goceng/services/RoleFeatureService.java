package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.RoleFeature;
import id.co.bcaf.goceng.repositories.RoleFeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleFeatureService {

    @Autowired
    private RoleFeatureRepository roleFeatureRepository;

    public List<RoleFeature> getAllRoleFeatures() {
        return roleFeatureRepository.findAll();
    }

    public RoleFeature getRoleFeatureById(Integer id) {
        return roleFeatureRepository.findById(id).orElse(null);
    }

    public RoleFeature createRoleFeature(RoleFeature roleFeature) {
        return roleFeatureRepository.save(roleFeature);
    }
}
