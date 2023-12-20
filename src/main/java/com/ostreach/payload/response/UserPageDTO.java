package com.ostreach.payload.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserPageDTO {

    private int pageNo;
    private  int pageSize;
    private Boolean lastPage;
    private List<UserResponse> userResponseList;

}
