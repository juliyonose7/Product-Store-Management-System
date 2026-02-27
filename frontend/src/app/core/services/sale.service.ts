import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Sale } from '../models/sale.model';
import { CreateSaleRequest } from '../models/create-sale-request.model';

@Injectable({
  providedIn: 'root'
})
export class SaleService {
  private readonly endpoint = `${environment.apiBaseUrl}/sales`;

  constructor(private readonly http: HttpClient) {}

  getSales(): Observable<Sale[]> {
    return this.http.get<Sale[]>(this.endpoint);
  }

  getSalesActivity(): Observable<Sale[]> {
    return this.http.get<Sale[]>(`${this.endpoint}/activity`);
  }

  createSale(payload: CreateSaleRequest): Observable<Sale> {
    return this.http.post<Sale>(this.endpoint, payload);
  }
}
