# 📚 DeliVer Backend API Dokümantasyonu

Bu dokümantasyon, DeliVer Backend API'sinin tüm endpoint'lerini ve işlevlerini detaylı olarak açıklar.

## 📋 İçindekiler

1. [Genel Bilgiler](#genel-bilgiler)
2. [Kimlik Doğrulama (Authentication)](#kimlik-doğrulama-authentication)
3. [Kullanıcı İşlemleri (Users)](#kullanıcı-işlemleri-users)
4. [Servis İşlemleri (Services)](#servis-işlemleri-services)
5. [Kategori İşlemleri (Categories)](#kategori-işlemleri-categories)
6. [Ürün İşlemleri (Products)](#ürün-işlemleri-products)
7. [Sepet İşlemleri (Cart)](#sepet-işlemleri-cart)
8. [Sipariş İşlemleri (Orders)](#sipariş-işlemleri-orders)
9. [Hata Kodları](#hata-kodları)
10. [Örnek Kullanım](#örnek-kullanım)

---

## 🔧 Genel Bilgiler

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **Kimlik Doğrulama**: JWT Bearer Token
- **API Versiyonu**: v1

### Güvenlik
Korumalı endpoint'ler için `Authorization` header'ı gereklidir:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## 🔐 Kimlik Doğrulama (Authentication)

### Base URL: `/api/auth`

#### 1. Kullanıcı Kaydı
```http
POST /api/auth/register
```

**İstek Gövdesi:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Yanıt:**
```json
{
  "token": "JWT_TOKEN",
  "refreshToken": "REFRESH_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

**Açıklama:** Yeni kullanıcı kaydı oluşturur ve JWT token döner.

#### 2. Giriş Yapma
```http
POST /api/auth/login
```

**İstek Gövdesi:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Yanıt:**
```json
{
  "token": "JWT_TOKEN",
  "refreshToken": "REFRESH_TOKEN", 
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

**Açıklama:** Kullanıcı girişi yapar ve JWT token döner.

#### 3. Token Yenileme
```http
POST /api/auth/refresh
```

**İstek Gövdesi:**
```json
{
  "refreshToken": "REFRESH_TOKEN"
}
```

**Yanıt:**
```json
{
  "token": "NEW_JWT_TOKEN",
  "refreshToken": "NEW_REFRESH_TOKEN",
  "tokenType": "Bearer", 
  "expiresIn": 86400
}
```

**Açıklama:** Süresi dolmuş JWT token'ı yeniler.

#### 4. E-posta Doğrulama Kodu Gönder
```http
POST /api/auth/verify-email/send
```

**İstek Gövdesi:**
```json
{
  "email": "string"
}
```

**Yanıt:**
```json
"Doğrulama kodu gönderildi: 123456"
```

**Açıklama:** E-posta adresine doğrulama kodu gönderir.

#### 5. E-posta Doğrulama
```http
POST /api/auth/verify-email/confirm?code=123456
```

**Query Parametreleri:**
- `code` (string, gerekli): Doğrulama kodu

**Yanıt:**
```json
"E-posta doğrulandı."
```

**Açıklama:** E-posta adresini doğrular.

#### 6. Şifre Sıfırlama Kodu Gönder
```http
POST /api/auth/reset-password/send
```

**İstek Gövdesi:**
```json
{
  "email": "string"
}
```

**Yanıt:**
```json
"Şifre sıfırlama kodu gönderildi: 123456"
```

**Açıklama:** Şifre sıfırlama kodu gönderir.

#### 7. Şifre Sıfırlama
```http
POST /api/auth/reset-password/confirm?code=123456&newPassword=newpass123
```

**Query Parametreleri:**
- `code` (string, gerekli): Doğrulama kodu
- `newPassword` (string, gerekli): Yeni şifre

**İstek Gövdesi:**
```json
{
  "email": "string"
}
```

**Yanıt:**
```json
"Şifre başarıyla güncellendi."
```

**Açıklama:** Kullanıcının şifresini sıfırlar.

#### 8. Çıkış Yapma
```http
POST /api/auth/logout
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Yanıt:**
```json
"Çıkış yapıldı, token iptal edildi."
```

**Açıklama:** Kullanıcıyı çıkış yapar ve token'ı kara listeye alır.

---

## 👤 Kullanıcı İşlemleri (Users)

### Base URL: `/api/users`

#### 1. Mevcut Kullanıcı Bilgilerini Getir
```http
GET /api/users/me
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Yanıt:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "emailVerified": true,
  "createdAt": "2025-10-10T10:00:00"
}
```

**Açıklama:** Giriş yapmış kullanıcının bilgilerini döner.

#### 2. Kullanıcı Bilgilerini ID ile Getir
```http
GET /api/users/{id}
```

**Path Parametreleri:**
- `id` (Long, gerekli): Kullanıcı ID'si

**Yanıt:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "emailVerified": true,
  "createdAt": "2025-10-10T10:00:00"
}
```

**Açıklama:** Belirtilen ID'ye sahip kullanıcının bilgilerini döner.

---

## 🏢 Servis İşlemleri (Services)

### Base URL: `/api/services`

#### 1. Tüm Servisleri Getir
```http
GET /api/services
```

**Yanıt:**
```json
[
  {
    "id": 1,
    "name": "DeliVerTech",
    "description": "Teknoloji tamiri ve destek hizmetleri",
    "key": "deliver-tech",
    "icon": "🔧",
    "sortOrder": 1,
    "isActive": true,
    "createdAt": "2025-10-10T10:00:00",
    "updatedAt": "2025-10-10T10:00:00"
  }
]
```

**Açıklama:** Sistemdeki tüm servisleri döner.

#### 2. Servis Detayını Getir
```http
GET /api/services/{id}
```

**Path Parametreleri:**
- `id` (Long, gerekli): Servis ID'si

**Yanıt:**
```json
{
  "id": 1,
  "name": "DeliVerTech",
  "description": "Teknoloji tamiri ve destek hizmetleri",
  "key": "deliver-tech",
  "icon": "🔧",
  "sortOrder": 1,
  "isActive": true,
  "createdAt": "2025-10-10T10:00:00",
  "updatedAt": "2025-10-10T10:00:00"
}
```

**Açıklama:** Belirtilen ID'ye sahip servisin detaylarını döner.

#### 3. Yeni Servis Oluştur
```http
POST /api/services
```

**İstek Gövdesi:**
```json
{
  "name": "string",
  "description": "string"
}
```

**Yanıt:**
```json
{
  "id": 1,
  "name": "Yeni Servis",
  "description": "Servis açıklaması",
  "createdAt": "2025-10-10T10:00:00",
  "updatedAt": "2025-10-10T10:00:00"
}
```

**Açıklama:** Yeni servis oluşturur.

#### 4. Servisi Güncelle
```http
PUT /api/services/{id}
```

**Path Parametreleri:**
- `id` (Long, gerekli): Servis ID'si

**İstek Gövdesi:**
```json
{
  "name": "string",
  "description": "string"
}
```

**Yanıt:**
```json
{
  "id": 1,
  "name": "Güncellenmiş Servis",
  "description": "Güncellenmiş açıklama",
  "updatedAt": "2025-10-10T10:00:00"
}
```

**Açıklama:** Mevcut servisi günceller.

#### 5. Servisi Sil
```http
DELETE /api/services/{id}
```

**Path Parametreleri:**
- `id` (Long, gerekli): Servis ID'si

**Yanıt:**
```
204 No Content
```

**Açıklama:** Belirtilen servisi siler.

---

## 📂 Kategori İşlemleri (Categories)

### Base URL: `/api/categories`

#### 1. Tüm Kategorileri Getir
```http
GET /api/categories
```

**Yanıt:**
```json
[
  {
    "id": 1,
    "name": "Telefon Tamiri",
    "description": "Telefon tamiri ve bakım hizmetleri",
    "serviceId": 1,
    "serviceName": "DeliVerTech",
    "parentId": null,
    "key": "tech-phone-repair",
    "icon": "📱",
    "sortOrder": 1,
    "isActive": true,
    "hasChildren": false,
    "createdAt": "2025-10-10T10:00:00",
    "updatedAt": "2025-10-10T10:00:00"
  }
]
```

**Açıklama:** Sistemdeki tüm aktif kategorileri sortOrder'a göre döner.

#### 2. Servise Göre Kategorileri Getir
```http
GET /api/categories/service/{serviceId}
```

**Path Parametreleri:**
- `serviceId` (Long, gerekli): Servis ID'si

**Yanıt:**
```json
[
  {
    "id": 1,
    "name": "Telefon Tamiri",
    "serviceId": 1,
    "serviceName": "DeliVerTech"
  }
]
```

**Açıklama:** Belirtilen servise ait kategorileri döner.

#### 3. Servise Göre Ana Kategorileri Getir
```http
GET /api/categories/service/{serviceId}/root
```

**Path Parametreleri:**
- `serviceId` (Long, gerekli): Servis ID'si

**Yanıt:**
```json
[
  {
    "id": 1,
    "name": "Telefon Tamiri",
    "parentId": null,
    "hasChildren": false
  }
]
```

**Açıklama:** Belirtilen servise ait ana kategorileri (parent'ı olmayan) döner.

#### 4. Alt Kategorileri Getir
```http
GET /api/categories/{id}/children
```

**Path Parametreleri:**
- `id` (Long, gerekli): Ana kategori ID'si

**Yanıt:**
```json
[
  {
    "id": 2,
    "name": "iPhone Tamiri",
    "parentId": 1,
    "hasChildren": false
  }
]
```

**Açıklama:** Belirtilen kategorinin alt kategorilerini döner.

#### 5. Kategori Detayını Getir
```http
GET /api/categories/{id}
```

**Path Parametreleri:**
- `id` (Long, gerekli): Kategori ID'si

**Yanıt:**
```json
{
  "id": 1,
  "name": "Telefon Tamiri",
  "description": "Telefon tamiri ve bakım hizmetleri",
  "serviceId": 1,
  "serviceName": "DeliVerTech",
  "parentId": null,
  "key": "tech-phone-repair",
  "icon": "📱",
  "sortOrder": 1,
  "isActive": true,
  "hasChildren": false,
  "createdAt": "2025-10-10T10:00:00",
  "updatedAt": "2025-10-10T10:00:00"
}
```

**Açıklama:** Belirtilen kategorinin detaylarını döner.

#### 6. Kategoriyi Key ile Getir
```http
GET /api/categories/key/{key}
```

**Path Parametreleri:**
- `key` (String, gerekli): Kategori anahtarı

**Yanıt:**
```json
{
  "id": 1,
  "name": "Telefon Tamiri",
  "key": "tech-phone-repair"
}
```

**Açıklama:** Belirtilen key'e sahip kategoriyi döner.

---

## 🛍️ Ürün İşlemleri (Products)

### Base URL: `/api/products`

#### 1. Kategoriye Göre Ürünleri Getir
```http
GET /api/products/category/{categoryId}?page=0&size=20
```

**Path Parametreleri:**
- `categoryId` (Long, gerekli): Kategori ID'si

**Query Parametreleri:**
- `page` (int, opsiyonel, varsayılan=0): Sayfa numarası
- `size` (int, opsiyonel, varsayılan=20): Sayfa başına öğe sayısı

**Yanıt:**
```json
[
  {
    "id": 1,
    "name": "iPhone Ekran Tamiri",
    "description": "iPhone modelleri için profesyonel ekran tamiri",
    "shortDescription": "iPhone ekran tamiri",
    "serviceId": 1,
    "serviceName": "DeliVerTech",
    "categoryId": 1,
    "categoryName": "Telefon Tamiri",
    "key": "tech-iphone-screen-repair",
    "sku": "TECH-001",
    "productType": "SERVICE",
    "attributes": {
      "warranty": "6 ay",
      "repair_time": "1-2 saat"
    },
    "isActive": true,
    "sortOrder": 1,
    "createdAt": "2025-10-10T10:00:00",
    "updatedAt": "2025-10-10T10:00:00",
    "pricing": [
      {
        "id": 1,
        "pricingType": "FIXED",
        "basePrice": 150.00,
        "minPrice": 120.00,
        "maxPrice": 200.00,
        "currency": "TRY"
      }
    ],
    "variants": [],
    "optionGroups": [],
    "flags": []
  }
]
```

**Açıklama:** Belirtilen kategorideki ürünleri sayfalama ile döner.

#### 2. Servise Göre Ürünleri Getir
```http
GET /api/products/service/{serviceId}?page=0&size=20
```

**Path Parametreleri:**
- `serviceId` (Long, gerekli): Servis ID'si

**Query Parametreleri:**
- `page` (int, opsiyonel, varsayılan=0): Sayfa numarası
- `size` (int, opsiyonel, varsayılan=20): Sayfa başına öğe sayısı

**Yanıt:**
```json
[
  {
    "id": 1,
    "name": "iPhone Ekran Tamiri",
    "serviceId": 1,
    "serviceName": "DeliVerTech"
  }
]
```

**Açıklama:** Belirtilen servisteki ürünleri sayfalama ile döner.

#### 3. Ürün Detayını Getir
```http
GET /api/products/{id}
```

**Path Parametreleri:**
- `id` (Long, gerekli): Ürün ID'si

**Yanıt:**
```json
{
  "id": 1,
  "name": "iPhone Ekran Tamiri",
  "description": "iPhone modelleri için profesyonel ekran tamiri ve değişimi",
  "shortDescription": "iPhone ekran tamiri",
  "serviceId": 1,
  "serviceName": "DeliVerTech",
  "categoryId": 1,
  "categoryName": "Telefon Tamiri",
  "key": "tech-iphone-screen-repair",
  "sku": "TECH-001",
  "productType": "SERVICE",
  "attributes": {
    "warranty": "6 ay",
    "repair_time": "1-2 saat",
    "device_types": ["iPhone 12", "iPhone 13", "iPhone 14"]
  },
  "isActive": true,
  "sortOrder": 1,
  "createdAt": "2025-10-10T10:00:00",
  "updatedAt": "2025-10-10T10:00:00",
  "pricing": [
    {
      "id": 1,
      "productId": 1,
      "pricingType": "FIXED",
      "basePrice": 150.00,
      "minPrice": 120.00,
      "maxPrice": 200.00,
      "currency": "TRY",
      "pricingRules": {
        "service_fee": true,
        "home_service": true
      },
      "isActive": true,
      "effectiveFrom": "2025-10-07T11:48:13.719043",
      "effectiveUntil": "2026-10-07T11:48:13.719043"
    }
  ],
  "variants": [
    {
      "id": 7,
      "productId": 1,
      "variantName": "iPhone 13",
      "sku": "IPHONE-13-REPAIR",
      "attributes": {
        "model": "iPhone 13",
        "garanti": "3 ay"
      },
      "priceModifier": 0.00,
      "isActive": true,
      "sortOrder": 1
    }
  ],
  "optionGroups": [
    {
      "id": 9,
      "productId": 1,
      "name": "iPhone Modeli",
      "description": "Tamir edilecek iPhone modeli",
      "optionType": "SINGLE",
      "isRequired": true,
      "isActive": true,
      "sortOrder": 1,
      "minSelections": 1,
      "maxSelections": 1,
      "options": [
        {
          "id": 32,
          "optionGroupId": 9,
          "name": "iPhone 12",
          "valueText": "iphone_12",
          "priceModifier": 0.00,
          "isActive": true,
          "sortOrder": 1
        }
      ]
    }
  ],
  "flags": [
    {
      "id": 17,
      "productId": 1,
      "flagKey": "warranty",
      "flagValue": "6_months",
      "flagType": "STRING",
      "description": "Garanti süresi",
      "isActive": true
    }
  ]
}
```

**Açıklama:** Belirtilen ürünün tam detaylarını (variants, options, flags dahil) döner.

#### 4. Ürün Arama
```http
GET /api/products/search?query=iPhone&serviceId=1&categoryId=1&page=0&size=20
```

**Query Parametreleri:**
- `query` (String, opsiyonel): Arama terimi
- `serviceId` (Long, opsiyonel): Servis ID'si ile filtreleme
- `categoryId` (Long, opsiyonel): Kategori ID'si ile filtreleme
- `page` (int, opsiyonel, varsayılan=0): Sayfa numarası
- `size` (int, opsiyonel, varsayılan=20): Sayfa başına öğe sayısı

**Yanıt:**
```json
[
  {
    "id": 1,
    "name": "iPhone Ekran Tamiri",
    "description": "iPhone modelleri için profesyonel ekran tamiri"
  }
]
```

**Açıklama:** Ürünleri arama terimi ve filtrelerle arar. Query boşsa tüm ürünleri döner.

---

## 🛒 Sepet İşlemleri (Cart)

### Base URL: `/api/cart`

#### 1. Sepeti Getir
```http
GET /api/cart
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Yanıt:**
```json
{
  "id": 1,
  "userId": 1,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "iPhone Ekran Tamiri",
      "quantity": 1,
      "unitPrice": 150.00,
      "totalPrice": 150.00,
      "selectedOptions": {
        "model": "iPhone 13",
        "service": "Hızlı Teslimat"
      },
      "variantId": 7,
      "variantName": "iPhone 13"
    }
  ],
  "totalAmount": 150.00,
  "itemCount": 1,
  "createdAt": "2025-10-10T10:00:00",
  "updatedAt": "2025-10-10T10:00:00"
}
```

**Açıklama:** Kullanıcının aktif sepetini döner.

#### 2. Sepete Ürün Ekle
```http
POST /api/cart/add
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**İstek Gövdesi:**
```json
{
  "productId": 1,
  "quantity": 1,
  "variantId": 7,
  "selectedOptions": {
    "model": "iPhone 13",
    "service": "Hızlı Teslimat"
  },
  "specialNotes": "Dikkatli kullanın"
}
```

**Yanıt:**
```json
{
  "id": 1,
  "userId": 1,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "iPhone Ekran Tamiri",
      "quantity": 1,
      "unitPrice": 150.00,
      "totalPrice": 150.00
    }
  ],
  "totalAmount": 150.00,
  "itemCount": 1
}
```

**Açıklama:** Sepete yeni ürün ekler ve güncellenmiş sepeti döner.

#### 3. Sepet Öğesini Güncelle
```http
PUT /api/cart/items/{cartItemId}
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parametreleri:**
- `cartItemId` (Long, gerekli): Sepet öğesi ID'si

**İstek Gövdesi:**
```json
{
  "quantity": 2,
  "selectedOptions": {
    "model": "iPhone 14",
    "service": "Standart Teslimat"
  },
  "specialNotes": "Yeni not"
}
```

**Yanıt:**
```json
{
  "id": 1,
  "userId": 1,
  "items": [
    {
      "id": 1,
      "quantity": 2,
      "totalPrice": 300.00
    }
  ],
  "totalAmount": 300.00
}
```

**Açıklama:** Sepetteki öğeyi günceller ve güncellenmiş sepeti döner.

#### 4. Sepet Öğesini Sil
```http
DELETE /api/cart/items/{cartItemId}
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parametreleri:**
- `cartItemId` (Long, gerekli): Sepet öğesi ID'si

**Yanıt:**
```json
{
  "id": 1,
  "userId": 1,
  "items": [],
  "totalAmount": 0.00,
  "itemCount": 0
}
```

**Açıklama:** Sepetten belirtilen öğeyi kaldırır.

#### 5. Sepeti Temizle
```http
DELETE /api/cart/clear
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Yanıt:**
```json
{
  "id": 1,
  "userId": 1,
  "items": [],
  "totalAmount": 0.00,
  "itemCount": 0
}
```

**Açıklama:** Sepetteki tüm öğeleri kaldırır.

#### 6. Sepet Öğe Sayısını Getir
```http
GET /api/cart/count
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Yanıt:**
```json
{
  "count": 3
}
```

**Açıklama:** Sepetteki toplam öğe sayısını döner.

#### 7. Sepetten Sipariş Oluştur (Checkout)
```http
POST /api/cart/checkout
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Yanıt:**
```json
{
  "orderId": 1,
  "orderNumber": "ORD-20251010-001",
  "orderStatus": "PENDING",
  "paymentStatus": "PENDING",
  "totalAmount": 150.00,
  "deliveryAddress": "Varsayılan adres",
  "phoneNumber": "Varsayılan telefon",
  "notes": "Cart checkout",
  "estimatedDeliveryTime": "2025-10-10T12:00:00",
  "createdAt": "2025-10-10T10:00:00",
  "message": "Siparişiniz başarıyla oluşturuldu!",
  "success": true
}
```

**Açıklama:** Sepetteki ürünlerden sipariş oluşturur ve sepeti temizler.

---

## 📦 Sipariş İşlemleri (Orders)

### Base URL: `/api/orders`

#### 1. Sipariş Oluştur
```http
POST /api/orders
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**İstek Gövdesi:**
```json
{
  "deliveryAddress": "İstanbul, Beşiktaş, Ortaköy Mh. No:123",
  "phoneNumber": "+90 555 123 4567",
  "notes": "Hızlı teslimat istiyorum"
}
```

**Yanıt:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20251010-001",
  "orderStatus": "PENDING",
  "paymentStatus": "PENDING",
  "totalAmount": 150.00,
  "deliveryAddress": "İstanbul, Beşiktaş, Ortaköy Mh. No:123",
  "phoneNumber": "+90 555 123 4567",
  "notes": "Hızlı teslimat istiyorum",
  "estimatedDeliveryTime": "2025-10-10T12:00:00",
  "createdAt": "2025-10-10T10:00:00",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "iPhone Ekran Tamiri",
      "quantity": 1,
      "unitPrice": 150.00,
      "totalPrice": 150.00,
      "selectedOptions": "iPhone 13, Hızlı Teslimat",
      "variantId": 7,
      "variantName": "iPhone 13"
    }
  ]
}
```

**Açıklama:** Sepetteki ürünlerden yeni sipariş oluşturur.

#### 2. Kullanıcı Siparişlerini Getir
```http
GET /api/orders?page=0&size=10
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parametreleri:**
- `page` (int, opsiyonel, varsayılan=0): Sayfa numarası
- `size` (int, opsiyonel, varsayılan=10): Sayfa başına öğe sayısı

**Yanıt:**
```json
{
  "content": [
    {
      "id": 1,
      "orderNumber": "ORD-20251010-001",
      "orderStatus": "PENDING",
      "paymentStatus": "PENDING",
      "totalAmount": 150.00,
      "createdAt": "2025-10-10T10:00:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

**Açıklama:** Kullanıcının tüm siparişlerini sayfalama ile döner.

#### 3. Sipariş Detayını Getir
```http
GET /api/orders/{orderId}
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parametreleri:**
- `orderId` (Long, gerekli): Sipariş ID'si

**Yanıt:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20251010-001",
  "orderStatus": "PENDING",
  "paymentStatus": "PENDING",
  "totalAmount": 150.00,
  "deliveryAddress": "İstanbul, Beşiktaş, Ortaköy Mh. No:123",
  "phoneNumber": "+90 555 123 4567",
  "notes": "Hızlı teslimat istiyorum",
  "estimatedDeliveryTime": "2025-10-10T12:00:00",
  "actualDeliveryTime": null,
  "createdAt": "2025-10-10T10:00:00",
  "updatedAt": "2025-10-10T10:00:00",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "iPhone Ekran Tamiri",
      "productDescription": "iPhone modelleri için profesyonel ekran tamiri",
      "quantity": 1,
      "unitPrice": 150.00,
      "totalPrice": 150.00,
      "selectedOptions": "iPhone 13, Hızlı Teslimat",
      "specialNotes": "Dikkatli kullanın",
      "variantId": 7,
      "variantName": "iPhone 13",
      "createdAt": "2025-10-10T10:00:00"
    }
  ]
}
```

**Açıklama:** Belirtilen siparişin tam detaylarını döner.

#### 4. Aktif Siparişleri Getir
```http
GET /api/orders/active
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Yanıt:**
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-20251010-001",
    "orderStatus": "PREPARING",
    "paymentStatus": "PAID",
    "totalAmount": 150.00,
    "estimatedDeliveryTime": "2025-10-10T12:00:00",
    "createdAt": "2025-10-10T10:00:00"
  }
]
```

**Açıklama:** Kullanıcının aktif siparişlerini (PENDING, CONFIRMED, PREPARING, OUT_FOR_DELIVERY) döner.

#### 5. Sipariş Durumunu Güncelle
```http
PUT /api/orders/{orderId}/status
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parametreleri:**
- `orderId` (Long, gerekli): Sipariş ID'si

**İstek Gövdesi:**
```json
{
  "orderStatus": "CANCELLED",
  "notes": "Artık ihtiyacım yok"
}
```

**Yanıt:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20251010-001",
  "orderStatus": "CANCELLED",
  "updatedAt": "2025-10-10T11:00:00"
}
```

**Açıklama:** Sipariş durumunu günceller. Kullanıcılar sadece "CANCELLED" durumuna geçiş yapabilir.

#### 6. Sipariş Numarasına Göre Sipariş Getir
```http
GET /api/orders/by-number/{orderNumber}
```

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parametreleri:**
- `orderNumber` (String, gerekli): Sipariş numarası

**Yanıt:**
```json
{
  "id": 1,
  "orderNumber": "ORD-20251010-001",
  "orderStatus": "PENDING",
  "paymentStatus": "PENDING",
  "totalAmount": 150.00
}
```

**Açıklama:** Sipariş numarasına göre sipariş detaylarını döner.

---

## ❌ Hata Kodları

### Genel Hata Formatı
```json
{
  "error": "ERROR_CODE",
  "message": "Hata açıklaması",
  "status": 400,
  "timestamp": "2025-10-10T10:00:00",
  "path": "/api/endpoint"
}
```

### Parametre Hataları
```json
{
  "parameter": "id",
  "expectedType": "Long",
  "error": "INVALID_PARAMETER",
  "message": "Geçersiz ID formatı: 'abc'. ID sayısal olmalıdır.",
  "value": "abc",
  "status": 400
}
```

### Yaygın Hata Kodları

| HTTP Kodu | Error Code | Açıklama |
|-----------|------------|----------|
| 400 | `VALIDATION_ERROR` | Geçersiz istek verisi |
| 400 | `INVALID_PARAMETER` | Geçersiz parametre formatı |
| 400 | `BAD_REQUEST` | Hatalı istek |
| 401 | `TOKEN_EXPIRED` | Token süresi dolmuş |
| 401 | `INVALID_TOKEN` | Geçersiz token |
| 401 | `INVALID_CREDENTIALS` | Geçersiz giriş bilgileri |
| 403 | `ACCESS_DENIED` | Erişim reddedildi |
| 404 | `NOT_FOUND` | Kaynak bulunamadı |
| 500 | `INTERNAL_SERVER_ERROR` | Sunucu hatası |

---

## 💡 Örnek Kullanım

### 1. Kullanıcı Kaydı ve Giriş
```bash
# Kayıt
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","email":"john@example.com","password":"password123"}'

