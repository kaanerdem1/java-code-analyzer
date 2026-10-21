package com.mock.legacytr;

/** Windows-1254 (eski TR) kaynak — UTF-8 sýký okumada Input length=1 üretir. */
public class CcsBusinessExceptionSample {

    public void failMissingStepRate() {
               //                                         throw new CCSBusinessException( "Seçilen BCH kredisine ait Kademeli Faiz Oraný bilgisi bulunamadý.");
        throw new CreditStepRateService.CCSBusinessException(
                "Seçilen BCH kredisine ait Kademeli Faiz Oraný bilgisi bulunamadý.");
    }
}
