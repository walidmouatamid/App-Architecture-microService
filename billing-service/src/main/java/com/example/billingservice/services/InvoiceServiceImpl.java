package com.example.billingservice.services;

import com.example.billingservice.dto.InvoiceRequestDTO;
import com.example.billingservice.dto.InvoiceResponseDTO;
import com.example.billingservice.entities.Customer;
import com.example.billingservice.entities.Invoice;
import com.example.billingservice.exceptions.CustomerNotFoundException;
import com.example.billingservice.mappers.InvoiceMapper;
import com.example.billingservice.openfeign.CustomerRestClient;
import com.example.billingservice.repositories.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvoiceServiceImpl implements InvoiceService {
    private InvoiceRepository invoiceRepository;
    private InvoiceMapper invoiceMapper;
    private CustomerRestClient customerRestClient;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, InvoiceMapper invoiceMapper, CustomerRestClient customerRestClient) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.customerRestClient = customerRestClient;
    }

    @Override
    public InvoiceResponseDTO save(InvoiceRequestDTO invoiceRequestDTO) {
        Customer customer=null;
        try {
            customer=customerRestClient.getCustomer(invoiceRequestDTO.getCustomerId());
        }catch (Exception e){
            throw new CustomerNotFoundException("Customer not fount");
        }
        Invoice invoice=invoiceMapper.fromInvoiceDTO(invoiceRequestDTO);
        invoice.setId(UUID.randomUUID().toString());
        invoice.setDate(new Date());


        /*verifier l' intégrité référentielle (si le client existe)*/
        Invoice savedInvoice=invoiceRepository.save(invoice);
        savedInvoice.setCustomer(customer);
        InvoiceResponseDTO invoiceResponseDTO=invoiceMapper.fromInvoice(savedInvoice);
        return invoiceResponseDTO;
    }



    @Override
    public InvoiceResponseDTO getInvoice(String id) {
        Invoice invoice=invoiceRepository.findById(id).get();
        Customer customer=customerRestClient.getCustomer(invoice.getCustomerId());
        invoice.setCustomer(customer);
        return invoiceMapper.fromInvoice(invoice);
    }

    @Override
    public List<InvoiceResponseDTO> invoicesByCustomer(String customerId) {
        List<Invoice> invoices=invoiceRepository.findByCustomerId(customerId);
        for (Invoice invoice : invoices) {
            Customer customer=customerRestClient.getCustomer(invoice.getCustomerId());
            invoice.setCustomer(customer);
        }
        List<InvoiceResponseDTO> invoiceResponseDTOS=
                invoices.stream()
                        .map(inv->invoiceMapper.fromInvoice(inv))
                        .collect(Collectors.toList());
        return invoiceResponseDTOS;
    }

    @Override
    public List<InvoiceResponseDTO> allInvoices() {
        List<Invoice> invoices=invoiceRepository.findAll();
        invoices.forEach(inv->{
            Customer customer=customerRestClient.getCustomer(inv.getCustomerId());
            inv.setCustomer(customer);
        });
        return invoices.stream().map(inv->invoiceMapper.fromInvoice(inv))
                .collect(Collectors.toList());
    }
}
