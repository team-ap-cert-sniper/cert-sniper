package com.certsniper.global.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/** 서식 본문에서 허용 태그만 남긴다 (XSS 방지) */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.none()
            .addTags("p", "br", "b", "strong", "i", "em", "u", "s", "ul", "ol", "li");

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        return html == null ? null : Jsoup.clean(html, SAFELIST);
    }

    public static String toText(String html) {
        return html == null ? null : Jsoup.parse(html).text();
    }
}
