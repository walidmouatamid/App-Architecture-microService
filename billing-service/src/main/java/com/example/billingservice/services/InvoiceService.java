package com.example.billingservice.services;


import com.example.billingservice.dto.InvoiceRequestDTO;
import com.example.billingservice.dto.InvoiceResponseDTO;

import java.util.List;

public interface InvoiceService {
     InvoiceResponseDTO save(InvoiceRequestDTO invoiceRequestDTO);
     InvoiceResponseDTO getInvoice(String id);
     List<InvoiceResponseDTO> invoicesByCustomer(String CustomerId);
     List<InvoiceResponseDTO> allInvoices();

}
