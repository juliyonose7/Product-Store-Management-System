import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subscription, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthRequest } from '../models/auth-request.model';
import { AuthResponse } from '../models/auth-response.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly loginEndpoint = `${environment.apiBaseUrl}/auth/login`;
  private readonly refreshEndpoint = `${environment.apiBaseUrl}/auth/refresh`;
  private readonly accessTokenKey = 'psms_access_token';
  private readonly refreshTokenKey = 'psms_refresh_token';
  private readonly userKey = 'psms_user';
  private readonly roleKey = 'psms_role';
  private readonly accessExpKey = 'psms_access_exp';
  private readonly refreshExpKey = 'psms_refresh_exp';

  private refreshTimer: ReturnType<typeof setTimeout> | null = null;
  private refreshSubscription: Subscription | null = null;

  constructor(private readonly http: HttpClient) {}

  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.loginEndpoint, payload).pipe(tap((response) => this.storeSession(response)));
  }

  refresh(): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(this.refreshEndpoint, { refreshToken: this.getRefreshToken() })
      .pipe(tap((response) => this.storeSession(response)));
  }

  logout(): void {
    this.stopAutoRefresh();
    localStorage.removeItem(this.accessTokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    localStorage.removeItem(this.userKey);
    localStorage.removeItem(this.roleKey);
    localStorage.removeItem(this.accessExpKey);
    localStorage.removeItem(this.refreshExpKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.accessTokenKey);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.refreshTokenKey);
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

  getAccessTokenExpiresAt(): number {
    return Number(localStorage.getItem(this.accessExpKey) ?? 0);
  }

  getRemainingSessionSeconds(): number {
    const remaining = this.getAccessTokenExpiresAt() - Date.now();
    return Math.max(Math.floor(remaining / 1000), 0);
  }

  startAutoRefresh(onExpired: () => void): void {
    this.stopAutoRefresh();

    if (!this.isAuthenticated()) {
      return;
    }

    const expiresAt = this.getAccessTokenExpiresAt();
    const delay = Math.max(expiresAt - Date.now() - 60000, 1000);

    this.refreshTimer = setTimeout(() => {
      this.refreshSubscription = this.refresh().subscribe({
        next: () => this.startAutoRefresh(onExpired),
        error: () => {
          this.logout();
          onExpired();
        }
      });
    }, delay);
  }

  stopAutoRefresh(): void {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }

    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
      this.refreshSubscription = null;
    }
  }

  private storeSession(response: AuthResponse): void {
    localStorage.setItem(this.accessTokenKey, response.accessToken);
    localStorage.setItem(this.refreshTokenKey, response.refreshToken);
    localStorage.setItem(this.userKey, response.username);
    localStorage.setItem(this.roleKey, response.role);
    localStorage.setItem(this.accessExpKey, String(response.accessTokenExpiresAt));
    localStorage.setItem(this.refreshExpKey, String(response.refreshTokenExpiresAt));
  }
}
