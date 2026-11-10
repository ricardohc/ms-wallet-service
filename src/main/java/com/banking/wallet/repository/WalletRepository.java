package com.banking.wallet.repository;

import com.banking.wallet.document.WalletDocument;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface WalletRepository extends ReactiveCrudRepository<WalletDocument, String> {

    Mono<WalletDocument> findByWalletUuid(UUID walletUuid);

    Mono<WalletDocument> findByPhoneNumber(String phoneNumber);

    Mono<WalletDocument> findByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);

    Mono<Boolean> existsByPhoneNumber(String phoneNumber);

    Mono<Boolean> existsByDocumentTypeAndDocumentNumber(String documentType, String documentNumber);
}
