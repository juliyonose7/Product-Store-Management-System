import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthRequest } from '../models/auth-request.model';
import { AuthResponse } from '../models/auth-response.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly endpoint = `${environment.apiBaseUrl}/auth/login`;
  private readonly tokenKey = 'psms_token';
  private readonly userKey = 'psms_user';
  private readonly roleKey = 'psms_role';

  constructor(private readonly http: HttpClient) {}

  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.endpoint, payload).pipe(
      tap((response) => {
        localStorage.setItem(this.tokenKey, response.token);
        localStorage.setItem(this.userKey, response.username);
        localStorage.setItem(this.roleKey, response.role);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    localStorage.removeItem(this.roleKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUsername(): string {
    return localStorage.getItem(this.userKey) ?? '';
  }

  getRole(): string {
    return localStorage.getItem(this.roleKey) ?? '';
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}
