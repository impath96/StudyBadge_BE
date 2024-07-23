package com.tenten.studybadge.notification.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class RelatedUrl {
    private String url;

    protected RelatedUrl() {}

    public RelatedUrl(String url) {
      this.url = url;
    }
}
