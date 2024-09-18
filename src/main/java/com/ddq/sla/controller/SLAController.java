package com.ddq.sla.controller;

import com.ddq.sla.service.SLACalculationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class SLAController {

    private final SLACalculationService slaCalculationService;

    public SLAController(SLACalculationService slaCalculationService) {
        this.slaCalculationService = slaCalculationService;
    }

    @GetMapping("/calculate-sla")
    public String calculateSLA(@RequestParam("triggerTime") LocalDateTime triggerTime,
                                      @RequestParam("slaType") String slaType,
                                      @RequestParam("slaValue") double slaValue) {
        return slaCalculationService.calculateSLAExpiration(triggerTime, slaType, slaValue);
    }
}
