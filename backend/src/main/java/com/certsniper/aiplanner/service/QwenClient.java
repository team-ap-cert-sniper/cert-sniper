package com.certsniper.aiplanner.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Qwen2.5 호출·프롬프트 */
@Component
@RequiredArgsConstructor
public class QwenClient {

    private final RestClient aiRestClient;
}
