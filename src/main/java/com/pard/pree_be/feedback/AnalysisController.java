package com.pard.pree_be.feedback.analysis.controller;

import com.pard.pree_be.feedback.analysis.service.AnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

}