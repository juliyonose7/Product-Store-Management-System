import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DashboardMetrics } from '../models/metrics.model';

@Injectable({
  providedIn: 'root'
})
export class MetricsService {
  private readonly endpoint = `${environment.apiBaseUrl}/metrics/dashboard`;

  constructor(private readonly http: HttpClient) {}

  getDashboardMetrics(): Observable<DashboardMetrics> {
    return this.http.get<DashboardMetrics>(this.endpoint);
  }
}
