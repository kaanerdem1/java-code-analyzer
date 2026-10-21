package com.mock.legacytr;

/**
 * UTF-8 Türkçe örnek — kademeli faiz / BCH kredi metinleri.
 */
public class CreditStepRateService {

    public static class CCSBusinessException extends RuntimeException {
        public CCSBusinessException(String message) {
            super(message);
        }
    }

    /**
     * Seçilen BCH kredisine ait kademeli faiz oranı kontrolü.
     */
    public void validateStepRate(String creditId) {
        if (creditId == null || creditId.isBlank()) {
            // Seçilen BCH kredisine ait Kademeli Faiz Oranı bilgisi bulunamadı.
            throw new CCSBusinessException(
                    "Seçilen BCH kredisine ait Kademeli Faiz Oranı bilgisi bulunamadı.");
        }
        if (creditId.length() < 3) {
            throw new CCSBusinessException("Geçersiz kredi numarası: şğüıöç ŞĞÜİÖÇ");
        }
    }
}
