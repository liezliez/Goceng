package id.co.bcaf.goceng.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalRequest {
    private boolean isApproved;
    private String note;
}
