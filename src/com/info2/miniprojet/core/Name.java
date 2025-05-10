package com.info2.miniprojet.core;

import java.util.List;

public record Name(String id, String originalName, List<String> processedTokens) {
}
