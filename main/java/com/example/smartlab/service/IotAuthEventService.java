package com.example.smartlab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.smartlab.entity.IotAuthEvent;
import com.example.smartlab.vo.AuthEventDetailVO;
import java.util.List;

public interface IotAuthEventService extends IService<IotAuthEvent> {

    List<IotAuthEvent> listLatest(int limit);

    List<IotAuthEvent> listGrantedEvents();

    List<AuthEventDetailVO> listAuthEventDetails(String authResult);

    AuthEventDetailVO getLatestDetail();
}
