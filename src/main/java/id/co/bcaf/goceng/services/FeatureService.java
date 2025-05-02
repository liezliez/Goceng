package id.co.bcaf.goceng.services;

import id.co.bcaf.goceng.models.Feature;
import id.co.bcaf.goceng.repositories.FeatureRepository;
import id.co.bcaf.goceng.repositories.RoleFeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureService {

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private RoleFeatureRepository roleFeatureRepository;

    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    public Feature getFeatureById(Integer id) {
        return featureRepository.findById(id).orElse(null);
    }

    public Feature createFeature(Feature feature) {
        return featureRepository.save(feature);
    }

    public List<Feature> getFeaturesByRoleName(String roleName) {
        return roleFeatureRepository.findFeaturesByRoleRoleName(roleName);
    }
}
