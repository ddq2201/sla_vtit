package com.ddq.sla.service;

import java.time.LocalDateTime;

public interface SLACalculationService {
    String calculateSLAExpiration(LocalDateTime triggerTime, String slaType, double slaValue);
}
