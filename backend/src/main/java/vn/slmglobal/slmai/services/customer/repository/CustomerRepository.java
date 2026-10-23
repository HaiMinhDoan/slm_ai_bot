package vn.slmglobal.slmai.services.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.customer.entity.Customer;

import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /** khach_hang.sdt là UNIQUE: kiểm tra trước khi gán số cho khách khác */
    boolean existsByPhoneAndIdNot(String phone, UUID id);
}
