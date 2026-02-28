import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';
import { environment } from '../../../environments/environment';

export interface CreateProductPayload {
  name: string;
  price: number;
  stock: number;
}

export interface ProductCsvImportResult {
  created: number;
  skipped: number;
  errors: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly endpoint = `${environment.apiBaseUrl}/products`;

  constructor(private readonly http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.endpoint);
  }

  createProduct(payload: CreateProductPayload): Observable<Product> {
    return this.http.post<Product>(this.endpoint, payload);
  }

  importProductsCsv(file: File): Observable<ProductCsvImportResult> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ProductCsvImportResult>(`${this.endpoint}/import-csv`, formData);
  }
}