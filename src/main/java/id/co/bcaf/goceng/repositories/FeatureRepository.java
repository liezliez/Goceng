package id.co.bcaf.goceng.repositories;

import id.co.bcaf.goceng.models.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FeatureRepository extends JpaRepository<Feature, Integer> {
    Optional<Feature> findByFeatureName(String featureName);
}