# Giriş
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'

# Response: {"token":"JWT_TOKEN","refreshToken":"REFRESH_TOKEN","tokenType":"Bearer","expiresIn":86400}
```

### 2. Ürün Arama ve Sepete Ekleme
```bash
# Ürün arama
curl -X GET "http://localhost:8080/api/products/search?query=iPhone" \
  -H "Accept: application/json"

# Sepete ekleme
curl -X POST http://localhost:8080/api/cart/add \
  -H "Authorization: Bearer JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"quantity":1,"variantId":7,"selectedOptions":{"model":"iPhone 13"}}'
```

### 3. Sipariş Oluşturma
```bash
# Sepetten sipariş
curl -X POST http://localhost:8080/api/cart/checkout \
  -H "Authorization: Bearer JWT_TOKEN" \
  -H "Content-Type: application/json"

# Veya doğrudan sipariş
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"deliveryAddress":"İstanbul","phoneNumber":"+905551234567","notes":"Hızlı teslimat"}'
```

### 4. Sipariş Takibi
```bash
# Aktif siparişler
curl -X GET http://localhost:8080/api/orders/active \
  -H "Authorization: Bearer JWT_TOKEN"

# Sipariş detayı
curl -X GET http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer JWT_TOKEN"
```

---

## 📝 Notlar

1. **Kimlik Doğrulama**: Çoğu endpoint JWT token gerektirir
2. **Sayfalama**: Liste endpoint'leri sayfalama destekler
3. **Filtreleme**: Ürün arama çoklu filtre destekler
4. **Hata Yönetimi**: Tüm hatalarda tutarlı format kullanılır
5. **Güvenlik**: Kullanıcılar sadece kendi verilerine erişebilir
6. **Performans**: Lazy loading ve optimize edilmiş sorgular kullanılır
