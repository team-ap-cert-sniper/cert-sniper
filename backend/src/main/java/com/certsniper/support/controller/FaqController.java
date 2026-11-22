package com.certsniper.support.controller;

import com.certsniper.support.dto.FaqResponse;
import com.certsniper.support.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "15. 고객센터")
@RestController
@RequestMapping("/api/v1/faqs")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @Operation(summary = "FAQ 목록")
    @GetMapping
    public List<FaqResponse> getFaqs() {
        return faqService.getFaqs();
    }
}
