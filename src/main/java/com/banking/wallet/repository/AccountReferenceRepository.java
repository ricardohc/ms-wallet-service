package com.banking.wallet.repository;

import com.banking.wallet.document.AccountReferenceDocument;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountReferenceRepository
        extends ReactiveCrudRepository<AccountReferenceDocument, String> {
}