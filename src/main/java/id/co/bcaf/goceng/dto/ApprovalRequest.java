package id.co.bcaf.goceng.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ApprovalRequest {
    @JsonProperty("isApproved")
    private boolean approved;
    private String note;
}
