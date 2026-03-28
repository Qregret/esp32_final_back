package com.example.smartlab.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.smartlab.entity.IotRelayAction;
import com.example.smartlab.mapper.IotRelayActionMapper;
import com.example.smartlab.service.IotRelayActionService;
import com.example.smartlab.support.AppTime;
import org.springframework.stereotype.Service;

@Service
public class IotRelayActionServiceImpl extends ServiceImpl<IotRelayActionMapper, IotRelayAction>
        implements IotRelayActionService {

    @Override
    public IotRelayAction recordAction(Long seatId, Long relayDeviceId, Integer relayChannel, String actionType,
                                       String actionSource, String actionResult, String remark) {
        IotRelayAction action = new IotRelayAction();
        action.setSeatId(seatId);
        action.setRelayDeviceId(relayDeviceId);
        action.setRelayChannel(relayChannel);
        action.setActionType(actionType);
        action.setActionSource(actionSource);
        action.setAction(resolveAction(actionType));
        action.setTriggerSource(resolveTriggerSource(actionSource));
        action.setOperatorName(null);
        action.setActionResult(resolveActionResult(actionResult));
        action.setActionMessage(remark);
        action.setExecutedAt(AppTime.now());
        action.setCreatedAt(AppTime.now());
        save(action);
        return action;
    }

    private String resolveAction(String actionType) {
        if ("power_on".equalsIgnoreCase(actionType)) {
            return "on";
        }
        if ("power_off".equalsIgnoreCase(actionType)) {
            return "off";
        }
        return "off";
    }

    private String resolveTriggerSource(String actionSource) {
        if (actionSource == null || actionSource.isBlank()) {
            return "system";
        }
        String value = actionSource.trim().toLowerCase();
        if ("auth_success".equals(value)) {
            return "auth_success";
        }
        if ("manual".equals(value) || "manual_control".equals(value)) {
            return "manual";
        }
        return "system";
    }

    private String resolveActionResult(String actionResult) {
        return "failed".equalsIgnoreCase(actionResult) ? "failed" : "success";
    }
}
