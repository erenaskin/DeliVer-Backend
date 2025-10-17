package com.deliverapp.backend.dto.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {
    
    @NotBlank(message = "Teslimat adresi gereklidir")
    @Size(max = 500, message = "Teslimat adresi 500 karakterden uzun olamaz")
    private String deliveryAddress;
    
    @NotBlank(message = "Telefon numarası gereklidir")
    @Size(max = 20, message = "Telefon numarası 20 karakterden uzun olamaz")  
    private String phoneNumber;
    
    @Size(max = 1000, message = "Notlar 1000 karakterden uzun olamaz")
    private String notes;
    
    private String paymentMethod; // "CASH", "CARD", "ONLINE" vb.
}